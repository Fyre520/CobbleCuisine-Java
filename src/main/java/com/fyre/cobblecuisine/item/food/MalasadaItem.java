package com.fyre.cobblecuisine.item.food;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.cooking.Flavour; // Corrected import (Flavour with 'u')
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

public class MalasadaItem extends CobblemonItem implements PokemonSelectingItem, HealingSource {

    private final int friendshipAmount;
    private final Flavour flavour; // Changed to lowercase 'flavour' for convention
    private final List<Text> tooltips;

    public MalasadaItem(String name, Flavour flavour, FoodComponent foodComponent) {
        super(new Settings().food(foodComponent));
        this.friendshipAmount = CobbleCuisineConfig.data.itemSettings.malasadaFriendship;
        this.flavour = flavour; // Assignment is now correct
        this.tooltips = CobbleCuisineUtils.getItemTooltip(name, foodComponent, null, 3, new Object[]{friendshipAmount}, null, null);
    }

    @Override
    public BagItem getBagItem() {
        return null;
    }

    @Override
    public boolean canUseOnPokemon(@NotNull ItemStack stack, Pokemon pokemon) {
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
            return use(serverPlayer, stack, false);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public TypedActionResult<ItemStack> applyToPokemon(@NotNull ServerPlayerEntity player, @NotNull ItemStack stack, @NotNull Pokemon pokemon) {
        boolean effectApplied;

        int totalFriendshipAmount = calculateFriendshipIncrease(pokemon);
        // Increases friendship (persistent change)
        effectApplied = pokemon.incrementFriendship(totalFriendshipAmount, true);

        if (!pokemon.isFullHealth()) {
            int amountToHeal = Math.min(20, pokemon.getMaxHealth() - pokemon.getCurrentHealth());
            final int[] healAmountHolder = {amountToHeal};
            CobblemonEvents.POKEMON_HEALED.postThen(
                    new PokemonHealedEvent(pokemon, amountToHeal, this),
                    (event) -> Unit.INSTANCE,
                    (event) -> {
                        healAmountHolder[0] = event.getAmount();
                        return Unit.INSTANCE;
                    }
            );
            pokemon.setCurrentHealth(CobbleCuisineUtils.calculateHealedHealth(
                    pokemon.getCurrentHealth(), pokemon.getMaxHealth(), healAmountHolder[0]
            ));
            effectApplied = true;
        }

        if (effectApplied) {
            // FIX: Explicitly mark the Pokémon for saving after persistent changes (1.7 required)
            pokemon.onChange(null);

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
        tooltip.addAll(tooltips);
        super.appendTooltip(stack, context, tooltip, type);
    }

    private int calculateFriendshipIncrease(Pokemon pokemon) {
        if (flavour == null) return friendshipAmount;

        if (pokemon.getNature().getFavouriteFlavour() == flavour) return (int) (friendshipAmount * 1.5);
        else if (pokemon.getNature().getDislikedFlavour() == flavour) return (int) (friendshipAmount * 0.75);

        return friendshipAmount;
    }

    private boolean hasNatureFlavourMatch(Pokemon pokemon) {
        return flavour != null && pokemon.getNature().getFavouriteFlavour() == flavour;
    }

    // Removed DefaultImpls overrides
}
