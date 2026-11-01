package com.fyre.cobblecuisine.item.food;

import com.fyre.cobblecuisine.component.MealEffectsComponent;
import com.fyre.cobblecuisine.meal.MealConsumption;
import com.fyre.cobblecuisine.util.CobbleCuisineUtils;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;

public class PreparedFoodItem extends Item {
	private final String name;
	private final FoodComponent foodComponent;
	private final UseAction useAction;

	protected PreparedFoodItem(String name, FoodComponent foodComponent, UseAction useAction) {
		super(new Item.Settings().food(foodComponent));
		this.name = name;
		this.foodComponent = foodComponent;
		this.useAction = useAction;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return useAction;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.addAll(CobbleCuisineUtils.getItemTooltip(name, foodComponent,
				MealConsumption.primaryValue(stack, foodComponent).orElse(null),
				MealConsumption.bonusDuration(stack, foodComponent)));
		super.appendTooltip(stack, context, tooltip, type);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		MealEffectsComponent meal = MealConsumption.capture(stack, foodComponent);
		ItemStack result = super.finishUsing(stack, world, user);
		if (activatesMeal() && !world.isClient && user instanceof ServerPlayerEntity player) {
			MealConsumption.consume(player, meal, MealConsumption.duration(foodComponent));
		}
		return result;
	}

	protected boolean activatesMeal() {
		return true;
	}
}
