package com.fyre.cobblecuisine.component;

import com.fyre.cobblecuisine.meal.MealEffectEntry;
import com.fyre.cobblecuisine.meal.MealPotency;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record MealEffectsComponent(Optional<MealEffectEntry> primary, List<MealEffectEntry> seasonings, int seasoningSlots, MealPotency potency) {
	public static final MealEffectsComponent EMPTY = new MealEffectsComponent(Optional.empty(), List.of(), 0, MealPotency.EMPTY);
	public static final Codec<MealEffectsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MealEffectEntry.CODEC.optionalFieldOf("primary").forGetter(MealEffectsComponent::primary),
			MealEffectEntry.CODEC.listOf().optionalFieldOf("seasonings", List.of()).forGetter(MealEffectsComponent::seasonings),
			Codec.intRange(0, 3).optionalFieldOf("seasoning_slots", 0).forGetter(MealEffectsComponent::seasoningSlots),
			MealPotency.CODEC.optionalFieldOf("potency", MealPotency.EMPTY).forGetter(MealEffectsComponent::potency)
	).apply(instance, MealEffectsComponent::new));
	public static final PacketCodec<RegistryByteBuf, MealEffectsComponent> PACKET_CODEC = PacketCodecs.codec(CODEC).cast();

	public MealEffectsComponent {
		primary = Objects.requireNonNull(primary, "primary").filter(entry -> entry.type().active());
		seasonings = seasonings.stream().filter(entry -> entry.type().active()).toList();
		potency = Objects.requireNonNull(potency, "potency");
		if (seasoningSlots < 0 || seasoningSlots > 3) {
			throw new IllegalArgumentException("A meal can contain at most three seasoning slots");
		}
		if (seasoningSlots == 0 && !seasonings.isEmpty()) {
			throw new IllegalArgumentException("Seasoning effects require at least one occupied slot");
		}
		if (seasonings.stream().anyMatch(entry -> entry.count() > seasoningSlots)) {
			throw new IllegalArgumentException("An effect count cannot exceed the occupied seasoning slots");
		}
	}

	public MealEffectsComponent(Optional<MealEffectEntry> primary, List<MealEffectEntry> seasonings, int seasoningSlots) {
		this(primary, seasonings, seasoningSlots, MealPotency.EMPTY);
	}
}
