package com.fyre.cobblecuisine.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import kotlin.Unit;

public class ShinySpawnEvent {

	public static void register() {
		CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe(Priority.NORMAL, event -> {
			handle(event);
			return Unit.INSTANCE;
		});
	}

	private static void handle(ShinyChanceCalculationEvent event) {
		event.addModificationFunction((currentRate, player, pokemon) -> {
			if (player == null || !player.hasStatusEffect(CobbleCuisineEffects.SHINY.entry)) return currentRate;
			return currentRate / CobbleCuisineConfig.data.boostSettings.shinyBoostMultiplier;
		});
	}
}
