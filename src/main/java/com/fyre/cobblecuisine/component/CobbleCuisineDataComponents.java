package com.fyre.cobblecuisine.component;

import com.fyre.cobblecuisine.CobbleCuisine;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class CobbleCuisineDataComponents {
	public static final ComponentType<MealEffectsComponent> MEAL_EFFECTS = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			Identifier.of(CobbleCuisine.MOD_ID, "meal_effects"),
			ComponentType.<MealEffectsComponent>builder()
					.codec(MealEffectsComponent.CODEC)
					.packetCodec(MealEffectsComponent.PACKET_CODEC)
					.build()
	);

	private CobbleCuisineDataComponents() {
	}

	public static void register() {
	}
}
