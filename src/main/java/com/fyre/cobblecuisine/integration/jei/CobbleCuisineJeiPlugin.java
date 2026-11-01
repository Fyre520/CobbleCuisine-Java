package com.fyre.cobblecuisine.integration.jei;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.block.CobbleCuisineBlocks;
import com.fyre.cobblecuisine.crafting.CobbleCuisineRecipeTypes;
import com.fyre.cobblecuisine.crafting.recipe.MixerShapelessRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.List;

@JeiPlugin
public class CobbleCuisineJeiPlugin implements IModPlugin {
	private static final Identifier ID = Identifier.of(CobbleCuisine.MOD_ID, "jei_plugin");

	@Override
	public Identifier getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new MixerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (MinecraftClient.getInstance().world == null) return;
		List<MixerShapelessRecipe> recipes = MinecraftClient.getInstance().world.getRecipeManager()
				.listAllOfType(CobbleCuisineRecipeTypes.MIXER_SHAPELESS_RECIPE).stream()
				.map(RecipeEntry::value)
				.toList();
		registration.addRecipes(MixerRecipeCategory.RECIPE_TYPE, recipes);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		for (Block mixer : List.of(CobbleCuisineBlocks.MIXER_RED, CobbleCuisineBlocks.MIXER_BLUE, CobbleCuisineBlocks.MIXER_GREEN,
				CobbleCuisineBlocks.MIXER_YELLOW, CobbleCuisineBlocks.MIXER_PINK, CobbleCuisineBlocks.MIXER_WHITE,
				CobbleCuisineBlocks.MIXER_BLACK)) {
			registration.addRecipeCatalyst(new ItemStack(mixer), MixerRecipeCategory.RECIPE_TYPE);
		}
	}
}
