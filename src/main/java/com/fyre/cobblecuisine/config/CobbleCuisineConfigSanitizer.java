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
		sanitizeIntegerFields(data.itemSettings, 0);
		sanitizeIntegerFields(data.effectDuration, 1);
		sanitizeProbabilityFields(data.dropRates, defaults.dropRates);
		sanitizeWeightPairs(data.typeMultipliers, defaults.typeMultipliers);
		sanitizeWeightPairs(data.eggGroupMultipliers, defaults.eggGroupMultipliers);
		return !SNAPSHOT_GSON.toJson(data).equals(before);
	}

	private static void sanitizeBoosts(
			CobbleCuisineConfigData.BoostSettings values,
			CobbleCuisineConfigData.BoostSettings defaults
	) {
		values.effectDistanceBlocks = nonNegative(values.effectDistanceBlocks, defaults.effectDistanceBlocks);

		values.expBoostMultiplier = nonNegative(values.expBoostMultiplier, defaults.expBoostMultiplier);
		values.catchRateMultiplier = nonNegative(values.catchRateMultiplier, defaults.catchRateMultiplier);
		values.shinyBoostMultiplier = finite(values.shinyBoostMultiplier, defaults.shinyBoostMultiplier);
		if (values.shinyBoostMultiplier <= 0F) values.shinyBoostMultiplier = defaults.shinyBoostMultiplier;

		values.teraBoostChance = probability(values.teraBoostChance, defaults.teraBoostChance);
		values.natureBoostChance = probability(values.natureBoostChance, defaults.natureBoostChance);
		// Legacy values above one (including the default 2) mean guaranteed success.
		values.haBoostChance = nonNegative(values.haBoostChance, defaults.haBoostChance);
		values.tinySizeBias = probability(values.tinySizeBias, defaults.tinySizeBias);
		values.giantSizeBias = probability(values.giantSizeBias, defaults.giantSizeBias);

		values.ivMinValue = clamp(values.ivMinValue, 0, 31);
		values.ivMaxValue = clamp(values.ivMaxValue, values.ivMinValue, 31);
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

	private static void sanitizeWeightPairs(Object values, Object defaults) {
		for (Field field : values.getClass().getFields()) {
			if (field.getType() != CobbleCuisineConfigData.WeightPair.class) continue;
			try {
				CobbleCuisineConfigData.WeightPair pair = (CobbleCuisineConfigData.WeightPair) field.get(values);
				CobbleCuisineConfigData.WeightPair fallback = (CobbleCuisineConfigData.WeightPair) field.get(defaults);
				pair.weightMultiplier = positive(pair.weightMultiplier, fallback.weightMultiplier);
				pair.nonWeightMultiplier = positive(pair.nonWeightMultiplier, fallback.nonWeightMultiplier);
			} catch (IllegalAccessException exception) {
				throw new IllegalStateException("Unable to sanitize weight pair " + field.getName(), exception);
			}
		}
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
