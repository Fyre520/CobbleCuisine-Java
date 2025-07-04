package com.fyre.cobblecuisine.loot;

import com.cobblemon.mod.common.CobblemonItems;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.item.food.BeanType;
import com.fyre.cobblecuisine.item.CobbleCuisineItems;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.*;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;

import java.util.Map;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public class CobbleCuisineLootInjector {
	private static final Identifier GRASS_BLOCK_ID = Identifier.of("minecraft", "blocks/short_grass");

	private static final Map<RegistryKey<LootTable>, Item[]> BEAN_MAP = Map.of(
			LootTables.VILLAGE_DESERT_HOUSE_CHEST, new Item[] { BeanType.ORANGE.item },
			LootTables.VILLAGE_PLAINS_CHEST, new Item[] { BeanType.GREEN.item, BeanType.INDIGO.item },
			LootTables.VILLAGE_SAVANNA_HOUSE_CHEST, new Item[] { BeanType.YELLOW.item },
			LootTables.VILLAGE_TAIGA_HOUSE_CHEST, new Item[] { BeanType.RED.item },
			LootTables.VILLAGE_SNOWY_HOUSE_CHEST, new Item[] { BeanType.BLUE.item, BeanType.VIOLET.item }
	);

	private static final Item[] COMMON_VILLAGE_ITEMS = new Item[] {
			CobblemonItems.GALARICA_NUTS,
			Items.TORCHFLOWER,
			Items.TORCHFLOWER_SEEDS
	};

	private CobbleCuisineLootInjector() {}

	public static void register() {
		LOGGER.info("CobbleCuisine >> Injecting Loot Tables...");
		LootTableEvents.MODIFY.register(CobbleCuisineLootInjector::onLootTableModify);
	}

	private static void onLootTableModify(RegistryKey<LootTable> key, LootTable.Builder table, LootTableSource source, WrapperLookup registries) {
		if (key.getValue().equals(GRASS_BLOCK_ID)) {
			addDrop(table, CobbleCuisineItems.BEAN_SEEDS, CobbleCuisineConfig.data.dropRates.beanSeedsDropRate, 1, 1);
			return;
		}

		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < COMMON_VILLAGE_ITEMS.length; i++) {
			Item item = COMMON_VILLAGE_ITEMS[i];
			addDrop(table, item, getDropRate(item), 1, 3);
		}

		Item[] beans = BEAN_MAP.get(key);
		if (beans == null) return;

		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < beans.length; i++) {
			addDrop(table, beans[i], CobbleCuisineConfig.data.dropRates.beanDropRate, 2, 5);
		}
	}

	private static void addDrop(LootTable.Builder table, Item item, float chance, float min, float max) {
		table.pool(LootPool.builder()
				.rolls(ConstantLootNumberProvider.create(1))
				.conditionally(RandomChanceLootCondition.builder(chance))
				.with(ItemEntry.builder(item))
				.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max)))
		);
	}

	private static float getDropRate(Item item) {
		if (item == CobblemonItems.GALARICA_NUTS) return CobbleCuisineConfig.data.dropRates.galaricaNutDropRate;
		if (item == Items.TORCHFLOWER) return CobbleCuisineConfig.data.dropRates.torchflowerDropRate;
		if (item == Items.TORCHFLOWER_SEEDS) return CobbleCuisineConfig.data.dropRates.torchflowerSeedsDropRate;
		return 0.1f;
	}
}
