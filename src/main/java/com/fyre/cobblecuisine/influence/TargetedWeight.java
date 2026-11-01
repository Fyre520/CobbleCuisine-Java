package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.pokemon.FormData;
import com.fyre.cobblecuisine.meal.ActiveMealResolver;
import com.fyre.cobblecuisine.meal.MealLoadout;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

final class TargetedWeight {
	private TargetedWeight() {
	}

	static float multiplier(SpawnDetail detail, List<MealLoadout> meals, String axis, BiPredicate<FormData, String> matches) {
		float multiplier = 1.0f;
		for (MealLoadout meal : meals) multiplier *= multiplier(detail, meal, axis, matches);
		return multiplier;
	}

	private static float multiplier(SpawnDetail detail, MealLoadout loadout, String axis, BiPredicate<FormData, String> matches) {
		List<Map.Entry<MealLoadout.EffectKey, Float>> mains = ActiveMealResolver.axisEffects(loadout, axis);
		List<Map.Entry<MealLoadout.EffectKey, Float>> seasonings = ActiveMealResolver.axisEffects(loadout, MealLoadout.seasoningAxis(axis));
		if (mains.isEmpty() && seasonings.isEmpty()) return 1.0f;
		float cut = loadout.strength(MealLoadout.cutAxis(axis), "");
		return SpawnDetailForms.weightedAverage(detail, form -> formMultiplier(form, mains, seasonings, cut, matches));
	}

	private static double formMultiplier(
			FormData form,
			List<Map.Entry<MealLoadout.EffectKey, Float>> mains,
			List<Map.Entry<MealLoadout.EffectKey, Float>> seasonings,
			float cut,
			BiPredicate<FormData, String> matches
	) {
		double multiplier = 1.0;
		if (!mains.isEmpty()) {
			boolean matchesMain = false;
			for (Map.Entry<MealLoadout.EffectKey, Float> main : mains) {
				if (!matches.test(form, main.getKey().target())) continue;
				multiplier *= main.getValue();
				matchesMain = true;
			}
			if (!matchesMain) multiplier *= matchesAny(form, seasonings, matches) ? Math.min(1.0, 2.0 * cut) : cut;
		}
		for (Map.Entry<MealLoadout.EffectKey, Float> seasoning : seasonings) {
			if (matches.test(form, seasoning.getKey().target())) multiplier *= seasoning.getValue();
		}
		return multiplier;
	}

	private static boolean matchesAny(FormData form, List<Map.Entry<MealLoadout.EffectKey, Float>> effects, BiPredicate<FormData, String> matches) {
		for (Map.Entry<MealLoadout.EffectKey, Float> effect : effects) {
			if (matches.test(form, effect.getKey().target())) return true;
		}
		return false;
	}
}
