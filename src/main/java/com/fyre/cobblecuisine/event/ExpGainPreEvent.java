package com.fyre.cobblecuisine.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedPreEvent;

import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;

import kotlin.Unit;

import net.minecraft.server.network.ServerPlayerEntity;

public class ExpGainPreEvent {
	public static void register() {
		CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(Priority.NORMAL, event -> {
			handle(event);
			return Unit.INSTANCE;
		});
	}

	private static void handle(ExperienceGainedPreEvent event) {
		ServerPlayerEntity player = event.getPokemon().getOwnerPlayer();
		if (player == null || !player.hasStatusEffect(CobbleCuisineEffects.EXP_BOOST.entry)) return;

		event.setExperience(event.getExperience() * (int) CobbleCuisineConfig.data.boostSettings.expBoostMultiplier);
	}
}
