package com.fyre.cobblecuisine.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.healing.PokemonHealedEvent;
import com.cobblemon.mod.common.api.item.HealingSource;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import com.google.gson.JsonElement;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import kotlin.Unit;

import java.util.*;

public class CobbleCuisineUtils {
	public static int calculateHealedHealth(int currentHealth, int maxHealth, int healAmount) {
		if (healAmount == -1) return maxHealth;
		return (int) Math.min(maxHealth, (long) currentHealth + Math.max(0, healAmount));
	}

	public static boolean increaseFriendship(Pokemon pokemon, int amount) {
		int before = pokemon.getFriendship();
		if (amount <= 0 || before >= Cobblemon.INSTANCE.getConfig().getMaxPokemonFriendship()) return false;
		pokemon.incrementFriendship(amount, true);
		return pokemon.getFriendship() > before;
	}

	public static boolean healPokemon(Pokemon pokemon, int amount, HealingSource source) {
		if (pokemon.isFullHealth() || amount <= 0) return false;
		boolean[] healed = {false};
		int requested = Math.min(amount, pokemon.getMaxHealth() - pokemon.getCurrentHealth());
		CobblemonEvents.POKEMON_HEALED.postThen(
				new PokemonHealedEvent(pokemon, requested, source),
				event -> Unit.INSTANCE,
				event -> {
					int before = pokemon.getCurrentHealth();
					int after = calculateHealedHealth(before, pokemon.getMaxHealth(), event.getAmount());
					if (after > before) {
						pokemon.setCurrentHealth(after);
						healed[0] = pokemon.getCurrentHealth() > before;
					}
					return Unit.INSTANCE;
				}
		);
		return healed[0];
	}

	public static <T> List<T> shuffledCopy(List<T> source) {
		List<T> copy = new ArrayList<>(source);
		Collections.shuffle(copy);
		return copy;
	}

	/**
	 * Resolves the form represented by a {@link PokemonSpawnDetail}.
	 *
	 * @param p The PokemonSpawnDetail instance.
	 * @return The resolved {@link FormData}, or {@code null} if unspecified.
	 */
	public static FormData resolveForm(PokemonSpawnDetail p) {
		return resolveForm(p.getPokemon());
	}

	public static FormData resolveForm(PokemonProperties properties) {
		String species = properties.getSpecies();
		if (species == null) return null;

		Species resolved = PokemonSpecies.INSTANCE.getByIdentifier(species.indexOf(':') >= 0 ? Identifier.of(species) : Identifier.of("cobblemon", species));
		return resolved == null ? null : resolved.getForm(properties.getAspects());
	}

	public static final int FOOD_FULLNESS = 4;

	public static boolean fillsWhenFed(ItemStack stack) {
		if (stack == null) return false;
		List<JsonElement> entries = CobbleCuisineConfig.data.itemSettings.fullnessItems;
		if (entries == null || entries.isEmpty()) return false;

		for (JsonElement entry : entries) {
			if (entry == null || !entry.isJsonObject()) continue;
			JsonElement excluded = entry.getAsJsonObject().get("exclude");
			if (excluded == null || !excluded.isJsonArray()) continue;
			for (JsonElement item : excluded.getAsJsonArray()) {
				if (item != null && item.isJsonPrimitive() && matchesItem(stack, item.getAsString())) {
					return false;
				}
			}
		}

		for (JsonElement entry : entries) {
			if (entry == null || !entry.isJsonPrimitive()) continue;
			if (matchesItem(stack, entry.getAsString())) return true;
		}
		return false;
	}

	private static boolean matchesItem(ItemStack stack, String entry) {
		if (entry == null || entry.isBlank()) return false;
		String trimmed = entry.trim();
		if (trimmed.charAt(0) == '#') {
			Identifier tagId = Identifier.tryParse(trimmed.substring(1));
			return tagId != null && stack.isIn(TagKey.of(RegistryKeys.ITEM, tagId));
		}
		return Registries.ITEM.getId(stack.getItem()).toString().equals(trimmed);
	}

	public static boolean canFeed(Pokemon pokemon, ItemStack stack) {
		if (pokemon == null) return false;
		return !fillsWhenFed(stack) || !pokemon.isFull();
	}

	public static void feedAndFill(Pokemon pokemon, ItemStack stack) {
		if (pokemon == null || pokemon.isFull()) return;
		if (!fillsWhenFed(stack)) return;
		pokemon.feedPokemon(FOOD_FULLNESS, true);
	}

	/**
	 * Builds the localized tooltip lines for a given item.
	 * Status Effects from the {@link FoodComponent} are appended automatically.
	 * Arguments will be added to each line in the same order as they were passed.
	 *
	 * @param name The name of the item whose tooltip is being built.
	 * @param foodComponent The {@link FoodComponent}.
	 * @param extraLines The amount of extra lines to add to the tooltip, up to 3.
	 * @param args Optional formatting arguments for the base line.
	 * @param args1 Optional formatting arguments for the extra second line.
	 * @param args2 Optional formatting arguments for the extra third line.
	 * @param args3 Optional formatting arguments for the extra last line.
	 * @return An immutable list of {@link Text} representing the full tooltip.
	 * */
	// TODO: Make this DSL it begs for it
	public static List<Text> getItemTooltip(String name, @Nullable FoodComponent foodComponent, @Nullable Text effectValue, int bonusTicks) {
		return getItemTooltip(name, foodComponent, effectValue, bonusTicks, null, 0, null, null, null);
	}

	public static List<Text> getItemTooltip(String name, @Nullable FoodComponent foodComponent, @Nullable Object[] args, int extraLines, @Nullable Object[] args1, @Nullable Object[] args2, @Nullable Object[] args3) {
		return getItemTooltip(name, foodComponent, null, 0, args, extraLines, args1, args2, args3);
	}

	private static List<Text> getItemTooltip(String name, @Nullable FoodComponent foodComponent, @Nullable Text effectValue, int bonusTicks, @Nullable Object[] args, int extraLines, @Nullable Object[] args1, @Nullable Object[] args2, @Nullable Object[] args3) {
		String base = "tooltip." + CobbleCuisine.MOD_ID + ".item." + name;

		List<Text> list = new ArrayList<>();
		list.add(args != null ? Text.translatable(base, args) : Text.translatable(base));

		if (extraLines > 3) extraLines = 3;
		if (extraLines > 0) {
			Object[][] argsArray = { args1, args2, args3 };
			for (int i = 1; i <= extraLines; i++) {
				Object[] lineArgs = argsArray[i - 1];
				list.add(lineArgs != null ? Text.translatable(base + "." + i, lineArgs) : Text.translatable(base + "." + i));
			}
		}

		if (foodComponent == null) return Collections.unmodifiableList(list);

		for (int j = 0; j < foodComponent.effects().size(); j++) {
			StatusEffectInstance effect = foodComponent.effects().get(j).effect();
			RegistryEntry<StatusEffect> type = effect.getEffectType();
			if (
					type == CobbleCuisineEffects.EGG_BUFF_MARKER.entry ||
					type == CobbleCuisineEffects.NATURE_BUFF_MARKER.entry ||
					type == CobbleCuisineEffects.TYPE_BUFF_MARKER.entry ||
					type == CobbleCuisineEffects.YIELD_BUFF_MARKER.entry ||
					type == CobbleCuisineEffects.TERA_BUFF_MARKER.entry
			) continue;

			MutableText text = Text.translatable(effect.getTranslationKey());

			if (effect.getAmplifier() > 0) text = Text.translatable("potion.withAmplifier", text, Text.translatable("potion.potency." + effect.getAmplifier()));
			if (effectValue != null) text = Text.empty().append(text).append(effectValue);
			int duration = effect.getDuration() + bonusTicks;
			if (duration > 20) text = Text.translatable("potion.withDuration", text, StringHelper.formatTicks(duration, 20F));

			list.add(text.formatted(Formatting.BLUE));
		}
		return Collections.unmodifiableList(list);
	}
}
