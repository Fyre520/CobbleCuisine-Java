package com.fyre.cobblecuisine.crafting;

import com.fyre.cobblecuisine.crafting.recipe.MixerShapelessRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class CobbleCuisineRecipeTypes {

	public static final RecipeType<MixerShapelessRecipe> MIXER_SHAPELESS_RECIPE = register("mixer_shapeless");

	private static <T extends Recipe<?>> RecipeType<T> register(String id) {
		return Registry.register(
				Registries.RECIPE_TYPE,
				Identifier.of("cobblecuisine", id),
				new RecipeType<T>() {
					@Override
					public String toString() {
						return "cobblecuisine:" + id;
					}
				}
		);
	}

	public static void register() {
	}
}