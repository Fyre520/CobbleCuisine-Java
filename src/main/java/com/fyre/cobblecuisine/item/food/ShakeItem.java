package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.item.PokemonSelectingItem;
import com.cobblemon.mod.common.api.pokemon.stats.EvSource;
import com.cobblemon.mod.common.api.pokemon.stats.SidemodEvSource;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.pokemon.EVs;
import com.cobblemon.mod.common.pokemon.Pokemon;

import com.fyre.cobblecuisine.util.CobbleCuisineUtils;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.fyre.cobblecuisine.CobbleCuisine.MOD_ID;

public class ShakeItem extends Item implements PokemonSelectingItem {

	private final Stat stat;
	private final int evIncreaseAmount;
	private final List<Text> tooltips;
	private final SoundEvent sound = SoundEvents.ENTITY_GENERIC_DRINK;

	public ShakeItem(String name, Stat stat, int evIncreaseAmount, FoodComponent foodComponent) {
		super(new Settings().food(foodComponent));
		this.stat = stat;
		this.evIncreaseAmount = evIncreaseAmount;
		this.tooltips = CobbleCuisineUtils.getItemTooltip(name, foodComponent, null, 1, new Object[] { evIncreaseAmount, stat }, null, null);
	}

	@Override public BagItem getBagItem() { return null; }

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.addAll(tooltips);
		super.appendTooltip(stack, context, tooltip, type);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public boolean canUseOnPokemon(@NotNull ItemStack stack, @NotNull Pokemon pokemon) {
		return pokemon.getEvs().getOrDefault(stat) < EVs.MAX_STAT_VALUE;
	}

	@Override
	public TypedActionResult<ItemStack> applyToPokemon(@NotNull ServerPlayerEntity player, @NotNull ItemStack stack, Pokemon pokemon) {
		int currentEvs = pokemon.getEvs().getOrDefault(stat);
		int maxAdd = EVs.MAX_STAT_VALUE - currentEvs;
		int actualAdd = Math.min(evIncreaseAmount, maxAdd);

		if (actualAdd > 0) {
            pokemon.getEvs().add(stat, actualAdd, new SidemodEvSource(MOD_ID, pokemon));

			if (pokemon.getEntity() != null && pokemon.getEntity().getWorld() instanceof ServerWorld) {
				pokemon.getEntity().playSound(sound, 0.8F, 1.1F);
			}

			if (!player.isCreative()) {
				stack.decrement(1);
				player.getInventory().offerOrDrop(new ItemStack(Items.GLASS_BOTTLE));
			}

			return TypedActionResult.success(stack);
		}

		return TypedActionResult.fail(stack);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		if (!user.isSneaking()) {
			if (user instanceof ServerPlayerEntity serverPlayer) {
				return use(serverPlayer, stack);
			}
			return TypedActionResult.success(stack);
		} else {
			return super.use(world, user, hand);
		}
	}

	@Override public void applyToBattlePokemon(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattlePokemon battlePokemon) { DefaultImpls.applyToBattlePokemon(this, serverPlayerEntity, itemStack, battlePokemon); }
	@Override public boolean canUseOnBattlePokemon(@NotNull ItemStack stack, @NotNull BattlePokemon battlePokemon) { return PokemonSelectingItem.DefaultImpls.canUseOnBattlePokemon(this, stack, battlePokemon); }
	@NotNull @Override public TypedActionResult<ItemStack> interactWithSpecificBattle(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattlePokemon battlePokemon) { return PokemonSelectingItem.DefaultImpls.interactWithSpecificBattle(this, serverPlayerEntity, itemStack, battlePokemon); }
	@NotNull @Override public TypedActionResult<ItemStack> interactGeneral(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack) { return PokemonSelectingItem.DefaultImpls.interactGeneral(this, serverPlayerEntity, itemStack); }
	@NotNull @Override public TypedActionResult<ItemStack> interactGeneralBattle(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattleActor battleActor) { return PokemonSelectingItem.DefaultImpls.interactGeneralBattle(this, serverPlayerEntity, itemStack, battleActor); }
	@NotNull @Override public TypedActionResult<ItemStack> use(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack) { return PokemonSelectingItem.DefaultImpls.use(this, serverPlayerEntity, itemStack); }
}
