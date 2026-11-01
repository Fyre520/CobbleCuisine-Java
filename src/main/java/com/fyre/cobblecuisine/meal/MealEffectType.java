package com.fyre.cobblecuisine.meal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;

public enum MealEffectType {
	TYPE("type", 1),
	EGG_GROUP("egg_group", 1),
	TERA("tera", 1),
	NATURE("nature", 1),
	TINY("scale", -1),
	GIANT("scale", 1),
	YIELD("yield", 1),
	IV("iv", 1),
	SHINY("shiny", 1),
	CATCH("catch", 1),
	EXP("exp", 1),
	DUBIOUS("dubious", 1),
	HIDDEN_ABILITY("hidden_ability", 1),
	HERD_FREQUENCY_INCREASE("herd_frequency", 1),
	HERD_FREQUENCY_DECREASE("herd_frequency", -1),
	HERD_LEVEL_INCREASE("herd_level", 1),
	HERD_LEVEL_DECREASE("herd_level", -1),
	HERD_VARIATION_INCREASE("herd_variation", 1),
	HERD_VARIATION_DECREASE("herd_variation", -1),
	ALPHA_FREQUENCY_INCREASE("alpha_frequency", 1),
	ALPHA_FREQUENCY_DECREASE("alpha_frequency", -1),
	DURATION("duration", 1);

	public static final Codec<MealEffectType> CODEC = Codec.STRING.comapFlatMap(
			id -> Arrays.stream(values())
					.filter(type -> type.id().equals(id))
					.findFirst()
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error(() -> "Unknown meal effect type: " + id)),
			MealEffectType::id
	);

	private final String axis;
	private final int direction;

	MealEffectType(String axis, int direction) {
		this.axis = axis;
		this.direction = direction;
	}

	public String id() {
		return name().toLowerCase();
	}

	public boolean active() {
		return switch (this) {
			case HERD_LEVEL_INCREASE, HERD_LEVEL_DECREASE, HERD_VARIATION_INCREASE, HERD_VARIATION_DECREASE -> false;
			default -> true;
		};
	}

	public String axis() {
		return axis;
	}

	public int direction() {
		return direction;
	}
}
