package com.fyre.cobblecuisine.util;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.effect.CobbleCuisineEffects;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CobbleCuisineUtils {
	/**
	 * Resolves the form represented by a {@link PokemonSpawnDetail}.
	 *
	 * @param p The PokemonSpawnDetail instance.
	 * @return The resolved {@link FormData}, or {@code null} if unspecified.
	 */
	public static FormData resolveForm(PokemonSpawnDetail p) {
		String species = p.getPokemon().getSpecies();
		if (species == null) return null;

		Species resolved = PokemonSpecies.INSTANCE.getByIdentifier(species.indexOf(':') >= 0 ? Identifier.of(species) : Identifier.of("cobblemon", species));
		return resolved == null ? null : resolved.getForm(p.getPokemon().getAspects());
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
	public static List<Text> getItemTooltip(String name, @Nullable FoodComponent foodComponent, @Nullable Object[] args, int extraLines, @Nullable Object[] args1, @Nullable Object[] args2, @Nullable Object[] args3) {
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
			if (effect.getDuration() > 20) text = Text.translatable("potion.withDuration", text, StatusEffectUtil.getDurationText(effect, 1.0f, 20));

			list.add(text.formatted(Formatting.BLUE));
		}
		return Collections.unmodifiableList(list);
	}
}
