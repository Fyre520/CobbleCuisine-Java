package com.fyre.cobblecuisine.item.food;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.fyre.cobblecuisine.item.CobbleCuisineItems;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public enum SaladType {
	SPORE_MUSHROOM("spore_mushroom_salad", effect(CobbleCuisineEffects.SASSY.entry)),
	SNOW_CLOAK_CAESAR("snow_cloak_caesar_salad", effect(CobbleCuisineEffects.TIMID.entry)),
	GLUTTONY_POTATO("gluttony_potato_salad", effect(CobbleCuisineEffects.LAX.entry)),
	SUPERPOWER_EXTREME("superpower_extreme_salad", effect(CobbleCuisineEffects.BRAVE.entry)),
	MOOMOO_CAPRESE("moomoo_caprese_salad", effect(CobbleCuisineEffects.GENTLE.entry)),
	CONTRARY_CHOCOLATE_MEAT("contrary_chocolate_meat_salad", effect(CobbleCuisineEffects.IMPISH.entry)),
	OVERHEAT_GINGER("overheat_ginger_salad", effect(CobbleCuisineEffects.RASH.entry)),
	CALM_MIND_FRUIT("calm_mind_fruit_salad", effect(CobbleCuisineEffects.CALM.entry));

	public final String id;
	public final Item item;

	SaladType(String id, CobbleCuisineItems.FoodEffect... foodEffects) {
		this.id = id;
		this.item = new SaladItem(id, CobbleCuisineItems.buildFoodComponent(10, 1f, false, foodEffects));
	}

	private static CobbleCuisineItems.FoodEffect effect(RegistryEntry<StatusEffect> effect) {
		return new CobbleCuisineItems.FoodEffect(effect, CobbleCuisineConfig.data.effectDuration.natureBoostEffectDuration);
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
