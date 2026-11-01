package com.fyre.cobblecuisine.inventory;

import com.cobblemon.mod.common.CobblemonItemComponents;
import com.cobblemon.mod.common.CobblemonSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;

public class MixerResultSlot extends Slot {
	public MixerResultSlot(Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		return false;
	}

	@Override
	public void onTakeItem(PlayerEntity player, ItemStack stack) {
		if (stack.contains(CobblemonItemComponents.FLAVOUR)) {
			stack.set(CobblemonItemComponents.CRAFTED, true);
		}
		if (player instanceof ServerPlayerEntity) {
			player.getWorld().playSound(null, player.getBlockPos(),
					CobblemonSounds.CAMPFIRE_POT_TAKE_ITEM, SoundCategory.BLOCKS, 1.0f, 1.0f);
		}
		super.onTakeItem(player, stack);
	}
}
