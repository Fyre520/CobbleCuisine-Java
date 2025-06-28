package com.fyre.cobblecuisine.item.food;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;
import com.fyre.cobblecuisine.util.CobbleCuisineUtils;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;

public class DrinkItem extends Item {
	private final List<Text> tooltips;
	private final RegistryEntry<StatusEffect> foodEffect;

	public DrinkItem(String name, FoodComponent foodComponent) {
		super(new Settings().food(foodComponent));
		this.tooltips = CobbleCuisineUtils.getItemTooltip(name, foodComponent, null, 0, null, null, null);
		this.foodEffect = foodComponent.effects().getFirst().effect().getEffectType();
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.addAll(tooltips);
		super.appendTooltip(stack, context, tooltip, type);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (!world.isClient && user instanceof PlayerEntity player) {
			RegistryEntry<StatusEffect> effect = CobbleCuisineEffects.TYPE_BUFF_MARKER.entry;
			if (player.hasStatusEffect(effect)) player.removeStatusEffect(effect);
			player.addStatusEffect(new StatusEffectInstance(effect, CobbleCuisineConfig.data.effectDuration.yieldBoostEffectDuration, 0, false,false, false));
		}

		return super.finishUsing(stack, world, user);
	}
}
