package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.event.ShinySpawnEvent;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;
import com.fyre.cobblecuisine.meal.MealPotency;
import com.fyre.cobblecuisine.meal.ProgressiveMealPotency;
import com.fyre.cobblecuisine.random.PRNG;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class ShinyInfluence implements SpawningInfluence {
	private final ActiveMealResolver meals;

	public ShinyInfluence(ServerPlayerEntity player) {
		this.meals = new ActiveMealResolver(player);
	}

	@Override
	public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position) {
		meals.resolve(position);
		return true;
	}

	@Override
	public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
		MealLoadout loadout = meals.resolveWith(action, "shiny");
		float strength = loadout.strength("shiny", "");
		if (strength <= 0.0f || !(action instanceof PokemonSpawnAction pokemonAction)
				|| pokemonAction.getProps().getShiny() != null
				|| !(entity instanceof PokemonEntity pokemon)
				|| pokemon.getPokemon().isPlayerOwned() || pokemon.getPokemon().getShiny()
				|| !meals.isWithinRange(entity.getBlockPos())) return;

		float multiplier = ProgressiveMealPotency.shinyMultiplier(
				MealLoadout.Scale.of(CobbleCuisineConfig.data, MealEffectType.SHINY),
				loadout.potency().count(MealPotency.GOLD_NUGGETS));
		float chance = ShinySpawnEvent.additionalRollChance(Cobblemon.INSTANCE.getConfig().getShinyRate(), multiplier);
		if (PRNG.nextFloat() < chance) pokemon.getPokemon().setShiny(true);
	}
}
