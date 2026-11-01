package com.fyre.cobblecuisine.event;

public final class ShinySpawnEvent {
	private ShinySpawnEvent() {
	}

	public static float additionalRollChance(float baseRate, float multiplier) {
		if (baseRate <= 1.0f || multiplier <= 1.0f) return 0.0f;
		float baseChance = 1.0f / baseRate;
		float boostedChance = Math.min(1.0f, baseChance * multiplier);
		return Math.max(0.0f, Math.min(1.0f, (boostedChance - baseChance) / (1.0f - baseChance)));
	}
}
