package com.fyre.cobblecuisine.item;

import com.fyre.cobblecuisine.CobbleCuisine;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

// THIS ENUM CONTAINS UNUSED ITEMS MARKED FOR DELETION
public enum LegacyItemType {
	PEPPER_STEAK("pepper_steak"),
	CEVICHE("ceviche"),

	SWEET_POTATO_SANDWICH("sweet_potato_salad_sandwich"),
	BITTER_VARIETY_SANDWICH("bitter_variety_sandwich"),

	REGULAR_JEWEL("regular_jewel_shake"),
	REGULAR_EARTHY("regular_earthy_shake"),
	REGULAR_VIOLET("regular_violet_shake"),
	REGULAR_VERDANT("regular_verdant_shake"),
	REGULAR_CORAL("regular_coral_shake"),
	REGULAR_BB("regular_bb_shake"),

	KANTONIAN_CREPE("kantonian_crepe"),
	ALOLAN_BLUE_SHAVED_ICE("alolan_blue_shaved_ice"),
	PICKLED_TOEDSCOOL_AND_CUCUMBER("pickled_toedscool_and_cucumber"),
	HOENNIAN_MELON_STIR_FRY("hoennian_melon_stir_fry");

	public final String id;
	public final Item item;

	LegacyItemType(String id) {
		this.id = id;
		this.item = new Item(new Item.Settings());
	}

	public void register() {
		Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, id), item);
	}

	public static void registerAll() {
		for (LegacyItemType type : values()) {
			type.register();
		}
	}
}
