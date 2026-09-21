package com.fyre.cobblecuisine.item;

import com.fyre.cobblecuisine.CobbleCuisine;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

// Retain registry identities for old saves; these items are not in the creative group.
public enum LegacyItemType {
	MIXED_VEGETABLE("mixed_vegetable_salad"),
	PUMPKIN_PIE("pumpkin_pie_salad"),
	SLOWPOKE_TAIL_PEPPER("slowpoke_tail_pepper_salad"),
	WATER_VEIL_TOFU("water_veil_tofu_salad"),
	BEAN_HAM("bean_ham_salad"),
	SNOOZY_TOMATO("snoozy_tomato_salad"),
	FANCY_APPLE("fancy_apple_salad"),
	IMMUNITY_LEEK("immunity_leek_salad"),
	DAZZLING_APPLE_CHEESE("dazzling_apple_cheese_salad"),
	NINJA("ninja_salad"),
	HEAT_WAVE_TOFU("heat_wave_tofu_salad"),
	GREENGRASS("greengrass_salad"),
	FURY_ATTACK_CORN("fury_attack_corn_salad"),
	CROSS_CHOP("cross_chop_salad"),
	DEFIANT_COFFEE_DRESSED("defiant_coffee_dressed_salad"),
	PETAL_BLIZZARD_LAYERED("petal_blizzard_layered_salad"),
	APPLE_ACID_YOGHURT_DRESSED("apple_acid_yoghurt_salad"),

	PEPPER_STEAK("pepper_steak"),

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
