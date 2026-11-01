package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;
import static com.fyre.cobblecuisine.CobbleCuisine.DEBUG;

public class DubiousInfluence implements SpawningInfluence {

	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	private final ServerPlayerEntity player;
	public DubiousInfluence(ServerPlayerEntity player) { this.player = player; }

	@Override
	public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition ctx) {
		if (!player.hasStatusEffect(CobbleCuisineEffects.DUBIOUS.entry)) return true;
		if (player.getBlockPos().getSquaredDistance(ctx.getPosition()) > EFFECT_DISTANCE) return true;

		if (DEBUG) LOGGER.info("CobbleCuisine >> DUBIOUS INFLUENCE >> PLAYER: {} PKM: {} PREVENTED: {}", player.getName(), detail.getName(), !(detail instanceof PokemonSpawnDetail));

		return !(detail instanceof PokemonSpawnDetail);
	}

	@Override public boolean isExpired() { return false; }
	@Override public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition ctx, float weight) { return weight; }
	@Override public void affectAction(@NotNull SpawnAction<?> action) { }
	@Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawnablePositionCalculator<?, ?> contextCalculator) { return true; }
}
