package com.fyre.cobblecuisine.meal;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.item.crafting.SeasoningProcessor;

import com.fyre.cobblecuisine.component.CobbleCuisineDataComponents;
import com.fyre.cobblecuisine.component.ShakeEvComponent;
import com.fyre.cobblecuisine.item.food.ShakeBlendMath;

import net.minecraft.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ShakeSeasoningProcessor implements SeasoningProcessor {
	public static final String TYPE = "cobblecuisine:shake_effects";
	public static final ShakeSeasoningProcessor INSTANCE = new ShakeSeasoningProcessor();

	private ShakeSeasoningProcessor() {
	}

	public static void register() {
		SeasoningProcessor.Companion.getProcessors().put(TYPE, INSTANCE);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void apply(ItemStack result, List<ItemStack> seasoning) {
		Map<Stat, Integer> extras = new LinkedHashMap<>();

		for (ItemStack stack : seasoning) {
			Stat stat = ShakeBlendMath.seasoningStat(stack);
			int ev = ShakeBlendMath.seasoningEv(stack);
			if (stat == null || ev <= 0) continue;
			extras.merge(stat, ev, Integer::sum);
		}

		if (extras.isEmpty()) {
			result.remove(CobbleCuisineDataComponents.SHAKE_EV);
		} else {
			result.set(CobbleCuisineDataComponents.SHAKE_EV, new ShakeEvComponent(extras));
		}
	}

	@Override
	public boolean consumesItem(ItemStack seasoning) {
		return ShakeBlendMath.seasoningStat(seasoning) != null;
	}
}
