package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.pokemon.Species;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.fyre.cobblecuisine.util.CobbleCuisineUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;
import static com.fyre.cobblecuisine.CobbleCuisine.DEBUG;

public class YieldInfluence implements SpawningInfluence {
	@SuppressWarnings("unchecked")
	private static final RegistryEntry<StatusEffect>[] STATUS_EFFECTS = new RegistryEntry[]{
			CobbleCuisineEffects.HP_YIELD.entry,
			CobbleCuisineEffects.ATK_YIELD.entry,
			CobbleCuisineEffects.DEF_YIELD.entry,
			CobbleCuisineEffects.SPA_YIELD.entry,
			CobbleCuisineEffects.SPD_YIELD.entry,
			CobbleCuisineEffects.SPE_YIELD.entry
	};

	private static final Stat[] STATS = new Stat[]{
			Stats.HP,
			Stats.ATTACK,
			Stats.DEFENCE,
			Stats.SPECIAL_ATTACK,
			Stats.SPECIAL_DEFENCE,
			Stats.SPEED
	};

	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	private final ServerPlayerEntity player;
	public YieldInfluence(ServerPlayerEntity player) { this.player = player; }

	@Override
	public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position) {
		if (!player.hasStatusEffect(CobbleCuisineEffects.YIELD_BUFF_MARKER.entry)) return true;
		if (!(detail instanceof PokemonSpawnDetail pkmDetail)) return true;

		Species species = CobbleCuisineUtils.resolveSpecies(pkmDetail);
		if (species == null) return true;

		if (player.getBlockPos().getSquaredDistance(position.getPosition()) > EFFECT_DISTANCE) return true;

		for (int i = 0; i < STATUS_EFFECTS.length; i++) {
			if (player.hasStatusEffect(STATUS_EFFECTS[i])) {
				Integer yield = species.getEvYield().get(STATS[i]);

				if (DEBUG) LOGGER.info("CobbleCuisine >> YIELD INFLUENCE >> PLAYER: {} PKM: {} YIELD: {}", player.getName(), species, yield);

				return yield != null && yield > 0;
			}
		}
		return true;
	}

    @Override public boolean isExpired() { return false; }
    @Override public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition, float weight) { return weight; }
    @Override public void affectAction(@NotNull SpawnAction<?> action) { }
    @Override public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity ) { }
    @Override public void affectBucketWeights(@NotNull Map<SpawnBucket, Float> bucketWeights) { }
    @Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawnablePositionCalculator<?, ?> spawnablePositionCalculator) { return true; }
}
