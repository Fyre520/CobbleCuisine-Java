package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.item.HealingSource;
import com.cobblemon.mod.common.api.item.PokemonSelectingItem;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.pokemon.Pokemon;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.config.TooltipSettings;
import com.fyre.cobblecuisine.component.MealEffectsComponent;
import com.fyre.cobblecuisine.component.CobbleCuisineDataComponents;
import com.fyre.cobblecuisine.meal.MealPotency;
import com.fyre.cobblecuisine.meal.ProgressiveMealPotency;
import com.fyre.cobblecuisine.util.CobbleCuisineUtils;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PokePuffItem extends CobblemonItem implements PokemonSelectingItem, HealingSource {

	private final String name;
	private final FoodComponent foodComponent;

	public PokePuffItem(String name, FoodComponent foodComponent) {
		super(new Settings().food(foodComponent));
		this.name = name;
		this.foodComponent = foodComponent;
	}

	@Override public BagItem getBagItem() { return null; }

	@Override
	public boolean canUseOnPokemon(@NotNull ItemStack stack, Pokemon pokemon) {
		boolean canHeal = !pokemon.isFullHealth();
		boolean canIncreaseFriendship = pokemon.getFriendship() < Cobblemon.INSTANCE.getConfig().getMaxPokemonFriendship();
		return CobbleCuisineUtils.canFeed(pokemon, stack) && (canHeal || canIncreaseFriendship);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		if (user.isSneaking()) {
			return super.use(world, user, hand);
		} else if (user instanceof ServerPlayerEntity serverPlayer) {
			return use(serverPlayer, stack, false);
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public TypedActionResult<ItemStack> applyToPokemon(@NotNull ServerPlayerEntity player, @NotNull ItemStack stack, @NotNull Pokemon pokemon) {
		if (stack.isEmpty() || !canUseOnPokemon(stack, pokemon)) return TypedActionResult.fail(stack);
		boolean effectApplied = CobbleCuisineUtils.increaseFriendship(pokemon, feedingAmount(stack,
				TooltipSettings.Feeding.pokePuffFriendship(CobbleCuisineConfig.data), MealPotency.IV_SWEETS));
		effectApplied |= CobbleCuisineUtils.healPokemon(pokemon, feedingAmount(stack,
				TooltipSettings.Feeding.pokePuffHealing(CobbleCuisineConfig.data), MealPotency.HEALING_FRUIT), this);

		if (effectApplied) {
			// FIX: Add save marker for all persistent changes (1.7 requirement)
			pokemon.onChange(null);
			CobbleCuisineUtils.feedAndFill(pokemon, stack);

			if (pokemon.getEntity() != null) {
				pokemon.getEntity().playSound(CobblemonSounds.BERRY_EAT, 0.7f, 1.3f);
				player.sendMessage(Text.translatable("item.cobblecuisine.pokepuff.use", pokemon.getDisplayName(true)), false);

				if (pokemon.getEntity().getWorld() instanceof ServerWorld serverWorld) {
					serverWorld.spawnParticles(ParticleTypes.HEART, pokemon.getEntity().getX(), pokemon.getEntity().getY() + pokemon.getEntity().getHeight(), pokemon.getEntity().getZ(), 5, 0.5, 0.5, 0.5, 0.1);
				}
			}

			if (!player.isCreative()) stack.decrement(1);
			return TypedActionResult.success(stack);
		}

		return TypedActionResult.pass(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		var settings = TooltipSettings.current();
		int friendship = feedingAmount(stack, settings.map(TooltipSettings.Snapshot::pokePuffFriendship)
				.orElseGet(() -> TooltipSettings.Feeding.pokePuffFriendship(CobbleCuisineConfig.data)), MealPotency.IV_SWEETS);
		tooltip.addAll(CobbleCuisineUtils.getItemTooltip(name, foodComponent, new Object[]{friendship}, 0, null, null, null));
		settings.ifPresent(values -> tooltip.add(Text.translatable("tooltip.cobblecuisine.pokemon_food.healing",
				feedingAmount(stack, values.pokePuffHealing(), MealPotency.HEALING_FRUIT)).formatted(Formatting.GOLD)));
		super.appendTooltip(stack, context, tooltip, type);
	}

	private int feedingAmount(ItemStack stack, TooltipSettings.Feeding feeding, String ingredient) {
		int count = stack.getOrDefault(CobbleCuisineDataComponents.MEAL_EFFECTS, MealEffectsComponent.EMPTY)
				.potency().count(ingredient);
		return ProgressiveMealPotency.feedingAmount(feeding.amount(), count, feeding);
	}
}
