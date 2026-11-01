package com.fyre.cobblecuisine.inventory;

import com.fyre.cobblecuisine.crafting.recipe.MixerShapelessRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.slot.Slot;

public class MixerSeasoningSlot extends Slot {
	private final MixerMenu menu;

	public MixerSeasoningSlot(MixerMenu menu, Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.menu = menu;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		RecipeEntry<MixerShapelessRecipe> recipe = menu.getActiveRecipe();
		return recipe != null && stack.isIn(recipe.value().getSeasoningTag());
	}
}
