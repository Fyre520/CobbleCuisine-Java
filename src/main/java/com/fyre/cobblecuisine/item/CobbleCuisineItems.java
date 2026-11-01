package com.fyre.cobblecuisine.item;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.block.CobbleCuisineBlocks;

import com.fyre.cobblecuisine.item.food.*;
import com.fyre.cobblecuisine.item.food.FancyShakeType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.block.Block;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public class CobbleCuisineItems {
	public static final RegistryKey<ItemGroup> COBBLECUISINE_FOODS = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(CobbleCuisine.MOD_ID, "cobblecuisine_foods"));

	public record FoodEffect(RegistryEntry<StatusEffect> effect, int duration) { }
	public static FoodComponent buildFoodComponent(int nutrition, float saturation, boolean snack, FoodEffect... foodEffects) {
		FoodComponent.Builder b = new FoodComponent.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible();
		if (snack) b.snack();
		if (foodEffects != null) {
			//noinspection ForLoopReplaceableByForEach
			for (int i = 0; i < foodEffects.length; i++) {
				FoodEffect e = foodEffects[i];
				if (e != null && e.effect() != null) b.statusEffect(new StatusEffectInstance(e.effect(), e.duration()), 1.0f);
			}
		}
		return b.build();
	}

	// THOSE ARE ONE-OFFS THAT I DON'T WANT TO PUT IN A SPECIFIC ENUM
	public static Item BEAN_SEEDS;
	public static Item SPICE_MIX;
	public static Item SALAD_MIX;

	public static Item MIXER_RED;
	public static Item MIXER_BLUE;
	public static Item MIXER_GREEN;
	public static Item MIXER_YELLOW;
	public static Item MIXER_PINK;
	public static Item MIXER_WHITE;
	public static Item MIXER_BLACK;

	public static void register() {
		LOGGER.info("CobbleCuisine >> Registering Items...");
		List<Item> items = new ArrayList<>();
		BEAN_SEEDS = Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, "bean_seeds"), new AliasedBlockItem(CobbleCuisineBlocks.BEAN_CROP_BLOCK, new Item.Settings()));
		SPICE_MIX = Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, "spice_mix"), new Item(new Item.Settings()));
		SALAD_MIX = Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, "salad_mix"), new Item(new Item.Settings()));
		MIXER_RED = registerBlockItem("mixer_red", CobbleCuisineBlocks.MIXER_RED);
		MIXER_BLUE = registerBlockItem("mixer_blue", CobbleCuisineBlocks.MIXER_BLUE);
		MIXER_GREEN = registerBlockItem("mixer_green", CobbleCuisineBlocks.MIXER_GREEN);
		MIXER_YELLOW = registerBlockItem("mixer_yellow", CobbleCuisineBlocks.MIXER_YELLOW);
		MIXER_PINK = registerBlockItem("mixer_pink", CobbleCuisineBlocks.MIXER_PINK);
		MIXER_WHITE = registerBlockItem("mixer_white", CobbleCuisineBlocks.MIXER_WHITE);
		MIXER_BLACK = registerBlockItem("mixer_black", CobbleCuisineBlocks.MIXER_BLACK);

		BeanType.registerAll();
		DrinkType.registerAll();
		ShakeType.registerAll();
		FancyShakeType.registerAll();
		CurryType.registerAll();
		CakeType.registerAll();
		MalasadaType.registerAll();
		PokePuffType.registerAll();
		SandwichType.registerAll();
		SaladType.registerAll();
		SauceType.registerAll();
		RoastedBerryType.registerAll();
		FoodType.registerAll();
		LegacyItemType.registerAll();

		// CREATIVE TAB ORDERING
		items.add(BEAN_SEEDS);

		for (var type : BeanType.values()) items.add(type.item);

		addSauces(items);
		addSalads(items);

		for (var type : FoodType.activeValues()) items.add(type.item);
		for (var type : DrinkType.values()) items.add(type.item);
		for (var type : CurryType.values()) items.add(type.item);
		for (var type : CakeType.values()) items.add(type.item);
		for (var type : MalasadaType.values()) items.add(type.item);
		for (var type : PokePuffType.values()) items.add(type.item);
		for (var type : SandwichType.values()) items.add(type.item);
		for (var type : ShakeType.values()) items.add(type.item);
		for (var type : FancyShakeType.values()) items.add(type.item);
		for (var type : RoastedBerryType.values()) items.add(type.item);

		items.add(MIXER_RED);
		items.add(MIXER_BLUE);
		items.add(MIXER_GREEN);
		items.add(MIXER_YELLOW);
		items.add(MIXER_PINK);
		items.add(MIXER_WHITE);
		items.add(MIXER_BLACK);

		Registry.register(Registries.ITEM_GROUP, COBBLECUISINE_FOODS.getValue(), FabricItemGroup.builder()
						.displayName(Text.translatable("itemgroup.cobblecuisine"))
						.icon(() -> new ItemStack(CurryType.BEAN_MEDLEY.item))
						.entries((context, entries) -> items.forEach(entries::add))
						.build()
		);
	}

	private static void addSauces(List<Item> items) {
		items.add(SauceType.SWEET_HONEY.item);
		items.add(SauceType.SWEET_FRUIT.item);
		items.add(SauceType.BITTER_MUSHROOM.item);
		items.add(SauceType.BITTER_CHOCOLATE.item);
		items.add(SauceType.SOUR_MILK.item);
		items.add(SauceType.SPICY_HONEY.item);
		items.add(SauceType.SOUR_BEAN.item);
		items.add(SauceType.SPICY_BEAN.item);
	}

	private static void addSalads(List<Item> items) {
		items.add(SaladType.MOOMOO_CAPRESE.item);
		items.add(SaladType.SNOOZY_TOMATO.item);
		items.add(SaladType.SUPERPOWER_EXTREME.item);
		items.add(SaladType.CALM_MIND_FRUIT.item);
	}

	private static Item registerBlockItem(String name, Block block) {
		return Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, name), new BlockItem(block, new Item.Settings()));
	}
}
