package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;
import com.fyre.cobblecuisine.random.PRNG;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class ScaleInfluence implements SpawningInfluence {
	private final ActiveMealResolver meals;

	public ScaleInfluence(ServerPlayerEntity player) {
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
				|| pokemonAction.getProps().getScaleModifier() != null) return;
		MealLoadout loadout = meals.resolve(action);
		float strength = loadout.strength(MealLoadout.SIZE_AXIS, "");
		float tinyChance = MealLoadout.seasoningSizeChance(loadout, MealEffectType.TINY);
		float giantChance = MealLoadout.seasoningSizeChance(loadout, MealEffectType.GIANT);
		if ((strength == 0.0f && tinyChance == 0.0f && giantChance == 0.0f) || !(entity instanceof PokemonEntity pokemon)
				|| pokemon.getPokemon().isPlayerOwned() || pokemon.getPokemon().isAlpha() || !meals.isWithinRange(entity.getBlockPos())) return;

		if (strength != 0.0f) {
			resize(pokemon, strength < 0.0f, Math.abs(strength));
		} else if (PRNG.nextFloat() < tinyChance) {
			resize(pokemon, true, 1F);
		} else if (PRNG.nextFloat() < giantChance) {
			resize(pokemon, false, 1F);
		}
	}

	private static void resize(PokemonEntity pokemon, boolean tiny, float extremeChance) {
		float scale = ScaleBiasSelector.selectScale(
				tiny,
				extremeChance,
				PRNG.nextFloat(),
				PRNG.nextFloat(),
				Cobblemon.INSTANCE.getConfig().getPokemonIntrinsicSizeMin(),
				Cobblemon.INSTANCE.getConfig().getPokemonIntrinsicSizeMax()
		);
		pokemon.getPokemon().setScaleModifier(scale);
		pokemon.calculateDimensions();
	}
}
