package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
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
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;
import static com.fyre.cobblecuisine.CobbleCuisine.DEBUG;

public class StatInfluence implements SpawningInfluence {

	private static final RegistryEntry<StatusEffect> IV_MODIFY_EFFECT = CobbleCuisineEffects.IV_MODIFY.entry;

	private static final Stat[] ALL_STATS = new Stat[] {
			Stats.HP,
			Stats.ATTACK,
			Stats.DEFENCE,
			Stats.SPECIAL_ATTACK,
			Stats.SPECIAL_DEFENCE,
			Stats.SPEED
	};

	private static final int IV_MIN = Math.max(0, CobbleCuisineConfig.data.boostSettings.ivMinValue);
	private static final int IV_MAX = Math.min(31, Math.max(IV_MIN + 1, CobbleCuisineConfig.data.boostSettings.ivMaxValue)) + 1;

	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	private final ServerPlayerEntity player;
	public StatInfluence(ServerPlayerEntity player) { this.player = player; }

	@Override
	public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
		if (!player.hasStatusEffect(IV_MODIFY_EFFECT)) return;
		if (!(entity instanceof PokemonEntity pokemonEntity) || pokemonEntity.getPokemon().isPlayerOwned()) return;

		if (player.getBlockPos().getSquaredDistance(entity.getBlockPos()) > EFFECT_DISTANCE) return;

		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < ALL_STATS.length; i++) pokemonEntity.getPokemon().setIV(ALL_STATS[i], PRNG.nextInt(IV_MIN, IV_MAX));

		if (DEBUG) LOGGER.info("CobbleCuisine >> STAT INFLUENCE >> PLAYER: {} PKM: {} IVS: {}", player.getName(), pokemonEntity.getName(), pokemonEntity.getPokemon().getIvs());
	}

    @Override public boolean isExpired() { return false; }
    @Override public void affectAction(@NotNull SpawnAction<?> action) { }
    @Override public void affectBucketWeights(@NotNull Map<SpawnBucket, Float> bucketWeights) { }
    @Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawnablePositionCalculator<?, ?> spawnablePositionCalculator) { return true; }
    @Override public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition) { return true; }
    @Override public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition, float weight) { return weight; }
}
