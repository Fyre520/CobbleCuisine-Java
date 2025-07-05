package com.fyre.cobblecuisine.config;

public class CobbleCuisineConfigData {
	public final BoostSettings boostSettings = new BoostSettings();
	public final ItemSettings itemSettings = new ItemSettings();
	public final EffectDuration effectDuration  = new EffectDuration();
	public final DropRates dropRates = new DropRates();
	public final TypeMultipliers typeMultipliers = new TypeMultipliers();
	public final EggGroupMultipliers eggGroupMultipliers = new EggGroupMultipliers();

	public static class BoostSettings {
		public final float effectDistanceBlocks = 80.0f;
		public final float expBoostMultiplier = 2.0f;
		public final float catchRateMultiplier = 2.0f;
		public final float shinyBoostMultiplier = 2.0f;
		public final float teraBoostChance = 0.75f;
		public final float natureBoostChance = 0.75f;
		public final float scaleMinChance = 0.5f;
		public final float scaleMaxChance = 0.9f;
		public final float ivMinChance = 0.5f;
		public final float ivAvgChance = 0.2f;
		public final float ivMaxChance = 0.05f;
		public final float haBoostChance = 2.0f;
	}

	public static class ItemSettings {
		public final int beanFriendship = 1;
		public final int pokepuffFriendship = 6;
		public final int malasadaFriendship = 12;
		public final int lowShakeEv = 2;
		public final int mediumShakeEv = 4;
		public final int highShakeEv = 8;
	}

	public static class EffectDuration {
		public final int shinyBoostEffectDuration = 6000;
		public final int expBoostEffectDuration = 6000;
		public final int catchRateEffectDuration = 6000;
		public final int typeBoostEffectDuration = 6000;
		public final int teraBoostEffectDuration = 6000;
		public final int eggBoostEffectDuration = 6000;
		public final int natureBoostEffectDuration = 6000;
		public final int statBoostEffectDuration = 6000;
		public final int yieldBoostEffectDuration = 6000;
		public final int scaleBoostEffectDuration = 6000;
		public final int dubiousFoodEffectDuration = 6000;
	}

	public static class DropRates {
		public final float beanSeedsDropRate = 0.1f;
		public final float galaricaNutDropRate = 0.25f;
		public final float beanDropRate = 0.25f;
		public final float torchflowerDropRate = 0.25f;
		public final float torchflowerSeedsDropRate = 0.25f;
	}

	public static class WeightPair {
		public final float weightMultiplier = 5.0f;
		public final float nonWeightMultiplier = 0.5f;
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
