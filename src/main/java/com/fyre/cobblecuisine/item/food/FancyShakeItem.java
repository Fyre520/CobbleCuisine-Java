package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.LevelUpEvent;
import com.cobblemon.mod.common.api.item.HealingSource;
import com.cobblemon.mod.common.api.item.PokemonSelectingItem;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.pokemon.Pokemon;

import com.fyre.cobblecuisine.random.PRNG;
import com.fyre.cobblecuisine.util.CobbleCuisineUtils;

import kotlin.Unit;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FancyShakeItem extends CobblemonItem implements PokemonSelectingItem, HealingSource {
	private final static Stat[] STATS = new Stat[]{Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED};

	private final int type;
	private final List<Text> tooltips;
	private final ShakeBlendMath.Colour[] blendColours;

	public FancyShakeItem(String name, int type, FoodComponent foodComponent, ShakeBlendMath.Colour... blendColours) {
		super(new Settings().food(foodComponent));
		this.type = type;
		this.blendColours = blendColours;
		this.tooltips = CobbleCuisineUtils.getItemTooltip(name, foodComponent, null, 1, null, null, null);
	}

	public ShakeBlendMath.Colour[] blendColours() {
		return this.blendColours;
	}

	@Override public BagItem getBagItem() { return null; }

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public boolean canUseOnPokemon(@NotNull ItemStack stack, @NotNull Pokemon pokemon) {
		if (!CobbleCuisineUtils.canFeed(pokemon, stack)) return false;
		return switch (this.type) {
			case 1 -> pokemon.getCurrentHealth() > 0 && pokemon.getDmaxLevel() < 10;
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
			return use(serverPlayer, stack, false);
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public TypedActionResult<ItemStack> applyToPokemon(@NotNull ServerPlayerEntity player, @NotNull ItemStack stack, @NotNull Pokemon pokemon) {
		if (stack.isEmpty() || !canUseOnPokemon(stack, pokemon)) return TypedActionResult.fail(stack);
		boolean success = false;

		Text detail = Text.empty();

		switch (this.type) {
			case 1:
				// Kinda bleh
				if (pokemon.getDmaxLevel() == 10) break;
				int dmaxBefore = pokemon.getDmaxLevel();
				pokemon.setDmaxLevel(Math.min(10, dmaxBefore + PRNG.nextInt(2, 4)));
				detail = Text.translatable("item.cobblecuisine.fancyshake.effect.dmax", pokemon.getDmaxLevel() - dmaxBefore);
				success = true;
				break;
			case 2:
				MutableText cleared = Text.empty();
				boolean firstCleared = true;
				//noinspection ForLoopReplaceableByForEach
				for (int i = 0; i < STATS.length; i++) {
					if (pokemon.getEvs().getOrDefault(STATS[i]) == 0) continue;
					pokemon.getEvs().set(STATS[i], 0);
					if (!firstCleared) cleared.append(Text.literal(", "));
					firstCleared = false;
					cleared.append(STATS[i].getDisplayName());
					success = true;
				}
				if (success) detail = Text.translatable("item.cobblecuisine.fancyshake.effect.evs", cleared);
				break;
			case 3:
				if (pokemon.getLevel() == 100) break;
				int levelBefore = pokemon.getLevel();
				LevelUpEvent levelUpEvent = new LevelUpEvent(pokemon, levelBefore, Math.min(100, levelBefore + PRNG.nextInt(2, 4)));
				CobblemonEvents.LEVEL_UP_EVENT.post(new LevelUpEvent[] { levelUpEvent }, (e) -> Unit.INSTANCE);
				pokemon.setLevel(Math.min(100, levelUpEvent.getNewLevel()));
				detail = Text.translatable("item.cobblecuisine.fancyshake.effect.level", pokemon.getLevel() - levelBefore);
				success = true;
				break;
			case 4:
				boolean ppRestored = false;
				List<Move> ppMoves = pokemon.getMoveSet().getMoves();
				//noinspection ForLoopReplaceableByForEach
				for (int i = 0; i < ppMoves.size(); i++) {
					Move currentMove = ppMoves.get(i);
					if (currentMove.getCurrentPp() < currentMove.getMaxPp()) {
						currentMove.setCurrentPp(currentMove.getMaxPp());
						pokemon.getMoveSet().update();
						ppRestored = true;
					}
				}

				boolean healed = CobbleCuisineUtils.healPokemon(pokemon, 20, this);
				success = ppRestored || healed;
				detail = Text.translatable(healed && ppRestored
						? "item.cobblecuisine.fancyshake.effect.heal_pp"
						: healed
							? "item.cobblecuisine.fancyshake.effect.heal"
							: "item.cobblecuisine.fancyshake.effect.pp");
				break;
			case 5:
				int raised = 0;
				List<Move> ppMaxMoves = pokemon.getMoveSet().getMoves();
				//noinspection ForLoopReplaceableByForEach
				for (int i = 0; i < ppMaxMoves.size(); i++) {
					if (ppMaxMoves.get(i).raiseMaxPP(3)) raised++;
				}
				success = raised > 0;
				if (success) {
					detail = raised == 1
							? Text.translatable("item.cobblecuisine.fancyshake.effect.max_pp")
							: Text.translatable("item.cobblecuisine.fancyshake.effect.max_pp_multi", raised);
				}
				break;
			case 6:
				// I hate this so much
				List<MoveTemplate> eggMoves = CobbleCuisineUtils.shuffledCopy(pokemon.getSpecies().getMoves().getEggMoves());
				Set<MoveTemplate> currentMoveSet = new HashSet<>();
				for (Move move : pokemon.getMoveSet().getMoves()) currentMoveSet.add(move.getTemplate());

				int max = PRNG.nextInt(2, 4);
				int added = 0;
				for (MoveTemplate candidate : eggMoves) {
					if (added >= max) break;
					if (!currentMoveSet.contains(candidate) && pokemon.getBenchedMoves().add(new BenchedMove(candidate, 0))) {
						added++;
						success = true;
					}
				}
				if (success) {
					detail = added == 1
							? Text.translatable("item.cobblecuisine.fancyshake.effect.egg_move", added)
							: Text.translatable("item.cobblecuisine.fancyshake.effect.egg_moves", added);
				}
				break;
			default:
				return TypedActionResult.fail(stack);
		}

		if (success) {
			pokemon.onChange(null);
			CobbleCuisineUtils.feedAndFill(pokemon, stack);
			if (!player.isCreative()) {
				stack.decrement(1);
				player.getInventory().offerOrDrop(new ItemStack(Items.GLASS_BOTTLE));
			}

			if (pokemon.getEntity() != null && pokemon.getEntity().getWorld() instanceof ServerWorld serverWorld) {
				pokemon.getEntity().playSound(CobblemonSounds.BERRY_EAT, 0.7f, 1.3f);
				player.sendMessage(Text.translatable("item.cobblecuisine.fancyshake.use", pokemon.getDisplayName(true), detail), false);
				serverWorld.spawnParticles(ParticleTypes.HEART, pokemon.getEntity().getX(), pokemon.getEntity().getY() + pokemon.getEntity().getHeight(), pokemon.getEntity().getZ(), 5, 0.5, 0.5, 0.5, 0.1);
			}

			return TypedActionResult.success(stack);
		}

		return TypedActionResult.fail(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.addAll(tooltips);
		super.appendTooltip(stack, context, tooltip, type);
	}
}
