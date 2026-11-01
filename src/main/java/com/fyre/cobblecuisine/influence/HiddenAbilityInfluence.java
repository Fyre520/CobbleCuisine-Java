package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealLoadout;
import com.fyre.cobblecuisine.random.PRNG;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class HiddenAbilityInfluence implements SpawningInfluence {
	private final ActiveMealResolver meals;

	public HiddenAbilityInfluence(ServerPlayerEntity player) {
		this.meals = new ActiveMealResolver(player);
	}

	@Override
	public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position) {
		meals.resolve(position);
		return true;
	}

	@Override
	public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
		MealLoadout loadout = meals.resolve(action);
		float strength = loadout.strength("hidden_ability", "");
		if (strength <= 0.0f || !(entity instanceof PokemonEntity pokemon) || pokemon.getPokemon().isPlayerOwned()
				|| !meals.isWithinRange(entity.getBlockPos())
				|| PRNG.nextDouble() >= Math.min(1.0f, strength)) return;

		var hiddenAbility = pokemon.getPokemon().getForm().getAbilities().getMapping().values().stream()
				.flatMap(Collection::stream)
				.filter(pool -> pool.getType() == HiddenAbilityType.INSTANCE)
				.findFirst()
				.orElse(null);
		if (hiddenAbility != null) {
			pokemon.getPokemon().updateAbility(hiddenAbility.getTemplate().create(false, hiddenAbility.getPriority()));
		}
	}
}
