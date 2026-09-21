package com.fyre.cobblecuisine.mixin;

import com.cobblemon.mod.common.item.crafting.CookingPotShapelessRecipe;
import com.fyre.cobblecuisine.CobbleCuisine;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CookingPotShapelessRecipe.class, remap = false)
public abstract class CookingPotShapelessRecipeMixin {
	@Inject(
			target = @Desc(value = "matches", args = {CraftingRecipeInput.class, World.class}, ret = boolean.class),
			at = @At("HEAD"),
			cancellable = true
	)
	private void cobblecuisine$matchOverlappingIngredients(
			CraftingRecipeInput input,
			World world,
			CallbackInfoReturnable<Boolean> cir
	) {
		CookingPotShapelessRecipe recipe = (CookingPotShapelessRecipe) (Object) this;
		if (!CobbleCuisine.MOD_ID.equals(Registries.ITEM.getId(recipe.getResult().getItem()).getNamespace())) {
			return;
		}

		// The pot already excludes optional seasoning slots from this input.
		// Vanilla's matcher assigns overlapping ingredients using one item per occupied slot.
		cir.setReturnValue(input.getStackCount() == recipe.getIngredients().size()
				&& input.getRecipeMatcher().match(recipe, null));
	}
}
