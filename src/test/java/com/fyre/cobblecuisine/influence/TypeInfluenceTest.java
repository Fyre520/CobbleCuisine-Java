package com.fyre.cobblecuisine.influence;

import com.fyre.cobblecuisine.config.CobbleCuisineConfigData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeInfluenceTest {
	@Test
	void defaultTypePairStronglyFavoursMatchingSpawns() {
		var settings = new CobbleCuisineConfigData.TypeMultipliers().fire;

		assertEquals(50.0f, TypeWeightCalculator.apply(10.0f, true, settings));
		assertEquals(1.0f, TypeWeightCalculator.apply(10.0f, false, settings));
	}

	@Test
	void zeroNonMatchConfigurationStillLeavesASelectableWeight() {
		var settings = new CobbleCuisineConfigData.TypeMultipliers().fire;
		settings.nonWeightMultiplier = 0.0f;

		assertTrue(TypeWeightCalculator.apply(1.0f, false, settings) > 0.0f);
	}

	@Test
	void readsUpdatedSettingsInsteadOfCachingInitialValues() {
		var settings = new CobbleCuisineConfigData.TypeMultipliers().fire;
		assertEquals(1.0f, TypeWeightCalculator.apply(10.0f, false, settings));

		settings.nonWeightMultiplier = 0.25f;

		assertEquals(2.5f, TypeWeightCalculator.apply(10.0f, false, settings));
	}
}
