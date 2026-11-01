package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;
import com.fyre.cobblecuisine.meal.MealPotency;
import com.fyre.cobblecuisine.meal.ProgressiveMealPotency;
import com.fyre.cobblecuisine.random.PRNG;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class StatInfluence implements SpawningInfluence {
	private static final Stat[] ALL_STATS = {
			Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
	};
	private final ActiveMealResolver meals;

	public StatInfluence(ServerPlayerEntity player) {
		this.meals = new ActiveMealResolver(player);
	}

	@Override
	public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position) {
		meals.resolve(position);
		return true;
	}

	public static void beforeSpawn(PokemonSpawnAction action, PokemonEntity pokemon) {
		for (SpawningInfluence influence : action.getSpawnablePosition().getInfluences()) {
			if (influence instanceof StatInfluence mealInfluence) mealInfluence.apply(action, pokemon);
		}
	}

	public static boolean isMealSpawn(PokemonSpawnAction action) {
		for (SpawningInfluence influence : action.getSpawnablePosition().getInfluences()) {
			if (influence instanceof StatInfluence) return true;
		}
		return false;
	}

	public static void restoreExplicitIvs(PokemonSpawnAction action, PokemonEntity pokemon) {
		if (!isMealSpawn(action) || pokemon.getPokemon().isPlayerOwned() || action.getProps().getIvs() == null) return;
		for (Stat stat : ALL_STATS) {
			Integer explicit = action.getProps().getIvs().get(stat);
			if (explicit != null) pokemon.getPokemon().setIV(stat, explicit);
		}
	}

	private void apply(PokemonSpawnAction action, PokemonEntity pokemon) {
		MealLoadout loadout = meals.resolveWith(action, "iv");
		if (loadout.strength("iv", "") <= 0.0f
				|| pokemon.getPokemon().isPlayerOwned() || !meals.isWithinRange(action.getSpawnablePosition().getPosition())) return;

		ProgressiveMealPotency.IntRange range = ProgressiveMealPotency.ivRange(
				MealLoadout.Scale.of(CobbleCuisineConfig.data, MealEffectType.IV),
				MealLoadout.Scale.ivMaximum(CobbleCuisineConfig.data),
				MealLoadout.Scale.ivCap(CobbleCuisineConfig.data),
				loadout.potency().count(MealPotency.GOLD_NUGGETS)
		);
		int min = range.min();
		int maxExclusive = range.max() + 1;
		for (Stat stat : ALL_STATS) {
			if (action.getProps().getIvs() != null && action.getProps().getIvs().get(stat) != null) continue;
			pokemon.getPokemon().setIV(stat, PRNG.nextInt(min, maxExclusive));
		}
	}
}
