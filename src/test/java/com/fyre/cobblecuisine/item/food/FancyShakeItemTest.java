package com.fyre.cobblecuisine.item.food;

import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FancyShakeItemTest {
	@Test
	void deluxeShakeDeclaresItsPlayerUseAction() throws NoSuchMethodException {
		var method = FancyShakeItem.class.getDeclaredMethod("getUseAction", ItemStack.class);

		assertEquals(UseAction.class, method.getReturnType());
	}
}
