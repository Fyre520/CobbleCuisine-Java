package com.fyre.cobblecuisine.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import kotlin.Unit;

import net.minecraft.entity.Entity;

public class ShinySpawnEvent {
	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	public static void register() {
		CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe(Priority.NORMAL, event -> {
			handle(event);
			return Unit.INSTANCE;
		});
	}

	private static void handle(ShinyChanceCalculationEvent event) {
		event.addModificationFunction((currentRate, player, pokemon) -> {
			if (player == null || !player.hasStatusEffect(CobbleCuisineEffects.SHINY.entry)) return currentRate;

			Entity pokemonEntity = pokemon.getEntity();
			if (pokemonEntity != null && player.squaredDistanceTo(pokemonEntity) <= EFFECT_DISTANCE) {
				return currentRate / CobbleCuisineConfig.data.boostSettings.shinyBoostMultiplier;
			}
			return currentRate;
		});
	}
}
