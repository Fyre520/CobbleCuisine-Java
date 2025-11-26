package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType;
import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.fyre.cobblecuisine.random.PRNG;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;
import static com.fyre.cobblecuisine.CobbleCuisine.DEBUG;

public class HiddenAbilityInfluence implements SpawningInfluence {

    private static final float HIDDEN_ABILITY_CHANCE = CobbleCuisineConfig.data.boostSettings.haBoostChance;
    private static final double EFFECT_DISTANCE = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);

    private final ServerPlayerEntity player;

    public HiddenAbilityInfluence(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
        // Check if player has the hidden ability effect
        if (!player.hasStatusEffect(CobbleCuisineEffects.HIDDEN_ABILITY.entry)) return;

        // Check if entity is a Pokemon and not player-owned
        if (!(entity instanceof PokemonEntity pokemonEntity) || pokemonEntity.getPokemon().isPlayerOwned()) return;

        // Check if player is within effect distance
        if (player.getBlockPos().getSquaredDistance(entity.getBlockPos()) > EFFECT_DISTANCE) return;

        // Check if random chance succeeds
        if (PRNG.nextDouble() >= HIDDEN_ABILITY_CHANCE) return;

        // Get the hidden ability for this Pokemon's form (should only be one)
        var hiddenAbility = pokemonEntity.getPokemon().getForm().getAbilities().getMapping()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(abilityPool -> abilityPool.getType() == HiddenAbilityType.INSTANCE)
                .findFirst()
                .orElse(null);

        // If no hidden ability available, return
        if (hiddenAbility == null) return;

        // Apply the hidden ability to the Pokemon
        pokemonEntity.getPokemon().updateAbility(hiddenAbility.getTemplate().create(false, hiddenAbility.getPriority()));

        if (DEBUG) {
            LOGGER.info("CobbleCuisine >> HIDDEN ABILITY INFLUENCE >> PLAYER: {} PKM: {} SET HIDDEN ABILITY: {}",
                    player.getName(),
                    pokemonEntity.getName(),
                    hiddenAbility.getTemplate().getName());
        }
    }

    @Override public boolean isExpired() { return false; }
    @Override public void affectAction(@NotNull SpawnAction<?> action) { }
    @Override public void affectBucketWeights(@NotNull Map<SpawnBucket, Float> bucketWeights) { }
    @Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawnablePositionCalculator<?, ?> spawnablePositionCalculator) { return true; }
    @Override public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition) { return true; }
    @Override public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition, float weight) { return weight; }
}
