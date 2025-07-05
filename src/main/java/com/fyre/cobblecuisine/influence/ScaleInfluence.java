package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.api.spawning.context.SpawningContext;
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;
import static com.fyre.cobblecuisine.CobbleCuisine.DEBUG;

public class ScaleInfluence implements SpawningInfluence {
	private final ServerPlayerEntity player;

	public ScaleInfluence(ServerPlayerEntity player) {
		this.player = player;
	}

	private static final float MIN_CHANCE = CobbleCuisineConfig.data.boostSettings.scaleMinChance;
	private static final float MAX_CHANCE = CobbleCuisineConfig.data.boostSettings.scaleMaxChance;

	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	@Override
	public void affectSpawn(@NotNull Entity entity) {
		if (!(player.hasStatusEffect(CobbleCuisineEffects.TINY.entry)) && !(player.hasStatusEffect(CobbleCuisineEffects.GIANT.entry))) return;
		if (!(entity instanceof PokemonEntity pokemonEntity) || pokemonEntity.getPokemon().isPlayerOwned()) return;

		if (player.getBlockPos().getSquaredDistance(entity.getBlockPos()) > EFFECT_DISTANCE) return;

		double random = CobbleCuisine.PRNG.nextDouble();
		if (player.hasStatusEffect(CobbleCuisineEffects.TINY.entry)) {
			if (random < MIN_CHANCE) {
				pokemonEntity.getPokemon().setScaleModifier(0.8f);
			} else if (random < MAX_CHANCE) {
				pokemonEntity.getPokemon().setScaleModifier(0.6f);
			} else {
				pokemonEntity.getPokemon().setScaleModifier(0.5f);
			}
		} else {
			if (random < MIN_CHANCE) {
				pokemonEntity.getPokemon().setScaleModifier(1.2f);
			} else if (random < MAX_CHANCE) {
				pokemonEntity.getPokemon().setScaleModifier(1.3f);
			} else {
				pokemonEntity.getPokemon().setScaleModifier(1.5f);
			}
		}

		if (DEBUG) LOGGER.info("CobbleCuisine >> SCALE INFLUENCE >> PLAYER: {} PKM: {} SCALE MODIFIER: {}", player.getName(), pokemonEntity.getName(), pokemonEntity.getPokemon().getScaleModifier());

		pokemonEntity.calculateDimensions();
	}

	@Override public boolean isExpired() { return false; }
	@Override public void affectAction(@NotNull SpawnAction<?> action) { }
	@Override public float affectBucketWeight(@NotNull SpawnBucket bucket, float weight) { return weight; }
	@Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawningContextCalculator<?, ?> contextCalculator) { return true; }
	@Override public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawningContext ctx) { return true; }
	@Override public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawningContext ctx, float weight) { return weight; }
}
