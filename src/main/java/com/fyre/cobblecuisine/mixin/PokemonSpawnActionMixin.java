package com.fyre.cobblecuisine.mixin;

import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.fyre.cobblecuisine.influence.StatInfluence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PokemonSpawnAction.class, remap = false)
public abstract class PokemonSpawnActionMixin {
	@Inject(target = @Desc(value = "createEntity", ret = PokemonEntity.class), at = @At("RETURN"))
	private void cobblecuisine$applyMealIvsBeforeBoosters(CallbackInfoReturnable<PokemonEntity> cir) {
		PokemonEntity pokemon = cir.getReturnValue();
		if (pokemon != null) StatInfluence.beforeSpawn((PokemonSpawnAction) (Object) this, pokemon);
	}
}
