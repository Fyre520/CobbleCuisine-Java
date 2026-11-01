package com.fyre.cobblecuisine.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class CobbleCuisineConfigMigration {
	private static final float LEGACY_TYPE_NON_MATCH_MULTIPLIER = 0.5f;
	private static final float TYPE_NON_MATCH_MULTIPLIER = 0.1f;
	private static final String[] STANDARD_STRENGTHS = {
			"type", "eggGroup", "tera", "nature", "scale", "yield", "iv", "shiny",
			"catchRate", "experience", "dubious", "hiddenAbility"
	};

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
		if (loadedVersion < 6 && json.has("mealStrengths") && json.get("mealStrengths").isJsonObject()) {
			JsonObject strengths = json.getAsJsonObject("mealStrengths");
			for (String key : STANDARD_STRENGTHS) {
				migrateStrength(strengths, key, 1F, 0.3F, 1.3F, 1F);
			}
			migrateStrength(strengths, "herdFrequencyIncrease", 1F, 0.3F, 1.3F, 1F);
			migrateStrength(strengths, "herdFrequencyDecrease", 0.75F, 0.3F, 1.05F, 0.75F);
			migrateStrength(strengths, "herdSizeIncrease", 0.5F, 0.2F, 0.7F, 0.5F);
			migrateStrength(strengths, "herdSizeDecrease", 0.5F, 0.2F, 0.7F, 0.5F);
			migrateStrength(strengths, "herdLevelIncrease", 10F, 3F, 13F, 10F);
			migrateStrength(strengths, "herdLevelDecrease", 10F, 3F, 13F, 10F);
			migrateStrength(strengths, "herdVariationIncrease", 6F, 3F, 9F, 6F);
			migrateStrength(strengths, "herdVariationDecrease", 6F, 3F, 9F, 6F);
			migrateStrength(strengths, "alphaFrequencyIncrease", 1F, 0.3F, 1.3F, 1F);
			migrateStrength(strengths, "alphaFrequencyDecrease", 0.75F, 0.3F, 1.05F, 0.75F);
		}
		if (loadedVersion < 7 && targetVersion >= 7 && json.has("mealStrengths") && json.get("mealStrengths").isJsonObject()) {
			JsonObject strengths = json.getAsJsonObject("mealStrengths");
			strengths.remove("herdVariationIncrease");
			strengths.remove("herdVariationDecrease");
		}
		if (loadedVersion < 8 && targetVersion >= 8 && json.has("mealStrengths") && json.get("mealStrengths").isJsonObject()) {
			JsonObject strengths = json.getAsJsonObject("mealStrengths");
			CobbleCuisineConfigData.Strength encounter = new CobbleCuisineConfigData.Strength(1F, 0.6F, 1.6F);
			CobbleCuisineConfigData.Strength size = new CobbleCuisineConfigData.Strength(0.45F, 0.45F, 0.9F);
			migrateStrength(strengths, "herdFrequencyIncrease", new CobbleCuisineConfigData.Strength(1F), encounter);
			migrateStrength(strengths, "herdFrequencyDecrease", new CobbleCuisineConfigData.Strength(0.75F), encounter);
			migrateStrength(strengths, "alphaFrequencyIncrease", new CobbleCuisineConfigData.Strength(1F), encounter);
			migrateStrength(strengths, "alphaFrequencyDecrease", new CobbleCuisineConfigData.Strength(0.75F), encounter);
			migrateStrength(strengths, "herdSizeIncrease", new CobbleCuisineConfigData.Strength(0.5F), size);
			migrateStrength(strengths, "herdSizeDecrease", new CobbleCuisineConfigData.Strength(0.5F), size);
		}
		if (loadedVersion < 10 && targetVersion >= 10 && json.has("itemSettings") && json.get("itemSettings").isJsonObject()) {
			JsonObject items = json.getAsJsonObject("itemSettings");
			items.remove("pokepuffFriendship");
			items.remove("malasadaFriendship");
		}
		if (loadedVersion < 11 && targetVersion >= 11) {
			resetWeightPairs(json, "typeMultipliers");
			resetWeightPairs(json, "eggGroupMultipliers");
		}
		if (loadedVersion < 12 && targetVersion >= 12 && json.has("boostSettings") && json.get("boostSettings").isJsonObject()) {
			JsonObject boosts = json.getAsJsonObject("boostSettings");
			replaceValue(boosts, "tinySeasoningChance", 0.15F, 0.10F);
			replaceValue(boosts, "giantSeasoningChance", 0.15F, 0.10F);
		}
		if (loadedVersion < 13 && targetVersion >= 13) moveShakeEvPower(json);
		json.addProperty("CONFIG_VERSION_INTERNAL", targetVersion);
	}

	private static void moveShakeEvPower(JsonObject json) {
		if (!json.has("itemSettings") || !json.get("itemSettings").isJsonObject()) return;
		JsonObject items = json.getAsJsonObject("itemSettings");
		JsonElement power = items.remove("shakeEvPower");
		if (power == null || !power.isJsonPrimitive()) return;
		if (!json.has("boostSettings") || !json.get("boostSettings").isJsonObject()) json.add("boostSettings", new JsonObject());
		json.getAsJsonObject("boostSettings").add("shakeEvPower", power);
	}

	private static void replaceValue(JsonObject section, String key, float oldValue, float newValue) {
		if (matches(section, key, oldValue)) section.addProperty(key, newValue);
	}

	private static void resetWeightPairs(JsonObject json, String section) {
		if (!json.has(section) || !json.get(section).isJsonObject()) return;
		for (JsonElement element : json.getAsJsonObject(section).asMap().values()) {
			if (!element.isJsonObject()) continue;
			JsonObject pair = element.getAsJsonObject();
			pair.remove("weightMultiplier");
			pair.remove("nonWeightMultiplier");
		}
	}

	private static void migrateStrength(
			JsonObject strengths,
			String key,
			float oldDirect,
			float oldSeasoning,
			float oldCap,
			float multiplier
	) {
		migrateStrength(strengths, key,
				new CobbleCuisineConfigData.Strength(oldDirect, oldSeasoning, oldCap),
				new CobbleCuisineConfigData.Strength(multiplier));
	}

	private static void migrateStrength(
			JsonObject strengths,
			String key,
			CobbleCuisineConfigData.Strength oldValues,
			CobbleCuisineConfigData.Strength replacement
	) {
		if (!strengths.has(key) || !strengths.get(key).isJsonObject()) return;
		JsonObject strength = strengths.getAsJsonObject(key);
		if (!matches(strength, "direct", oldValues.direct)
				|| !matches(strength, "seasoning", oldValues.seasoning)
				|| !matches(strength, "cap", oldValues.cap)) return;

		strength.addProperty("direct", replacement.direct);
		strength.addProperty("seasoning", replacement.seasoning);
		strength.addProperty("cap", replacement.cap);
	}

	private static boolean matches(JsonObject object, String key, float expected) {
		return object.has(key)
				&& object.get(key).isJsonPrimitive()
				&& Float.compare(object.get(key).getAsFloat(), expected) == 0;
	}
}
