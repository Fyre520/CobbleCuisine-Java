package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.CobblemonItemComponents;
import com.cobblemon.mod.common.api.cooking.Flavour;
import com.cobblemon.mod.common.api.item.PokemonSelectingItem;
import com.fyre.cobblecuisine.CobbleCuisine;
import com.cobblemon.mod.common.api.pokemon.stats.SidemodEvSource;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.item.components.FlavourComponent;
import com.cobblemon.mod.common.pokemon.EVs;
import com.cobblemon.mod.common.pokemon.Pokemon;


import com.fyre.cobblecuisine.component.CobbleCuisineDataComponents;
import com.fyre.cobblecuisine.component.ShakeEvComponent;
import com.fyre.cobblecuisine.util.CobbleCuisineUtils;
import com.fyre.cobblecuisine.config.TooltipSettings;
import net.minecraft.registry.Registries;

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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShakeItem extends Item implements PokemonSelectingItem {

	private final Stat stat;
	private final int evIncreaseAmount;
	private final String name;
	private final FoodComponent foodComponent;
	private final SoundEvent sound = SoundEvents.ENTITY_GENERIC_DRINK;

	public ShakeItem(String name, Stat stat, int evIncreaseAmount, FoodComponent foodComponent) {
		super(new Settings().food(foodComponent));
		this.stat = stat;
		this.evIncreaseAmount = evIncreaseAmount;
		this.name = name;
		this.foodComponent = foodComponent;
	}

	public int evIncreaseAmount() { return evIncreaseAmount; }

	public Stat stat() { return stat; }

	@Override public BagItem getBagItem() { return null; }

	private Map<Stat, Integer> grantedEvs(ItemStack stack) {
		int base = TooltipSettings.current()
				.flatMap(settings -> settings.itemAmount(Registries.ITEM.getId(this).toString()))
				.orElse(evIncreaseAmount);

		Map<Stat, Integer> totals = new LinkedHashMap<>();
		if (base > 0) totals.merge(stat, base, Integer::sum);
		stack.getOrDefault(CobbleCuisineDataComponents.SHAKE_EV, ShakeEvComponent.EMPTY)
				.extraEvs()
				.forEach((extraStat, amount) -> totals.merge(extraStat, amount, Integer::sum));
		return totals;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.addAll(CobbleCuisineUtils.getItemTooltip(name, foodComponent, null, 0, null, null, null));

		String lineKey = "tooltip." + CobbleCuisine.MOD_ID + ".item." + name + ".1";
		for (Map.Entry<Stat, Integer> entry : grantedEvs(stack).entrySet()) {
			tooltip.add(Text.translatable(lineKey, entry.getValue(), entry.getKey().getDisplayName()));
		}

		super.appendTooltip(stack, context, tooltip, type);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public boolean canUseOnPokemon(@NotNull ItemStack stack, Pokemon pokemon) {
		if (!CobbleCuisineUtils.canFeed(pokemon, stack)) return false;
		if (pokemon.getEvs().total() >= EVs.MAX_TOTAL_VALUE) return false;
		for (Stat target : grantedEvs(stack).keySet()) {
			if (pokemon.getEvs().getOrDefault(target) < EVs.MAX_STAT_VALUE) return true;
		}
		return false;
	}

	private static final double LIKED_MULTIPLIER = 1.2;
	private static final double DISLIKED_MULTIPLIER = 0.8;

	private enum Reaction { LIKED, NEUTRAL, DISLIKED }

	private static Reaction reactionTo(ItemStack stack, Pokemon pokemon) {
		FlavourComponent flavours = stack.get(CobblemonItemComponents.FLAVOUR);
		if (flavours == null || flavours.getFlavours().isEmpty()) return Reaction.NEUTRAL;

		List<Flavour> dominant = flavours.getDominantFlavours();
		if (dominant.isEmpty()) return Reaction.NEUTRAL;
		Flavour flavour = dominant.getFirst();

		if (pokemon.getNature().getFavouriteFlavour() == flavour) return Reaction.LIKED;
		if (pokemon.getNature().getDislikedFlavour() == flavour) return Reaction.DISLIKED;
		return Reaction.NEUTRAL;
	}

	private static double multiplierFor(Reaction reaction) {
		return switch (reaction) {
			case LIKED -> LIKED_MULTIPLIER;
			case DISLIKED -> DISLIKED_MULTIPLIER;
			case NEUTRAL -> 1.0;
		};
	}

	private static void announceReaction(
			ServerPlayerEntity player,
			ServerWorld world,
			Pokemon pokemon,
			Reaction reaction,
			Map<Stat, Integer> granted
	) {
		var entity = pokemon.getEntity();
		if (entity == null) return;

		String suffix = switch (reaction) {
			case LIKED -> "love";
			case DISLIKED -> "dislike";
			case NEUTRAL -> "use";
		};

		MutableText gains = Text.empty();
		boolean first = true;
		for (Map.Entry<Stat, Integer> entry : granted.entrySet()) {
			if (!first) gains.append(Text.literal(", "));
			first = false;
			gains.append(Text.literal("+" + entry.getValue() + " ")).append(entry.getKey().getDisplayName());
		}

		player.sendMessage(Text.translatable("item.cobblecuisine.shake." + suffix, pokemon.getDisplayName(true), gains), false);

		double x = entity.getX();
		double y = entity.getY() + entity.getHeight();
		double z = entity.getZ();

		switch (reaction) {
			case LIKED -> world.spawnParticles(ParticleTypes.HEART, x, y, z, 10, 0.5, 0.5, 0.5, 0.1);
			case DISLIKED -> world.spawnParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, 5, 0.3, 0.3, 0.3, 0.05);
			case NEUTRAL -> world.spawnParticles(ParticleTypes.NOTE, x, y, z, 3, 0.4, 0.4, 0.4, 0.1);
		}
	}

	@Override
	public TypedActionResult<ItemStack> applyToPokemon(@NotNull ServerPlayerEntity player, @NotNull ItemStack stack, Pokemon pokemon) {
		if (stack.isEmpty() || !canUseOnPokemon(stack, pokemon)) return TypedActionResult.fail(stack);

		Map<Stat, Integer> wanted = new LinkedHashMap<>();
		if (evIncreaseAmount > 0) wanted.merge(stat, evIncreaseAmount, Integer::sum);
		stack.getOrDefault(CobbleCuisineDataComponents.SHAKE_EV, ShakeEvComponent.EMPTY)
				.extraEvs()
				.forEach((extraStat, amount) -> wanted.merge(extraStat, amount, Integer::sum));

		Reaction reaction = reactionTo(stack, pokemon);
		double preference = multiplierFor(reaction);
		Map<Stat, Integer> granted = new LinkedHashMap<>();

		for (Map.Entry<Stat, Integer> entry : wanted.entrySet()) {
			int amount = (int) Math.round(entry.getValue() * preference);
			if (amount <= 0) continue;

			Stat target = entry.getKey();
			amount = Math.min(amount, EVs.MAX_STAT_VALUE - pokemon.getEvs().getOrDefault(target));
			amount = Math.min(amount, EVs.MAX_TOTAL_VALUE - pokemon.getEvs().total());
			if (amount <= 0) continue;

			int added = pokemon.getEvs().add(target, amount, new SidemodEvSource("cobblecuisine", pokemon));
			if (added > 0) granted.merge(target, added, Integer::sum);
		}

		if (!granted.isEmpty()) {
			pokemon.onChange(null);

			CobbleCuisineUtils.feedAndFill(pokemon, stack);

			if (pokemon.getEntity() != null && pokemon.getEntity().getWorld() instanceof ServerWorld serverWorld) {
				announceReaction(player, serverWorld, pokemon, reaction, granted);
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
				return use(serverPlayer, stack, false);
			}
			return TypedActionResult.success(stack);
		} else {
			return super.use(world, user, hand);
		}
	}

}
