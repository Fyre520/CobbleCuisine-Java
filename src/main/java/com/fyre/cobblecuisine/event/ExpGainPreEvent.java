package com.fyre.cobblecuisine.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedEvent;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;
import com.fyre.cobblecuisine.meal.MealPotency;
import com.fyre.cobblecuisine.meal.ProgressiveMealPotency;

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
		if (player == null) return;
		MealLoadout loadout = ActiveMealResolver.currentWith(player, MealEffectType.EXP.axis());
		float strength = loadout.strength("exp", "");
		if (strength <= 0.0f) return;

		int originalExp = event.getExperience();
		float multiplier = ProgressiveMealPotency.expMultiplier(
				MealLoadout.Scale.of(CobbleCuisineConfig.data, MealEffectType.EXP),
				loadout.potency().count(MealPotency.GOLD_NUGGETS)
		);
		int exp = ActiveMealResolver.multiplyAndRound(originalExp, multiplier);
		event.setExperience(exp);
		player.sendMessage(Text.translatable("message.cobblecuisine.expboost", event.getPokemon().getDisplayName(true), exp - originalExp));
	}
}
