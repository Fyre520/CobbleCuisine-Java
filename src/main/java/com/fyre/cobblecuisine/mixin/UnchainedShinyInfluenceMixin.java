package com.fyre.cobblecuisine.mixin;

import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.fyre.cobblecuisine.influence.StatInfluence;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "us.timinc.mc.cobblemon.unchained.booster.ShinyBooster$ShinyBoosterInfluence", remap = false)
public abstract class UnchainedShinyInfluenceMixin {
	@Inject(
			target = @Desc(value = "affectSpawn", args = {SpawnAction.class, Entity.class}),
			at = @At("HEAD"), cancellable = true
	)
	private void cobblecuisine$preserveExplicitShiny(SpawnAction<?> action, Entity entity, CallbackInfo ci) {
		if (action instanceof PokemonSpawnAction pokemonAction && entity instanceof PokemonEntity
				&& StatInfluence.isMealSpawn(pokemonAction) && pokemonAction.getProps().getShiny() != null) {
			ci.cancel();
		}
	}
}
