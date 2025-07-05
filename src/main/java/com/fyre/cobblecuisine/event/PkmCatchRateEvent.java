package com.fyre.cobblecuisine.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;

import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;

import kotlin.Unit;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class PkmCatchRateEvent {
	public static void register() {
		CobblemonEvents.POKEMON_CATCH_RATE.subscribe(Priority.NORMAL, event -> {
			handle(event);
			return Unit.INSTANCE;
		});
	}

	private static void handle(PokemonCatchRateEvent event) {
		if (!(event.getThrower() instanceof ServerPlayerEntity player) || !player.hasStatusEffect(CobbleCuisineEffects.CATCH_BOOST.entry)) return;
		float oldCatchRate = event.getCatchRate();
		event.setCatchRate(event.getCatchRate() * CobbleCuisineConfig.data.boostSettings.catchRateMultiplier);

		player.sendMessage(Text.literal("Catch rate boosted!"), false);
		System.out.println("Catch rate boosted from " + oldCatchRate + " to " + event.getCatchRate() + " for player " + player.getName().getString());


	}
}