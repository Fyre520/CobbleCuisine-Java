package com.fyre.cobblecuisine.network;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.meal.ActiveMealState;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;
import com.fyre.cobblecuisine.meal.ProgressiveMealPotency;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class CatchBoostSync {
	private CatchBoostSync() { }

	public static void register() {
		PayloadTypeRegistry.playS2C().register(CatchBoostPayload.ID, CatchBoostPayload.CODEC);
		S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
			if (channels.contains(CatchBoostPayload.ID.id())) server.execute(() -> {
				if (server.getPlayerManager().getPlayer(handler.player.getUuid()) == handler.player) {
					ActiveMealState.getActiveWith(handler.player, MealEffectType.CATCH.axis()).ifPresent(loadout -> send(handler.player, loadout));
				}
			});
		});
	}

	public static void send(ServerPlayerEntity player, MealLoadout loadout) {
		if (loadout.strength(MealEffectType.CATCH.axis(), "") <= 0F) return;
		if (!ServerPlayNetworking.canSend(player, CatchBoostPayload.ID)) return;
		float multiplier = ProgressiveMealPotency.catchMultiplier(loadout,
				MealLoadout.Scale.of(CobbleCuisineConfig.data, MealEffectType.CATCH));
		ServerPlayNetworking.send(player, new CatchBoostPayload(multiplier));
	}
}
