package com.fyre.cobblecuisine.item.food;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.fyre.cobblecuisine.item.CobbleCuisineItems;
import com.fyre.cobblecuisine.meal.MealEffectType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum SaladType {
	MIXED_VEGETABLE("mixed_vegetable_salad"),
	PUMPKIN_PIE("pumpkin_pie_salad"),
	SLOWPOKE_TAIL_PEPPER("slowpoke_tail_pepper_salad"),
	SPORE_MUSHROOM("spore_mushroom_salad"),
	SNOW_CLOAK_CAESAR("snow_cloak_caesar_salad"),
	GLUTTONY_POTATO("gluttony_potato_salad"),
	WATER_VEIL_TOFU("water_veil_tofu_salad"),
	SUPERPOWER_EXTREME("superpower_extreme_salad", MealEffectType.ALPHA_FREQUENCY_INCREASE),
	BEAN_HAM("bean_ham_salad"),
	SNOOZY_TOMATO("snoozy_tomato_salad", MealEffectType.HERD_FREQUENCY_DECREASE),
	MOOMOO_CAPRESE("moomoo_caprese_salad", MealEffectType.HERD_FREQUENCY_INCREASE),
	CONTRARY_CHOCOLATE_MEAT("contrary_chocolate_meat_salad"),
	OVERHEAT_GINGER("overheat_ginger_salad"),
	FANCY_APPLE("fancy_apple_salad"),
	IMMUNITY_LEEK("immunity_leek_salad"),
	DAZZLING_APPLE_CHEESE("dazzling_apple_cheese_salad"),
	NINJA("ninja_salad"),
	HEAT_WAVE_TOFU("heat_wave_tofu_salad"),
	GREENGRASS("greengrass_salad"),
	CALM_MIND_FRUIT("calm_mind_fruit_salad", MealEffectType.ALPHA_FREQUENCY_DECREASE),
	FURY_ATTACK_CORN("fury_attack_corn_salad"),
	CROSS_CHOP("cross_chop_salad"),
	DEFIANT_COFFEE_DRESSED("defiant_coffee_dressed_salad"),
	PETAL_BLIZZARD_LAYERED("petal_blizzard_layered_salad"),
	APPLE_ACID_YOGHURT_DRESSED("apple_acid_yoghurt_salad");

	public final String id;
	public Item item;
	private final MealEffectType effectType;

	SaladType(String id) {
		this(id, null);
	}

	SaladType(String id, MealEffectType effectType) {
		this.id = id;
		this.effectType = effectType;
	}

	public boolean active() {
		return effectType != null;
	}

	public Optional<MealEffectType> effectType() {
		return Optional.ofNullable(effectType);
	}

	public int nutrition() {
		return 10;
	}

	public float saturation() {
		return 1f;
	}

	public boolean activatesMeal() {
		return active();
	}

	public static List<SaladType> activeValues() {
		return Arrays.stream(values()).filter(SaladType::active).toList();
	}

	public void register() {
		RegistryEntry<StatusEffect> effect = active() ? effectEntry() : null;
		CobbleCuisineItems.FoodEffect[] effects = effect == null ? new CobbleCuisineItems.FoodEffect[0]
				: new CobbleCuisineItems.FoodEffect[]{new CobbleCuisineItems.FoodEffect(effect,
				CobbleCuisineConfig.data.effectDuration.satiatedEffectDuration)};
		item = new SaladItem(id, CobbleCuisineItems.buildFoodComponent(nutrition(), saturation(), false, effects), activatesMeal());
		Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, id), item);
	}

	private RegistryEntry<StatusEffect> effectEntry() {
		return switch (effectType) {
			case HERD_FREQUENCY_INCREASE -> CobbleCuisineEffects.HERD_FREQUENCY_INCREASE.entry;
			case HERD_FREQUENCY_DECREASE -> CobbleCuisineEffects.HERD_FREQUENCY_DECREASE.entry;
			case ALPHA_FREQUENCY_INCREASE -> CobbleCuisineEffects.ALPHA_FREQUENCY_INCREASE.entry;
			case ALPHA_FREQUENCY_DECREASE -> CobbleCuisineEffects.ALPHA_FREQUENCY_DECREASE.entry;
			default -> throw new IllegalStateException("Unsupported Salad effect: " + effectType);
		};
	}

	public static void registerAll() {
		for (var type : values()) type.register();
	}
}
