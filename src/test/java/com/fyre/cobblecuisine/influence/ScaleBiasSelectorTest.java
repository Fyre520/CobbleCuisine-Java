package com.fyre.cobblecuisine.influence;

import com.fyre.cobblecuisine.config.CobbleCuisineConfigData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScaleBiasSelectorTest {
	private static final float MIN_SCALE = 0.95F;
	private static final float MAX_SCALE = 1.05F;

	@Test
	void zeroTinyBiasSplitsEvenlyBetweenXsAndS() {
		assertEquals(0.96F, ScaleBiasSelector.selectScale(true, 0F, 0.49F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
		assertEquals(0.98F, ScaleBiasSelector.selectScale(true, 0F, 0.51F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
	}

	@Test
	void zeroGiantBiasSplitsEvenlyBetweenXlAndL() {
		assertEquals(1.04F, ScaleBiasSelector.selectScale(false, 0F, 0.49F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
		assertEquals(1.02F, ScaleBiasSelector.selectScale(false, 0F, 0.51F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
	}

	@Test
	void halfBiasSelectsTheExtremeCategorySeventyFivePercentOfTheTime() {
		assertEquals(0.96F, ScaleBiasSelector.selectScale(true, 0.5F, 0.74F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
		assertEquals(0.98F, ScaleBiasSelector.selectScale(true, 0.5F, 0.76F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
		assertEquals(1.04F, ScaleBiasSelector.selectScale(false, 0.5F, 0.74F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
		assertEquals(1.02F, ScaleBiasSelector.selectScale(false, 0.5F, 0.76F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
	}

	@Test
	void fullBiasAlwaysSelectsTheExtremeCategory() {
		assertEquals(0.96F, ScaleBiasSelector.selectScale(true, 1F, 0.999F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
		assertEquals(1.04F, ScaleBiasSelector.selectScale(false, 1F, 0.999F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
	}

	@Test
	void scaleRollStaysWithinTheSelectedCategory() {
		assertEquals(0.95F, ScaleBiasSelector.selectScale(true, 1F, 0F, 0F, MIN_SCALE, MAX_SCALE), 0.0001F);
		assertEquals(0.969998F, ScaleBiasSelector.selectScale(true, 1F, 0F, 0.9999F, MIN_SCALE, MAX_SCALE), 0.0001F);
		assertEquals(1.03F, ScaleBiasSelector.selectScale(false, 1F, 0F, 0F, MIN_SCALE, MAX_SCALE), 0.0001F);
		assertEquals(1.049998F, ScaleBiasSelector.selectScale(false, 1F, 0F, 0.9999F, MIN_SCALE, MAX_SCALE), 0.0001F);
	}

	@Test
	void tinyAndGiantReadIndependentBiasSettings() {
		CobbleCuisineConfigData.BoostSettings settings = new CobbleCuisineConfigData.BoostSettings();
		settings.tinySizeBias = 1F;
		settings.giantSizeBias = 0F;

		assertEquals(0.96F, ScaleBiasSelector.selectScale(true, settings, 0.75F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
		assertEquals(1.02F, ScaleBiasSelector.selectScale(false, settings, 0.75F, 0.5F, MIN_SCALE, MAX_SCALE), 0.0001F);
	}
}
