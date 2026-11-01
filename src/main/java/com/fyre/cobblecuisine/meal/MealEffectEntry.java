package com.fyre.cobblecuisine.meal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public record MealEffectEntry(MealEffectType type, String target, int count) {
	public static final Codec<MealEffectEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MealEffectType.CODEC.fieldOf("type").forGetter(MealEffectEntry::type),
			Codec.STRING.optionalFieldOf("target", "").forGetter(MealEffectEntry::target),
			Codec.intRange(1, 3).fieldOf("count").forGetter(MealEffectEntry::count)
	).apply(instance, MealEffectEntry::new));

	public MealEffectEntry {
		Objects.requireNonNull(type, "type");
		target = Objects.requireNonNull(target, "target");
		if (count < 1 || count > 3) throw new IllegalArgumentException("count must be between 1 and 3");
	}
}
