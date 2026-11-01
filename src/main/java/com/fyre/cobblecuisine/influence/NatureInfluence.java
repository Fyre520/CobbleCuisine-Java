package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Nature;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealLoadout;
import com.fyre.cobblecuisine.meal.NatureTargetSelector;
import com.fyre.cobblecuisine.random.PRNG;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class NatureInfluence implements SpawningInfluence {
	private final ActiveMealResolver meals;

	public NatureInfluence(ServerPlayerEntity player) {
		this.meals = new ActiveMealResolver(player);
	}

	@Override
	public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position) {
		meals.resolve(position);
		return true;
	}

	@Override
	public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
		if (!(action instanceof PokemonSpawnAction pokemonAction)
				|| pokemonAction.getProps().getNature() != null) return;
		MealLoadout loadout = meals.resolve(action);
		if (!(entity instanceof PokemonEntity pokemon) || pokemon.getPokemon().isPlayerOwned() || !meals.isWithinRange(entity.getBlockPos())) return;

		float strength = ActiveMealResolver.totalStrength(loadout, "nature");
		if (strength <= 0.0f || PRNG.nextDouble() >= Math.min(1.0f, strength)) return;
		String selected = ActiveMealResolver.chooseTarget(loadout, "nature", PRNG.nextFloat());
		String target = selected == null ? null : NatureTargetSelector.select(selected, PRNG.nextFloat());
		Nature nature = target == null ? null : Natures.getNature(target);
		if (nature != null) pokemon.getPokemon().setNature(nature);
	}
}
