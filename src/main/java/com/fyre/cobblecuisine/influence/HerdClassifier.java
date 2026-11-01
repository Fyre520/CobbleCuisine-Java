package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.detail.PokemonHerdSpawnDetail;

public final class HerdClassifier {
	private HerdClassifier() {
	}

	public static Kind classify(PokemonHerdSpawnDetail detail) {
		return detail.getHerdablePokemon().stream()
				.anyMatch(member -> Boolean.TRUE.equals(member.getPokemon().isAlpha()))
				? Kind.ALPHA
				: Kind.ORDINARY;
	}

	public enum Kind {
		ORDINARY,
		ALPHA
	}
}
