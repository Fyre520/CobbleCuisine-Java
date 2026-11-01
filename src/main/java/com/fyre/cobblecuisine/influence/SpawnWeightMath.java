package com.fyre.cobblecuisine.influence;

public final class SpawnWeightMath {
	private static final float MAX_WEIGHT = 0x1.0p96f;

	private SpawnWeightMath() {
	}

	public static float multiply(float weight, float multiplier) {
		if (!(weight > 0.0f) || !(multiplier > 0.0f)) return 0.0f;
		float product = weight * multiplier;
		return Math.max(Float.MIN_VALUE, Math.min(MAX_WEIGHT, product));
	}
}
