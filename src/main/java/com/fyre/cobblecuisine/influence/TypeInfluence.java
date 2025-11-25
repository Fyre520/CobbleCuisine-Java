package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
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

public class TypeInfluence implements SpawningInfluence {

	private static final ElementalType[] TYPE_ORDER = new ElementalType[]{
			ElementalTypes.INSTANCE.getNORMAL(),
			ElementalTypes.INSTANCE.getFIRE(),
			ElementalTypes.INSTANCE.getWATER(),
			ElementalTypes.INSTANCE.getELECTRIC(),
			ElementalTypes.INSTANCE.getGRASS(),
			ElementalTypes.INSTANCE.getICE(),
			ElementalTypes.INSTANCE.getFIGHTING(),
			ElementalTypes.INSTANCE.getPOISON(),
			ElementalTypes.INSTANCE.getGROUND(),
			ElementalTypes.INSTANCE.getFLYING(),
			ElementalTypes.INSTANCE.getPSYCHIC(),
			ElementalTypes.INSTANCE.getBUG(),
			ElementalTypes.INSTANCE.getROCK(),
			ElementalTypes.INSTANCE.getGHOST(),
			ElementalTypes.INSTANCE.getDRAGON(),
			ElementalTypes.INSTANCE.getDARK(),
			ElementalTypes.INSTANCE.getSTEEL(),
			ElementalTypes.INSTANCE.getFAIRY()
	};

	@SuppressWarnings("unchecked")
	private static final RegistryEntry<StatusEffect>[] STATUS_EFFECTS = new RegistryEntry[]{
			CobbleCuisineEffects.NORMAL.entry,
			CobbleCuisineEffects.FIRE.entry,
			CobbleCuisineEffects.WATER.entry,
			CobbleCuisineEffects.ELECTRIC.entry,
			CobbleCuisineEffects.GRASS.entry,
			CobbleCuisineEffects.ICE.entry,
			CobbleCuisineEffects.FIGHTING.entry,
			CobbleCuisineEffects.POISON.entry,
			CobbleCuisineEffects.GROUND.entry,
			CobbleCuisineEffects.FLYING.entry,
			CobbleCuisineEffects.PSYCHIC.entry,
			CobbleCuisineEffects.BUG.entry,
			CobbleCuisineEffects.ROCK.entry,
			CobbleCuisineEffects.GHOST.entry,
			CobbleCuisineEffects.DRAGON.entry,
			CobbleCuisineEffects.DARK.entry,
			CobbleCuisineEffects.STEEL.entry,
			CobbleCuisineEffects.FAIRY.entry
	};

	private static final float[] MATCH_MULTIPLIERS = {
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.normal.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.fire.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.water.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.electric.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.grass.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.ice.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.fighting.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.poison.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.ground.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.flying.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.psychic.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.bug.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.rock.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.ghost.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.dragon.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.dark.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.steel.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.fairy.weightMultiplier)
	};

	private static final float[] NON_MATCH_MULTIPLIERS = {
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.normal.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.fire.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.water.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.electric.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.grass.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.ice.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.fighting.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.poison.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.ground.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.flying.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.psychic.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.bug.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.rock.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.ghost.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.dragon.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.dark.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.steel.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.typeMultipliers.fairy.nonWeightMultiplier)
	};

	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	private final ServerPlayerEntity player;
	public TypeInfluence(ServerPlayerEntity player) { this.player = player; }

	@Override
	public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition, float weight) {
		if (!player.hasStatusEffect(CobbleCuisineEffects.TYPE_BUFF_MARKER.entry)) return weight;
		if (!(detail instanceof PokemonSpawnDetail pkm)) return weight;

		Species species = CobbleCuisineUtils.resolveSpecies(pkm);
		if (species == null) return weight;

		if (player.getBlockPos().getSquaredDistance(spawnablePosition.getPosition()) > EFFECT_DISTANCE) return weight;

		ElementalType primary = species.getPrimaryType();
		ElementalType secondary = species.getSecondaryType() != null ? species.getSecondaryType() : null;

		float result = weight;
		for (int i = 0; i < STATUS_EFFECTS.length; i++) {
			if (player.hasStatusEffect(STATUS_EFFECTS[i])) {
				boolean matches = (TYPE_ORDER[i] == primary) || (secondary != null && TYPE_ORDER[i] == secondary);
				result *= matches ? MATCH_MULTIPLIERS[i] : NON_MATCH_MULTIPLIERS[i];

				if (DEBUG) LOGGER.info("CobbleCuisine >> TYPE INFLUENCE >> PLAYER: {} PKM: {} OLD WEIGHT: {} NEW WEIGHT: {}", player.getName(), species, weight, result);
			}
		}
		return result;
	}

    @Override public boolean isExpired() { return false; }
    @Override public void affectAction(@NotNull SpawnAction<?> action) { }
    @Override public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) { }
    @Override public void affectBucketWeights(@NotNull Map<SpawnBucket, Float> bucketWeights) { }
    @Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawnablePositionCalculator<?, ?> spawnablePositionCalculator) { return true; }
    @Override public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition) { return true; }
}
