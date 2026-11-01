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

public enum FoodType {
	ECLAIR("eclair", 6, 0.5f, effect(CobbleCuisineEffects.GIANT.entry, CobbleCuisineConfig.data.effectDuration.scaleBoostEffectDuration)),
	FRUITY_FLAN("fruity_flan", 6, 0.5f, effect(CobbleCuisineEffects.TINY.entry, CobbleCuisineConfig.data.effectDuration.scaleBoostEffectDuration)),
	DUBIOUS_FOOD("dubious_food", 2, 0.15f, effect(CobbleCuisineEffects.DUBIOUS.entry, CobbleCuisineConfig.data.effectDuration.dubiousFoodEffectDuration)),
	CANDIED_RAZZ_BERRY("candied_razz_berry", 6, 0.5f, effect(CobbleCuisineEffects.CATCH_BOOST.entry, CobbleCuisineConfig.data.effectDuration.catchRateEffectDuration)),
	CANDIED_NANAB_BERRY("candied_nanab_berry", 6, 0.5f, effect(CobbleCuisineEffects.SHINY.entry, CobbleCuisineConfig.data.effectDuration.shinyBoostEffectDuration)),
	CANDIED_PINAP_BERRY("candied_pinap_berry", 6, 0.5f, effect(CobbleCuisineEffects.EXP_BOOST.entry, CobbleCuisineConfig.data.effectDuration.expBoostEffectDuration)),
	CANDIED_BLUK_BERRY("candied_bluk_berry", 6, 0.5f, effect(CobbleCuisineEffects.IV_MODIFY.entry, CobbleCuisineConfig.data.effectDuration.statBoostEffectDuration)),
	CEVICHE("ceviche", 6, 0.5f, false);




	public final String id;
	public final Item item;
	private final boolean active;

	FoodType(String id, int nutrition, float saturation, CobbleCuisineItems.FoodEffect ...effect) {
		this(id, nutrition, saturation, true, effect);
	}

	FoodType(String id, int nutrition, float saturation, boolean active, CobbleCuisineItems.FoodEffect ...effect) {
		this.id = id;
		this.active = active;
		this.item = new FoodItem(id, CobbleCuisineItems.buildFoodComponent(nutrition, saturation, false, effect), active);
	}

	public boolean active() {
		return active;
	}

	public static java.util.List<FoodType> activeValues() {
		return java.util.Arrays.stream(values()).filter(FoodType::active).toList();
	}

	private static CobbleCuisineItems.FoodEffect effect(RegistryEntry<StatusEffect> effect, int duration) {
		return new CobbleCuisineItems.FoodEffect(effect, duration);
	}

	public void register() {
		Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, id), item);
	}

	public static void registerAll() {
		for (FoodType type : values()) {
			type.register();
		}
	}
}
