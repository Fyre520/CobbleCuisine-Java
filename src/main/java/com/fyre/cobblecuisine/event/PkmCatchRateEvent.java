package com.fyre.cobblecuisine.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;

import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;

import kotlin.Unit;

import net.minecraft.server.network.ServerPlayerEntity;

public class PkmCatchRateEvent {
	public static void register() {
		CobblemonEvents.POKEMON_CATCH_RATE.subscribe(Priority.NORMAL, event -> {
			handle(event);
			return Unit.INSTANCE;
		});
	}

	private static void handle(PokemonCatchRateEvent event) {
		if (!(event.getThrower() instanceof ServerPlayerEntity player) || !player.hasStatusEffect(CobbleCuisineEffects.CATCH_BOOST.entry)) return;
		event.setCatchRate(event.getCatchRate() * CobbleCuisineConfig.data.boostSettings.catchRateMultiplier);
	}
}
