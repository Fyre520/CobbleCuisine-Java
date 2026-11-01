package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.types.tera.TeraType;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealLoadout;
import com.fyre.cobblecuisine.random.PRNG;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class TeraInfluence implements SpawningInfluence {
	private final ActiveMealResolver meals;

	public TeraInfluence(ServerPlayerEntity player) {
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
				|| pokemonAction.getProps().getTeraType() != null) return;
		MealLoadout loadout = meals.resolve(action);
		if (!(entity instanceof PokemonEntity pokemon) || pokemon.getPokemon().isPlayerOwned() || !meals.isWithinRange(entity.getBlockPos())) return;

		float strength = ActiveMealResolver.totalStrength(loadout, "tera");
		if (strength <= 0.0f || PRNG.nextDouble() >= Math.min(1.0f, strength)) return;
		String target = ActiveMealResolver.chooseTarget(loadout, "tera", PRNG.nextFloat());
		TeraType type = target == null ? null : TeraTypes.get(target);
		if (type != null) pokemon.getPokemon().setTeraType(type);
	}
}
