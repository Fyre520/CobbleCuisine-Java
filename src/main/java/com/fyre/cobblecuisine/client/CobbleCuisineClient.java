package com.fyre.cobblecuisine.client;

import com.cobblemon.mod.common.client.tooltips.TooltipManager;
import com.fyre.cobblecuisine.block.CobbleCuisineBlocks;
import com.fyre.cobblecuisine.client.gui.MixerScreen;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.config.TooltipSettings;
import com.fyre.cobblecuisine.inventory.CobbleCuisineScreenHandlers;
import com.fyre.cobblecuisine.network.CatchBoostPayload;
import com.fyre.cobblecuisine.network.TooltipSettingsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;

public class CobbleCuisineClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		TooltipManager.INSTANCE.registerTooltipGenerator(new MealPowerTooltipGenerator());
		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			TooltipSettings.beginConnection(handler);
			ClientPlayNetworking.registerReceiver(TooltipSettingsPayload.ID, (payload, context) ->
					TooltipSettings.receive(handler, payload.snapshot()));
			ClientPlayNetworking.registerReceiver(CatchBoostPayload.ID, (payload, context) ->
					CobbleCuisineConfig.writeCatchRateMultiplier(payload.multiplier()));
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> TooltipSettings.endConnection(handler));
		BlockRenderLayerMap.INSTANCE.putBlock(CobbleCuisineBlocks.BEAN_CROP_BLOCK, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CobbleCuisineBlocks.MIXER_RED, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CobbleCuisineBlocks.MIXER_BLUE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CobbleCuisineBlocks.MIXER_GREEN, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CobbleCuisineBlocks.MIXER_YELLOW, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CobbleCuisineBlocks.MIXER_PINK, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CobbleCuisineBlocks.MIXER_WHITE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CobbleCuisineBlocks.MIXER_BLACK, RenderLayer.getCutout());

		HandledScreens.register(CobbleCuisineScreenHandlers.MIXER_MENU_TYPE, MixerScreen::new);

		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
					if (view != null && view.getBlockEntityRenderData(pos) instanceof Integer color) {
						return color;
					}
					return 0xFFFFFFFF;
				},
				CobbleCuisineBlocks.MIXER_RED,
				CobbleCuisineBlocks.MIXER_BLUE,
				CobbleCuisineBlocks.MIXER_GREEN,
				CobbleCuisineBlocks.MIXER_YELLOW,
				CobbleCuisineBlocks.MIXER_PINK,
				CobbleCuisineBlocks.MIXER_WHITE,
				CobbleCuisineBlocks.MIXER_BLACK
		);
	}
}
