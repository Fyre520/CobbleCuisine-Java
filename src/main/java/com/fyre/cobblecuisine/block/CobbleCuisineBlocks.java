package com.fyre.cobblecuisine.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.block.crop.BeanCropBlock;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public class CobbleCuisineBlocks {
	public static final Block BEAN_CROP_BLOCK = registerBlock("bean_crop",
			new BeanCropBlock(AbstractBlock.Settings.create()
							.noCollision()
							.ticksRandomly()
							.breakInstantly()
							.sounds(BlockSoundGroup.CROP)
							.pistonBehavior(PistonBehavior.DESTROY)
							.mapColor(MapColor.DARK_GREEN)
			)
	);

	public static final Block MIXER_RED = registerMixer("mixer_red");
	public static final Block MIXER_BLUE = registerMixer("mixer_blue");
	public static final Block MIXER_GREEN = registerMixer("mixer_green");
	public static final Block MIXER_YELLOW = registerMixer("mixer_yellow");
	public static final Block MIXER_PINK = registerMixer("mixer_pink");
	public static final Block MIXER_WHITE = registerMixer("mixer_white");
	public static final Block MIXER_BLACK = registerMixer("mixer_black");

	private static Block registerBlock(String name, Block block) {
		LOGGER.info("CobbleCuisine >> Registering block '{}'", name);
		return Registry.register(Registries.BLOCK, Identifier.of(CobbleCuisine.MOD_ID, name), block);
	}

	private static Block registerMixer(String name) {
		return registerBlock(name, new MixerBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)
				.strength(5.0f, 6.0f)
				.requiresTool()));
	}
}
