package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.healing.PokemonHealedEvent;
import com.cobblemon.mod.common.api.item.HealingSource;
import com.cobblemon.mod.common.api.item.PokemonSelectingItem;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.pokemon.Pokemon;

import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.util.CobbleCuisineUtils;

import kotlin.Unit;

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

public class PokePuffItem extends CobblemonItem implements PokemonSelectingItem, HealingSource {

	private final int friendshipAmount;
	private final List<Text> tooltips;

	public PokePuffItem(String name, FoodComponent foodComponent) {
		super(new Settings().food(foodComponent));
		this.friendshipAmount = CobbleCuisineConfig.data.itemSettings.pokepuffFriendship;
		this.tooltips = CobbleCuisineUtils.getItemTooltip(name, foodComponent, null, 1, new Object[] { friendshipAmount }, null, null);
	}

	@Override public BagItem getBagItem() { return null; }

	@Override
	public boolean canUseOnPokemon(@NotNull ItemStack stack, @NotNull Pokemon pokemon) {
		boolean canHeal = !pokemon.isFullHealth();
		boolean canIncreaseFriendship = pokemon.getFriendship() < 255;
		return pokemon.getCurrentHealth() > 0 && (canHeal || canIncreaseFriendship);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		if (user.isSneaking()) {
			return super.use(world, user, hand);
		} else if (user instanceof ServerPlayerEntity serverPlayer) {
			return use(serverPlayer, stack);
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public TypedActionResult<ItemStack> applyToPokemon(@NotNull ServerPlayerEntity player, @NotNull ItemStack stack, @NotNull Pokemon pokemon) {
		boolean effectApplied = pokemon.incrementFriendship(friendshipAmount, true);

		if (!pokemon.isFullHealth()) {
			int amountToHeal = Math.min(20, pokemon.getMaxHealth() - pokemon.getCurrentHealth());
			final int[] healAmountHolder = { amountToHeal };
			CobblemonEvents.POKEMON_HEALED.postThen(
					new PokemonHealedEvent(pokemon, amountToHeal, this),
					(event) -> Unit.INSTANCE,
					(event) -> {
						healAmountHolder[0] = event.getAmount();
						return Unit.INSTANCE;
					}
			);
			pokemon.setCurrentHealth(healAmountHolder[0]);
			effectApplied = true;
		}

		if (effectApplied) {
			if (pokemon.getEntity() != null) {
				pokemon.getEntity().playSound(CobblemonSounds.BERRY_EAT, 0.7f, 1.3f);
				player.sendMessage(Text.translatable("item.cobblecuisine.pokepuff.use", pokemon.getDisplayName(false)), false);

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
		tooltip.addAll(tooltips);
		super.appendTooltip(stack, context, tooltip, type);
	}

	@Override public void applyToBattlePokemon(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattlePokemon battlePokemon) { DefaultImpls.applyToBattlePokemon(this, serverPlayerEntity, itemStack, battlePokemon); }
	@Override public boolean canUseOnBattlePokemon(@NotNull ItemStack stack, @NotNull BattlePokemon battlePokemon) { return PokemonSelectingItem.DefaultImpls.canUseOnBattlePokemon(this, stack, battlePokemon); }
	@NotNull @Override public TypedActionResult<ItemStack> interactWithSpecificBattle(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattlePokemon battlePokemon) { return PokemonSelectingItem.DefaultImpls.interactWithSpecificBattle(this, serverPlayerEntity, itemStack, battlePokemon); }
	@NotNull @Override public TypedActionResult<ItemStack> interactGeneral(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack) { return PokemonSelectingItem.DefaultImpls.interactGeneral(this, serverPlayerEntity, itemStack); }
	@NotNull @Override public TypedActionResult<ItemStack> interactGeneralBattle(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattleActor battleActor) { return PokemonSelectingItem.DefaultImpls.interactGeneralBattle(this, serverPlayerEntity, itemStack, battleActor); }
	@NotNull @Override public TypedActionResult<ItemStack> use(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack) { return PokemonSelectingItem.DefaultImpls.use(this, serverPlayerEntity, itemStack); }
}
