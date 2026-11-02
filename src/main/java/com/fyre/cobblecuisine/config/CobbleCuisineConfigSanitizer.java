package com.fyre.cobblecuisine.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;

final class CobbleCuisineConfigSanitizer {
	private static final Gson SNAPSHOT_GSON = new GsonBuilder().serializeSpecialFloatingPointValues().create();

	private CobbleCuisineConfigSanitizer() {
	}

	static boolean sanitize(CobbleCuisineConfigData data) {
		String before = SNAPSHOT_GSON.toJson(data);
		CobbleCuisineConfigData defaults = new CobbleCuisineConfigData();
		sanitizeBoosts(data.boostSettings, defaults.boostSettings);
		sanitizeClassic(data.classicSettings, defaults.classicSettings);
		data.globalSettings.maxActiveMeals = clamp(data.globalSettings.maxActiveMeals, 1, 4);
		sanitizeIntegerFields(data.itemSettings, 0);
		sanitizeFeedingAmounts(data.itemSettings, defaults.itemSettings);
		sanitizeIntegerFields(data.effectDuration, 1);
		sanitizeStrengths(data.mealStrengths, defaults.mealStrengths);
		sanitizeHerd(data.herdSettings, defaults.herdSettings);
		sanitizeProbabilityFields(data.globalSettings.dropRates, defaults.globalSettings.dropRates);
		sanitizeWeightPairs(data.typeMultipliers, defaults.typeMultipliers);
		sanitizeWeightPairs(data.eggGroupMultipliers, defaults.eggGroupMultipliers);
		sanitizeYieldMultipliers(data.yieldMultipliers, defaults.yieldMultipliers);
		return !SNAPSHOT_GSON.toJson(data).equals(before);
	}

	private static void sanitizeClassic(
			CobbleCuisineConfigData.ClassicSettings values,
			CobbleCuisineConfigData.ClassicSettings defaults
	) {
		values.typeMultiplier = nonNegative(values.typeMultiplier, defaults.typeMultiplier);
		values.typeNonMatchMultiplier = nonNegative(values.typeNonMatchMultiplier, defaults.typeNonMatchMultiplier);
		values.eggGroupMultiplier = nonNegative(values.eggGroupMultiplier, defaults.eggGroupMultiplier);
		values.eggGroupNonMatchMultiplier = nonNegative(values.eggGroupNonMatchMultiplier, defaults.eggGroupNonMatchMultiplier);
		values.herdFrequencyMultiplier = atLeastOne(values.herdFrequencyMultiplier, defaults.herdFrequencyMultiplier);
		values.herdFrequencyNonHerdMultiplier = nonNegative(values.herdFrequencyNonHerdMultiplier, defaults.herdFrequencyNonHerdMultiplier);
		values.teraChance = probability(values.teraChance, defaults.teraChance);
		values.natureChancePerMint = probability(values.natureChancePerMint, defaults.natureChancePerMint);
		values.hiddenAbilityChancePerSauce = probability(values.hiddenAbilityChancePerSauce, defaults.hiddenAbilityChancePerSauce);
		values.catchMultiplier = atLeastOne(values.catchMultiplier, defaults.catchMultiplier);
		values.expMultiplier = atLeastOne(values.expMultiplier, defaults.expMultiplier);
		values.shinyMultiplier = atLeastOne(values.shinyMultiplier, defaults.shinyMultiplier);
		values.ivMaximum = clamp(values.ivMaximum, 0, 31);
		values.ivMinimum = clamp(values.ivMinimum, 0, values.ivMaximum);
		values.tinyChance = probability(values.tinyChance, defaults.tinyChance);
		values.giantChance = probability(values.giantChance, defaults.giantChance);
		values.alphaIncreaseChance = probability(values.alphaIncreaseChance, defaults.alphaIncreaseChance);
		values.alphaDecreaseChance = probability(values.alphaDecreaseChance, defaults.alphaDecreaseChance);
		values.pokePuffFriendship = Math.max(0, values.pokePuffFriendship);
		values.malasadaFriendship = Math.max(0, values.malasadaFriendship);
		values.pokePuffHealing = Math.max(0, values.pokePuffHealing);
		values.malasadaHealing = Math.max(0, values.malasadaHealing);
		values.durationSeasoningSeconds = Math.max(0, values.durationSeasoningSeconds);
	}

	private static void sanitizeBoosts(
			CobbleCuisineConfigData.BoostSettings values,
			CobbleCuisineConfigData.BoostSettings defaults
	) {
		values.effectDistanceBlocks = nonNegative(values.effectDistanceBlocks, defaults.effectDistanceBlocks);

		values.expBaseMultiplier = nonNegative(values.expBaseMultiplier, defaults.expBaseMultiplier);
		values.expSeasoningMultiplier = nonNegative(values.expSeasoningMultiplier, defaults.expSeasoningMultiplier);
		values.catchRateMultiplier = atLeastOne(values.catchRateMultiplier, defaults.catchRateMultiplier);
		values.catchBaseMultiplier = atLeastOne(values.catchBaseMultiplier, defaults.catchBaseMultiplier);
		values.shinyBaseMultiplier = atLeastOne(values.shinyBaseMultiplier, defaults.shinyBaseMultiplier);
		values.shinySeasoningMultiplier = nonNegative(values.shinySeasoningMultiplier, defaults.shinySeasoningMultiplier);

		values.catchSeasoningMultiplier = nonNegative(values.catchSeasoningMultiplier, defaults.catchSeasoningMultiplier);
		values.teraBaseChance = probability(values.teraBaseChance, defaults.teraBaseChance);
		values.teraSeasoningChance = probability(values.teraSeasoningChance, defaults.teraSeasoningChance);
		values.natureSeasoningChance = probability(values.natureSeasoningChance, defaults.natureSeasoningChance);
		values.hiddenAbilitySeasoningChance = probability(values.hiddenAbilitySeasoningChance, defaults.hiddenAbilitySeasoningChance);
		values.tinyBaseChance = probability(values.tinyBaseChance, defaults.tinyBaseChance);
		values.tinySeasoningChance = probability(values.tinySeasoningChance, defaults.tinySeasoningChance);
		values.giantBaseChance = probability(values.giantBaseChance, defaults.giantBaseChance);
		values.giantSeasoningChance = probability(values.giantSeasoningChance, defaults.giantSeasoningChance);

		values.ivMaxValue = clamp(values.ivMaxValue, 0, 31);
		values.ivBaseMinimum = clamp(values.ivBaseMinimum, 0, values.ivMaxValue);
		values.ivSeasoningMinimum = clamp(values.ivSeasoningMinimum, 0, 31);
		values.ivBaseMaximum = clamp(values.ivBaseMaximum, values.ivBaseMinimum, values.ivMaxValue);
		values.ivSeasoningMaximum = clamp(values.ivSeasoningMaximum, 0, 31);
		values.shakeEvPower = clamp(values.shakeEvPower, 0, 2);
	}

	private static void sanitizeStrengths(
			CobbleCuisineConfigData.MealStrengths values,
			CobbleCuisineConfigData.MealStrengths defaults
	) {
		for (Field field : CobbleCuisineConfigData.MealStrengths.class.getFields()) {
			if (field.getType() != CobbleCuisineConfigData.Strength.class) continue;
			try {
				CobbleCuisineConfigData.Strength strength = (CobbleCuisineConfigData.Strength) field.get(values);
				CobbleCuisineConfigData.Strength fallback = (CobbleCuisineConfigData.Strength) field.get(defaults);
				strength.direct = nonNegative(strength.direct, fallback.direct);
				strength.seasoning = clamp(finite(strength.seasoning, fallback.seasoning), 0F, strength.direct);
				strength.cap = Math.max(strength.direct, nonNegative(strength.cap, fallback.cap));
			} catch (IllegalAccessException exception) {
				throw new IllegalStateException("Unable to sanitize meal strength " + field.getName(), exception);
			}
		}
	}

	private static void sanitizeHerd(
			CobbleCuisineConfigData.HerdSettings values,
			CobbleCuisineConfigData.HerdSettings defaults
	) {
		sanitizeWeightPair(values.herdFrequency, defaults.herdFrequency);
		values.alphaIncreaseBaseChance = probability(values.alphaIncreaseBaseChance, defaults.alphaIncreaseBaseChance);
		values.alphaIncreaseSeasoningChance = probability(values.alphaIncreaseSeasoningChance, defaults.alphaIncreaseSeasoningChance);
		values.alphaDecreaseBaseChance = probability(values.alphaDecreaseBaseChance, defaults.alphaDecreaseBaseChance);
		values.alphaDecreaseSeasoningChance = probability(values.alphaDecreaseSeasoningChance, defaults.alphaDecreaseSeasoningChance);
	}

	private static void sanitizeProbabilityFields(Object values, Object defaults) {
		for (Field field : values.getClass().getFields()) {
			if (field.getType() != float.class) continue;
			try {
				field.setFloat(values, probability(field.getFloat(values), field.getFloat(defaults)));
			} catch (IllegalAccessException exception) {
				throw new IllegalStateException("Unable to sanitize probability " + field.getName(), exception);
			}
		}
	}

	private static void sanitizeIntegerFields(Object values, int minimum) {
		for (Field field : values.getClass().getFields()) {
			if (field.getType() != int.class) continue;
			try {
				field.setInt(values, Math.max(minimum, field.getInt(values)));
			} catch (IllegalAccessException exception) {
				throw new IllegalStateException("Unable to sanitize integer " + field.getName(), exception);
			}
		}
	}

	private static void sanitizeFeedingAmounts(Object values, Object defaults) {
		for (Field field : values.getClass().getFields()) {
			if (field.getType() != CobbleCuisineConfigData.FeedingAmount.class) continue;
			try {
				CobbleCuisineConfigData.FeedingAmount feeding = (CobbleCuisineConfigData.FeedingAmount) field.get(values);
				CobbleCuisineConfigData.FeedingAmount fallback = (CobbleCuisineConfigData.FeedingAmount) field.get(defaults);
				feeding.amount = nonNegative(feeding.amount, fallback.amount);
				feeding.baseMultiplier = nonNegative(feeding.baseMultiplier, fallback.baseMultiplier);
				feeding.perSeasoningMultiplier = nonNegative(feeding.perSeasoningMultiplier, fallback.perSeasoningMultiplier);
			} catch (IllegalAccessException exception) {
				throw new IllegalStateException("Unable to sanitize feeding amount " + field.getName(), exception);
			}
		}
	}

	private static void sanitizeWeightPair(CobbleCuisineConfigData.WeightPair pair, CobbleCuisineConfigData.WeightPair fallback) {
		pair.spawnWeightMultiplier = positive(pair.spawnWeightMultiplier, fallback.spawnWeightMultiplier);
		pair.seasoningWeightMultiplier = nonNegative(pair.seasoningWeightMultiplier, fallback.seasoningWeightMultiplier);
		pair.nonWeightMultiplier = positive(pair.nonWeightMultiplier, fallback.nonWeightMultiplier);
	}

	private static void sanitizeYieldMultipliers(Object values, Object defaults) {
		for (Field field : values.getClass().getFields()) {
			if (field.getType() != CobbleCuisineConfigData.YieldMultiplier.class) continue;
			try {
				CobbleCuisineConfigData.YieldMultiplier yield = (CobbleCuisineConfigData.YieldMultiplier) field.get(values);
				CobbleCuisineConfigData.YieldMultiplier fallback = (CobbleCuisineConfigData.YieldMultiplier) field.get(defaults);
				yield.seasoningWeightMultiplier = nonNegative(yield.seasoningWeightMultiplier, fallback.seasoningWeightMultiplier);
			} catch (IllegalAccessException exception) {
				throw new IllegalStateException("Unable to sanitize yield multiplier " + field.getName(), exception);
			}
		}
	}

	private static void sanitizeWeightPairs(Object values, Object defaults) {
		for (Field field : values.getClass().getFields()) {
			if (field.getType() != CobbleCuisineConfigData.WeightPair.class) continue;
			try {
				sanitizeWeightPair((CobbleCuisineConfigData.WeightPair) field.get(values),
						(CobbleCuisineConfigData.WeightPair) field.get(defaults));
			} catch (IllegalAccessException exception) {
				throw new IllegalStateException("Unable to sanitize weight pair " + field.getName(), exception);
			}
		}
	}

	private static float atLeastOne(float value, float fallback) {
		return Math.max(1F, finite(value, fallback));
	}

	private static float nonNegative(float value, float fallback) {
		return Math.max(0F, finite(value, fallback));
	}

	private static float positive(float value, float fallback) {
		return Math.max(Float.MIN_VALUE, finite(value, fallback));
	}

	private static float probability(float value, float fallback) {
		return clamp(finite(value, fallback), 0F, 1F);
	}

	private static float finite(float value, float fallback) {
		return Float.isFinite(value) ? value : fallback;
	}

	private static float clamp(float value, float minimum, float maximum) {
		return Math.max(minimum, Math.min(value, maximum));
	}

	private static int clamp(int value, int minimum, int maximum) {
		return Math.max(minimum, Math.min(value, maximum));
	}
}
