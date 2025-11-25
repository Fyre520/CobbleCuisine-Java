package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;

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

public class NatureInfluence implements SpawningInfluence {

	private static final float NATURE_CHANCE = CobbleCuisineConfig.data.boostSettings.natureBoostChance;

	@SuppressWarnings("unchecked")
	private static final RegistryEntry<StatusEffect>[] STATUS_EFFECTS = new RegistryEntry[]{
			CobbleCuisineEffects.SERIOUS.entry,
			CobbleCuisineEffects.HARDY.entry,
			CobbleCuisineEffects.LONELY.entry,
			CobbleCuisineEffects.BOLD.entry,
			CobbleCuisineEffects.TIMID.entry,
			CobbleCuisineEffects.HASTY.entry,
			CobbleCuisineEffects.JOLLY.entry,
			CobbleCuisineEffects.NAIVE.entry,
			CobbleCuisineEffects.MODEST.entry,
			CobbleCuisineEffects.MILD.entry,
			CobbleCuisineEffects.QUIET.entry,
			CobbleCuisineEffects.BASHFUL.entry,
			CobbleCuisineEffects.RASH.entry,
			CobbleCuisineEffects.CALM.entry,
			CobbleCuisineEffects.GENTLE.entry,
			CobbleCuisineEffects.SASSY.entry,
			CobbleCuisineEffects.CAREFUL.entry,
			CobbleCuisineEffects.QUIRKY.entry,
			CobbleCuisineEffects.LAX.entry,
			CobbleCuisineEffects.RELAXED.entry,
			CobbleCuisineEffects.IMPISH.entry,
			CobbleCuisineEffects.ADAMANT.entry,
			CobbleCuisineEffects.DOCILE.entry,
			CobbleCuisineEffects.BRAVE.entry,
			CobbleCuisineEffects.NAUGHTY.entry
	};

	private static final Nature[] NATURES = new Nature[]{
			Natures.INSTANCE.getSERIOUS(),
			Natures.INSTANCE.getHARDY(),
			Natures.INSTANCE.getLONELY(),
			Natures.INSTANCE.getBOLD(),
			Natures.INSTANCE.getTIMID(),
			Natures.INSTANCE.getHASTY(),
			Natures.INSTANCE.getJOLLY(),
			Natures.INSTANCE.getNAIVE(),
			Natures.INSTANCE.getMODEST(),
			Natures.INSTANCE.getMILD(),
			Natures.INSTANCE.getQUIET(),
			Natures.INSTANCE.getBASHFUL(),
			Natures.INSTANCE.getRASH(),
			Natures.INSTANCE.getCALM(),
			Natures.INSTANCE.getGENTLE(),
			Natures.INSTANCE.getSASSY(),
			Natures.INSTANCE.getCAREFUL(),
			Natures.INSTANCE.getQUIRKY(),
			Natures.INSTANCE.getLAX(),
			Natures.INSTANCE.getRELAXED(),
			Natures.INSTANCE.getIMPISH(),
			Natures.INSTANCE.getADAMANT(),
			Natures.INSTANCE.getDOCILE(),
			Natures.INSTANCE.getBRAVE(),
			Natures.INSTANCE.getNAUGHTY()
	};

	private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

	private final ServerPlayerEntity player;
	public NatureInfluence(ServerPlayerEntity player) { this.player = player; }

	@Override
	public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
		if (!player.hasStatusEffect(CobbleCuisineEffects.NATURE_BUFF_MARKER.entry)) return;
		if (!(entity instanceof PokemonEntity pokemonEntity) || pokemonEntity.getPokemon().isPlayerOwned()) return;

		if (player.getBlockPos().getSquaredDistance(entity.getBlockPos()) > EFFECT_DISTANCE) return;

		if (PRNG.nextDouble() >= NATURE_CHANCE) return;
		for (int i = 0; i < STATUS_EFFECTS.length; i++) {
			if (player.hasStatusEffect(STATUS_EFFECTS[i])) {
				pokemonEntity.getPokemon().setNature(NATURES[i]);

				if (DEBUG) LOGGER.info("CobbleCuisine >> NATURE INFLUENCE >> PLAYER: {} PKM: {} SET NEW NATURE: {}", player.getName(), pokemonEntity.getName(), pokemonEntity.getPokemon().getNature());

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
