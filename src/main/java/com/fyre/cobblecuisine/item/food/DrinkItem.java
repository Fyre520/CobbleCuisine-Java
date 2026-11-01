package com.fyre.cobblecuisine.item.food;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.util.UseAction;

public class DrinkItem extends PreparedFoodItem {
	public DrinkItem(String name, FoodComponent foodComponent) {
		super(name, foodComponent, UseAction.DRINK);
	}
}
