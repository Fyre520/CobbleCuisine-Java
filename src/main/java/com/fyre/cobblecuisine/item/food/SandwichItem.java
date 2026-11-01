package com.fyre.cobblecuisine.item.food;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.util.UseAction;

public class SandwichItem extends PreparedFoodItem {
	public SandwichItem(String name, FoodComponent foodComponent) {
		super(name, foodComponent, UseAction.EAT);
	}
}
