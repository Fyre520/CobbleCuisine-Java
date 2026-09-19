package com.fyre.cobblecuisine.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class CobbleCuisineUtilsTest {
	@Test
	void healingAddsToCurrentHealth() {
		assertEquals(60, CobbleCuisineUtils.calculateHealedHealth(40, 100, 20));
	}

	@Test
	void healingClampsAtMaximumHealth() {
		assertEquals(100, CobbleCuisineUtils.calculateHealedHealth(90, 100, 20));
	}

	@Test
	void shuffledCopyDoesNotExposeTheSourceList() {
		List<String> source = new ArrayList<>(List.of("a", "b", "c"));

		List<String> copy = CobbleCuisineUtils.shuffledCopy(source);
		copy.clear();

		assertNotSame(source, copy);
		assertEquals(List.of("a", "b", "c"), source);
	}
}
