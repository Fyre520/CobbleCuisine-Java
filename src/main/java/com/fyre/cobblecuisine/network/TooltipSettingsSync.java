package com.fyre.cobblecuisine.network;

import com.fyre.cobblecuisine.config.TooltipSettings;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TooltipSettingsSync {
	private static volatile MinecraftServer activeServer;

	private TooltipSettingsSync() { }

	public static void register() {
		PayloadTypeRegistry.playS2C().register(TooltipSettingsPayload.ID, TooltipSettingsPayload.CODEC);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> activeServer = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			if (activeServer == server) activeServer = null;
		});
		S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
			if (channels.contains(TooltipSettingsPayload.ID.id())) server.execute(() -> {
				if (server.getPlayerManager().getPlayer(handler.player.getUuid()) == handler.player) {
					send(handler.player, new TooltipSettingsPayload(TooltipSettings.capture()));
				}
			});
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				send(handler.player, new TooltipSettingsPayload(TooltipSettings.capture())));
	}

	public static void configPublished() {
		MinecraftServer server = activeServer;
		if (server == null) return;
		server.execute(() -> {
			if (activeServer != server) return;
			TooltipSettingsPayload payload = new TooltipSettingsPayload(TooltipSettings.capture());
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) send(player, payload);
		});
	}

	private static void send(ServerPlayerEntity player, TooltipSettingsPayload payload) {
		if (ServerPlayNetworking.canSend(player, TooltipSettingsPayload.ID)) ServerPlayNetworking.send(player, payload);
	}
}
