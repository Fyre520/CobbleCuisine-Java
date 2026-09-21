package com.fyre.cobblecuisine.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.healing.PokemonHealedEvent;
import com.cobblemon.mod.common.api.item.HealingSource;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;

/** Shared feeding mutations that report whether a Pokemon actually benefited. */
public final class PokemonFeeding {
    private PokemonFeeding() {}

	public static int calculateHealedHealth(int currentHealth, int maxHealth, int healAmount) {
		if (healAmount == -1) return maxHealth;
		return (int) Math.min(maxHealth, (long) currentHealth + Math.max(0, healAmount));
	}

	/** Cobblemon's friendship result can be true even when no friendship was gained. */
	public static boolean increaseFriendship(Pokemon pokemon, int amount) {
		int before = pokemon.getFriendship();
		if (amount <= 0 || before >= Cobblemon.INSTANCE.getConfig().getMaxPokemonFriendship()) return false;
		pokemon.incrementFriendship(amount, true);
		return pokemon.getFriendship() > before;
	}

	/** Apply only accepted healing and report an actual HP gain to the feeding caller. */
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

}
