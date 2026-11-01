package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.item.PokemonSelectingItem;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.pokemon.Pokemon;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;

import com.fyre.cobblecuisine.util.CobbleCuisineUtils;
import com.fyre.cobblecuisine.config.TooltipSettings;
import net.minecraft.registry.Registries;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BeanItem extends CobblemonItem implements PokemonSelectingItem {
	private final int friendshipAmount;
	private final String name;
	private final FoodComponent foodComponent;

	public BeanItem(String name, FoodComponent foodComponent) {
		super(new Settings().food(foodComponent));
		this.friendshipAmount = CobbleCuisineConfig.data.itemSettings.beanFriendship;
		this.name = name;
		this.foodComponent = foodComponent;
	}

	@Override
	public BagItem getBagItem() { return null; }

	public int friendshipAmount() { return friendshipAmount; }

	@Override
	public boolean canUseOnPokemon(@NotNull ItemStack stack, Pokemon pokemon) {
		return CobbleCuisineUtils.canFeed(pokemon, stack)
				&& pokemon.getFriendship() < Cobblemon.INSTANCE.getConfig().getMaxPokemonFriendship();
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
	public TypedActionResult<ItemStack> applyToPokemon(@NotNull ServerPlayerEntity player, @NotNull ItemStack stack, Pokemon pokemon) {
		if (stack.isEmpty() || !canUseOnPokemon(stack, pokemon)) return TypedActionResult.fail(stack);
		if (CobbleCuisineUtils.increaseFriendship(pokemon, friendshipAmount)) {
			// FIX: Add save marker for Friendship change (1.7 requirement)
			pokemon.onChange(null);
			CobbleCuisineUtils.feedAndFill(pokemon, stack);

			player.sendMessage(Text.translatable("item.cobblecuisine.bean.use", pokemon.getDisplayName(true)), false);
			if (pokemon.getEntity() != null) {
				pokemon.getEntity().playSound(CobblemonSounds.BERRY_EAT, 0.7f, 1.3f);
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
		var amount = TooltipSettings.current().flatMap(settings -> settings.itemAmount(Registries.ITEM.getId(this).toString()));
		Object[] args = new Object[]{amount.orElseGet(this::friendshipAmount)};
		tooltip.addAll(CobbleCuisineUtils.getItemTooltip(name, foodComponent, args, 0, null, null, null));
		super.appendTooltip(stack, context, tooltip, type);
	}

	// Removed six deprecated DefaultImpls overrides
}
