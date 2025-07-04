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

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public class CobbleCuisineLootInjector {
	private static final Identifier GRASS_BLOCK_ID = Identifier.of("minecraft", "blocks/short_grass");

	private static final Item[] BEAN_MAP = new Item[] {
			BeanType.ORANGE.item,
			BeanType.GREEN.item,
			BeanType.INDIGO.item,
			BeanType.YELLOW.item,
			BeanType.RED.item,
			BeanType.BLUE.item,
			BeanType.VIOLET.item
	};

	@SuppressWarnings("unchecked")
	private static final RegistryKey<LootTable>[] LOOT_TABLES = new RegistryKey[] {
			LootTables.VILLAGE_DESERT_HOUSE_CHEST,
			LootTables.VILLAGE_PLAINS_CHEST,
			LootTables.VILLAGE_SAVANNA_HOUSE_CHEST,
			LootTables.VILLAGE_TAIGA_HOUSE_CHEST,
			LootTables.VILLAGE_SNOWY_HOUSE_CHEST
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
		for (int i = 0; i < LOOT_TABLES.length; i++) {
			if (LOOT_TABLES[i] != key) continue;
			addDrop(table, CobblemonItems.GALARICA_NUTS, CobbleCuisineConfig.data.dropRates.galaricaNutDropRate, 1, 3);
			addDrop(table, Items.TORCHFLOWER, CobbleCuisineConfig.data.dropRates.torchflowerDropRate, 1, 3);
			addDrop(table, Items.TORCHFLOWER_SEEDS, CobbleCuisineConfig.data.dropRates.torchflowerSeedsDropRate, 1, 3);
			//noinspection ForLoopReplaceableByForEach
			for (int j = 0; j < BEAN_MAP.length; j++) addDrop(table, BEAN_MAP[j], CobbleCuisineConfig.data.dropRates.beanDropRate, 2, 5);
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
}
