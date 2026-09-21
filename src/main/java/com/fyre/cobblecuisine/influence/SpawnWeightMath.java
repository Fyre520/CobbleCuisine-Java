package com.fyre.cobblecuisine.influence;

/** Bounds adjusted weights for Cobblemon's float-based weighted selection. */
final class SpawnWeightMath {
	// Leaves headroom for repeated float addition across the candidate pool.
	private static final float MAX_WEIGHT = 0x1.0p96f;

	private SpawnWeightMath() { }

	static float multiplier(float value) {
		if (Float.isNaN(value)) return 1.0f;
		return Math.max(Float.MIN_VALUE, Math.min(MAX_WEIGHT, value));
	}

	static float blend(float matchingFraction, float match, float nonMatch) {
		return (float) (matchingFraction * (double) multiplier(match)
				+ (1.0 - matchingFraction) * multiplier(nonMatch));
	}

	static float multiply(float weight, float multiplier) {
		if (!(weight > 0.0f)) return 0.0f;
		float product = weight * multiplier(multiplier);
		return Math.max(Float.MIN_VALUE, Math.min(MAX_WEIGHT, product));
	}
}
