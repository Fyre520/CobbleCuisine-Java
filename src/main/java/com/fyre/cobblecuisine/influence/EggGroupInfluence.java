package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
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

public class EggGroupInfluence implements SpawningInfluence {

	private static final EggGroup[] GROUP_ORDER = new EggGroup[]{
			EggGroup.AMORPHOUS,
			EggGroup.FAIRY,
			EggGroup.BUG,
			EggGroup.DRAGON,
			EggGroup.FIELD,
			EggGroup.FLYING,
			EggGroup.GRASS,
			EggGroup.HUMAN_LIKE,
			EggGroup.MINERAL,
			EggGroup.MONSTER,
			EggGroup.UNDISCOVERED,
			EggGroup.WATER_1,
			EggGroup.WATER_2,
			EggGroup.WATER_3
	};

	@SuppressWarnings("unchecked")
	private static final RegistryEntry<StatusEffect>[] STATUS_EFFECTS = new RegistryEntry[]{
			CobbleCuisineEffects.AMORPHOUS_EG.entry,
			CobbleCuisineEffects.FAIRY_EG.entry,
			CobbleCuisineEffects.BUG_EG.entry,
			CobbleCuisineEffects.DRAGON_EG.entry,
			CobbleCuisineEffects.FIELD_EG.entry,
			CobbleCuisineEffects.FLYING_EG.entry,
			CobbleCuisineEffects.GRASS_EG.entry,
			CobbleCuisineEffects.HUMANLIKE_EG.entry,
			CobbleCuisineEffects.MINERAL_EG.entry,
			CobbleCuisineEffects.MONSTER_EG.entry,
			CobbleCuisineEffects.UNDISCOVERED_EG.entry,
			CobbleCuisineEffects.WATER1_EG.entry,
			CobbleCuisineEffects.WATER23_EG.entry,
			CobbleCuisineEffects.WATER23_EG.entry
	};

	private static final float[] MATCH_MULTIPLIERS = {
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.amorphous.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.fairy.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.bug.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.dragon.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.field.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.flying.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.grass.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.humanLike.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.mineral.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.monster.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.undiscovered.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.water1.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.water23.weightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.water23.weightMultiplier)
	};

	private static final float[] NON_MATCH_MULTIPLIERS = {
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.amorphous.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.fairy.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.bug.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.dragon.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.field.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.flying.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.grass.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.humanLike.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.mineral.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.monster.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.undiscovered.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.water1.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.water23.nonWeightMultiplier),
			Math.max(Float.MIN_VALUE, CobbleCuisineConfig.data.eggGroupMultipliers.water23.nonWeightMultiplier)
	};

	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	private final ServerPlayerEntity player;
	public EggGroupInfluence(ServerPlayerEntity player) { this.player = player; }

	@Override
	public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition, float weight) {
		if (!player.hasStatusEffect(CobbleCuisineEffects.EGG_BUFF_MARKER.entry)) return weight;
		if (!(detail instanceof PokemonSpawnDetail pkm)) return weight;

		Species species = CobbleCuisineUtils.resolveSpecies(pkm);
		if (species == null) return weight;

		if (player.getBlockPos().getSquaredDistance(spawnablePosition.getPosition()) > EFFECT_DISTANCE) return weight;

		float result = weight;
		for (int i = 0; i < STATUS_EFFECTS.length; i++) {
			if (player.hasStatusEffect(STATUS_EFFECTS[i])) {
				boolean matches = species.getEggGroups().contains(GROUP_ORDER[i]);
				result *= matches ? MATCH_MULTIPLIERS[i] : NON_MATCH_MULTIPLIERS[i];

				if (DEBUG) LOGGER.info("CobbleCuisine >> EGG GROUP INFLUENCE >> PLAYER: {} PKM: {} OLD WEIGHT: {} NEW WEIGHT: {}", player.getName(), species, weight, result);
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
