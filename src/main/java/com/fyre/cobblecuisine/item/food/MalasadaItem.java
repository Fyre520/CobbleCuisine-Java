package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.cooking.Flavour; // Corrected import (Flavour with 'u')
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

import java.util.ArrayList;
import java.util.List;

public class MalasadaItem extends CobblemonItem implements PokemonSelectingItem, HealingSource {

	private final String name;
	private final FoodComponent foodComponent;
	private final Flavour flavour; // Changed to lowercase 'flavour' for convention

	public MalasadaItem(String name, Flavour flavour, FoodComponent foodComponent) {
		super(new Settings().food(foodComponent));
		this.name = name;
		this.foodComponent = foodComponent;
		this.flavour = flavour; // Assignment is now correct
	}

	@Override
	public BagItem getBagItem() {
		return null;
	}

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
		TooltipSettings.Feeding friendship = TooltipSettings.Feeding.malasadaFriendship(CobbleCuisineConfig.data);
		TooltipSettings.Feeding healing = TooltipSettings.Feeding.malasadaHealing(CobbleCuisineConfig.data);
		int totalFriendshipAmount = feedingAmount(stack, friendship,
				applyFlavourPreference(pokemon, friendship.amount()), MealPotency.FRIENDSHIP_SUGAR);
		boolean effectApplied = CobbleCuisineUtils.increaseFriendship(pokemon, totalFriendshipAmount);
		effectApplied |= CobbleCuisineUtils.healPokemon(pokemon, feedingAmount(stack, healing,
				applyFlavourPreference(pokemon, healing.amount()), MealPotency.HEALING_FRUIT), this);

		if (effectApplied) {
			// FIX: Explicitly mark the Pokémon for saving after persistent changes (1.7 required)
			pokemon.onChange(null);
			CobbleCuisineUtils.feedAndFill(pokemon, stack);

			if (pokemon.getEntity() != null && pokemon.getEntity().getWorld() instanceof ServerWorld serverWorld) {
				double x = pokemon.getEntity().getX();
				double y = pokemon.getEntity().getY() + pokemon.getEntity().getHeight();
				double z = pokemon.getEntity().getZ();

				if (hasNatureFlavourMatch(pokemon)) {
					player.sendMessage(Text.translatable("item.cobblecuisine.malasada.love", pokemon.getDisplayName(true)), false);
					serverWorld.spawnParticles(ParticleTypes.HEART, x, y, z, 10, 0.5, 0.5, 0.5, 0.1);
				} else if (flavour != null && pokemon.getNature().getDislikedFlavour() == flavour) {
					player.sendMessage(Text.translatable("item.cobblecuisine.malasada.dislike", pokemon.getDisplayName(true)), false);
					serverWorld.spawnParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, 5, 0.3, 0.3, 0.3, 0.05);
				} else if (flavour != null) {
					player.sendMessage(Text.translatable("item.cobblecuisine.malasada.use", pokemon.getDisplayName(true)), false);
					serverWorld.spawnParticles(ParticleTypes.NOTE, x, y, z, 3, 0.4, 0.4, 0.4, 0.1);
				}

				pokemon.getEntity().playSound(CobblemonSounds.BERRY_EAT, 0.7f, 1.3f);
			}
			if (!player.isCreative()) stack.decrement(1);
			return TypedActionResult.success(stack);
		}
		return TypedActionResult.pass(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		var settings = TooltipSettings.current();
		TooltipSettings.Feeding feeding = settings.map(TooltipSettings.Snapshot::malasadaFriendship)
				.orElseGet(() -> TooltipSettings.Feeding.malasadaFriendship(CobbleCuisineConfig.data));
		int friendship = feedingAmount(stack, feeding, feeding.amount(), MealPotency.FRIENDSHIP_SUGAR);
		tooltip.addAll(CobbleCuisineUtils.getItemTooltip(name, foodComponent, new Object[]{friendship}, 0, null, null, null));
		super.appendTooltip(stack, context, tooltip, type);
	}

	public List<Text> tasteTooltip(ItemStack stack) {
		var settings = TooltipSettings.current();
		TooltipSettings.Feeding feeding = settings.map(TooltipSettings.Snapshot::malasadaHealing)
				.orElseGet(() -> TooltipSettings.Feeding.malasadaHealing(CobbleCuisineConfig.data));
		int healing = feedingAmount(stack, feeding, feeding.amount(), MealPotency.HEALING_FRUIT);

		List<Text> lines = new ArrayList<>();
		lines.add(Text.translatable("tooltip.cobblecuisine.item." + name + ".2"));
		lines.add(Text.translatable("tooltip.cobblecuisine.item." + name + ".3"));
		lines.add(Text.translatable("tooltip.cobblecuisine.pokemon_food.healing", healing).formatted(Formatting.GOLD));
		return lines;
	}

	private int feedingAmount(ItemStack stack, TooltipSettings.Feeding feeding, double amount, String ingredient) {
		int count = stack.getOrDefault(CobbleCuisineDataComponents.MEAL_EFFECTS, MealEffectsComponent.EMPTY)
				.potency().count(ingredient);
		return ProgressiveMealPotency.feedingAmount(amount, count, feeding);
	}

	private double applyFlavourPreference(Pokemon pokemon, double baseAmount) {
		if (flavour == null) return baseAmount;

		if (pokemon.getNature().getFavouriteFlavour() == flavour) return baseAmount * 1.5;
		else if (pokemon.getNature().getDislikedFlavour() == flavour) return baseAmount * 0.75;

		return baseAmount;
	}

	private boolean hasNatureFlavourMatch(Pokemon pokemon) {
		return flavour != null && pokemon.getNature().getFavouriteFlavour() == flavour;
	}

	// Removed DefaultImpls overrides
}
