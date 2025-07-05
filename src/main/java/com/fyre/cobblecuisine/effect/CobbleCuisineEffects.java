package com.fyre.cobblecuisine.effect;

import com.fyre.cobblecuisine.CobbleCuisine;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public enum CobbleCuisineEffects {
	// —— Tera Spawns ——
	TERA_GRASS("tera_grass_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x98D982)),
	TERA_WATER("tera_water_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x0000FF)),
	TERA_FIRE("tera_fire_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF0000)),
	TERA_ELECTRIC("tera_electric_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFFF00)),
	TERA_BUG("tera_bug_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x9ACD32)),
	TERA_NORMAL("tera_normal_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x98D981)),
	TERA_POISON("tera_poison_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x800080)),
	TERA_ICE("tera_ice_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x00FFFF)),
	TERA_FLYING("tera_flying_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x87CEEB)),
	TERA_FIGHTING("tera_fighting_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x8B0000)),
	TERA_FAIRY("tera_fairy_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFB6C1)),
	TERA_PSYCHIC("tera_psychic_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF69B4)),
	TERA_GHOST("tera_ghost_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x4B0082)),
	TERA_DARK("tera_dark_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x2F4F4F)),
	TERA_ROCK("tera_rock_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xA0522D)),
	TERA_STEEL("tera_steel_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x708090)),
	TERA_GROUND("tera_ground_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xD2B48C)),
	TERA_DRAGON("tera_dragon_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x4169E1)),
	TERA_STELLAR("tera_stellar_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),

	// —— Boosts ——
	EXP_BOOST("exp_boost", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	CATCH_BOOST("catch_boost", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),

	// —— Egg Spawns ——
	AMORPHOUS_EG("amorphous_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	FAIRY_EG("fairy_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	BUG_EG("bug_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	DRAGON_EG("dragon_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	FIELD_EG("field_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	FLYING_EG("flying_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	GRASS_EG("grass_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	HUMANLIKE_EG("humanlike_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	MINERAL_EG("mineral_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	MONSTER_EG("monster_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	UNDISCOVERED_EG("undiscovered_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	WATER1_EG("water1_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	WATER23_EG("water23_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	WATER3_EG("water3_eg_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),

	// —— Misc ——
	DUBIOUS("dubious_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),

	// —— Size & Yield ——
	TINY("tiny_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	GIANT("giant_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	SHINY("shinyspawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	HIDDEN_ABILITY("hidden_ability", () -> new BasicStatusEffect(StatusEffectCategory.NEUTRAL, 0x000000)),
	ATK_YIELD("atkyieldspawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	DEF_YIELD("defyieldspawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	SPA_YIELD("spayieldspawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	SPD_YIELD("spdyieldspawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	SPE_YIELD("speyieldspawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	HP_YIELD("hpyieldspawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	IV_MODIFY("iv_modify", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),

	// —— Basic Spawns ——
	GRASS("grass_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x98D982)),
	NORMAL("normal_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x98D981)),
	FIRE("fire_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF0000)),
	WATER("water_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x0000FF)),
	ELECTRIC("electric_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFFF00)),
	ICE("ice_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x00FFFF)),
	FIGHTING("fighting_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x8B0000)),
	POISON("poison_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x800080)),
	GROUND("ground_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xD2B48C)),
	FLYING("flying_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x87CEEB)),
	PSYCHIC("psychic_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF69B4)),
	BUG("bug_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x9ACD32)),
	ROCK("rock_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xA0522D)),
	GHOST("ghost_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x4B0082)),
	DRAGON("dragon_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x4169E1)),
	DARK("dark_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x2F4F4F)),
	STEEL("steel_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x708090)),
	FAIRY("fairy_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFB6C1)),

	// —— Nature Spawns ——
	SERIOUS("nature_serious_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xA9A9A9)),
	HARDY("nature_hardy_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)),
	LONELY("nature_lonely_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF4500)),
	BOLD("nature_bold_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x4682B4)),
	TIMID("nature_timid_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x87CEEB)),
	HASTY("nature_hasty_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF6347)),
	JOLLY("nature_jolly_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x32CD32)),
	NAIVE("nature_naive_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFDAB9)),
	MODEST("nature_modest_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x6495ED)),
	MILD("nature_mild_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xF08080)),
	QUIET("nature_quiet_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x8A2BE2)),
	BASHFUL("nature_bashful_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFC0CB)),
	RASH("nature_rash_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF69B4)),
	CALM("nature_calm_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x00CED1)),
	GENTLE("nature_gentle_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xF5DEB3)),
	SASSY("nature_sassy_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF00FF)),
	CAREFUL("nature_careful_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x8B4513)),
	QUIRKY("nature_quirky_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xD2691E)),
	LAX("nature_lax_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xB22222)),
	RELAXED("nature_relaxed_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x2E8B57)),
	IMPISH("nature_impish_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x556B2F)),
	ADAMANT("nature_adamant_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xDC143C)),
	DOCILE("nature_docile_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xF0E68C)),
	BRAVE("nature_brave_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0x98D982)),
	NAUGHTY("nature_naughty_spawn", () -> new BasicStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF4500)),

	// —— Dummy Markers ——
	TYPE_BUFF_MARKER("type_buff_marker", () -> new BasicStatusEffect(StatusEffectCategory.NEUTRAL, 0x000000)),
	EGG_BUFF_MARKER("egg_buff_marker", () -> new BasicStatusEffect(StatusEffectCategory.NEUTRAL, 0x000000)),
	YIELD_BUFF_MARKER("yield_buff_marker", () -> new BasicStatusEffect(StatusEffectCategory.NEUTRAL, 0x000000)),
	TERA_BUFF_MARKER("tera_buff_marker", () -> new BasicStatusEffect(StatusEffectCategory.NEUTRAL, 0x000000)),
	NATURE_BUFF_MARKER("nature_buff_marker", () -> new BasicStatusEffect(StatusEffectCategory.NEUTRAL, 0x000000));

	public final RegistryEntry<StatusEffect> entry;

	CobbleCuisineEffects(String id, Supplier<StatusEffect> factory) {
		StatusEffect effect = Registry.register(
				Registries.STATUS_EFFECT,
				Identifier.of(CobbleCuisine.MOD_ID, id),
				factory.get()
		);
		this.entry = Registries.STATUS_EFFECT.getEntry(effect);
	}

	public static void register() {
		LOGGER.info("CobbleCuisine >> Registering Effects...");
		//noinspection ResultOfMethodCallIgnored
		values();
	}
}
