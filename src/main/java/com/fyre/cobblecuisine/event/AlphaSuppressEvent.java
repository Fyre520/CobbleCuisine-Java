package com.fyre.cobblecuisine.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.entity.SpawnEvent;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealLoadout;
import com.fyre.cobblecuisine.random.PRNG;
import kotlin.Unit;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.fyre.cobblecuisine.CobbleCuisine.DEBUG;
import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public final class AlphaSuppressEvent {
	private AlphaSuppressEvent() {
	}

	public static void register() {
		CobblemonEvents.ENTITY_SPAWN.subscribe(Priority.NORMAL, event -> {
			handle(event);
			return Unit.INSTANCE;
		});
	}

	private static void handle(SpawnEvent<?> event) {
		if (!(event.getEntity() instanceof PokemonEntity pokemon) || !pokemon.getPokemon().isAlpha()
				|| !(event.getCause().getEntity() instanceof ServerPlayerEntity player)) return;
		double radius = CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks;
		if (player.getBlockPos().getSquaredDistance(event.getSpawnablePosition().getPosition()) > radius * radius) return;

		float chance = ActiveMealResolver.current(player).strength(MealLoadout.ALPHA_SUPPRESS_AXIS, "");
		if (chance <= 0F || PRNG.nextDouble() >= chance) return;
		event.cancel();
		if (DEBUG) LOGGER.info("CobbleCuisine >> ALPHA SUPPRESS >> PLAYER: {} CHANCE: {} BLOCKED ALPHA: {}",
				player.getName().getString(), chance, pokemon.getPokemon().getSpecies().getName());
	}
}
