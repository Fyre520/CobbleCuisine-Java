package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.pokemon.FormData;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TypeInfluence implements SpawningInfluence {
	private final ActiveMealResolver meals;

	public TypeInfluence(ServerPlayerEntity player) {
		this.meals = new ActiveMealResolver(player);
	}

	@Override
	public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position, float weight) {
		List<MealLoadout> loadouts = meals.meals(position);
		if (!meals.isWithinRange(position.getPosition()) || !SpawnDetailForms.isPokemon(detail)) return weight;

		float multiplier = TargetedWeight.multiplier(detail, loadouts, MealEffectType.TYPE.axis(), TypeInfluence::hasType);
		return multiplier < 0.0f ? weight : SpawnWeightMath.multiply(weight, multiplier);
	}

	private static boolean hasType(FormData form, String target) {
		ElementalType type = ElementalTypes.INSTANCE.get(target);
		return type != null && (form.getPrimaryType() == type || form.getSecondaryType() == type);
	}
}
