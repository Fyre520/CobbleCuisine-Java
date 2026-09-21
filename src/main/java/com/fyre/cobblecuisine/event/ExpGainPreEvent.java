package com.fyre.cobblecuisine.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedEvent;

import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;

import kotlin.Unit;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ExpGainPreEvent {
	public static void register() {
		CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(Priority.NORMAL, event -> {
			handle(event);
			return Unit.INSTANCE;
		});
	}

	private static void handle(ExperienceGainedEvent.Pre event) {
		ServerPlayerEntity player = event.getPokemon().getOwnerPlayer();
		if (player == null || !player.hasStatusEffect(CobbleCuisineEffects.EXP_BOOST.entry)) return;

		int originalExp = event.getExperience();
		// Preserve fractional multipliers and round only the final, bounded EXP amount.
		long scaledExp = Math.round(originalExp * (double) CobbleCuisineConfig.data.boostSettings.expBoostMultiplier);
		int exp = (int) Math.max(0L, Math.min(Integer.MAX_VALUE, scaledExp));
		event.setExperience(exp);
		player.sendMessage(Text.translatable("message.cobblecuisine.expboost", event.getPokemon().getDisplayName(true), exp - originalExp));
	}
}