package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;


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
	public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition ctx) {
		if (!player.hasStatusEffect(CobbleCuisineEffects.YIELD_BUFF_MARKER.entry)) return true;
		if (!SpawnDetailForms.isPokemon(detail)) return true;
		var forms = SpawnDetailForms.resolve(detail);
		if (forms.isEmpty() && detail instanceof PokemonSpawnDetail) return true;

		if (player.getBlockPos().getSquaredDistance(ctx.getPosition()) > EFFECT_DISTANCE) return true;

		for (int i = 0; i < STATUS_EFFECTS.length; i++) {
			if (player.hasStatusEffect(STATUS_EFFECTS[i])) {
				Stat target = STATS[i];
				return forms.stream().anyMatch(form -> {
					Integer yield = form.getEvYield().get(target);
					return yield != null && yield > 0;
				});
			}
		}
		return true;
	}

	@Override public boolean isExpired() { return false; }
	@Override public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition ctx, float weight) { return weight; }
	@Override public void affectAction(@NotNull SpawnAction<?> action) { }
	@Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawnablePositionCalculator<?, ?> contextCalculator) { return true; }
}
