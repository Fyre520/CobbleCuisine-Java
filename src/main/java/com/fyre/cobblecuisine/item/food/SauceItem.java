package com.fyre.cobblecuisine.item.food;

import com.fyre.cobblecuisine.util.CobbleCuisineUtils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class SauceItem extends Item {
	private final List<Text> tooltips;

	public SauceItem(String name, Item.Settings settings) {
		super(settings);
		this.tooltips = CobbleCuisineUtils.getItemTooltip(name, null, null, 0, null, null, null);
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.addAll(tooltips);
		super.appendTooltip(stack, context, tooltip, type);
	}
}
