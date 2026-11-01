package com.fyre.cobblecuisine.meal;

import com.fyre.cobblecuisine.component.MealEffectsComponent;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.config.CobbleCuisineConfigData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MealLoadout(Map<EffectKey, Float> effects, MealPotency potency) {
	public static final String ALPHA_CHANCE_AXIS = "alpha_chance";
	public static final String ALPHA_SUPPRESS_AXIS = "alpha_suppress";
	public static final String SIZE_AXIS = MealEffectType.GIANT.axis();

	public static final String YIELD_BONUS_AXIS = "yield_bonus";
	private static final String SEASONING_SUFFIX = "_seasoning";
	private static final String CUT_SUFFIX = "_cut";
	private static final int VERSION = 3;
	public static final MealLoadout EMPTY = new MealLoadout(Map.of(), MealPotency.EMPTY);
	private static final Codec<ResolvedEffect> RESOLVED_EFFECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EffectKey.CODEC.fieldOf("effect").forGetter(ResolvedEffect::key),
			Codec.FLOAT.fieldOf("strength").forGetter(ResolvedEffect::strength)
	).apply(instance, ResolvedEffect::new));
	public static final Codec<MealLoadout> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOLVED_EFFECT_CODEC.listOf().fieldOf("effects").forGetter(MealLoadout::toResolvedEffects),
			MealPotency.CODEC.optionalFieldOf("potency", MealPotency.EMPTY).forGetter(MealLoadout::potency),
			Codec.INT.optionalFieldOf("version", 0).forGetter(loadout -> VERSION)
	).apply(instance, MealLoadout::fromResolvedEffects));

	public MealLoadout {
		effects = Map.copyOf(effects);
		potency = java.util.Objects.requireNonNull(potency, "potency");
	}

	public MealLoadout(Map<EffectKey, Float> effects) {
		this(effects, MealPotency.EMPTY);
	}

	public float strength(String axis, String target) {
		return effects.getOrDefault(new EffectKey(axis, target), 0.0f);
	}

	public static String seasoningAxis(String axis) {
		return axis + SEASONING_SUFFIX;
	}

	public static String cutAxis(String axis) {
		return axis + CUT_SUFFIX;
	}

	public static Optional<String> powerAxis(String axis) {
		if (axis.endsWith(CUT_SUFFIX)) return Optional.empty();
		if (axis.equals(YIELD_BONUS_AXIS)) return Optional.of(MealEffectType.YIELD.axis());
		if (axis.endsWith(SEASONING_SUFFIX)) return Optional.of(axis.substring(0, axis.length() - SEASONING_SUFFIX.length()));
		return Optional.of(axis);
	}

	public static boolean resolvedSeparately(MealEffectType type) {
		return weighted(type) || switch (type) {
			case TERA, NATURE, HIDDEN_ABILITY, TINY, GIANT, DURATION,
					ALPHA_FREQUENCY_INCREASE, ALPHA_FREQUENCY_DECREASE -> true;
			default -> !type.active();
		};
	}

	public static boolean weighted(MealEffectType type) {
		return type == MealEffectType.TYPE || type == MealEffectType.EGG_GROUP
				|| type == MealEffectType.HERD_FREQUENCY_INCREASE || type == MealEffectType.HERD_FREQUENCY_DECREASE;
	}

	public static MealLoadout combine(MealEffectsComponent meal, StrengthProvider strengths) {
		if (strengths.classic()) meal = classicMeal(meal);
		Map<EffectKey, Accumulator> combined = new LinkedHashMap<>();
		Map<EffectKey, Double> primaryStrengths = new LinkedHashMap<>();
		meal.primary().filter(entry -> !resolvedSeparately(entry.type())).ifPresent(entry -> {
			double strength = strengths.direct(entry.type()) * entry.type().direction();
			primaryStrengths.put(key(entry), strength);
			add(combined, entry, strength, strengths.cap(entry.type()));
		});
		for (MealEffectEntry entry : meal.seasonings()) {
			if (entry.type() == MealEffectType.YIELD || resolvedSeparately(entry.type())) continue;
			double fraction = entry.count() / 3.0;
			add(
					combined,
					entry,
					strengths.seasoning(entry.type()) * fraction * entry.type().direction(),
					strengths.cap(entry.type())
			);
		}

		Map<EffectKey, Float> resolved = new LinkedHashMap<>();
		combined.forEach((key, value) -> {
			double cap = Math.max(0.0, value.cap);
			double strength = Math.max(-cap, Math.min(cap, value.strength));
			double primaryStrength = primaryStrengths.getOrDefault(key, 0.0);
			if (primaryStrength != 0.0F && strength * primaryStrength < 0.0F) strength = 0.0F;
			resolved.put(key, (float) Math.max(-Float.MAX_VALUE, Math.min(Float.MAX_VALUE, strength)));
		});
		addTargetedWeights(meal, strengths, MealEffectType.TYPE.axis(), resolved);
		addTargetedWeights(meal, strengths, MealEffectType.EGG_GROUP.axis(), resolved);
		addHerdFrequency(meal, strengths, resolved);
		addYieldBonuses(meal, strengths, resolved);
		addTera(meal, strengths, resolved);
		addSeasoningChance(meal, strengths, MealEffectType.NATURE, resolved);
		addSeasoningChance(meal, strengths, MealEffectType.HIDDEN_ABILITY, resolved);
		addSize(meal, strengths, resolved);
		addAlpha(meal, strengths, resolved);

		return new MealLoadout(resolved, meal.potency());
	}

	public static boolean keptInClassic(MealEffectType type) {
		return type == MealEffectType.NATURE || type == MealEffectType.HIDDEN_ABILITY || type == MealEffectType.DURATION;
	}

	private static MealEffectsComponent classicMeal(MealEffectsComponent meal) {
		List<MealEffectEntry> seasonings = meal.seasonings().stream().filter(entry -> keptInClassic(entry.type())).toList();
		return new MealEffectsComponent(meal.primary(), seasonings, seasonings.isEmpty() ? 0 : meal.seasoningSlots());
	}

	private static void addTargetedWeights(MealEffectsComponent meal, StrengthProvider strengths, String axis, Map<EffectKey, Float> resolved) {
		Optional<MealEffectEntry> main = meal.primary().filter(entry -> entry.type().axis().equals(axis));
		Map<String, Integer> counts = seasoningCounts(meal, axis);
		main.ifPresent(entry -> {
			WeightSettings settings = strengths.weight(axis, entry.target());
			int matching = counts.getOrDefault(entry.target(), 0);
			resolved.put(new EffectKey(axis, entry.target()), settings.spawn() * seasoningFactor(settings, matching));
			resolved.put(new EffectKey(cutAxis(axis), ""), settings.nonWeight());
		});
		counts.forEach((target, count) -> {
			if (main.isPresent() && main.get().target().equals(target)) return;
			resolved.put(new EffectKey(seasoningAxis(axis), target), seasoningFactor(strengths.weight(axis, target), count));
		});
	}

	private static void addHerdFrequency(MealEffectsComponent meal, StrengthProvider strengths, Map<EffectKey, Float> resolved) {
		String axis = MealEffectType.HERD_FREQUENCY_INCREASE.axis();
		Optional<MealEffectEntry> main = meal.primary().filter(entry -> entry.type().axis().equals(axis));
		int up = typeCount(meal, MealEffectType.HERD_FREQUENCY_INCREASE);
		int down = typeCount(meal, MealEffectType.HERD_FREQUENCY_DECREASE);
		if (main.isEmpty() && up == down) return;
		WeightSettings settings = strengths.weight(axis, "");
		int direction = main.map(entry -> entry.type().direction()).orElse(up > down ? 1 : -1);
		float base = main.isEmpty() ? 1F : direction > 0 ? settings.spawn() : 1F / settings.spawn();
		float herds = base * seasoningFactor(settings, up) / seasoningFactor(settings, down);
		resolved.put(new EffectKey(axis, ""), direction * herds);
		if (main.isPresent()) {
			resolved.put(new EffectKey(cutAxis(axis), ""), direction > 0 ? settings.nonWeight() : 1F / settings.nonWeight());
		}
	}

	private static void addYieldBonuses(MealEffectsComponent meal, StrengthProvider strengths, Map<EffectKey, Float> resolved) {
		Optional<String> drinkStat = meal.primary().filter(entry -> entry.type() == MealEffectType.YIELD).map(MealEffectEntry::target);
		seasoningCounts(meal, MealEffectType.YIELD.axis()).forEach((target, count) -> {
			if (drinkStat.isPresent() && drinkStat.get().equals(target)) return;
			resolved.put(new EffectKey(YIELD_BONUS_AXIS, target), seasoningFactor(strengths.weight(MealEffectType.YIELD.axis(), target), count));
		});
	}

	private static void addTera(MealEffectsComponent meal, StrengthProvider strengths, Map<EffectKey, Float> resolved) {
		String axis = MealEffectType.TERA.axis();
		Scale scale = strengths.scale(MealEffectType.TERA);
		Optional<MealEffectEntry> main = meal.primary().filter(entry -> entry.type() == MealEffectType.TERA);
		Map<String, Integer> counts = seasoningCounts(meal, axis);
		main.ifPresent(entry -> resolved.put(new EffectKey(axis, entry.target()),
				scale.base() + scale.perSeasoning() * counts.getOrDefault(entry.target(), 0)));
		counts.forEach((target, count) -> {
			if (main.isPresent() && main.get().target().equals(target)) return;
			resolved.put(new EffectKey(axis, target), scale.perSeasoning() * count);
		});
	}

	private static void addSeasoningChance(MealEffectsComponent meal, StrengthProvider strengths, MealEffectType type, Map<EffectKey, Float> resolved) {
		float perSeasoning = strengths.scale(type).perSeasoning();
		seasoningCounts(meal, type.axis()).forEach((target, count) ->
				resolved.put(new EffectKey(type.axis(), target), perSeasoning * count));
	}

	private static void addSize(MealEffectsComponent meal, StrengthProvider strengths, Map<EffectKey, Float> resolved) {
		Optional<MealEffectType> main = meal.primary().map(MealEffectEntry::type)
				.filter(type -> type == MealEffectType.TINY || type == MealEffectType.GIANT);
		for (MealEffectType type : List.of(MealEffectType.TINY, MealEffectType.GIANT)) {
			Scale scale = strengths.scale(type);
			int count = typeCount(meal, type);
			if (main.isPresent() && main.get() == type) {
				resolved.put(new EffectKey(SIZE_AXIS, ""), type.direction() * ProgressiveMealPotency.extremeSizeChance(scale, count));
			} else if (count > 0) {
				resolved.put(new EffectKey(seasoningAxis(SIZE_AXIS), type.id()),
						type.direction() * Math.min(1F, scale.perSeasoning() * count));
			}
		}
	}

	public static float seasoningSizeChance(MealLoadout loadout, MealEffectType type) {
		return Math.abs(loadout.strength(seasoningAxis(SIZE_AXIS), type.id()));
	}

	private static void addAlpha(MealEffectsComponent meal, StrengthProvider strengths, Map<EffectKey, Float> resolved) {
		Optional<MealEffectType> main = meal.primary().map(MealEffectEntry::type);
		float chance = alphaChance(meal, strengths, MealEffectType.ALPHA_FREQUENCY_INCREASE, main);
		float suppress = alphaChance(meal, strengths, MealEffectType.ALPHA_FREQUENCY_DECREASE, main);
		String axis = MealEffectType.ALPHA_FREQUENCY_INCREASE.axis();
		if (chance > 0F) {
			resolved.put(new EffectKey(ALPHA_CHANCE_AXIS, ""), chance);
			resolved.put(new EffectKey(axis, ""), 1F);
		}
		if (suppress > 0F) {
			resolved.put(new EffectKey(ALPHA_SUPPRESS_AXIS, ""), suppress);
			resolved.put(new EffectKey(axis, ALPHA_SUPPRESS_AXIS), -1F);
		}
	}

	private static float alphaChance(MealEffectsComponent meal, StrengthProvider strengths, MealEffectType type, Optional<MealEffectType> main) {
		Scale scale = strengths.scale(type);
		float chance = (main.isPresent() && main.get() == type ? scale.base() : 0F) + scale.perSeasoning() * typeCount(meal, type);
		return Math.max(0F, Math.min(1F, chance));
	}

	public static float seasoningFactor(WeightSettings settings, int count) {
		return 1F + settings.seasoning() * count;
	}

	private static Map<String, Integer> seasoningCounts(MealEffectsComponent meal, String axis) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (MealEffectEntry entry : meal.seasonings()) {
			if (entry.type().axis().equals(axis)) counts.merge(entry.target(), entry.count(), Integer::sum);
		}
		return counts;
	}

	private static int typeCount(MealEffectsComponent meal, MealEffectType type) {
		return meal.seasonings().stream().filter(entry -> entry.type() == type).mapToInt(MealEffectEntry::count).sum();
	}

	private static void add(Map<EffectKey, Accumulator> combined, MealEffectEntry entry, double strength, double cap) {
		EffectKey key = key(entry);
		combined.compute(key, (ignored, current) -> current == null
				? new Accumulator(strength, Math.abs(cap))
				: new Accumulator(current.strength + strength, Math.min(current.cap, Math.abs(cap))));
	}

	private static EffectKey key(MealEffectEntry entry) {
		return new EffectKey(entry.type().axis(), entry.target());
	}

	private static MealLoadout fromResolvedEffects(List<ResolvedEffect> resolvedEffects, MealPotency potency, int version) {
		if (version < VERSION) return EMPTY;
		Map<EffectKey, Float> effects = new LinkedHashMap<>();
		resolvedEffects.forEach(effect -> effects.put(effect.key(), effect.strength()));
		return new MealLoadout(effects, potency);
	}

	private List<ResolvedEffect> toResolvedEffects() {
		List<ResolvedEffect> resolved = new ArrayList<>();
		effects.forEach((key, strength) -> resolved.add(new ResolvedEffect(key, strength)));
		resolved.sort(Comparator.comparing((ResolvedEffect effect) -> effect.key().axis())
				.thenComparing(effect -> effect.key().target()));
		return resolved;
	}

	public interface StrengthProvider {
		float direct(MealEffectType type);
		float seasoning(MealEffectType type);
		float cap(MealEffectType type);
		WeightSettings weight(String axis, String target);
		Scale scale(MealEffectType type);
		default boolean classic() {
			return false;
		}
	}

	public record EffectKey(String axis, String target) {
		private static final Codec<EffectKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("axis").forGetter(EffectKey::axis),
				Codec.STRING.optionalFieldOf("target", "").forGetter(EffectKey::target)
		).apply(instance, EffectKey::new));
	}

	public record WeightSettings(float spawn, float seasoning, float nonWeight) {
		public static WeightSettings of(CobbleCuisineConfigData config, String axis, String target) {
			if (config.globalSettings.classicMealPowers) {
				CobbleCuisineConfigData.ClassicSettings classic = config.classicSettings;
				if (axis.equals(MealEffectType.TYPE.axis())) return new WeightSettings(classic.typeMultiplier, 0F, classic.typeNonMatchMultiplier);
				if (axis.equals(MealEffectType.EGG_GROUP.axis())) return new WeightSettings(classic.eggGroupMultiplier, 0F, classic.eggGroupNonMatchMultiplier);
				if (axis.equals(MealEffectType.HERD_FREQUENCY_INCREASE.axis())) {
					return new WeightSettings(classic.herdFrequencyMultiplier, 0F, classic.herdFrequencyNonHerdMultiplier);
				}
				return new WeightSettings(1F, 0F, 1F);
			}
			if (axis.equals(MealEffectType.YIELD.axis())) {
				return new WeightSettings(1F, config.yieldMultipliers.get(target).seasoningWeightMultiplier, 1F);
			}
			CobbleCuisineConfigData.WeightPair pair = axis.equals(MealEffectType.TYPE.axis()) ? config.typeMultipliers.get(target)
					: axis.equals(MealEffectType.EGG_GROUP.axis()) ? config.eggGroupMultipliers.get(target)
					: config.herdSettings.herdFrequency;
			return new WeightSettings(pair.spawnWeightMultiplier, pair.seasoningWeightMultiplier, pair.nonWeightMultiplier);
		}
	}

	public record Scale(float base, float perSeasoning) {
		public static Scale of(CobbleCuisineConfigData config, MealEffectType type) {
			if (config.globalSettings.classicMealPowers) return classic(config.classicSettings, type);
			CobbleCuisineConfigData.BoostSettings boosts = config.boostSettings;
			CobbleCuisineConfigData.HerdSettings herds = config.herdSettings;
			return switch (type) {
				case TERA -> new Scale(boosts.teraBaseChance, boosts.teraSeasoningChance);
				case NATURE -> new Scale(0F, boosts.natureSeasoningChance);
				case HIDDEN_ABILITY -> new Scale(0F, boosts.hiddenAbilitySeasoningChance);
				case EXP -> new Scale(boosts.expBaseMultiplier, boosts.expSeasoningMultiplier);
				case CATCH -> new Scale(boosts.catchBaseMultiplier, boosts.catchSeasoningMultiplier);
				case SHINY -> new Scale(boosts.shinyBaseMultiplier, boosts.shinySeasoningMultiplier);
				case IV -> new Scale(boosts.ivBaseMinimum, boosts.ivSeasoningMinimum);
				case TINY -> new Scale(boosts.tinyBaseChance, boosts.tinySeasoningChance);
				case GIANT -> new Scale(boosts.giantBaseChance, boosts.giantSeasoningChance);
				case DURATION -> new Scale(0F, config.effectDuration.durationSeasoningSeconds * 20F);
				case ALPHA_FREQUENCY_INCREASE -> new Scale(herds.alphaIncreaseBaseChance, herds.alphaIncreaseSeasoningChance);
				case ALPHA_FREQUENCY_DECREASE -> new Scale(herds.alphaDecreaseBaseChance, herds.alphaDecreaseSeasoningChance);
				default -> new Scale(0F, 0F);
			};
		}

		public static Scale ivMaximum(CobbleCuisineConfigData config) {
			if (config.globalSettings.classicMealPowers) return new Scale(config.classicSettings.ivMaximum, 0F);
			return new Scale(config.boostSettings.ivBaseMaximum, config.boostSettings.ivSeasoningMaximum);
		}

		public static int ivCap(CobbleCuisineConfigData config) {
			return config.globalSettings.classicMealPowers ? config.classicSettings.ivMaximum : config.boostSettings.ivMaxValue;
		}

		private static Scale classic(CobbleCuisineConfigData.ClassicSettings classic, MealEffectType type) {
			return switch (type) {
				case TERA -> new Scale(classic.teraChance, 0F);
				case NATURE -> new Scale(0F, classic.natureChancePerMint);
				case HIDDEN_ABILITY -> new Scale(0F, classic.hiddenAbilityChancePerSauce);
				case DURATION -> new Scale(0F, classic.durationSeasoningSeconds * 20F);
				case EXP -> new Scale(classic.expMultiplier, 0F);
				case CATCH -> new Scale(classic.catchMultiplier, 0F);
				case SHINY -> new Scale(classic.shinyMultiplier, 0F);
				case IV -> new Scale(classic.ivMinimum, 0F);
				case TINY -> new Scale(classic.tinyChance, 0F);
				case GIANT -> new Scale(classic.giantChance, 0F);
				case ALPHA_FREQUENCY_INCREASE -> new Scale(classic.alphaIncreaseChance, 0F);
				case ALPHA_FREQUENCY_DECREASE -> new Scale(classic.alphaDecreaseChance, 0F);
				default -> new Scale(0F, 0F);
			};
		}
	}

	private record ResolvedEffect(EffectKey key, float strength) {
	}

	private record Accumulator(double strength, double cap) {
	}
}
