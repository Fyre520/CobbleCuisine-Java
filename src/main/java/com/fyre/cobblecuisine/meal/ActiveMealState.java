package com.fyre.cobblecuisine.meal;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ActiveMealState {
	private static final List<Set<MealEffectType>> OPPOSITES = List.of(
			Set.of(MealEffectType.TINY, MealEffectType.GIANT),
			Set.of(MealEffectType.HERD_FREQUENCY_INCREASE, MealEffectType.HERD_FREQUENCY_DECREASE),
			Set.of(MealEffectType.ALPHA_FREQUENCY_INCREASE, MealEffectType.ALPHA_FREQUENCY_DECREASE)
	);

	private ActiveMealState() {
	}

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> refreshDisplay(handler.player));
	}

	public static void add(ServerPlayerEntity player, MealLoadout loadout, Optional<MealEffectType> primary, int duration) {
		if (duration <= 0) throw new IllegalArgumentException("duration must be positive");
		long now = now(player);
		List<ActiveMeal> meals = new ArrayList<>();
		if (stacking()) {
			for (ActiveMeal meal : meals(player)) {
				ActiveMeal remaining = meal.without(loadout, primary);
				if (meal.loadout().effects().isEmpty() || !remaining.loadout().effects().isEmpty()) meals.add(remaining);
			}
		}
		meals.add(new ActiveMeal(loadout, primary, now + duration));
		int maximum = stacking() ? Math.max(1, CobbleCuisineConfig.data.globalSettings.maxActiveMeals) : 1;
		while (meals.size() > maximum) meals.removeFirst();
		player.setAttached(CobbleCuisineMealAttachments.ACTIVE_MEALS, List.copyOf(meals));
		player.removeAttached(CobbleCuisineMealAttachments.ACTIVE_MEAL);
		show(player, meals, now);
	}

	public static Optional<MealLoadout> getActive(ServerPlayerEntity player) {
		List<ActiveMeal> meals = meals(player);
		if (meals.isEmpty()) return Optional.empty();
		Map<MealLoadout.EffectKey, Float> effects = new LinkedHashMap<>();
		for (ActiveMeal meal : meals) effects.putAll(meal.loadout().effects());
		return Optional.of(new MealLoadout(effects, meals.getLast().loadout().potency()));
	}

	public static List<MealLoadout> getActiveMeals(ServerPlayerEntity player) {
		return meals(player).stream().map(ActiveMeal::loadout).toList();
	}

	public static Optional<MealLoadout> getActiveWith(ServerPlayerEntity player, String axis) {
		return newestWith(getActiveMeals(player), axis);
	}

	public static Optional<MealLoadout> newestWith(List<MealLoadout> meals, String axis) {
		for (int i = meals.size() - 1; i >= 0; i--) {
			if (meals.get(i).strength(axis, "") > 0.0F) return Optional.of(meals.get(i));
		}
		return Optional.empty();
	}

	public static void clear(ServerPlayerEntity player) {
		removeEffects(player);
		player.removeAttached(CobbleCuisineMealAttachments.ACTIVE_MEALS);
		player.removeAttached(CobbleCuisineMealAttachments.ACTIVE_MEAL);
	}

	private static List<ActiveMeal> meals(ServerPlayerEntity player) {
		StatusEffectInstance satiated = player.getStatusEffect(CobbleCuisineEffects.SATIATED.entry);
		if (satiated == null) return List.of();
		long now = now(player);
		List<ActiveMeal> stored = player.getAttached(CobbleCuisineMealAttachments.ACTIVE_MEALS);
		if (stored == null) {
			MealLoadout legacy = player.getAttached(CobbleCuisineMealAttachments.ACTIVE_MEAL);
			if (legacy == null) return List.of();
			return List.of(new ActiveMeal(legacy, Optional.empty(), now + satiated.getDuration()));
		}
		return stored.stream().filter(meal -> meal.expiresAt() > now).toList();
	}

	private static void refreshDisplay(ServerPlayerEntity player) {
		List<ActiveMeal> meals = meals(player);
		if (!meals.isEmpty()) show(player, meals, now(player));
	}

	private static void show(ServerPlayerEntity player, List<ActiveMeal> meals, long now) {
		removeEffects(player);
		long longest = meals.stream().mapToLong(ActiveMeal::expiresAt).max().orElse(now + 1) - now;
		player.addStatusEffect(new StatusEffectInstance(
				CobbleCuisineEffects.SATIATED.entry,
				(int) Math.max(1, Math.min(Integer.MAX_VALUE, longest)),
				0,
				false,
				false,
				false
		));
		for (ActiveMeal meal : meals) {
			int remaining = (int) Math.max(1, Math.min(Integer.MAX_VALUE, meal.expiresAt() - now));
			displayedEffects(meal.loadout()).forEach((effect, level) ->
					player.addStatusEffect(new StatusEffectInstance(effect, remaining, level - 1, false, true, true)));
		}
	}

	private static void removeEffects(ServerPlayerEntity player) {
		for (CobbleCuisineEffects effect : CobbleCuisineEffects.values()) {
			player.removeStatusEffect(effect.entry);
		}
	}

	private static boolean stacking() {
		return CobbleCuisineConfig.data.globalSettings.allowMultipleActiveMeals;
	}

	private static long now(ServerPlayerEntity player) {
		return player.getServerWorld().getTime();
	}

	private static Map<RegistryEntry<StatusEffect>, Integer> displayedEffects(MealLoadout loadout) {
		Map<RegistryEntry<StatusEffect>, Integer> displayed = new LinkedHashMap<>();
		loadout.effects().forEach((key, strength) -> displayEffect(key, strength)
				.ifPresent(effect -> displayed.merge(effect.entry(), effect.level(), Math::max)));
		return displayed;
	}

	private static Optional<DisplayedEffect> displayEffect(MealLoadout.EffectKey key, float strength) {
		if (!Float.isFinite(strength) || strength == 0.0f) return Optional.empty();
		return MealLoadout.powerAxis(key.axis()).flatMap(axis -> Arrays.stream(MealEffectType.values())
				.filter(MealEffectType::active)
				.filter(type -> type.axis().equals(axis) && strength * type.direction() > 0.0f)
				.findFirst())
				.flatMap(type -> registeredEffect(new MealEffectEntry(type, key.target(), 1)))
				.map(effect -> new DisplayedEffect(effect, 1));
	}

	private static Optional<RegistryEntry<StatusEffect>> registeredEffect(MealEffectEntry entry) {
		String id = MealEffectMappings.statusEffectId(entry);
		return Arrays.stream(CobbleCuisineEffects.values())
				.map(effect -> effect.entry)
				.filter(effectEntry -> effectEntry.getKey().map(key -> key.getValue().toString().equals(id)).orElse(false))
				.findFirst();
	}

	private static Set<String> oppositeAxes(MealEffectType type) {
		return switch (type) {
			case TINY, GIANT -> Set.of(MealLoadout.SIZE_AXIS);
			case HERD_FREQUENCY_INCREASE, HERD_FREQUENCY_DECREASE -> Set.of(type.axis(), MealLoadout.cutAxis(type.axis()));
			case ALPHA_FREQUENCY_INCREASE, ALPHA_FREQUENCY_DECREASE ->
					Set.of(type.axis(), MealLoadout.ALPHA_CHANCE_AXIS, MealLoadout.ALPHA_SUPPRESS_AXIS);
			default -> Set.of();
		};
	}

	private record DisplayedEffect(RegistryEntry<StatusEffect> entry, int level) {
	}

	public record ActiveMeal(MealLoadout loadout, Optional<MealEffectType> primary, long expiresAt) {
		public static final Codec<ActiveMeal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MealLoadout.CODEC.fieldOf("loadout").forGetter(ActiveMeal::loadout),
				MealEffectType.CODEC.optionalFieldOf("primary").forGetter(ActiveMeal::primary),
				Codec.LONG.fieldOf("expires_at").forGetter(ActiveMeal::expiresAt)
		).apply(instance, ActiveMeal::new));

		ActiveMeal without(MealLoadout newer, Optional<MealEffectType> newerPrimary) {
			Map<MealLoadout.EffectKey, Float> effects = new LinkedHashMap<>(loadout.effects());
			newer.effects().keySet().stream()
					.filter(key -> !key.axis().endsWith("_cut"))
					.forEach(effects::remove);
			boolean opposed = newerPrimary.isPresent() && primary.isPresent() && OPPOSITES.stream()
					.anyMatch(group -> group.contains(newerPrimary.get()) && group.contains(primary.get()));
			if (opposed) {
				Set<String> axes = oppositeAxes(primary.get());
				effects.keySet().removeIf(key -> axes.contains(key.axis()));
			}
			for (MealLoadout.EffectKey key : List.copyOf(effects.keySet())) {
				if (!key.axis().endsWith("_cut")) continue;
				String axis = key.axis().substring(0, key.axis().length() - "_cut".length());
				if (effects.keySet().stream().noneMatch(other -> other.axis().equals(axis))) effects.remove(key);
			}
			return new ActiveMeal(new MealLoadout(effects, loadout.potency()), primary, expiresAt);
		}
	}
}
