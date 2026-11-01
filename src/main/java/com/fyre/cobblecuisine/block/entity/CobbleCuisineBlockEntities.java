package com.fyre.cobblecuisine.block.entity;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.block.CobbleCuisineBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class CobbleCuisineBlockEntities {

	public static final BlockEntityType<MixerBlockEntity> MIXER_ENTITY_TYPE =
			Registry.register(
					Registries.BLOCK_ENTITY_TYPE,
					Identifier.of(CobbleCuisine.MOD_ID, "mixer"),
					BlockEntityType.Builder.create(MixerBlockEntity::new,
							CobbleCuisineBlocks.MIXER_RED,
							CobbleCuisineBlocks.MIXER_BLUE,
							CobbleCuisineBlocks.MIXER_GREEN,
							CobbleCuisineBlocks.MIXER_YELLOW,
							CobbleCuisineBlocks.MIXER_PINK,
							CobbleCuisineBlocks.MIXER_WHITE,
							CobbleCuisineBlocks.MIXER_BLACK
					).build(null)
			);

	public static void register() {
	}
}