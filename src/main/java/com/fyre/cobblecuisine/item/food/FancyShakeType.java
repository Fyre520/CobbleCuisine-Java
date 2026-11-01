package com.fyre.cobblecuisine.item.food;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.item.CobbleCuisineItems;
import com.fyre.cobblecuisine.item.food.ShakeBlendMath.Colour;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public enum FancyShakeType {
	DELUXE_JEWEL("deluxe_jewel_shake", 1, Colour.GREEN, Colour.PINK, Colour.RED, Colour.BLUE, Colour.PURPLE),
	DELUXE_EARTHY("deluxe_earthy_shake", 2, Colour.PURPLE, Colour.GREEN, Colour.RED, Colour.YELLOW),
	DELUXE_VIOLET("deluxe_violet_shake", 6, Colour.BLUE, Colour.PINK, Colour.PURPLE),
	DELUXE_VERDANT("deluxe_verdant_shake", 4, Colour.BLUE, Colour.YELLOW, Colour.GREEN),
	DELUXE_CORAL("deluxe_coral_shake", 3, Colour.PINK, Colour.RED, Colour.YELLOW),
	DELUXE_BB("deluxe_bb_shake", 5,
			Colour.GREEN, Colour.RED, Colour.PURPLE, Colour.YELLOW, Colour.BLUE, Colour.PINK);

	public final String id;
	public final Item item;

	FancyShakeType(String id, int type, Colour... blendColours) {
		this.id = id;
		this.item = new FancyShakeItem(id, type, CobbleCuisineItems.buildFoodComponent(5, 1f, false), blendColours);
	}

	private static CobbleCuisineItems.FoodEffect effect(RegistryEntry<StatusEffect> effect) {
		return new CobbleCuisineItems.FoodEffect(effect, 200);
	}

	public void register() {
		Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, id), item);
	}

	public static void registerAll() {
		for (var type : values()) {
			type.register();
		}
	}
}
