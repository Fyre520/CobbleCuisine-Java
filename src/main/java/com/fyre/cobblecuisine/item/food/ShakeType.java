package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.item.CobbleCuisineItems;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public enum ShakeType {
	LOW_GREEN("low_green_shake", 1, Stats.HP),
	LOW_RED("low_red_shake", 1, Stats.ATTACK),
	LOW_PURPLE("low_purple_shake", 1, Stats.DEFENCE),
	LOW_YELLOW("low_yellow_shake", 1, Stats.SPECIAL_ATTACK),
	LOW_BLUE("low_blue_shake", 1, Stats.SPECIAL_DEFENCE),
	LOW_PINK("low_pink_shake", 1, Stats.SPEED),

	MEDIUM_GREEN("medium_green_shake", 2, Stats.HP),
	MEDIUM_RED("medium_red_shake", 2, Stats.ATTACK),
	MEDIUM_PURPLE("medium_purple_shake", 2, Stats.DEFENCE),
	MEDIUM_YELLOW("medium_yellow_shake", 2, Stats.SPECIAL_ATTACK),
	MEDIUM_BLUE("medium_blue_shake", 2, Stats.SPECIAL_DEFENCE),
	MEDIUM_PINK("medium_pink_shake", 2, Stats.SPEED),

	HIGH_GREEN("high_green_shake", 3, Stats.HP),
	HIGH_RED("high_red_shake", 3, Stats.ATTACK),
	HIGH_PURPLE("high_purple_shake", 3, Stats.DEFENCE),
	HIGH_YELLOW("high_yellow_shake", 3, Stats.SPECIAL_ATTACK),
	HIGH_BLUE("high_blue_shake", 3, Stats.SPECIAL_DEFENCE),
	HIGH_PINK("high_pink_shake", 3, Stats.SPEED);

	public final String id;
	public final Item item;

	ShakeType(String id, int tier, Stat stat) {
		this.id = id;
		this.item = new ShakeItem(id, stat, deriveEv(tier), CobbleCuisineItems.buildFoodComponent(deriveNutrition(tier), 0.5f, false));
	}

	private static CobbleCuisineItems.FoodEffect effect(RegistryEntry<StatusEffect> effect) {
		return new CobbleCuisineItems.FoodEffect(effect, 200);
	}

	private static int deriveNutrition(int tier) {
		return switch (tier) {
			case 1 -> 2;
			case 2 -> 4;
			case 3 -> 8;
			default -> 1;
		};
	}

	private static int deriveEv(int tier) {
		int power = Math.max(0, Math.min(2, CobbleCuisineConfig.data.boostSettings.shakeEvPower));
		return switch (tier) {
			case 1 -> 4 + 2 * power;
			case 2 -> 6 + 2 * power;
			case 3 -> 8 + 2 * power;
			default -> 1;
		};
	}

	public void register() {
		Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, id), item);
	}

	public static void registerAll() {
		for (ShakeType type : values()) {
			type.register();
		}
	}
}
