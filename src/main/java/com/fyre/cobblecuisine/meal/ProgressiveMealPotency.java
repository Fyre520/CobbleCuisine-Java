package com.fyre.cobblecuisine.meal;

import com.fyre.cobblecuisine.config.CobbleCuisineConfigData;
import com.fyre.cobblecuisine.config.TooltipSettings;

public final class ProgressiveMealPotency {
	private ProgressiveMealPotency() {
	}

	public static int feedingAmount(double amount, int matchingSlots, TooltipSettings.Feeding feeding) {
		double multiplier = feeding.baseMultiplier() + feeding.perSeasoningMultiplier() * clamp(matchingSlots, 0, 3);
		return (int) Math.min(Integer.MAX_VALUE, Math.round(Math.max(0, amount) * Math.max(0, multiplier)));
	}

	public static IntRange ivRange(MealLoadout.Scale minimum, MealLoadout.Scale maximum, int maxValue, int nuggetCount) {
		int nuggets = clamp(nuggetCount, 0, 3);
		int cap = clamp(maxValue, 0, 31);
		int max = clamp(Math.round(maximum.base() + maximum.perSeasoning() * nuggets), 0, cap);
		int min = Math.round(minimum.base() + minimum.perSeasoning() * nuggets);
		return new IntRange(clamp(min, 0, max), max);
	}

	public static float extremeSizeChance(MealLoadout.Scale scale, int boosterCount) {
		return clamp(scale.base() + scale.perSeasoning() * clamp(boosterCount, 0, 3), 0F, 1F);
	}

	public static float shinyMultiplier(MealLoadout.Scale scale, int nuggetCount) {
		return Math.max(1F, scale.base() + scale.perSeasoning() * clamp(nuggetCount, 0, 3));
	}

	public static float catchMultiplier(MealLoadout loadout, MealLoadout.Scale scale) {
		return catchMultiplier(scale, loadout.potency().count(MealPotency.GOLD_NUGGETS));
	}

	public static float catchMultiplier(MealLoadout.Scale scale, int nuggetCount) {
		return Math.max(1F, scale.base() + scale.perSeasoning() * clamp(nuggetCount, 0, 3));
	}

	public static float expMultiplier(MealLoadout.Scale scale, int nuggetCount) {
		return Math.max(0F, scale.base() + scale.perSeasoning() * clamp(nuggetCount, 0, 3));
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(value, max));
	}

	private static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(value, max));
	}

	public record IntRange(int min, int max) {
	}
}
