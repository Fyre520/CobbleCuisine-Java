package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.config.CobbleCuisineConfigData;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

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

	private final ServerPlayerEntity player;
	public TypeInfluence(ServerPlayerEntity player) { this.player = player; }

	@Override
	public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition ctx, float weight) {
		if (!player.hasStatusEffect(CobbleCuisineEffects.TYPE_BUFF_MARKER.entry)) return weight;
		if (!SpawnDetailForms.isPokemon(detail)) return weight;

		double effectDistance = Math.pow(CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks, 2);
		if (player.getBlockPos().getSquaredDistance(ctx.getPosition()) > effectDistance) return weight;

		float result = weight;
		for (int i = 0; i < STATUS_EFFECTS.length; i++) {
			if (player.hasStatusEffect(STATUS_EFFECTS[i])) {
				ElementalType target = TYPE_ORDER[i];
				float fraction = SpawnDetailForms.matchingFraction(detail, form -> target == form.getPrimaryType() || target == form.getSecondaryType());
				if (fraction < 0.0f) return weight;
				result = TypeWeightCalculator.apply(result, fraction, settingsFor(i));

				if (DEBUG) LOGGER.info("CobbleCuisine >> TYPE INFLUENCE >> PLAYER: {} PKM: {} OLD WEIGHT: {} NEW WEIGHT: {}", player.getName(), detail.getName(), weight, result);
			}
		}
		return result;
	}

	private static CobbleCuisineConfigData.WeightPair settingsFor(int index) {
		CobbleCuisineConfigData.TypeMultipliers settings = CobbleCuisineConfig.data.typeMultipliers;
		return switch (index) {
			case 0 -> settings.normal;
			case 1 -> settings.fire;
			case 2 -> settings.water;
			case 3 -> settings.electric;
			case 4 -> settings.grass;
			case 5 -> settings.ice;
			case 6 -> settings.fighting;
			case 7 -> settings.poison;
			case 8 -> settings.ground;
			case 9 -> settings.flying;
			case 10 -> settings.psychic;
			case 11 -> settings.bug;
			case 12 -> settings.rock;
			case 13 -> settings.ghost;
			case 14 -> settings.dragon;
			case 15 -> settings.dark;
			case 16 -> settings.steel;
			case 17 -> settings.fairy;
			default -> throw new IllegalArgumentException("Unknown type index: " + index);
		};
	}

	@Override public boolean isExpired() { return false; }
	@Override public void affectAction(@NotNull SpawnAction<?> action) { }
	@Override public boolean isAllowedPosition(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull SpawnablePositionCalculator<?, ?> contextCalculator) { return true; }
	@Override public boolean affectSpawnable(@NotNull SpawnDetail detail, @NotNull SpawnablePosition ctx) { return true; }
}
