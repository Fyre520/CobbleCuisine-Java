package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EggGroupInfluence implements SpawningInfluence {
	private final ActiveMealResolver meals;

	public EggGroupInfluence(ServerPlayerEntity player) {
		this.meals = new ActiveMealResolver(player);
	}

	@Override
	public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position, float weight) {
		List<MealLoadout> loadouts = meals.meals(position);
		if (!meals.isWithinRange(position.getPosition()) || !SpawnDetailForms.isPokemon(detail)) return weight;

		float multiplier = TargetedWeight.multiplier(detail, loadouts, MealEffectType.EGG_GROUP.axis(),
				(form, target) -> matches(form.getEggGroups(), target));
		return multiplier < 0.0f ? weight : SpawnWeightMath.multiply(weight, multiplier);
	}

	private static boolean matches(Iterable<EggGroup> groups, String target) {
		for (EggGroup group : groups) {
			if (target.equals("water23") && (group == EggGroup.WATER_2 || group == EggGroup.WATER_3)) return true;
			if (group.name().replace("_", "").equalsIgnoreCase(target)) return true;
		}
		return false;
	}
}
