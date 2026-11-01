package com.fyre.cobblecuisine.config;

import com.fyre.cobblecuisine.meal.MealEffectMappings;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TooltipSettings {
	private static volatile Connection connection;
	private static volatile LocalSnapshot localSnapshot;
	private static final Strength ZERO = new Strength(0, 0, 0);
	private static final MealLoadout.WeightSettings NEUTRAL_WEIGHT = new MealLoadout.WeightSettings(1F, 0F, 1F);
	private static final MealLoadout.Scale ZERO_SCALE = new MealLoadout.Scale(0F, 0F);
	private static final List<MealEffectType> WEIGHTED = List.of(
			MealEffectType.TYPE, MealEffectType.EGG_GROUP, MealEffectType.HERD_FREQUENCY_INCREASE, MealEffectType.YIELD);

	private TooltipSettings() { }

	private record Connection(Object identity, Snapshot snapshot) { }
	private record LocalSnapshot(CobbleCuisineConfigData config, Snapshot snapshot) { }

	public record Strength(float direct, float seasoning, float cap) { }

	public record Feeding(float amount, float baseMultiplier, float perSeasoningMultiplier) {
		public static Feeding pokePuffFriendship(CobbleCuisineConfigData config) {
			return of(config, config.itemSettings.pokePuffFriendship, config.classicSettings.pokePuffFriendship);
		}

		public static Feeding malasadaFriendship(CobbleCuisineConfigData config) {
			return of(config, config.itemSettings.malasadaFriendship, config.classicSettings.malasadaFriendship);
		}

		public static Feeding pokePuffHealing(CobbleCuisineConfigData config) {
			return of(config, config.itemSettings.pokePuffHealing, config.classicSettings.pokePuffHealing);
		}

		public static Feeding malasadaHealing(CobbleCuisineConfigData config) {
			return of(config, config.itemSettings.malasadaHealing, config.classicSettings.malasadaHealing);
		}

		private static Feeding of(CobbleCuisineConfigData config, CobbleCuisineConfigData.FeedingAmount feeding, int classicAmount) {
			if (config.globalSettings.classicMealPowers) return new Feeding(classicAmount, 1F, 0F);
			return new Feeding(feeding.amount, feeding.baseMultiplier, feeding.perSeasoningMultiplier);
		}
	}

	public record Snapshot(boolean classic, Feeding pokePuffFriendship, Feeding malasadaFriendship,
			Feeding pokePuffHealing, Feeding malasadaHealing,
			Map<MealEffectType, Strength> strengths, Map<String, MealLoadout.WeightSettings> weights,
			Map<MealEffectType, MealLoadout.Scale> scales, MealLoadout.Scale ivMaximum, int ivMaxValue,
			Map<String, Integer> itemAmounts) {
		public Snapshot {
			strengths = Map.copyOf(strengths);
			weights = Map.copyOf(weights);
			scales = Map.copyOf(scales);
			itemAmounts = Map.copyOf(itemAmounts);
		}

		public float direct(MealEffectType type) {
			return strengths.getOrDefault(type, ZERO).direct();
		}

		public float seasoning(MealEffectType type) {
			return strengths.getOrDefault(type, ZERO).seasoning();
		}

		public float cap(MealEffectType type) {
			return strengths.getOrDefault(type, ZERO).cap();
		}

		public MealLoadout.WeightSettings weight(String axis, String target) {
			return weights.getOrDefault(weightKey(axis, target), NEUTRAL_WEIGHT);
		}

		public MealLoadout.Scale scale(MealEffectType type) {
			return scales.getOrDefault(type, ZERO_SCALE);
		}

		public Optional<Integer> itemAmount(String id) { return Optional.ofNullable(itemAmounts.get(id)); }
	}

	public static Optional<Snapshot> current() {
		Connection active = connection;
		if (active != null) return Optional.ofNullable(active.snapshot());
		CobbleCuisineConfigData config = CobbleCuisineConfig.data;
		LocalSnapshot cached = localSnapshot;
		if (cached == null || cached.config() != config) {
			cached = new LocalSnapshot(config, capture(config));
			localSnapshot = cached;
		}
		return Optional.of(cached.snapshot());
	}

	public static void beginConnection(Object identity) {
		connection = new Connection(identity, null);
	}

	public static void receive(Object identity, Snapshot snapshot) {
		Connection active = connection;
		if (active != null && active.identity() == identity) connection = new Connection(identity, snapshot);
	}

	public static void endConnection(Object identity) {
		Connection active = connection;
		if (active != null && active.identity() == identity) connection = null;
	}

	public static Snapshot capture() {
		return capture(CobbleCuisineConfig.data);
	}

	private static Snapshot capture(CobbleCuisineConfigData config) {
		Map<MealEffectType, Strength> strengths = new EnumMap<>(MealEffectType.class);
		for (MealEffectType type : MealEffectType.values()) {
			switch (type) {
				case IV, TINY, GIANT, SHINY, CATCH, EXP, DUBIOUS, YIELD -> { }
				default -> {
					CobbleCuisineConfigData.Strength strength = config.mealStrengths.get(type);
					float direct = Math.max(0, strength.direct);
					strengths.put(type, new Strength(direct, Math.min(direct, Math.max(0, strength.seasoning)),
							Math.max(direct, Math.max(0, strength.cap))));
				}
			}
		}
		Map<String, MealLoadout.WeightSettings> weights = new HashMap<>();
		for (MealEffectType type : WEIGHTED) {
			for (String target : MealEffectMappings.targets(type)) {
				weights.put(weightKey(type.axis(), target), MealLoadout.WeightSettings.of(config, type.axis(), target));
			}
		}
		Map<MealEffectType, MealLoadout.Scale> scales = new EnumMap<>(MealEffectType.class);
		for (MealEffectType type : MealEffectType.values()) {
			MealLoadout.Scale scale = MealLoadout.Scale.of(config, type);
			if (scale.base() != 0F || scale.perSeasoning() != 0F) scales.put(type, scale);
		}
		Map<String, Integer> amounts = new HashMap<>();
		return new Snapshot(config.globalSettings.classicMealPowers,
				Feeding.pokePuffFriendship(config), Feeding.malasadaFriendship(config),
				Feeding.pokePuffHealing(config), Feeding.malasadaHealing(config),
				strengths, weights, scales, MealLoadout.Scale.ivMaximum(config), MealLoadout.Scale.ivCap(config), amounts);
	}

	public static String weightKey(String axis, String target) {
		return axis + "/" + target;
	}
}
