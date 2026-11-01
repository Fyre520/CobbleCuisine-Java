package com.fyre.cobblecuisine.inventory;

import com.fyre.cobblecuisine.CobbleCuisine;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class CobbleCuisineScreenHandlers {

	public static final ScreenHandlerType<MixerMenu> MIXER_MENU_TYPE = Registry.register(
			Registries.SCREEN_HANDLER,
			Identifier.of("cobblecuisine", "mixer"),
			new ScreenHandlerType<>(MixerMenu::new, FeatureFlags.VANILLA_FEATURES)
	);

	public static void register() {
	}
}