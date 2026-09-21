package com.fyre.cobblecuisine.influence;

import com.fyre.cobblecuisine.config.CobbleCuisineConfigData;

final class TypeWeightCalculator {
	private TypeWeightCalculator() { }

	static float apply(float weight, boolean matches, CobbleCuisineConfigData.WeightPair settings) {
		return apply(weight, matches ? 1.0f : 0.0f, settings);
	}

	static float apply(float weight, float matchingFraction, CobbleCuisineConfigData.WeightPair settings) {
		return SpawnWeightMath.multiply(weight, SpawnWeightMath.blend(
				matchingFraction, settings.weightMultiplier, settings.nonWeightMultiplier));
	}
}
