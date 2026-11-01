package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.detail.PokemonHerdSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.fyre.cobblecuisine.CobbleCuisine.DEBUG;
import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public final class HerdInfluence implements SpawningInfluence {
	private final ActiveMealResolver meals;
	private final ServerPlayerEntity player;

	public HerdInfluence(ServerPlayerEntity player) {
		this.meals = new ActiveMealResolver(player);
		this.player = player;
	}

	@Override
	public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position, float weight) {
		if (!meals.isWithinRange(position.getPosition()) || !SpawnDetailForms.isPokemon(detail)) return weight;

		float herds = 1F;
		float cut = 1F;
		boolean active = false;
		for (MealLoadout loadout : meals.meals(position)) {
			float mealHerds = Math.abs(loadout.strength(MealEffectType.HERD_FREQUENCY_INCREASE.axis(), ""));
			if (mealHerds == 0F) continue;
			active = true;
			herds *= mealHerds;
			float mealCut = loadout.strength(MealLoadout.cutAxis(MealEffectType.HERD_FREQUENCY_INCREASE.axis()), "");
			if (mealCut > 0F) cut *= mealCut;
		}
		if (!active) return weight;

		if (detail instanceof PokemonHerdSpawnDetail herd) {
			if (HerdClassifier.classify(herd) != HerdClassifier.Kind.ORDINARY) return weight;

			float result = SpawnWeightMath.multiply(weight, herds);
			if (DEBUG) LOGGER.info("CobbleCuisine >> HERD INFLUENCE >> PLAYER: {} KIND: ORDINARY ADJUSTMENT: {} OLD WEIGHT: {} NEW WEIGHT: {}",
					player.getName().getString(), herds, weight, result);
			return result;
		}

		return cut != 1F ? SpawnWeightMath.multiply(weight, cut) : weight;
	}

	@Override
	public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
		if (!(entity instanceof PokemonEntity pokemonEntity)) return;
		if (!meals.isWithinRange(entity.getBlockPos())) return;

		if (action.getDetail() instanceof PokemonHerdSpawnDetail herd && !isLeaderSpawn(herd, action)) return;

		float chance = meals.resolve(action).strength(MealLoadout.ALPHA_CHANCE_AXIS, "");
		if (chance <= 0F) return;
		if (pokemonEntity.getRandom().nextFloat() >= chance) return;

		FishingSpawnCause.Companion.alterAlphaAttempt(pokemonEntity);
		if (DEBUG) LOGGER.info("CobbleCuisine >> HERD INFLUENCE >> PLAYER: {} ALPHA CHANCE: {} CONVERTED A SPAWN INTO AN ALPHA",
				player.getName().getString(), chance);
	}

	private static boolean isLeaderSpawn(PokemonHerdSpawnDetail herd, SpawnAction<?> action) {
		if (!(action instanceof PokemonSpawnAction pokemonAction)) return true;

		List<PokemonHerdSpawnDetail.Herdable> matches = herd.getHerdablePokemon().stream()
				.filter(herdable -> herdable.getPokemon().equals(pokemonAction.getProps()))
				.toList();

		return matches.isEmpty() || matches.stream().anyMatch(PokemonHerdSpawnDetail.Herdable::isLeader);
	}

}
