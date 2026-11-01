package com.fyre.cobblecuisine.block;

import com.fyre.cobblecuisine.block.entity.CobbleCuisineBlockEntities;
import com.fyre.cobblecuisine.block.entity.MixerBlockEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ItemScatterer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class MixerBlock extends BlockWithEntity {

	private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 14.25, 15);

	public static final BooleanProperty COOKING = BooleanProperty.of("cooking");
	public static final BooleanProperty HAS_RESULT = BooleanProperty.of("has_result");
	public static final BooleanProperty POWERED = BooleanProperty.of("powered");
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	private static final int COOKING_LUMINANCE = 12;

	private static final int IDLE_LUMINANCE = 7;

	public MixerBlock(Settings settings) {
		super(settings.luminance(state -> state.get(COOKING) ? COOKING_LUMINANCE : IDLE_LUMINANCE));
		setDefaultState(stateManager.getDefaultState()
				.with(COOKING, false)
				.with(HAS_RESULT, false)
				.with(POWERED, false)
				.with(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
		return true;
	}


	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new MixerBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
			World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : (w, p, s, be) -> ((MixerBlockEntity) be).serverTick();
	}


	@Override
	protected ActionResult onUse(BlockState state,
			World world,
			BlockPos pos,
			PlayerEntity player,
			BlockHitResult hit) {
		if (!world.isClient) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof MixerBlockEntity mixer) player.openHandledScreen(mixer);
		}
		return ActionResult.SUCCESS;
	}


	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.isOf(state.getBlock())) {
			updatePowered(world, pos, state);
		}
	}

	@Override
	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		updatePowered(world, pos, state);
	}

	private static void updatePowered(World world, BlockPos pos, BlockState state) {
		boolean powered = world.isReceivingRedstonePower(pos);
		if (powered != state.get(POWERED)) {
			world.setBlockState(pos, state.with(POWERED, powered), Block.NOTIFY_LISTENERS);
		}
	}


	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof MixerBlockEntity mixer) {
				ItemScatterer.spawn(world, pos, mixer);
				world.updateComparators(pos, this);
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}


	@Override
	protected boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
	}


	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(COOKING, HAS_RESULT, POWERED, FACING);
	}


	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(COOKING)) {
			double x = pos.getX() + 0.5;
			double y = pos.getY() + 0.8;
			double z = pos.getZ() + 0.5;

			if (random.nextInt(3) == 0) {
				world.addParticle(
						ParticleTypes.BUBBLE_POP,
						x + (random.nextDouble() - 0.5) * 0.4,
						y,
						z + (random.nextDouble() - 0.5) * 0.4,
						0, 0.08, 0
				);
			}

			if (random.nextInt(6) == 0) {
				world.addParticle(
						ParticleTypes.SPLASH,
						x + (random.nextDouble() - 0.5) * 0.35,
						y + 0.1,
						z + (random.nextDouble() - 0.5) * 0.35,
						0, 0.12, 0
				);
			}

			if (random.nextInt(15) == 0) {
				world.addParticle(
						ParticleTypes.CLOUD,
						x + (random.nextDouble() - 0.5) * 0.25,
						y + 0.4,
						z + (random.nextDouble() - 0.5) * 0.25,
						0, 0.03, 0
				);
			}
		}
	}


	public static final MapCodec<MixerBlock> CODEC = RecordCodecBuilder.mapCodec(i ->
			i.group(Settings.CODEC.fieldOf("settings").forGetter(Block::getSettings))
					.apply(i, MixerBlock::new));

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}
}