package com.fyre.cobblecuisine.meal;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MealEffectMappings {
	private static final Set<String> POKEMON_TYPES = Set.of(
			"normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison", "ground",
			"flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"
	);
	private static final Set<String> EGG_GROUPS = Set.of(
			"amorphous", "bug", "dragon", "fairy", "field", "flying", "grass", "humanlike", "mineral",
			"monster", "undiscovered", "water1", "water23", "water3"
	);
	private static final Map<String, String> YIELD_TARGETS = Map.of(
			"hp", "hp",
			"atk", "attack",
			"def", "defence",
			"spa", "special_attack",
			"spd", "special_defence",
			"spe", "speed"
	);

	private MealEffectMappings() {
	}

	public static Optional<MealEffectEntry> fromStatusEffectId(String identifier) {
		String prefix = "cobblecuisine:";
		if (!identifier.startsWith(prefix)) return Optional.empty();
		String path = identifier.substring(prefix.length());

		if (path.startsWith("tera_") && path.endsWith("_spawn")) {
			String target = path.substring(5, path.length() - 6);
			if (POKEMON_TYPES.contains(target) || target.equals("stellar")) return entry(MealEffectType.TERA, target);
		}
		if (path.startsWith("nature_") && path.endsWith("_spawn")) {
			String target = path.substring(7, path.length() - 6);
			return entry(MealEffectType.NATURE, target.equals("serious") ? "neutral" : target);
		}
		if (path.endsWith("_eg_spawn")) {
			String target = path.substring(0, path.length() - 9);
			if (EGG_GROUPS.contains(target)) return entry(MealEffectType.EGG_GROUP, target);
		}
		if (path.endsWith("yieldspawn")) {
			String target = YIELD_TARGETS.get(path.substring(0, path.length() - 10));
			if (target != null) return entry(MealEffectType.YIELD, target);
		}
		if (path.endsWith("_spawn")) {
			String target = path.substring(0, path.length() - 6);
			if (POKEMON_TYPES.contains(target)) return entry(MealEffectType.TYPE, target);
		}
		return switch (path) {
			case "tiny_spawn" -> entry(MealEffectType.TINY, "");
			case "giant_spawn" -> entry(MealEffectType.GIANT, "");
			case "iv_modify" -> entry(MealEffectType.IV, "");
			case "shinyspawn" -> entry(MealEffectType.SHINY, "");
			case "catch_boost" -> entry(MealEffectType.CATCH, "");
			case "exp_boost" -> entry(MealEffectType.EXP, "");
			case "dubious_spawn" -> entry(MealEffectType.DUBIOUS, "");
			case "hidden_ability" -> entry(MealEffectType.HIDDEN_ABILITY, "");
			case "herd_frequency_increase" -> entry(MealEffectType.HERD_FREQUENCY_INCREASE, "");
			case "herd_frequency_decrease" -> entry(MealEffectType.HERD_FREQUENCY_DECREASE, "");
			case "duration_extend" -> entry(MealEffectType.DURATION, "");
			case "alpha_frequency_increase" -> entry(MealEffectType.ALPHA_FREQUENCY_INCREASE, "");
			case "alpha_frequency_decrease" -> entry(MealEffectType.ALPHA_FREQUENCY_DECREASE, "");
			default -> Optional.empty();
		};
	}

	public static Optional<MealEffectEntry> fromPrimaryStatusEffectId(String identifier) {
		return fromStatusEffectId(identifier).filter(entry ->
				entry.type() != MealEffectType.NATURE && entry.type() != MealEffectType.HIDDEN_ABILITY);
	}

	public static String translationKey(MealEffectEntry entry) {
		return "effect.cobblecuisine." + effectPath(entry);
	}

	public static String seasoningTranslationKey(MealEffectEntry entry) {
		return translationKey(entry) + ".seasoning";
	}

	public static String baseTranslationKey(MealEffectEntry entry) {
		return translationKey(entry) + ".base";
	}

	public static Set<String> targets(MealEffectType type) {
		return switch (type) {
			case TYPE -> POKEMON_TYPES;
			case EGG_GROUP -> EGG_GROUPS;
			case YIELD -> Set.copyOf(YIELD_TARGETS.values());
			default -> Set.of("");
		};
	}

	public static String statusEffectId(MealEffectEntry entry) {
		return "cobblecuisine:" + effectPath(entry);
	}

	private static String effectPath(MealEffectEntry entry) {
		return switch (entry.type()) {
			case TYPE -> entry.target() + "_spawn";
			case EGG_GROUP -> entry.target() + "_eg_spawn";
			case TERA -> "tera_" + entry.target() + "_spawn";
			case NATURE -> "nature_" + (entry.target().equals("neutral") ? "serious" : entry.target()) + "_spawn";
			case TINY -> "tiny_spawn";
			case GIANT -> "giant_spawn";
			case YIELD -> yieldEffectPath(entry.target());
			case IV -> "iv_modify";
			case SHINY -> "shinyspawn";
			case CATCH -> "catch_boost";
			case EXP -> "exp_boost";
			case DUBIOUS -> "dubious_spawn";
			case HIDDEN_ABILITY -> "hidden_ability";
			case DURATION -> "duration_extend";
			default -> entry.type().id();
		};
	}

	private static String yieldEffectPath(String target) {
		return switch (target) {
			case "hp" -> "hpyieldspawn";
			case "attack" -> "atkyieldspawn";
			case "defence" -> "defyieldspawn";
			case "special_attack" -> "spayieldspawn";
			case "special_defence" -> "spdyieldspawn";
			case "speed" -> "speyieldspawn";
			default -> target;
		};
	}

	private static Optional<MealEffectEntry> entry(MealEffectType type, String target) {
		return Optional.of(new MealEffectEntry(type, target, 1));
	}
}
