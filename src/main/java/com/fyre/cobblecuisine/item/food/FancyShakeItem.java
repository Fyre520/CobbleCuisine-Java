package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.item.HealingSource;
import com.cobblemon.mod.common.api.item.PokemonSelectingItem;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.pokemon.Pokemon;

import com.fyre.cobblecuisine.random.PRNG;
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
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FancyShakeItem extends CobblemonItem implements PokemonSelectingItem, HealingSource {

	private final int type;
	private final List<Text> tooltips;

	public FancyShakeItem(String name, int type, FoodComponent foodComponent) {
		super(new Settings().food(foodComponent));
		this.type = type;
		this.tooltips = CobbleCuisineUtils.getItemTooltip(name, foodComponent, null, 1, null, null, null);
	}

	@Override public BagItem getBagItem() { return null; }

	@Override
	public boolean canUseOnPokemon(@NotNull Pokemon pokemon) {
		return switch (this.type) {
			case 1 -> pokemon.getCurrentHealth() > 0 && pokemon.getDmaxLevel() <= 7;
			case 2, 5, 6 -> pokemon.getCurrentHealth() > 0;
			case 3 -> pokemon.getCurrentHealth() > 0 && pokemon.getLevel() < 100;
			case 4 -> true;
			default -> false;
		};
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
		switch (this.type) {
			case 1:
				pokemon.setDmaxLevel(pokemon.getDmaxLevel() + PRNG.nextInt(2, 4));
				break;
			case 2:
				for (int i = 0; i < Stats.values().length; i++) pokemon.setEV(Stats.values()[i], 0);
				break;
			case 3:
				pokemon.setLevel(Math.min(100, pokemon.getLevel() + PRNG.nextInt(2, 4)));
				break;
			case 4:
				pokemon.heal();
				break;
			case 5:
				List<Move> moves = pokemon.getMoveSet().getMoves();
				//noinspection ForLoopReplaceableByForEach
				for (int i = 0; i < moves.size(); i++) moves.get(i).raiseMaxPP(3);
				break;
			case 6:
				List<MoveTemplate> eggMoves = pokemon.getSpecies().getMoves().getEggMoves();
				Collections.shuffle(eggMoves);
				int added = 0;
				int maxAdd = PRNG.nextInt(2, 4);
				for (int i = 0; i < eggMoves.size() && added < maxAdd; i++) {
					MoveTemplate candidate = eggMoves.get(PRNG.nextInt(0, eggMoves.size()));

					boolean alreadyHas = false;
					List<Move> currentMoves = pokemon.getMoveSet().getMoves();
					//noinspection ForLoopReplaceableByForEach
					for (int j = 0; j < currentMoves.size(); j++) {
						if (currentMoves.get(j).getTemplate().equals(candidate)) {
							alreadyHas = true;
							break;
						}
					}

					if (!alreadyHas) {
						pokemon.getMoveSet().add(new Move(candidate, 0, 0));
						added++;
					}
				}
				break;
			default:
				return TypedActionResult.fail(stack);
		}

		if (pokemon.getEntity() != null && pokemon.getEntity().getWorld() instanceof ServerWorld serverWorld) {
			pokemon.getEntity().playSound(CobblemonSounds.BERRY_EAT, 0.7f, 1.3f);
			player.sendMessage(Text.translatable("item.cobblecuisine.fancyshake.use", pokemon.getDisplayName()), false);
			serverWorld.spawnParticles(ParticleTypes.HEART, pokemon.getEntity().getX(), pokemon.getEntity().getY() + pokemon.getEntity().getHeight(), pokemon.getEntity().getZ(), 5, 0.5, 0.5, 0.5, 0.1);
		}

		if (!player.isCreative()) stack.decrement(1);

		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.addAll(tooltips);
		super.appendTooltip(stack, context, tooltip, type);
	}

	@Override public void applyToBattlePokemon(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattlePokemon battlePokemon) { DefaultImpls.applyToBattlePokemon(this, serverPlayerEntity, itemStack, battlePokemon); }
	@Override public boolean canUseOnBattlePokemon(@NotNull BattlePokemon battlePokemon) { return PokemonSelectingItem.DefaultImpls.canUseOnBattlePokemon(this, battlePokemon); }
	@NotNull @Override public TypedActionResult<ItemStack> interactWithSpecificBattle(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattlePokemon battlePokemon) { return PokemonSelectingItem.DefaultImpls.interactWithSpecificBattle(this, serverPlayerEntity, itemStack, battlePokemon); }
	@NotNull @Override public TypedActionResult<ItemStack> interactGeneral(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack) { return PokemonSelectingItem.DefaultImpls.interactGeneral(this, serverPlayerEntity, itemStack); }
	@NotNull @Override public TypedActionResult<ItemStack> interactGeneralBattle(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattleActor battleActor) { return PokemonSelectingItem.DefaultImpls.interactGeneralBattle(this, serverPlayerEntity, itemStack, battleActor); }
	@NotNull @Override public TypedActionResult<ItemStack> use(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack) { return PokemonSelectingItem.DefaultImpls.use(this, serverPlayerEntity, itemStack); }
}
