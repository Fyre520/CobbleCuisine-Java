package com.fyre.cobblecuisine.meal;

import com.cobblemon.mod.common.api.spawning.SpawnCause;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class ActiveMealResolver {
	private final ServerPlayerEntity player;
	private SpawnCause resolvedCause;
	private MealLoadout resolvedLoadout = MealLoadout.EMPTY;
	private List<MealLoadout> resolvedMeals = List.of();

	public ActiveMealResolver(ServerPlayerEntity player) {
		this.player = player;
	}

	public MealLoadout resolve(SpawnablePosition position) {
		refresh(position);
		return resolvedLoadout;
	}

	public MealLoadout resolve(SpawnAction<?> action) {
		return resolve(action.getSpawnablePosition());
	}

	public List<MealLoadout> meals(SpawnablePosition position) {
		refresh(position);
		return resolvedMeals;
	}

	public MealLoadout resolveWith(SpawnAction<?> action, String axis) {
		refresh(action.getSpawnablePosition());
		return ActiveMealState.newestWith(resolvedMeals, axis).orElse(MealLoadout.EMPTY);
	}

	private void refresh(SpawnablePosition position) {
		SpawnCause cause = position.getCause();
		if (cause != resolvedCause) {
			resolvedCause = cause;
			resolvedLoadout = current(player);
			resolvedMeals = ActiveMealState.getActiveMeals(player);
		}
	}

	public boolean isWithinRange(BlockPos position) {
		double radius = CobbleCuisineConfig.data.boostSettings.effectDistanceBlocks;
		return player.getBlockPos().getSquaredDistance(position) <= radius * radius;
	}

	public static MealLoadout current(ServerPlayerEntity player) {
		return ActiveMealState.getActive(player).orElse(MealLoadout.EMPTY);
	}

	public static MealLoadout currentWith(ServerPlayerEntity player, String axis) {
		return ActiveMealState.getActiveWith(player, axis).orElse(MealLoadout.EMPTY);
	}

	public static int multiplyAndRound(int value, float multiplier) {
		return Math.round(value * multiplier);
	}

	public static float totalStrength(MealLoadout loadout, String axis) {
		return (float) Math.min(Float.MAX_VALUE, axisEffects(loadout, axis).stream()
				.mapToDouble(entry -> Math.abs((double) entry.getValue())).sum());
	}

	public static String chooseTarget(MealLoadout loadout, String axis, float randomFraction) {
		List<Map.Entry<MealLoadout.EffectKey, Float>> effects = axisEffects(loadout, axis);
		double total = effects.stream().mapToDouble(entry -> Math.abs((double) entry.getValue())).sum();
		if (total <= 0.0f) return null;

		double cursor = Math.max(0.0f, Math.min(Math.nextDown(1.0f), randomFraction)) * total;
		for (Map.Entry<MealLoadout.EffectKey, Float> effect : effects) {
			cursor -= Math.abs(effect.getValue());
			if (cursor < 0.0f) return effect.getKey().target();
		}
		return effects.getLast().getKey().target();
	}

	public static List<Map.Entry<MealLoadout.EffectKey, Float>> axisEffects(MealLoadout loadout, String axis) {
		return loadout.effects().entrySet().stream()
				.filter(entry -> entry.getKey().axis().equals(axis) && entry.getValue() != 0.0f)
				.sorted(Comparator.comparing(entry -> entry.getKey().target()))
				.toList();
	}
}
