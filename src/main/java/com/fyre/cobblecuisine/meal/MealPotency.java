package com.fyre.cobblecuisine.meal;

import com.mojang.serialization.Codec;

import java.util.Map;
import java.util.Objects;

public record MealPotency(Map<String, Integer> values) {
	public static final String IV_SWEETS = "iv_sweets";
	public static final String GOLD_NUGGETS = "gold_nuggets";
	public static final String EXP_POINTS = "exp_points";
	public static final String FRIENDSHIP_SUGAR = "friendship_sugar";
	public static final String HEALING_FRUIT = "healing_fruit";
	private static final String GEM_PREFIX = "gem/";
	public static final MealPotency EMPTY = new MealPotency(Map.of());
	public static final Codec<MealPotency> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT)
			.xmap(MealPotency::new, MealPotency::values);

	public MealPotency {
		Objects.requireNonNull(values, "values");
		if (values.entrySet().stream().anyMatch(entry -> entry.getKey().isBlank() || entry.getValue() < 0)) {
			throw new IllegalArgumentException("Meal potency keys must be non-blank and values non-negative");
		}
		values = Map.copyOf(values);
	}

	public int count(String key) {
		return values.getOrDefault(key, 0);
	}

	public static String gemKey(String type) {
		return GEM_PREFIX + type;
	}
}
