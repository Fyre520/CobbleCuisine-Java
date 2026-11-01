package com.fyre.cobblecuisine.meal;

import com.cobblemon.mod.common.api.cooking.Seasonings;
import com.fyre.cobblecuisine.component.MealEffectsComponent;
import com.fyre.cobblecuisine.component.CobbleCuisineDataComponents;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.config.CobbleCuisineConfigData;
import com.fyre.cobblecuisine.config.TooltipSettings;
import com.fyre.cobblecuisine.network.CatchBoostSync;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fyre.cobblecuisine.CobbleCuisine.DEBUG;
import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public final class MealConsumption {
	private static final Pattern LEADING_FORMAT = Pattern.compile("(?:§[0-9a-fk-or])+", Pattern.CASE_INSENSITIVE);

	private MealConsumption() {
	}

	public static MealLoadout resolve(
			MealEffectsComponent crafted,
			Optional<MealEffectEntry> fallbackPrimary,
			MealLoadout.StrengthProvider strengths
	) {
		MealEffectsComponent resolved = crafted.primary().isPresent()
				? crafted
				: new MealEffectsComponent(fallbackPrimary, crafted.seasonings(), crafted.seasoningSlots(), crafted.potency());
		return MealLoadout.combine(resolved, strengths);
	}

	public static MealEffectsComponent capture(ItemStack stack, FoodComponent foodComponent) {
		MealEffectsComponent crafted = stack.getOrDefault(CobbleCuisineDataComponents.MEAL_EFFECTS, MealEffectsComponent.EMPTY);
		if (crafted.primary().isPresent()) return crafted;
		return new MealEffectsComponent(primaryFromFood(foodComponent), crafted.seasonings(), crafted.seasoningSlots(), crafted.potency());
	}

	public static Optional<MealEffectEntry> primaryFromFood(FoodComponent foodComponent) {
		if (foodComponent == null) return Optional.empty();
		return foodComponent.effects().stream()
				.map(FoodComponent.StatusEffectEntry::effect)
				.map(effect -> effect.getEffectType().getKey())
				.flatMap(Optional::stream)
				.map(key -> key.getValue().toString())
				.map(MealEffectMappings::fromPrimaryStatusEffectId)
				.flatMap(Optional::stream)
				.findFirst();
	}

	public static int duration(FoodComponent foodComponent) {
		return foodComponent.effects().stream()
				.mapToInt(entry -> entry.effect().getDuration())
				.max()
				.orElse(CobbleCuisineConfig.data.effectDuration.satiatedEffectDuration);
	}

	public static void consume(ServerPlayerEntity player, MealEffectsComponent meal, int duration) {
		MealLoadout loadout = resolve(meal, Optional.empty(), CONFIG_STRENGTHS);
		int bonus = bonusDuration(meal, CONFIG_STRENGTHS.scale(MealEffectType.DURATION));
		ActiveMealState.add(player, loadout, meal.primary().map(MealEffectEntry::type), Math.max(1, duration + bonus));
		ActiveMealState.getActiveWith(player, MealEffectType.CATCH.axis()).ifPresent(catchMeal -> CatchBoostSync.send(player, catchMeal));
		if (DEBUG) LOGGER.info("CobbleCuisine >> MEAL CONSUMED >> PLAYER: {} PRIMARY: {} EFFECTS: {}",
				player.getName().getString(), meal.primary().map(Object::toString).orElse("none"), loadout.effects());
	}

	public static int bonusDuration(ItemStack stack, FoodComponent foodComponent) {
		MealEffectsComponent meal = capture(stack, foodComponent);
		return TooltipSettings.current().map(values -> bonusDuration(meal, values.scale(MealEffectType.DURATION))).orElse(0);
	}

	private static int bonusDuration(MealEffectsComponent meal, MealLoadout.Scale scale) {
		int count = meal.seasonings().stream()
				.filter(entry -> entry.type() == MealEffectType.DURATION)
				.mapToInt(MealEffectEntry::count)
				.sum();
		return Math.round(scale.perSeasoning() * count);
	}

	public static Optional<Text> primaryValue(ItemStack stack, FoodComponent foodComponent) {
		MealEffectsComponent meal = capture(stack, foodComponent);
		Optional<TooltipSettings.Snapshot> settings = TooltipSettings.current();
		if (meal.primary().isEmpty() || settings.isEmpty()) return Optional.empty();
		MealEffectEntry primary = meal.primary().get();
		Optional<Text> value = combinedValue(meal, primary, settings.get());
		if (primary.type() == MealEffectType.IV) return value;
		return value.map(text -> Text.literal(" ").append(text));
	}

	public static List<Text> shiftTooltip(ItemStack stack, FoodComponent foodComponent) {
		MealEffectsComponent meal = capture(stack, foodComponent);
		Optional<TooltipSettings.Snapshot> settings = TooltipSettings.current();
		List<Text> tooltip = new ArrayList<>();
		boolean classic = settings.map(TooltipSettings.Snapshot::classic).orElse(false);
		if (!classic) meal.primary().ifPresent(primary -> {
			Optional<Text> base = settings.flatMap(values -> baseValue(meal, primary, values));
			Optional<Text> combined = settings.flatMap(values -> combinedValue(meal, primary, values));
			if (base.isPresent() && !base.map(Text::getString).equals(combined.map(Text::getString))) {
				tooltip.add(line(MealEffectMappings.baseTranslationKey(primary), 0, base));
			}
			int boosters = boosterCount(meal, primary.type());
			if (boosters > 0 && primary.type() != MealEffectType.IV) {
				tooltip.add(line(MealEffectMappings.seasoningTranslationKey(primary), boosters,
						settings.flatMap(values -> boosterValue(primary.type(), boosters, values))));
			}
		});
		for (MealEffectEntry seasoning : meal.seasonings()) {
			if (isDrinkStat(meal, seasoning) || (classic && !MealLoadout.keptInClassic(seasoning.type()))) continue;
			tooltip.add(line(MealEffectMappings.seasoningTranslationKey(seasoning), seasoning.count(),
					settings.flatMap(values -> seasoningValue(meal, seasoning, values))));
		}
		return tooltip;
	}

	public static List<Text> ingredientShiftTooltip(ItemStack stack) {
		return Seasonings.INSTANCE.getMobEffectsFromItemStack(stack).stream()
				.map(effect -> MealEffectMappings.fromStatusEffectId(effect.getEffect().toString()))
				.flatMap(Optional::stream)
				.map(entry -> line(MealEffectMappings.seasoningTranslationKey(entry), 0, Optional.empty()))
				.toList();
	}

	private static Optional<Text> combinedValue(MealEffectsComponent meal, MealEffectEntry primary, TooltipSettings.Snapshot values) {
		MealEffectType type = primary.type();
		MealLoadout loadout = MealLoadout.combine(meal, strengths(values));
		if (MealLoadout.weighted(type)) return Optional.of(multiplier(Math.abs(loadout.strength(type.axis(), primary.target()))));
		MealLoadout.Scale scale = values.scale(type);
		int boosters = values.classic() ? 0 : boosterCount(meal, type);
		return switch (type) {
			case TERA -> Optional.of(percent(realChance(loadout, type.axis(), loadout.strength(type.axis(), primary.target()))));
			case EXP -> Optional.of(multiplier(ProgressiveMealPotency.expMultiplier(scale, boosters)));
			case CATCH -> Optional.of(multiplier(ProgressiveMealPotency.catchMultiplier(scale, boosters)));
			case SHINY -> Optional.of(multiplier(ProgressiveMealPotency.shinyMultiplier(scale, boosters)));
			case IV -> {
				ProgressiveMealPotency.IntRange range = ProgressiveMealPotency.ivRange(scale, values.ivMaximum(), values.ivMaxValue(), boosters);
				yield Optional.of(Text.literal("+".repeat(boosters) + " ")
						.append(Text.translatable("tooltip.cobblecuisine.power.iv_range", range.min(), range.max())));
			}
			case TINY, GIANT -> Optional.of(sizeChance(type, Math.abs(loadout.strength(MealLoadout.SIZE_AXIS, ""))));
			case ALPHA_FREQUENCY_INCREASE -> Optional.of(percent(loadout.strength(MealLoadout.ALPHA_CHANCE_AXIS, "")));
			case ALPHA_FREQUENCY_DECREASE -> Optional.of(percent(loadout.strength(MealLoadout.ALPHA_SUPPRESS_AXIS, "")));
			default -> Optional.empty();
		};
	}

	private static Optional<Text> baseValue(MealEffectsComponent meal, MealEffectEntry primary, TooltipSettings.Snapshot values) {
		MealEffectType type = primary.type();
		if (MealLoadout.weighted(type)) {
			float spawn = values.weight(type.axis(), primary.target()).spawn();
			return Optional.of(multiplier(type.direction() > 0 ? spawn : 1F / spawn));
		}
		MealLoadout.Scale scale = values.scale(type);
		return switch (type) {
			case TERA -> Optional.of(percent(realChance(MealLoadout.combine(meal, strengths(values)), type.axis(), scale.base())));
			case EXP, CATCH, SHINY -> Optional.of(multiplier(scale.base()));
			case TINY, GIANT -> Optional.of(sizeChance(type, ProgressiveMealPotency.extremeSizeChance(scale, 0)));
			case ALPHA_FREQUENCY_INCREASE, ALPHA_FREQUENCY_DECREASE -> Optional.of(percent(Math.min(1F, scale.base())));
			default -> Optional.empty();
		};
	}

	private static Optional<Text> boosterValue(MealEffectType type, int count, TooltipSettings.Snapshot values) {
		float step = values.scale(type).perSeasoning() * count;
		return switch (type) {
			case EXP, CATCH, SHINY -> Optional.of(Text.literal("(+×" + number(step, 2) + ")"));
			default -> Optional.empty();
		};
	}

	private static Optional<Text> seasoningValue(MealEffectsComponent meal, MealEffectEntry seasoning, TooltipSettings.Snapshot values) {
		MealEffectType type = seasoning.type();
		if (type == MealEffectType.YIELD || MealLoadout.weighted(type)) return Optional.of(multiplier(weightedSeasoningValue(meal, seasoning, values)));
		float step = values.scale(type).perSeasoning() * seasoning.count();
		return switch (type) {
			case TERA, NATURE -> Optional.of(percent(realChance(MealLoadout.combine(meal, strengths(values)), type.axis(), step)));
			case HIDDEN_ABILITY, ALPHA_FREQUENCY_INCREASE, ALPHA_FREQUENCY_DECREASE -> Optional.of(percent(Math.min(1F, step)));
			case TINY, GIANT -> Optional.of(sizeChance(type, Math.min(1F, step)));
			case DURATION -> Optional.of(Text.literal("(+" + StringHelper.formatTicks(Math.round(step), 20F) + ")"));
			default -> Optional.empty();
		};
	}

	private static float weightedSeasoningValue(MealEffectsComponent meal, MealEffectEntry seasoning, TooltipSettings.Snapshot values) {
		MealEffectType type = seasoning.type();
		float factor = MealLoadout.seasoningFactor(values.weight(type.axis(), seasoning.target()), seasoning.count());
		if (type == MealEffectType.HERD_FREQUENCY_DECREASE) return 1F / factor;
		if (type != MealEffectType.TYPE && type != MealEffectType.EGG_GROUP) return factor;
		Optional<MealEffectEntry> main = meal.primary().filter(entry -> entry.type() == type);
		if (main.isEmpty() || main.get().target().equals(seasoning.target())) return factor;
		float cut = values.weight(type.axis(), main.get().target()).nonWeight();
		return Math.min(1F, 2F * cut) * factor;
	}

	private static float realChance(MealLoadout loadout, String axis, float chance) {
		float total = ActiveMealResolver.totalStrength(loadout, axis);
		return total > 1F ? chance / total : chance;
	}

	private static int boosterCount(MealEffectsComponent meal, MealEffectType type) {
		return switch (type) {
			case EXP, IV, CATCH, SHINY -> meal.potency().count(MealPotency.GOLD_NUGGETS);
			default -> 0;
		};
	}

	private static boolean isDrinkStat(MealEffectsComponent meal, MealEffectEntry seasoning) {
		return seasoning.type() == MealEffectType.YIELD && meal.primary()
				.filter(entry -> entry.type() == MealEffectType.YIELD && entry.target().equals(seasoning.target()))
				.isPresent();
	}

	private static Text line(String key, int count, Optional<Text> value) {
		String name = Text.translatable(key).getString();
		Matcher codes = LEADING_FORMAT.matcher(name);
		List<Formatting> formats = new ArrayList<>();
		if (codes.lookingAt()) {
			for (int i = 1; i < codes.end(); i += 2) {
				Formatting format = Formatting.byCode(codes.group().charAt(i));
				if (format != null && format != Formatting.RESET) formats.add(format);
			}
		}
		if (formats.stream().noneMatch(Formatting::isColor)) formats.add(0, Formatting.BLUE);
		formats.replaceAll(MealConsumption::otherShade);
		StringBuilder text = new StringBuilder(Formatting.strip(name));
		if (count > 0) text.append(' ').append(Text.translatable("enchantment.level." + count).getString());
		value.ifPresent(amount -> text.append(' ').append(amount.getString()));
		return Text.literal(text.toString()).formatted(formats.toArray(Formatting[]::new));
	}

	private static Formatting otherShade(Formatting format) {
		return switch (format) {
			case DARK_BLUE -> Formatting.BLUE;
			case BLUE -> Formatting.DARK_BLUE;
			case DARK_GREEN -> Formatting.GREEN;
			case GREEN -> Formatting.DARK_GREEN;
			case DARK_AQUA -> Formatting.AQUA;
			case AQUA -> Formatting.DARK_AQUA;
			case DARK_RED -> Formatting.RED;
			case RED -> Formatting.DARK_RED;
			case DARK_PURPLE -> Formatting.LIGHT_PURPLE;
			case LIGHT_PURPLE -> Formatting.DARK_PURPLE;
			case GOLD -> Formatting.YELLOW;
			case YELLOW -> Formatting.GOLD;
			case GRAY -> Formatting.WHITE;
			case WHITE -> Formatting.GRAY;
			case DARK_GRAY -> Formatting.GRAY;
			default -> format;
		};
	}

	private static Text multiplier(float amount) {
		return Text.literal("(×" + number(amount, 2) + ")");
	}

	private static Text percent(float chance) {
		return Text.literal("(" + number(chance * 100F, 1) + "%)");
	}

	private static Text sizeChance(MealEffectType type, float chance) {
		String key = type == MealEffectType.TINY ? "tooltip.cobblecuisine.power.xs_chance" : "tooltip.cobblecuisine.power.xl_chance";
		return Text.translatable(key, number(chance * 100F, 1));
	}

	private static String number(float amount, int decimals) {
		return BigDecimal.valueOf(amount).setScale(decimals, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
	}

	private static MealLoadout.StrengthProvider strengths(TooltipSettings.Snapshot values) {
		return new MealLoadout.StrengthProvider() {
			@Override public float direct(MealEffectType type) { return values.direct(type); }
			@Override public float seasoning(MealEffectType type) { return values.seasoning(type); }
			@Override public float cap(MealEffectType type) { return values.cap(type); }
			@Override public MealLoadout.WeightSettings weight(String axis, String target) { return values.weight(axis, target); }
			@Override public MealLoadout.Scale scale(MealEffectType type) { return values.scale(type); }
			@Override public boolean classic() { return values.classic(); }
		};
	}

	private static final MealLoadout.StrengthProvider CONFIG_STRENGTHS = new MealLoadout.StrengthProvider() {
		@Override
		public float direct(MealEffectType type) {
			return Math.max(0.0f, settings(type).direct);
		}

		@Override
		public float seasoning(MealEffectType type) {
			float direct = direct(type);
			return Math.min(direct, Math.max(0.0f, settings(type).seasoning));
		}

		@Override
		public float cap(MealEffectType type) {
			return Math.max(direct(type), Math.max(0.0f, settings(type).cap));
		}

		@Override
		public MealLoadout.WeightSettings weight(String axis, String target) {
			return MealLoadout.WeightSettings.of(CobbleCuisineConfig.data, axis, target);
		}

		@Override
		public MealLoadout.Scale scale(MealEffectType type) {
			return MealLoadout.Scale.of(CobbleCuisineConfig.data, type);
		}

		@Override
		public boolean classic() {
			return CobbleCuisineConfig.data.globalSettings.classicMealPowers;
		}

		private CobbleCuisineConfigData.Strength settings(MealEffectType type) {
			return CobbleCuisineConfig.data.mealStrengths.get(type);
		}
	};
}
