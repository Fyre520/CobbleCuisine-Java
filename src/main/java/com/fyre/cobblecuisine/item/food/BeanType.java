package com.fyre.cobblecuisine.item.food;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.item.CobbleCuisineItems;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public enum BeanType {
	RED("red_bean", effect(StatusEffects.REGENERATION)),
	BLUE("blue_bean", effect(StatusEffects.REGENERATION)),
	ORANGE("orange_bean", effect(StatusEffects.REGENERATION)),
	GREEN("green_bean", effect(StatusEffects.REGENERATION)),
	YELLOW("yellow_bean", effect(StatusEffects.REGENERATION)),
	VIOLET("violet_bean", effect(StatusEffects.REGENERATION)),
	INDIGO("indigo_bean", effect(StatusEffects.REGENERATION));



	public final String id;
	public final Item item;

	BeanType(String id, CobbleCuisineItems.FoodEffect... foodEffects) {
		this.id = id;
		this.item = new BeanItem(id, CobbleCuisineItems.buildFoodComponent(3, 0.25f, true, foodEffects));
	}

	private static CobbleCuisineItems.FoodEffect effect(RegistryEntry<StatusEffect> effect) {
		return new CobbleCuisineItems.FoodEffect(effect, 200);
	}

	public void register() {
		Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, id), item);
	}

	public static void registerAll() {
		for (BeanType type : values()) {
			type.register();
		}
	}
}
