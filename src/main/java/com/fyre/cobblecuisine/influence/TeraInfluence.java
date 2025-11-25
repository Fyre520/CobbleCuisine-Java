package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.api.types.tera.TeraType;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import com.fyre.cobblecuisine.random.PRNG;
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

public class TeraInfluence implements SpawningInfluence {

	@SuppressWarnings("unchecked")
	private static final RegistryEntry<StatusEffect>[] STATUS_EFFECTS = new RegistryEntry[]{
			CobbleCuisineEffects.TERA_NORMAL.entry,
			CobbleCuisineEffects.TERA_FIRE.entry,
			CobbleCuisineEffects.TERA_WATER.entry,
			CobbleCuisineEffects.TERA_GRASS.entry,
			CobbleCuisineEffects.TERA_ELECTRIC.entry,
			CobbleCuisineEffects.TERA_ICE.entry,
			CobbleCuisineEffects.TERA_FIGHTING.entry,
			CobbleCuisineEffects.TERA_POISON.entry,
			CobbleCuisineEffects.TERA_GROUND.entry,
			CobbleCuisineEffects.TERA_FLYING.entry,
			CobbleCuisineEffects.TERA_PSYCHIC.entry,
			CobbleCuisineEffects.TERA_BUG.entry,
			CobbleCuisineEffects.TERA_ROCK.entry,
			CobbleCuisineEffects.TERA_GHOST.entry,
			CobbleCuisineEffects.TERA_DRAGON.entry,
			CobbleCuisineEffects.TERA_DARK.entry,
			CobbleCuisineEffects.TERA_STEEL.entry,
			CobbleCuisineEffects.TERA_FAIRY.entry,
			CobbleCuisineEffects.TERA_STELLAR.entry
	};

	private static final TeraType[] TERA_TYPES = new TeraType[]{
			TeraTypes.getNORMAL(),
			TeraTypes.getFIRE(),
			TeraTypes.getWATER(),
			TeraTypes.getGRASS(),
			TeraTypes.getELECTRIC(),
			TeraTypes.getICE(),
			TeraTypes.getFIGHTING(),
			TeraTypes.getPOISON(),
			TeraTypes.getGROUND(),
			TeraTypes.getFLYING(),
			TeraTypes.getPSYCHIC(),
			TeraTypes.getBUG(),
			TeraTypes.getROCK(),
			TeraTypes.getGHOST(),
			TeraTypes.getDRAGON(),
			TeraTypes.getDARK(),
			TeraTypes.getSTEEL(),
			TeraTypes.getFAIRY(),
			TeraTypes.getSTELLAR()
	};

	private static final float TERA_CHANCE = CobbleCuisineConfig.data.boostSettings.teraBoostChance;

	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	private final ServerPlayerEntity player;
	public TeraInfluence(ServerPlayerEntity player) { this.player = player; }

	@Override
	public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
		if (!player.hasStatusEffect(CobbleCuisineEffects.TERA_BUFF_MARKER.entry)) return;
		if (!(entity instanceof PokemonEntity pokemonEntity) || pokemonEntity.getPokemon().isPlayerOwned()) return;

		if (player.getBlockPos().getSquaredDistance(entity.getBlockPos()) > EFFECT_DISTANCE) return;

		if (PRNG.nextDouble() >= TERA_CHANCE) return;
		for (int i = 0; i < STATUS_EFFECTS.length; i++) {
			if (player.hasStatusEffect(STATUS_EFFECTS[i])) {
				pokemonEntity.getPokemon().setTeraType(TERA_TYPES[i]);

				if (DEBUG) LOGGER.info("CobbleCuisine >> TERA INFLUENCE >> PLAYER: {} PKM: {} SET TERA TYPE: {}", player.getName(), pokemonEntity.getName(), TERA_TYPES[i].toString());

				break;
			}
		}
	}

    @Override public boolean isExpired() { return false; }
    @Override public void affectAction(@NotNull SpawnAction<?> action) { }
    @Override public void affectBucketWeights(@NotNull Map<SpawnBucket, Float> bucketWeights) { }
    @Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawnablePositionCalculator<?, ?> spawnablePositionCalculator) { return true; }
    @Override public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition) { return true; }
    @Override public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition, float weight) { return weight; }
}
