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

public enum DrinkType {
	MILTANK_MIX_AU_LAIT("miltank_mix_au_lait", effect(CobbleCuisineEffects.HP_YIELD.entry)),
	PROTEIN_SMOOTHIE("protein_smoothie", effect(CobbleCuisineEffects.ATK_YIELD.entry)),
	COFFEE("coffee", effect(CobbleCuisineEffects.DEF_YIELD.entry)),
	FRUIT_PUNCH("fruit_punch", effect(CobbleCuisineEffects.SPA_YIELD.entry)),
	LILLIGANT_FLORAL_TEA("lilligant_floral_tea", effect(CobbleCuisineEffects.SPD_YIELD.entry)),
	LEMON_SODA("lemon_soda", effect(CobbleCuisineEffects.SPE_YIELD.entry));


	public final String id;
	public final Item item;

	DrinkType(String id, CobbleCuisineItems.FoodEffect ...foodEffects) {
		this.id = id;
		this.item = new DrinkItem(id, CobbleCuisineItems.buildFoodComponent(6, 0.5f, false, foodEffects));
	}

	private static CobbleCuisineItems.FoodEffect effect(RegistryEntry<StatusEffect> effect) {
		return new CobbleCuisineItems.FoodEffect(effect, CobbleCuisineConfig.data.effectDuration.yieldBoostEffectDuration);
	}

	public void register() {
		Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, id), item);
	}

	public static void registerAll() {
		for (DrinkType type : values()) {
			type.register();
		}
	}
}
