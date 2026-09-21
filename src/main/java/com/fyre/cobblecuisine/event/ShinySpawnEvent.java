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
			float multiplier = CobbleCuisineConfig.data.boostSettings.shinyBoostMultiplier;
			if (!Float.isFinite(currentRate) || currentRate <= 0.0f || !Float.isFinite(multiplier) || multiplier <= 0.0f) return currentRate;
			return Math.max(Float.MIN_VALUE, Math.min(Float.MAX_VALUE, currentRate / multiplier));
		});
	}
}
