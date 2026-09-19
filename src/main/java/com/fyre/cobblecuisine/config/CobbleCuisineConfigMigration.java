package com.fyre.cobblecuisine.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class CobbleCuisineConfigMigration {
	private static final float LEGACY_TYPE_NON_MATCH_MULTIPLIER = 0.5f;
	private static final float TYPE_NON_MATCH_MULTIPLIER = 0.1f;

	private CobbleCuisineConfigMigration() {
	}

	static void migrate(JsonObject json, int loadedVersion, int targetVersion) {
		if (loadedVersion < 5 && json.has("typeMultipliers") && json.get("typeMultipliers").isJsonObject()) {
			for (JsonElement element : json.getAsJsonObject("typeMultipliers").asMap().values()) {
				if (!element.isJsonObject()) continue;
				JsonObject pair = element.getAsJsonObject();
				if (!pair.has("nonWeightMultiplier") || !pair.get("nonWeightMultiplier").isJsonPrimitive()) continue;
				if (Float.compare(pair.get("nonWeightMultiplier").getAsFloat(), LEGACY_TYPE_NON_MATCH_MULTIPLIER) == 0) {
					pair.addProperty("nonWeightMultiplier", TYPE_NON_MATCH_MULTIPLIER);
				}
			}
		}
		json.addProperty("CONFIG_VERSION_INTERNAL", targetVersion);
	}
}
