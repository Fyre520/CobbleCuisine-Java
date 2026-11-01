package com.fyre.cobblecuisine.item.food;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.item.CobbleCuisineItems;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public enum RoastedBerryType {
	ORAN("roasted_oran_berry"),
	BABIRI("roasted_babiri_berry"),
	CHARTI("roasted_charti_berry"),
	CHILAN("roasted_chilan_berry"),
	CHOPLE("roasted_chople_berry"),
	COBA("roasted_coba_berry"),
	COLBUR("roasted_colbur_berry"),
	HABAN("roasted_haban_berry"),
	KASIB("roasted_kasib_berry"),
	KEBIA("roasted_kebia_berry"),
	OCCA("roasted_occa_berry"),
	PASSHO("roasted_passho_berry"),
	PAYAPA("roasted_payapa_berry"),
	RINDO("roasted_rindo_berry"),
	ROSELI("roasted_roseli_berry"),
	SHUCA("roasted_shuca_berry"),
	TANGA("roasted_tanga_berry"),
	WACAN("roasted_wacan_berry"),
	YACHE("roasted_yache_berry"),
	CHERI("roasted_cheri_berry"),
	CHESTO("roasted_chesto_berry"),
	PECHA("roasted_pecha_berry"),
	RAWST("roasted_rawst_berry"),
	ASPEAR("roasted_aspear_berry"),
	PERSIM("roasted_persim_berry"),
	LEPPA("roasted_leppa_berry"),
	SITRUS("roasted_sitrus_berry"),
	LUM("roasted_lum_berry"),
	ENIGMA("roasted_enigma_berry"),
	FIGY("roasted_figy_berry"),
	AGUAV("roasted_aguav_berry"),
	IAPAPA("roasted_iapapa_berry"),
	WIKI("roasted_wiki_berry"),
	CUSTAP("roasted_custap_berry"),
	KEE("roasted_kee_berry"),
	PAMTRE("roasted_pamtre_berry"),
	HOPO("roasted_hopo_berry");

	public final String id;
	public final Item item;

	RoastedBerryType(String id, CobbleCuisineItems.FoodEffect... foodEffects) {
		this.id = id;
		this.item = new RoastedBerryItem(id, CobbleCuisineItems.buildFoodComponent(5, 1f, false, foodEffects));
	}

	private static CobbleCuisineItems.FoodEffect effect(RegistryEntry<StatusEffect> effect, int duration) {
		return new CobbleCuisineItems.FoodEffect(effect, duration);
	}

	public void register() {
		Registry.register(Registries.ITEM, Identifier.of(CobbleCuisine.MOD_ID, id), item);
	}

	public static void registerAll() {
		for (RoastedBerryType type : values()) {
			type.register();
		}
	}
}
