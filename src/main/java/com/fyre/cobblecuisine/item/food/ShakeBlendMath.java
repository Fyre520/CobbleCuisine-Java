package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;

import com.fyre.cobblecuisine.CobbleCuisine;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ShakeBlendMath {
	private ShakeBlendMath() {}

	public static final int MIN_RANK_SUM = 3;
	public static final int MAX_RANK_SUM = 9;

	public static final int LOW_MAX = 4;
	public static final int MEDIUM_MAX = 7;

	public static final int VITAMIN_EV = 2;

	public enum Colour {
		GREEN("green", Stats.HP, 0xFF4CAF50),
		RED("red", Stats.ATTACK, 0xFFE53935),
		PURPLE("purple", Stats.DEFENCE, 0xFF9C27B0),
		YELLOW("yellow", Stats.SPECIAL_ATTACK, 0xFFFDD835),
		BLUE("blue", Stats.SPECIAL_DEFENCE, 0xFF1E88E5),
		PINK("pink", Stats.SPEED, 0xFFEC407A);

		public final String key;
		public final Stat stat;
		public final int tint;

		Colour(String key, Stat stat, int tint) {
			this.key = key;
			this.stat = stat;
			this.tint = tint;
		}

		public TagKey<Item> berryTag() {
			return TagKey.of(RegistryKeys.ITEM, Identifier.of(CobbleCuisine.MOD_ID, key + "_berry"));
		}

		public TagKey<Item> rankTag(int rank) {
			return TagKey.of(RegistryKeys.ITEM, Identifier.of(CobbleCuisine.MOD_ID, key + "_group" + rank));
		}
	}

	public static Colour colourFor(Stat stat) {
		if (stat == null) return null;
		for (Colour colour : Colour.values()) {
			if (colour.stat == stat) return colour;
		}
		return null;
	}

	public static Colour colourOf(ItemStack stack) {
		for (Colour colour : Colour.values()) {
			if (stack.isIn(colour.berryTag())) return colour;
		}
		return null;
	}

	public static int rankOf(ItemStack stack, Colour colour) {
		if (colour == null) return 0;
		for (int rank = 1; rank <= 3; rank++) {
			if (stack.isIn(colour.rankTag(rank))) return rank;
		}
		return 0;
	}

	public static int tier(int rankSum) {
		if (rankSum <= LOW_MAX) return 1;
		if (rankSum <= MEDIUM_MAX) return 2;
		return 3;
	}

	public static int berrySeasoningEv(int rank) {
		return Math.max(0, Math.min(3, rank));
	}

	private static final int[][] BASE_EV_BY_POWER = {
			{4, 6, 8},
			{6, 8, 10},
			{8, 10, 12}
	};

	public static int baseEv(int tier, int power) {
		int row = Math.max(0, Math.min(BASE_EV_BY_POWER.length - 1, power));
		int index = Math.max(0, Math.min(2, tier - 1));
		return BASE_EV_BY_POWER[row][index];
	}

	public enum Vitamin {
		HP_UP("hp_up", Stats.HP),
		PROTEIN("protein", Stats.ATTACK),
		IRON("iron", Stats.DEFENCE),
		CALCIUM("calcium", Stats.SPECIAL_ATTACK),
		ZINC("zinc", Stats.SPECIAL_DEFENCE),
		CARBOS("carbos", Stats.SPEED);

		public final String key;
		public final Stat stat;

		Vitamin(String key, Stat stat) {
			this.key = key;
			this.stat = stat;
		}
	}

	private static final TagKey<Item> VITAMINS =
			TagKey.of(RegistryKeys.ITEM, Identifier.of("cobblemon", "vitamins"));

	public static Stat vitaminStat(ItemStack stack) {
		if (stack.isEmpty() || !stack.isIn(VITAMINS)) return null;
		if (!Registries.ITEM.getId(stack.getItem()).getNamespace().equals("cobblemon")) return null;
		String path = Registries.ITEM.getId(stack.getItem()).getPath();
		for (Vitamin vitamin : Vitamin.values()) {
			if (vitamin.key.equals(path)) return vitamin.stat;
		}
		return null;
	}

	public static Stat seasoningStat(ItemStack stack) {
		Colour colour = colourOf(stack);
		if (colour != null) return colour.stat;
		return vitaminStat(stack);
	}

	public static int seasoningEv(ItemStack stack) {
		Colour colour = colourOf(stack);
		if (colour != null) return berrySeasoningEv(rankOf(stack, colour));
		return vitaminStat(stack) == null ? 0 : VITAMIN_EV;
	}

	public static ShakeType shakeFor(Colour colour, int tier) {
		if (colour == null) return null;
		for (ShakeType type : ShakeType.values()) {
			if (type.tier == tier && type.stat == colour.stat) return type;
		}
		return null;
	}

	public static ShakeType resolve(ItemStack first, ItemStack second, ItemStack third) {
		Colour colour = colourOf(first);
		if (colour == null || colourOf(second) != colour || colourOf(third) != colour) return null;

		int sum = rankOf(first, colour) + rankOf(second, colour) + rankOf(third, colour);
		if (sum < MIN_RANK_SUM || sum > MAX_RANK_SUM) return null;

		return shakeFor(colour, tier(sum));
	}
}
