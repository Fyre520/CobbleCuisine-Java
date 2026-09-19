package com.fyre.cobblecuisine.influence;

import com.fyre.cobblecuisine.config.CobbleCuisineConfigData;

final class ScaleBiasSelector {
	private static final int CATEGORY_COUNT = 5;
	private static final int XS = 0;
	private static final int S = 1;
	private static final int L = 3;
	private static final int XL = 4;

	private ScaleBiasSelector() { }

	static float selectScale(boolean tiny, CobbleCuisineConfigData.BoostSettings settings, float categoryRoll, float scaleRoll, float minScale, float maxScale) {
		float bias = tiny ? settings.tinySizeBias : settings.giantSizeBias;
		return selectScale(tiny, bias, categoryRoll, scaleRoll, minScale, maxScale);
	}

	static float selectScale(boolean tiny, float bias, float categoryRoll, float scaleRoll, float minScale, float maxScale) {
		float clampedBias = clamp(bias, 0F, 1F);
		float extremeChance = 0.5F + clampedBias * 0.5F;
		boolean extreme = categoryRoll < extremeChance;
		int category = tiny ? (extreme ? XS : S) : (extreme ? XL : L);

		float range = Math.max(maxScale - minScale, 0.0001F);
		float segmentSize = range / CATEGORY_COUNT;
		float positionInCategory = clamp(scaleRoll, 0F, Math.nextDown(1F));
		return minScale + segmentSize * (category + positionInCategory);
	}

	private static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(value, max));
	}
}
