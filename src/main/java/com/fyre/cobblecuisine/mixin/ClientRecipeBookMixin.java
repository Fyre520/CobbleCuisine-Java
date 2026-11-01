package com.fyre.cobblecuisine.mixin;

import com.fyre.cobblecuisine.crafting.recipe.MixerShapelessRecipe;

import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.recipe.RecipeEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookMixin {

	@Inject(method = "getGroupForRecipe", at = @At("HEAD"), cancellable = true)
	private static void cobblecuisine$groupMixerRecipes(RecipeEntry<?> recipe, CallbackInfoReturnable<RecipeBookGroup> cir) {
		if (recipe.value() instanceof MixerShapelessRecipe) {
			cir.setReturnValue(RecipeBookGroup.UNKNOWN);
		}
	}
}
