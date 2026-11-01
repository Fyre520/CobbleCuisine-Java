package com.fyre.cobblecuisine.crafting;

import com.fyre.cobblecuisine.crafting.recipe.MixerShapelessRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class CobbleCuisineRecipeSerializers {

	public static final RecipeSerializer<MixerShapelessRecipe> MIXER_SHAPELESS_RECIPE = Registry.register(
			Registries.RECIPE_SERIALIZER,
			Identifier.of("cobblecuisine", "mixer_shapeless"),
			new MixerShapelessRecipe.Serializer()
	);

	public static void register() {
	}
}