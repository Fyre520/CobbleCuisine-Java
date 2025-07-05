package com.fyre.cobblecuisine;

import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawnerFactory;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.fyre.cobblecuisine.event.ExpGainPreEvent;
import com.fyre.cobblecuisine.event.PkmCatchRateEvent;
import com.fyre.cobblecuisine.event.ShinySpawnEvent;
import com.fyre.cobblecuisine.influence.*;
import com.fyre.cobblecuisine.item.food.BeanType;
import com.fyre.cobblecuisine.item.CobbleCuisineItems;
import com.fyre.cobblecuisine.loot.CobbleCuisineLootInjector;
import com.fyre.cobblecuisine.random.PRNG;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobbleCuisine implements ModInitializer {
	public static final String MOD_ID = "cobblecuisine";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// DO NOT LEAVE THIS ON TRUE IN PRODUCTION!!
	public static final boolean DEBUG = false;

	@Override
	public void onInitialize() {
		long timer = System.nanoTime();
		if (DEBUG) LOGGER.info("CobbleCuisine >> DEBUG ENABLED!");

		// LOAD CONFIG
		CobbleCuisineConfig.load();

		// REGISTER ITEMS
		CobbleCuisineEffects.register();
		CobbleCuisineItems.register();

		// INJECT LOOT
		CobbleCuisineLootInjector.register();

		// REGISTER CALLBACKS
		ShinySpawnEvent.register();
		ExpGainPreEvent.register();
		PkmCatchRateEvent.register();

		// REGISTER INFLUENCES
		PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders().add(TypeInfluence::new);
		PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders().add(EggGroupInfluence::new);
		PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders().add(TeraInfluence::new);
		PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders().add(NatureInfluence::new);
		PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders().add(ScaleInfluence::new);
		PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders().add(YieldInfluence::new);
		PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders().add(StatInfluence::new);
		PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders().add(DubiousInfluence::new);
		PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders().add(HiddenAbilityInfluence::new);



		// COMPOSTING (?)
		CompostingChanceRegistry.INSTANCE.add(BeanType.YELLOW.item, 0.5f);
		CompostingChanceRegistry.INSTANCE.add(BeanType.RED.item, 0.5f);
		CompostingChanceRegistry.INSTANCE.add(BeanType.BLUE.item, 0.5f);
		CompostingChanceRegistry.INSTANCE.add(BeanType.ORANGE.item, 0.5f);
		CompostingChanceRegistry.INSTANCE.add(BeanType.GREEN.item, 0.5f);
		CompostingChanceRegistry.INSTANCE.add(CobbleCuisineItems.BEAN_SEEDS, 0.25f);

		// INIT PRNG
		ServerLifecycleEvents.SERVER_STARTED.register(server -> PRNG.server = server);

		LOGGER.info("CobbleCuisine >> Up and running! Time spent: {}ms", String.format("%.2f", (System.nanoTime() - timer) / 1_000_000.0));
	}
}
