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
	MIXED_VEGETABLE("mixed_vegetable_salad", effect(CobbleCuisineEffects.BOLD.entry)),
	PUMPKIN_PIE("pumpkin_pie_salad", effect(CobbleCuisineEffects.BASHFUL.entry)),
	SLOWPOKE_TAIL_PEPPER("slowpoke_tail_pepper_salad", effect(CobbleCuisineEffects.RELAXED.entry)),
	SPORE_MUSHROOM("spore_mushroom_salad", effect(CobbleCuisineEffects.SASSY.entry)),
	SNOW_CLOAK_CAESAR("snow_cloak_caesar_salad", effect(CobbleCuisineEffects.TIMID.entry)),
	GLUTTONY_POTATO("gluttony_potato_salad", effect(CobbleCuisineEffects.LAX.entry)),
	WATER_VEIL_TOFU("water_veil_tofu_salad", effect(CobbleCuisineEffects.SERIOUS.entry)),
	SUPERPOWER_EXTREME("superpower_extreme_salad", effect(CobbleCuisineEffects.BRAVE.entry)),
	BEAN_HAM("bean_ham_salad", effect(CobbleCuisineEffects.DOCILE.entry)),
	SNOOZY_TOMATO("snoozy_tomato_salad", effect(CobbleCuisineEffects.MILD.entry)),
	MOOMOO_CAPRESE("moomoo_caprese_salad", effect(CobbleCuisineEffects.GENTLE.entry)),
	CONTRARY_CHOCOLATE_MEAT("contrary_chocolate_meat_salad", effect(CobbleCuisineEffects.IMPISH.entry)),
	OVERHEAT_GINGER("overheat_ginger_salad", effect(CobbleCuisineEffects.RASH.entry)),
	FANCY_APPLE("fancy_apple_salad", effect(CobbleCuisineEffects.JOLLY.entry)),
	IMMUNITY_LEEK("immunity_leek_salad", effect(CobbleCuisineEffects.CAREFUL.entry)),
	DAZZLING_APPLE_CHEESE("dazzling_apple_cheese_salad", effect(CobbleCuisineEffects.NAIVE.entry)),
	NINJA("ninja_salad", effect(CobbleCuisineEffects.HASTY.entry)),
	HEAT_WAVE_TOFU("heat_wave_tofu_salad", effect(CobbleCuisineEffects.HARDY.entry)),
	GREENGRASS("greengrass_salad", effect(CobbleCuisineEffects.QUIET.entry)),
	CALM_MIND_FRUIT("calm_mind_fruit_salad", effect(CobbleCuisineEffects.CALM.entry)),
	FURY_ATTACK_CORN("fury_attack_corn_salad", effect(CobbleCuisineEffects.ADAMANT.entry)),
	CROSS_CHOP("cross_chop_salad", effect(CobbleCuisineEffects.LONELY.entry)),
	DEFIANT_COFFEE_DRESSED("defiant_coffee_dressed_salad", effect(CobbleCuisineEffects.NAUGHTY.entry)),
	PETAL_BLIZZARD_LAYERED("petal_blizzard_layered_salad", effect(CobbleCuisineEffects.MODEST.entry)),
	APPLE_ACID_YOGHURT_DRESSED("apple_acid_yoghurt_salad", effect(CobbleCuisineEffects.QUIRKY.entry));

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
