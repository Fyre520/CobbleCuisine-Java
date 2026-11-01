package com.fyre.cobblecuisine.meal;

import java.util.List;

public final class NatureTargetSelector {
	private static final List<String> NEUTRAL_NATURES = List.of("hardy", "docile", "serious", "bashful", "quirky");

	private NatureTargetSelector() {
	}

	public static String select(String target, float randomFraction) {
		if (!target.equals("neutral")) return target;
		float roll = Math.max(0.0f, Math.min(Math.nextDown(1.0f), randomFraction));
		return NEUTRAL_NATURES.get((int) (roll * NEUTRAL_NATURES.size()));
	}
}
