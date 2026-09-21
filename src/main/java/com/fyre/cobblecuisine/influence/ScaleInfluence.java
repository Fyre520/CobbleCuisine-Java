package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import com.fyre.cobblecuisine.random.PRNG;
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

	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	@Override
	public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
		if (!(action instanceof PokemonSpawnAction pokemonAction) || pokemonAction.getProps().getScaleModifier() != null) return;
		if (!(player.hasStatusEffect(CobbleCuisineEffects.TINY.entry)) && !(player.hasStatusEffect(CobbleCuisineEffects.GIANT.entry))) return;
		if (!(entity instanceof PokemonEntity pokemonEntity) || pokemonEntity.getPokemon().isPlayerOwned() || pokemonEntity.getPokemon().isAlpha()) return;

		if (player.getBlockPos().getSquaredDistance(entity.getBlockPos()) > EFFECT_DISTANCE) return;

		boolean tiny = player.hasStatusEffect(CobbleCuisineEffects.TINY.entry);
		float scale = ScaleBiasSelector.selectScale(
				tiny,
				CobbleCuisineConfig.data.boostSettings,
				PRNG.nextFloat(),
				PRNG.nextFloat(),
				Cobblemon.INSTANCE.getConfig().getPokemonIntrinsicSizeMin(),
				Cobblemon.INSTANCE.getConfig().getPokemonIntrinsicSizeMax()
		);
		pokemonEntity.getPokemon().setScaleModifier(scale);

		if (DEBUG) LOGGER.info("CobbleCuisine >> SCALE INFLUENCE >> PLAYER: {} PKM: {} SCALE MODIFIER: {}", player.getName(), pokemonEntity.getName(), pokemonEntity.getPokemon().getScaleModifier());

		pokemonEntity.calculateDimensions();
	}

	@Override public boolean isExpired() { return false; }
	@Override public void affectAction(@NotNull SpawnAction<?> action) { }
	@Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawnablePositionCalculator<?, ?> contextCalculator) { return true; }
	@Override public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition ctx) { return true; }
	@Override public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition ctx, float weight) { return weight; }
}
