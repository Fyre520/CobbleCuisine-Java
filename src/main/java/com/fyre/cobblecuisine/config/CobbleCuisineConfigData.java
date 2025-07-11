package com.fyre.cobblecuisine.config;

public class CobbleCuisineConfigData {
	public final BoostSettings boostSettings = new BoostSettings();
	public final ItemSettings itemSettings = new ItemSettings();
	public final EffectDuration effectDuration  = new EffectDuration();
	public final DropRates dropRates = new DropRates();
	public final TypeMultipliers typeMultipliers = new TypeMultipliers();
	public final EggGroupMultipliers eggGroupMultipliers = new EggGroupMultipliers();

	public static class BoostSettings {
		public float effectDistanceBlocks = 80.0f;
		public float expBoostMultiplier = 2.0f;
		public float catchRateMultiplier = 2.0f;
		public float shinyBoostMultiplier = 2.0f;
		public float teraBoostChance = 0.75f;
		public float natureBoostChance = 0.75f;
		public float scaleMinValue = 0.9f;
		public float scaleMaxValue = 1.1f;
		public int ivMinValue = 20;
		public int ivMaxValue = 31;
		public float haBoostChance = 2.0f;
	}

	public static class ItemSettings {
		public int beanFriendship = 1;
		public int pokepuffFriendship = 6;
		public int malasadaFriendship = 12;
		public int lowShakeEv = 2;
		public int mediumShakeEv = 4;
		public int highShakeEv = 8;
	}

	public static class EffectDuration {
		public int shinyBoostEffectDuration = 6000;
		public int expBoostEffectDuration = 6000;
		public int catchRateEffectDuration = 6000;
		public int typeBoostEffectDuration = 6000;
		public int teraBoostEffectDuration = 6000;
		public int eggBoostEffectDuration = 6000;
		public int natureBoostEffectDuration = 6000;
		public int statBoostEffectDuration = 6000;
		public int yieldBoostEffectDuration = 6000;
		public int scaleBoostEffectDuration = 6000;
		public int dubiousFoodEffectDuration = 6000;
	}

	public static class DropRates {
		public float beanSeedsDropRate = 0.1f;
		public float galaricaNutDropRate = 0.25f;
		public float beanDropRate = 0.25f;
		public float torchflowerDropRate = 0.25f;
		public float torchflowerSeedsDropRate = 0.25f;
	}

	public static class WeightPair {
		public float weightMultiplier = 5.0f;
		public float nonWeightMultiplier = 0.5f;
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
	}
}
