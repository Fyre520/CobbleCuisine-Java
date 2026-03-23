package com.fyre.cobblecuisine.mixin;

import com.fyre.cobblecuisine.item.CobbleCuisineItems;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {

	@Inject(method = "canGather", at = @At("RETURN"), cancellable = true)
	private void cobblecuisine$canGather(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			VillagerEntity villager = (VillagerEntity) (Object) this;
			if (villager.getVillagerData().getProfession() == VillagerProfession.FARMER
					&& villager.getInventory().canInsert(stack)
					&& stack.isOf(CobbleCuisineItems.BEAN_SEEDS)) {
				cir.setReturnValue(true);
			}
		}
	}
}
