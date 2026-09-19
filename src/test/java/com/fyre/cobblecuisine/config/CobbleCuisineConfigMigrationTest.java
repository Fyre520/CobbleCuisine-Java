package com.fyre.cobblecuisine.config;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CobbleCuisineConfigMigrationTest {
	@Test
	void migratesUntouchedLegacyTypeNonMatchMultiplier() {
		JsonObject config = legacyConfig(0.5f, 0.5f, 0.5f);

		CobbleCuisineConfigMigration.migrate(config, 4, 5);

		assertEquals(0.1f, nonMatch(config, "typeMultipliers", "fire"));
	}

	@Test
	void preservesCustomizedTypeNonMatchMultiplier() {
		JsonObject config = legacyConfig(0.5f, 0.25f, 0.5f);

		CobbleCuisineConfigMigration.migrate(config, 4, 5);

		assertEquals(0.25f, nonMatch(config, "typeMultipliers", "water"));
	}

	@Test
	void preservesLegacyEggGroupNonMatchMultiplier() {
		JsonObject config = legacyConfig(0.5f, 0.5f, 0.5f);

		CobbleCuisineConfigMigration.migrate(config, 4, 5);

		assertEquals(0.5f, nonMatch(config, "eggGroupMultipliers", "field"));
	}

	private static JsonObject legacyConfig(float fire, float water, float field) {
		JsonObject config = new JsonObject();
		config.addProperty("CONFIG_VERSION_INTERNAL", 4);
		JsonObject types = new JsonObject();
		types.add("fire", pair(fire));
		types.add("water", pair(water));
		config.add("typeMultipliers", types);
		JsonObject eggGroups = new JsonObject();
		eggGroups.add("field", pair(field));
		config.add("eggGroupMultipliers", eggGroups);
		return config;
	}

	private static JsonObject pair(float nonMatch) {
		JsonObject pair = new JsonObject();
		pair.addProperty("weightMultiplier", 5.0f);
		pair.addProperty("nonWeightMultiplier", nonMatch);
		return pair;
	}

	private static float nonMatch(JsonObject config, String group, String key) {
		return config.getAsJsonObject(group).getAsJsonObject(key).get("nonWeightMultiplier").getAsFloat();
	}
}
