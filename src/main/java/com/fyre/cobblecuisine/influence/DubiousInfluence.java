package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealLoadout;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class DubiousInfluence implements SpawningInfluence {
	private final ActiveMealResolver meals;

	public DubiousInfluence(ServerPlayerEntity player) {
		this.meals = new ActiveMealResolver(player);
	}

	@Override
	public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position) {
		MealLoadout loadout = meals.resolve(position);
		if (loadout.strength("dubious", "") <= 0.0f || !meals.isWithinRange(position.getPosition())) return true;
		return !SpawnDetailForms.isPokemon(detail);
	}
}
