package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import com.fyre.cobblecuisine.random.PRNG;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;
import static com.fyre.cobblecuisine.CobbleCuisine.DEBUG;

public class ScaleInfluence implements SpawningInfluence {
	private final ServerPlayerEntity player;

	public ScaleInfluence(ServerPlayerEntity player) {
		this.player = player;
	}

	private static final float MIN_CHANCE = CobbleCuisineConfig.data.boostSettings.scaleMinValue;
	private static final float MAX_CHANCE = CobbleCuisineConfig.data.boostSettings.scaleMaxValue;

	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	@Override
	public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
		if (!(player.hasStatusEffect(CobbleCuisineEffects.TINY.entry)) && !(player.hasStatusEffect(CobbleCuisineEffects.GIANT.entry))) return;
		if (!(entity instanceof PokemonEntity pokemonEntity) || pokemonEntity.getPokemon().isPlayerOwned()) return;

		if (player.getBlockPos().getSquaredDistance(entity.getBlockPos()) > EFFECT_DISTANCE) return;

		float random = PRNG.nextFloat();
		if (player.hasStatusEffect(CobbleCuisineEffects.TINY.entry)) {
			random = random * (1.0f - MIN_CHANCE) + MIN_CHANCE;
		} else {
			random = random * (MAX_CHANCE - 1.0f) + 1.0f;
		}
		pokemonEntity.getPokemon().setScaleModifier(random);

		if (DEBUG) LOGGER.info("CobbleCuisine >> SCALE INFLUENCE >> PLAYER: {} PKM: {} SCALE MODIFIER: {}", player.getName(), pokemonEntity.getName(), pokemonEntity.getPokemon().getScaleModifier());

		pokemonEntity.calculateDimensions();
	}

    @Override public boolean isExpired() { return false; }
    @Override public void affectAction(@NotNull SpawnAction<?> action) { }
    @Override public void affectBucketWeights(@NotNull Map<SpawnBucket, Float> bucketWeights) { }
    @Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawnablePositionCalculator<?, ?> spawnablePositionCalculator) { return true; }
    @Override public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition) { return true; }
    @Override public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition, float weight) { return weight; }
}
