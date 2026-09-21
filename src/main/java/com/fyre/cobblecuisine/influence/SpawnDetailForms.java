package com.fyre.cobblecuisine.influence;

import com.cobblemon.mod.common.api.spawning.detail.PokemonHerdSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.pokemon.FormData;
import com.fyre.cobblecuisine.util.CobbleCuisineUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

final class SpawnDetailForms {
	private SpawnDetailForms() {
	}

	static boolean isPokemon(SpawnDetail detail) {
		return detail instanceof PokemonSpawnDetail || detail instanceof PokemonHerdSpawnDetail;
	}

	/** Definition weights only; native selection still controls roles and membership. */
	static float matchingFraction(SpawnDetail detail, Predicate<FormData> matches) {
		if (detail instanceof PokemonSpawnDetail single) {
			FormData form = CobbleCuisineUtils.resolveForm(single);
			return form == null ? -1.0f : matches.test(form) ? 1.0f : 0.0f;
		}
		if (!(detail instanceof PokemonHerdSpawnDetail herd)) return -1.0f;

		double matchingWeight = 0.0;
		double totalWeight = 0.0;
		for (PokemonHerdSpawnDetail.Herdable member : herd.getHerdablePokemon()) {
			FormData form = CobbleCuisineUtils.resolveForm(member.getPokemon());
			if (form == null) continue;
			float weight = Math.max(0.0f, member.getWeight());
			if (!Float.isFinite(weight)) continue;
			totalWeight += weight;
			if (matches.test(form)) matchingWeight += weight;
		}
		return totalWeight > 0.0 ? (float) (matchingWeight / totalWeight) : -1.0f;
	}

	static List<FormData> resolve(SpawnDetail detail) {
		List<FormData> forms = new ArrayList<>();
		if (detail instanceof PokemonSpawnDetail single) {
			FormData form = CobbleCuisineUtils.resolveForm(single);
			if (form != null) forms.add(form);
		} else if (detail instanceof PokemonHerdSpawnDetail herd) {
			for (PokemonHerdSpawnDetail.Herdable member : herd.getHerdablePokemon()) {
				FormData form = CobbleCuisineUtils.resolveForm(member.getPokemon());
				if (form != null) forms.add(form);
			}
		}
		return forms;
	}
}
