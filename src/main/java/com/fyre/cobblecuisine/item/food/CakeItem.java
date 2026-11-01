package com.fyre.cobblecuisine.item.food;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.util.UseAction;

public class CakeItem extends PreparedFoodItem {
	public CakeItem(String name, FoodComponent foodComponent) {
		super(name, foodComponent, UseAction.EAT);
	}
}
