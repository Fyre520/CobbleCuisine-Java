package com.fyre.cobblecuisine.item.food;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.util.UseAction;

public class FoodItem extends PreparedFoodItem {
	private final boolean active;

	public FoodItem(String name, FoodComponent foodComponent) {
		this(name, foodComponent, true);
	}

	public FoodItem(String name, FoodComponent foodComponent, boolean active) {
		super(name, foodComponent, UseAction.EAT);
		this.active = active;
	}

	@Override
	protected boolean activatesMeal() {
		return active;
	}
}
