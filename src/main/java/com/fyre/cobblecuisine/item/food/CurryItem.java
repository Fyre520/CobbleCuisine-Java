package com.fyre.cobblecuisine.item.food;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.util.UseAction;

public class CurryItem extends PreparedFoodItem {
	public CurryItem(String name, FoodComponent foodComponent) {
		super(name, foodComponent, UseAction.EAT);
	}
}
