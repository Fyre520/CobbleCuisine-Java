package com.fyre.cobblecuisine.influence;

import com.fyre.cobblecuisine.config.CobbleCuisineConfigData;

final class TypeWeightCalculator {
	private TypeWeightCalculator() {
	}

	static float apply(float weight, boolean matches, CobbleCuisineConfigData.WeightPair settings) {
		float multiplier = matches ? settings.weightMultiplier : settings.nonWeightMultiplier;
		return weight * Math.max(Float.MIN_VALUE, multiplier);
	}
}
