package com.fyre.cobblecuisine.config;

import com.fyre.cobblecuisine.meal.MealEffectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

public class CobbleCuisineConfigData {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface HiddenFromEditor {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface EditorRange {
		double min();
		double max();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface EditorTicksAsSeconds {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface EditorOptions {
		String[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface EditorInline {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface ClassicOnly {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface StandardOnly {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface ClassicToggle {
	}

	public final GlobalSettings globalSettings = new GlobalSettings();
	@ClassicOnly
	public final ClassicSettings classicSettings = new ClassicSettings();
	public final BoostSettings boostSettings = new BoostSettings();
	public final ItemSettings itemSettings = new ItemSettings();
	public final EffectDuration effectDuration  = new EffectDuration();
	@HiddenFromEditor
	public final MealStrengths mealStrengths = new MealStrengths();
	@StandardOnly
	public final HerdSettings herdSettings = new HerdSettings();
	public final DropRates dropRates = new DropRates();
	@StandardOnly
	public final TypeMultipliers typeMultipliers = new TypeMultipliers();
	@StandardOnly
	public final EggGroupMultipliers eggGroupMultipliers = new EggGroupMultipliers();
	@StandardOnly
	public final YieldMultipliers yieldMultipliers = new YieldMultipliers();

	public static class GlobalSettings {
		@ClassicToggle
		public boolean classicMealPowers = false;
		public boolean allowMultipleActiveMeals = false;
		@EditorRange(min = 1, max = 4)
		public int maxActiveMeals = 3;
		public boolean mixerRequiresRedstone = true;
	}

	public static class ClassicSettings {
		public float typeMultiplier = 5.0f;
		public float typeNonMatchMultiplier = 0.5f;
		public float eggGroupMultiplier = 5.0f;
		public float eggGroupNonMatchMultiplier = 0.5f;
		public float herdFrequencyMultiplier = 5.0f;
		public float herdFrequencyNonHerdMultiplier = 0.5f;
		@EditorRange(min = 0, max = 1)
		public float teraChance = 0.75f;
		@EditorRange(min = 0, max = 1)
		public float natureChancePerMint = 0.375f;
		@EditorRange(min = 0, max = 1)
		public float hiddenAbilityChancePerSauce = 0.333f;
		public int durationSeasoningSeconds = 150;
		public float catchMultiplier = 2.0f;
		public float expMultiplier = 2.0f;
		public float shinyMultiplier = 2.0f;
		@EditorRange(min = 0, max = 31)
		public int ivMinimum = 15;
		@EditorRange(min = 0, max = 31)
		public int ivMaximum = 31;
		@EditorRange(min = 0, max = 1)
		public float tinyChance = 0.70f;
		@EditorRange(min = 0, max = 1)
		public float giantChance = 0.70f;
		@EditorRange(min = 0, max = 1)
		public float alphaIncreaseChance = 0.20f;
		@EditorRange(min = 0, max = 1)
		public float alphaDecreaseChance = 0.80f;
		public int pokePuffFriendship = 6;
		public int malasadaFriendship = 12;
		public int pokePuffHealing = 20;
		public int malasadaHealing = 20;
	}
	public static class BoostSettings {
		public float effectDistanceBlocks = 80.0f;
		@StandardOnly
		public float expBaseMultiplier = 1.5f;
		@StandardOnly
		public float expSeasoningMultiplier = 0.25f;
		@HiddenFromEditor
		public float catchRateMultiplier = 2.0f;
		@StandardOnly
		public float catchBaseMultiplier = 1.5f;
		@StandardOnly
		public float catchSeasoningMultiplier = 0.25f;
		@StandardOnly
		public float shinyBaseMultiplier = 1.5f;
		@StandardOnly
		public float shinySeasoningMultiplier = 0.25f;
		@StandardOnly
		@EditorRange(min = 0, max = 1)
		public float teraBaseChance = 0.55f;
		@StandardOnly
		@EditorRange(min = 0, max = 1)
		public float teraSeasoningChance = 0.10f;
		@StandardOnly
		@EditorRange(min = 0, max = 1)
		public float natureSeasoningChance = 0.375f;
		@StandardOnly
		@EditorRange(min = 0, max = 1)
		public float hiddenAbilitySeasoningChance = 0.333f;
		@StandardOnly
		@EditorRange(min = 0, max = 1)
		public float tinyBaseChance = 0.5f;
		@StandardOnly
		@EditorRange(min = 0, max = 1)
		public float tinySeasoningChance = 0.10f;
		@StandardOnly
		@EditorRange(min = 0, max = 1)
		public float giantBaseChance = 0.5f;
		@StandardOnly
		@EditorRange(min = 0, max = 1)
		public float giantSeasoningChance = 0.10f;
		@StandardOnly
		@EditorRange(min = 0, max = 31)
		public int ivBaseMinimum = 10;
		@StandardOnly
		@EditorRange(min = 0, max = 31)
		public int ivSeasoningMinimum = 5;
		@StandardOnly
		@EditorRange(min = 0, max = 31)
		public int ivBaseMaximum = 20;
		@StandardOnly
		@EditorRange(min = 0, max = 31)
		public int ivSeasoningMaximum = 4;
		@StandardOnly
		@EditorRange(min = 0, max = 31)
		public int ivMaxValue = 31;
		@EditorOptions({"Low (4/6/8)", "Medium (6/8/10)", "High (8/10/12)"})
		public int shakeEvPower = 0;
	}

	public static class ItemSettings {
		public int beanFriendship = 1;
		@StandardOnly
		public final FeedingAmount pokePuffFriendship = new FeedingAmount(6);
		@StandardOnly
		public final FeedingAmount malasadaFriendship = new FeedingAmount(12);
		@StandardOnly
		public final FeedingAmount pokePuffHealing = new FeedingAmount(20);
		@StandardOnly
		public final FeedingAmount malasadaHealing = new FeedingAmount(20);

		@HiddenFromEditor
		public List<JsonElement> fullnessItems = new ArrayList<>(List.of(
				new JsonPrimitive("#cobblecuisine:beans"),
				new JsonPrimitive("#cobblecuisine:poke_puffs"),
				new JsonPrimitive("#cobblecuisine:malasadas"),
				new JsonPrimitive("#cobblecuisine:regular_shakes"),
				new JsonPrimitive("#cobblecuisine:deluxe_shakes"),
				emptyExclude()
		));

		private static JsonElement emptyExclude() {
			JsonObject holder = new JsonObject();
			holder.add("exclude", new JsonArray());
			return holder;
		}
	}

	public static class EffectDuration {
		@EditorTicksAsSeconds
		public int satiatedEffectDuration = 6000;
		@EditorTicksAsSeconds
		public int shinyBoostEffectDuration = 6000;
		@EditorTicksAsSeconds
		public int expBoostEffectDuration = 6000;
		@EditorTicksAsSeconds
		public int catchRateEffectDuration = 6000;
		@EditorTicksAsSeconds
		public int typeBoostEffectDuration = 6000;
		@EditorTicksAsSeconds
		public int teraBoostEffectDuration = 6000;
		@EditorTicksAsSeconds
		public int eggBoostEffectDuration = 6000;
		@HiddenFromEditor
		public int natureBoostEffectDuration = 6000;
		@EditorTicksAsSeconds
		public int statBoostEffectDuration = 6000;
		@EditorTicksAsSeconds
		public int yieldBoostEffectDuration = 6000;
		@EditorTicksAsSeconds
		public int scaleBoostEffectDuration = 6000;
		@EditorTicksAsSeconds
		public int dubiousFoodEffectDuration = 6000;
		@StandardOnly
		public int durationSeasoningSeconds = 150;
	}

	public static class MealStrengths {
		@HiddenFromEditor
		public final Strength yield = new Strength();
		@HiddenFromEditor
		public final Strength iv = new Strength();
		@HiddenFromEditor
		public final Strength shiny = new Strength();
		@HiddenFromEditor
		public final Strength catchRate = new Strength();
		@HiddenFromEditor
		public final Strength experience = new Strength();
		@HiddenFromEditor
		public final Strength dubious = new Strength();

		public Strength get(MealEffectType type) {
			return switch (type) {
				case YIELD -> yield;
				case IV -> iv;
				case SHINY -> shiny;
				case CATCH -> catchRate;
				case EXP -> experience;
				case DUBIOUS -> dubious;
				case TINY, GIANT, DURATION, TYPE, EGG_GROUP, TERA, NATURE, HIDDEN_ABILITY, HERD_FREQUENCY_INCREASE, HERD_FREQUENCY_DECREASE,
						HERD_LEVEL_INCREASE, HERD_LEVEL_DECREASE, HERD_VARIATION_INCREASE, HERD_VARIATION_DECREASE,
						ALPHA_FREQUENCY_INCREASE, ALPHA_FREQUENCY_DECREASE -> new Strength(0F, 0F, 0F);
			};
		}
	}

	public static class Strength {
		public float direct;
		public float seasoning;
		public float cap;

		public Strength() {
			this(1.0f);
		}

		public Strength(float multiplier) {
			this(0.75f * multiplier, 0.45f * multiplier, 1.2f * multiplier);
		}

		public Strength(float direct, float seasoning, float cap) {
			this.direct = direct;
			this.seasoning = seasoning;
			this.cap = cap;
		}
	}

	public static class FeedingAmount {
		public float amount;
		public float baseMultiplier = 0.5f;
		public float perSeasoningMultiplier = 0.25f;

		public FeedingAmount(float amount) {
			this.amount = amount;
		}
	}

	public static class HerdSettings {
		public final WeightPair herdFrequency = new WeightPair();

		@EditorRange(min = 0, max = 1)
		public float alphaIncreaseBaseChance = 0.10f;

		@EditorRange(min = 0, max = 1)
		public float alphaIncreaseSeasoningChance = 0.05f;
		@EditorRange(min = 0, max = 1)
		public float alphaDecreaseBaseChance = 0.50f;
		@EditorRange(min = 0, max = 1)
		public float alphaDecreaseSeasoningChance = 0.15f;
	}

	public static class DropRates {
		@EditorRange(min = 0, max = 1)
		public float beanSeedsDropRate = 0.1f;
		@EditorRange(min = 0, max = 1)
		public float beanDropRate = 0.25f;
	}

	public static class WeightPair {
		public float spawnWeightMultiplier = 2.5f;
		public float seasoningWeightMultiplier = 0.5f;
		public float nonWeightMultiplier = 0.25f;
	}

	public static class YieldMultiplier {
		public float seasoningWeightMultiplier = 0.5f;
	}

	public static class TypeMultipliers {
		public final WeightPair bug = new WeightPair();
		public final WeightPair normal = new WeightPair();
		public final WeightPair fire = new WeightPair();
		public final WeightPair water = new WeightPair();
		public final WeightPair electric = new WeightPair();
		public final WeightPair grass = new WeightPair();
		public final WeightPair ice = new WeightPair();
		public final WeightPair fighting = new WeightPair();
		public final WeightPair poison = new WeightPair();
		public final WeightPair ground = new WeightPair();
		public final WeightPair flying = new WeightPair();
		public final WeightPair psychic = new WeightPair();
		public final WeightPair rock = new WeightPair();
		public final WeightPair ghost = new WeightPair();
		public final WeightPair dragon = new WeightPair();
		public final WeightPair steel = new WeightPair();
		public final WeightPair dark = new WeightPair();
		public final WeightPair fairy = new WeightPair();

		public WeightPair get(String target) {
			return switch (target) {
				case "normal" -> normal;
				case "fire" -> fire;
				case "water" -> water;
				case "electric" -> electric;
				case "grass" -> grass;
				case "ice" -> ice;
				case "fighting" -> fighting;
				case "poison" -> poison;
				case "ground" -> ground;
				case "flying" -> flying;
				case "psychic" -> psychic;
				case "bug" -> bug;
				case "rock" -> rock;
				case "ghost" -> ghost;
				case "dragon" -> dragon;
				case "dark" -> dark;
				case "steel" -> steel;
				case "fairy" -> fairy;
				default -> throw new IllegalArgumentException("Unknown type: " + target);
			};
		}
	}

	public static class EggGroupMultipliers {
		public final WeightPair amorphous = new WeightPair();
		public final WeightPair fairy = new WeightPair();
		public final WeightPair bug = new WeightPair();
		public final WeightPair dragon = new WeightPair();
		public final WeightPair field = new WeightPair();
		public final WeightPair flying = new WeightPair();
		public final WeightPair grass = new WeightPair();
		public final WeightPair humanLike = new WeightPair();
		public final WeightPair mineral = new WeightPair();
		public final WeightPair monster = new WeightPair();
		public final WeightPair undiscovered = new WeightPair();
		public final WeightPair water1 = new WeightPair();
		public final WeightPair water23 = new WeightPair();

		public WeightPair get(String target) {
			return switch (target) {
				case "amorphous" -> amorphous;
				case "fairy" -> fairy;
				case "bug" -> bug;
				case "dragon" -> dragon;
				case "field" -> field;
				case "flying" -> flying;
				case "grass" -> grass;
				case "humanlike" -> humanLike;
				case "mineral" -> mineral;
				case "monster" -> monster;
				case "undiscovered" -> undiscovered;
				case "water1" -> water1;
				case "water23", "water3" -> water23;
				default -> throw new IllegalArgumentException("Unknown egg group: " + target);
			};
		}
	}

	public static class YieldMultipliers {
		@EditorInline
		public final YieldMultiplier hp = new YieldMultiplier();
		@EditorInline
		public final YieldMultiplier attack = new YieldMultiplier();
		@EditorInline
		public final YieldMultiplier defence = new YieldMultiplier();
		@EditorInline
		public final YieldMultiplier specialAttack = new YieldMultiplier();
		@EditorInline
		public final YieldMultiplier specialDefence = new YieldMultiplier();
		@EditorInline
		public final YieldMultiplier speed = new YieldMultiplier();

		public YieldMultiplier get(String target) {
			return switch (target) {
				case "hp" -> hp;
				case "attack" -> attack;
				case "defence" -> defence;
				case "special_attack" -> specialAttack;
				case "special_defence" -> specialDefence;
				case "speed" -> speed;
				default -> throw new IllegalArgumentException("Unknown yield stat: " + target);
			};
		}
	}
}
