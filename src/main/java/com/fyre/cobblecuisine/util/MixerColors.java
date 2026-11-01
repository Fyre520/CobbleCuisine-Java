package com.fyre.cobblecuisine.util;

import com.cobblemon.mod.common.CobblemonItemComponents;
import com.cobblemon.mod.common.api.cooking.Flavour;
import com.cobblemon.mod.common.api.cooking.FlavourColourHelperKt;
import com.cobblemon.mod.common.api.riding.stats.RidingStat;
import com.cobblemon.mod.common.item.AprijuiceItem;
import com.cobblemon.mod.common.item.components.FlavourComponent;
import com.cobblemon.mod.common.item.components.FoodColourComponent;
import com.cobblemon.mod.common.item.components.RideBoostsComponent;
import com.fyre.cobblecuisine.item.food.FancyShakeItem;
import com.fyre.cobblecuisine.item.food.ShakeBlendMath;
import com.fyre.cobblecuisine.item.food.ShakeItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MixerColors {
	private MixerColors() {}

	public static Integer shakeTint(ItemStack stack) {
		if (stack.getItem() instanceof AprijuiceItem) return aprijuiceTint(stack);

		if (stack.getItem() instanceof FancyShakeItem fancy) return meanTint(fancy.blendColours());

		if (!(stack.getItem() instanceof ShakeItem shake)) return null;
		ShakeBlendMath.Colour colour = ShakeBlendMath.colourFor(shake.stat());
		return colour == null ? null : colour.tint;
	}

	private static Integer aprijuiceTint(ItemStack stack) {
		RideBoostsComponent boosts = stack.get(CobblemonItemComponents.RIDE_BOOST);
		if (boosts == null || boosts.getBoosts().isEmpty()) return null;

		int highest = 0;
		for (int value : boosts.getBoosts().values()) highest = Math.max(highest, value);

		List<RidingStat> dominant = new ArrayList<>();
		for (Map.Entry<RidingStat, Integer> entry : boosts.getBoosts().entrySet()) {
			if (entry.getValue() == highest) dominant.add(entry.getKey());
		}
		return FlavourColourHelperKt.getColourMixFromRideStatBoosts(dominant, false);
	}

	private static Integer meanTint(ShakeBlendMath.Colour[] colours) {
		if (colours == null || colours.length == 0) return null;

		long a = 0, r = 0, g = 0, b = 0;
		for (ShakeBlendMath.Colour colour : colours) {
			int argb = colour.tint;
			a += (argb >> 24) & 0xFF;
			r += (argb >> 16) & 0xFF;
			g += (argb >> 8) & 0xFF;
			b += argb & 0xFF;
		}
		int n = colours.length;
		return (int) ((a / n) << 24 | (r / n) << 16 | (g / n) << 8 | (b / n));
	}


	private static final float SATURATION_BOOST = 1.6F;
	private static final float VALUE_BOOST = 1.15F;

	public static int resultColour(ItemStack stack) {
		FoodColourComponent colourComp = stack.get(CobblemonItemComponents.FOOD_COLOUR);
		if (colourComp != null && !colourComp.getColours().isEmpty()) {
			Integer mixed = FlavourColourHelperKt.getColourMixFromColors(colourComp.getColoursAsARGB());
			if (mixed != null) return mixed;
		}

		FlavourComponent flavourComp = stack.get(CobblemonItemComponents.FLAVOUR);
		if (flavourComp != null && !flavourComp.getFlavours().isEmpty()) {
			int maxValue = 0;
			List<Flavour> dominantFlavours = new ArrayList<>();
			for (Map.Entry<Flavour, Integer> entry : flavourComp.getFlavours().entrySet()) {
				if (entry.getValue() > maxValue) {
					maxValue = entry.getValue();
					dominantFlavours.clear();
					dominantFlavours.add(entry.getKey());
				} else if (entry.getValue() == maxValue) {
					dominantFlavours.add(entry.getKey());
				}
			}
			Integer mixed = FlavourColourHelperKt.getColourMixFromFlavours(dominantFlavours, false);
			if (mixed != null) return mixed;
		}

		return 0xFFFFFFFF;
	}

	public static int boost(int argb) {
		int a = (argb >> 24) & 0xFF;
		float r = ((argb >> 16) & 0xFF) / 255.0F;
		float g = ((argb >> 8) & 0xFF) / 255.0F;
		float b = (argb & 0xFF) / 255.0F;

		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float delta = max - min;

		float v = Math.min(1.0F, max * VALUE_BOOST);

		float s = max <= 0.0F ? 0.0F : delta / max;
		s = Math.min(1.0F, s * SATURATION_BOOST);

		float h;
		if (delta <= 0.0F) {
			h = 0.0F;
		} else if (max == r) {
			h = ((g - b) / delta) % 6.0F;
		} else if (max == g) {
			h = (b - r) / delta + 2.0F;
		} else {
			h = (r - g) / delta + 4.0F;
		}
		h *= 60.0F;
		if (h < 0.0F) h += 360.0F;

		return (a << 24) | hsvToRgb(h, s, v);
	}

	private static int hsvToRgb(float h, float s, float v) {
		float c = v * s;
		float x = c * (1.0F - Math.abs((h / 60.0F) % 2.0F - 1.0F));
		float m = v - c;

		float r, g, b;
		if (h < 60.0F)       { r = c; g = x; b = 0.0F; }
		else if (h < 120.0F) { r = x; g = c; b = 0.0F; }
		else if (h < 180.0F) { r = 0.0F; g = c; b = x; }
		else if (h < 240.0F) { r = 0.0F; g = x; b = c; }
		else if (h < 300.0F) { r = x; g = 0.0F; b = c; }
		else                 { r = c; g = 0.0F; b = x; }

		int ri = Math.round((r + m) * 255.0F);
		int gi = Math.round((g + m) * 255.0F);
		int bi = Math.round((b + m) * 255.0F);
		return (ri << 16) | (gi << 8) | bi;
	}
}
