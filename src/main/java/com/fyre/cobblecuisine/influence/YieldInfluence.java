package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.pokemon.FormData;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealLoadout;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class YieldInfluence implements SpawningInfluence {
	private final ActiveMealResolver meals;

	public YieldInfluence(ServerPlayerEntity player) {
		this.meals = new ActiveMealResolver(player);
	}

	@Override
	public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position) {
		MealLoadout loadout = meals.resolve(position);
		var effects = ActiveMealResolver.axisEffects(loadout, "yield");
		if (effects.isEmpty() || !meals.isWithinRange(position.getPosition()) || !SpawnDetailForms.isPokemon(detail)) return true;

		return SpawnDetailForms.resolve(detail).stream().anyMatch(form -> effects.stream().anyMatch(effect -> {
			Stat stat = stat(effect.getKey().target());
			Integer yield = form.getEvYield().get(stat);
			return yield != null && yield > 0;
		}));
	}

	@Override
	public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position, float weight) {
		if (!meals.isWithinRange(position.getPosition()) || !SpawnDetailForms.isPokemon(detail)) return weight;
		MealLoadout loadout = meals.resolve(position);
		float bonus = 0.0F;
		for (var effect : ActiveMealResolver.axisEffects(loadout, MealLoadout.YIELD_BONUS_AXIS)) {
			float fraction = SpawnDetailForms.matchingFraction(detail, form -> yields(form, stat(effect.getKey().target())));
			if (fraction > 0.0F) bonus += Math.max(0.0F, effect.getValue() - 1.0F) * fraction;
		}
		return bonus > 0.0F ? SpawnWeightMath.multiply(weight, 1.0F + bonus) : weight;
	}

	private static boolean yields(FormData form, Stat stat) {
		Integer value = form.getEvYield().get(stat);
		return value != null && value > 0;
	}

	private static Stat stat(String target) {
		return switch (target) {
			case "hp" -> Stats.HP;
			case "attack" -> Stats.ATTACK;
			case "defence" -> Stats.DEFENCE;
			case "special_attack" -> Stats.SPECIAL_ATTACK;
			case "special_defence" -> Stats.SPECIAL_DEFENCE;
			case "speed" -> Stats.SPEED;
			default -> throw new IllegalArgumentException("Unknown yield stat: " + target);
		};
	}
}
