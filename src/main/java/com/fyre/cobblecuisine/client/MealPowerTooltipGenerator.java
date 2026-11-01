package com.fyre.cobblecuisine.client;

import com.cobblemon.mod.common.client.tooltips.TooltipGenerator;
import com.fyre.cobblecuisine.item.food.MalasadaItem;
import com.fyre.cobblecuisine.item.food.PokePuffItem;
import com.fyre.cobblecuisine.item.food.PreparedFoodItem;
import com.fyre.cobblecuisine.meal.MealConsumption;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class MealPowerTooltipGenerator extends TooltipGenerator {
	@Override
	public List<Text> generateAdditionalTooltip(ItemStack stack, List<Text> lines) {
		List<Text> powers;
		if (isMeal(stack)) {
			powers = new ArrayList<>(MealConsumption.shiftTooltip(stack, stack.get(DataComponentTypes.FOOD)));
		} else {
			powers = new ArrayList<>(MealConsumption.ingredientShiftTooltip(stack));
		}
		if (stack.getItem() instanceof MalasadaItem malasada) {
			powers.addAll(malasada.tasteTooltip(stack));
		}
		return powers.isEmpty() ? null : powers;
	}

	private static boolean isMeal(ItemStack stack) {
		return stack.getItem() instanceof PreparedFoodItem
				|| stack.getItem() instanceof PokePuffItem
				|| stack.getItem() instanceof MalasadaItem;
	}
}
