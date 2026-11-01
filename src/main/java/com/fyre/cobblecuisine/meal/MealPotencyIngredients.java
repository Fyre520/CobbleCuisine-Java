package com.fyre.cobblecuisine.meal;

import com.fyre.cobblecuisine.CobbleCuisine;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MealPotencyIngredients {
	private static final TagKey<Item> IV_SWEETS = tag("iv_sweets");
	private static final TagKey<Item> GOLD_NUGGETS = tag("gold_nuggets");
	private static final TagKey<Item> FRUITS = TagKey.of(RegistryKeys.ITEM, Identifier.of("minecraft", "fruits"));

	private MealPotencyIngredients() {
	}

	static MealPotency fromStacks(List<ItemStack> stacks) {
		Map<String, Integer> values = new LinkedHashMap<>();
		for (ItemStack stack : stacks) {
			if (stack.isOf(Items.SUGAR)) add(values, MealPotency.FRIENDSHIP_SUGAR, 1);
			if (stack.isIn(FRUITS)) add(values, MealPotency.HEALING_FRUIT, 1);
			if (stack.isIn(IV_SWEETS)) add(values, MealPotency.IV_SWEETS, 1);
			if (stack.isIn(GOLD_NUGGETS)) add(values, MealPotency.GOLD_NUGGETS, 1);
		}
		return new MealPotency(values);
	}

	static boolean recognizes(ItemStack stack) {
		return stack.isIn(IV_SWEETS)
				|| stack.isIn(GOLD_NUGGETS)
				|| stack.isOf(Items.SUGAR) || stack.isIn(FRUITS);
	}

	private static void add(Map<String, Integer> values, String key, int amount) {
		if (amount > 0) values.merge(key, amount, Integer::sum);
	}

	private static TagKey<Item> tag(String path) {
		return TagKey.of(RegistryKeys.ITEM, Identifier.of(CobbleCuisine.MOD_ID, "seasoning_groups/" + path));
	}
}
