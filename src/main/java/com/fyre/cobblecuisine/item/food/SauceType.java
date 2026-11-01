package com.fyre.cobblecuisine.item.food;

import com.fyre.cobblecuisine.CobbleCuisine;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public enum SauceType {
	SWEET_HONEY("sweet_honey_sauce"),
	SOUR_DUBIOUS("sour_dubious_sauce"),
	SWEET_FRUIT("sweet_fruit_sauce"),
	SOUR_MILK("sour_milk_sauce"),
	SPICY_BEAN("spicy_bean_sauce"),
	BITTER_CHOCOLATE("bitter_chocolate_sauce"),
	SPICY_HONEY("spicy_honey_sauce"),
	BITTER_MUSHROOM("bitter_mushroom_sauce"),
	SOUR_BEAN("sour_bean_sauce");

	public final String id;
	public Item item;

	SauceType(String id) {
		this.id = id;
	}

	public void register() {
		item = Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, id), new SauceItem(id, new Item.Settings().maxCount(16)));
	}

	public static void registerAll() {
		for (SauceType type : values()) type.register();
	}
}
