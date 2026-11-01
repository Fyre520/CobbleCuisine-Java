package com.fyre.cobblecuisine.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;
import com.fyre.cobblecuisine.meal.ProgressiveMealPotency;

import kotlin.Unit;

import net.minecraft.server.network.ServerPlayerEntity;

import static com.fyre.cobblecuisine.CobbleCuisine.DEBUG;
import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public class PkmCatchRateEvent {
	public static void register() {
		CobblemonEvents.POKEMON_CATCH_RATE.subscribe(Priority.NORMAL, event -> {
			handle(event);
			return Unit.INSTANCE;
		});
	}

	private static void handle(PokemonCatchRateEvent event) {
		if (!(event.getThrower() instanceof ServerPlayerEntity player)) return;
		MealLoadout loadout = ActiveMealResolver.currentWith(player, MealEffectType.CATCH.axis());
		float strength = loadout.strength("catch", "");
		if (strength <= 0.0f) return;
		float multiplier = ProgressiveMealPotency.catchMultiplier(loadout,
				MealLoadout.Scale.of(CobbleCuisineConfig.data, MealEffectType.CATCH));
		float original = event.getCatchRate();
		float adjusted = original * multiplier;
		if (DEBUG) LOGGER.info("CobbleCuisine >> CATCH POWER >> PLAYER: {} STRENGTH: {} MULTIPLIER: {} ORIGINAL CATCH RATE: {} ADJUSTED CATCH RATE: {}",
				player.getName().getString(), strength, multiplier, original, adjusted);
		event.setCatchRate(adjusted);
	}
}
