package com.fyre.cobblecuisine.meal;

import com.fyre.cobblecuisine.CobbleCuisine;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

import java.util.List;

public final class CobbleCuisineMealAttachments {
	public static final AttachmentType<MealLoadout> ACTIVE_MEAL = AttachmentRegistry.create(
			Identifier.of(CobbleCuisine.MOD_ID, "active_meal"),
			builder -> builder.persistent(MealLoadout.CODEC).copyOnDeath()
	);
	public static final AttachmentType<List<ActiveMealState.ActiveMeal>> ACTIVE_MEALS = AttachmentRegistry.create(
			Identifier.of(CobbleCuisine.MOD_ID, "active_meals"),
			builder -> builder.persistent(ActiveMealState.ActiveMeal.CODEC.listOf()).copyOnDeath()
	);

	private CobbleCuisineMealAttachments() {
	}

	public static void register() {
	}
}
