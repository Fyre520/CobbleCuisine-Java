package com.fyre.cobblecuisine.block.entity;

import com.cobblemon.mod.common.api.cooking.Seasonings;
import com.cobblemon.mod.common.item.crafting.SeasoningProcessor;
import com.fyre.cobblecuisine.block.CobbleCuisineBlocks;
import com.fyre.cobblecuisine.block.MixerBlock;
import com.fyre.cobblecuisine.config.CobbleCuisineConfig;
import com.fyre.cobblecuisine.crafting.CobbleCuisineRecipeTypes;
import com.fyre.cobblecuisine.crafting.recipe.MixerShapelessRecipe;
import com.fyre.cobblecuisine.inventory.MixerMenu;
import com.fyre.cobblecuisine.util.MixerColors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MixerBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory, RecipeUnlocker {
	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(10, ItemStack.EMPTY);

	private static final Direction[] REMAINDER_OUTPUT_ORDER = {
			Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP
	};

	private final Map<Identifier, Integer> recipesUsed = new HashMap<>();
	@Nullable
	private RecipeEntry<?> lastRecipe;

	public static final int CRAFTING_START = 0;
	public static final int CRAFTING_END = 5;
	public static final int SEASONING_START = 6;
	public static final int SEASONING_END = 8;
	public static final int RESULT_SLOT = 9;

	private int cookingProgress = 0;
	private int cookingTime = 100;
	private boolean running = false;

	protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> cookingProgress;
				case 1 -> cookingTime;
				case 2 -> running ? 1 : 0;
				case 3 -> colorIndex();
				case 4 -> hasRequiredPower() ? 1 : 0;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			if (index == 0) cookingProgress = value;
			else if (index == 1) cookingTime = value;
			else if (index == 2) running = value != 0;
		}

		@Override
		public int size() {
			return 5;
		}
	};

	private int colorIndex() {
		Block block = getCachedState().getBlock();
		if (block == CobbleCuisineBlocks.MIXER_BLUE) return 1;
		if (block == CobbleCuisineBlocks.MIXER_GREEN) return 2;
		if (block == CobbleCuisineBlocks.MIXER_YELLOW) return 3;
		if (block == CobbleCuisineBlocks.MIXER_PINK) return 4;
		if (block == CobbleCuisineBlocks.MIXER_WHITE) return 5;
		if (block == CobbleCuisineBlocks.MIXER_BLACK) return 6;
		return 0;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
		markDirty();
	}

	public boolean isPowered() {
		World level = getWorld();
		return level != null && level.isReceivingRedstonePower(pos);
	}

	public boolean hasRequiredPower() {
		return !CobbleCuisineConfig.data.globalSettings.mixerRequiresRedstone || isPowered();
	}

	public MixerBlockEntity(BlockPos pos, BlockState state) {
		super(CobbleCuisineBlockEntities.MIXER_ENTITY_TYPE, pos, state);
	}

	@Nullable
	@Override
	public Object getRenderData() {
		Integer cookingColour = computeSeasonedResultColour();
		if (cookingColour != null) {
			return MixerColors.boost(cookingColour);
		}

		ItemStack result = items.get(RESULT_SLOT);
		if (!result.isEmpty()) {
			Integer resultTint = MixerColors.shakeTint(result);
			if (resultTint != null) return MixerColors.boost(resultTint);
			return MixerColors.boost(MixerColors.resultColour(result));
		}

		return 0xFFFFFFFF;
	}

	@Nullable
	private Integer computeSeasonedResultColour() {
		if (world == null) return null;
		CraftingRecipeInput input = CraftingRecipeInput.create(2, 3, items.subList(CRAFTING_START, CRAFTING_END + 1));
		RecipeEntry<MixerShapelessRecipe> recipeEntry = world.getRecipeManager()
				.getFirstMatch(CobbleCuisineRecipeTypes.MIXER_SHAPELESS_RECIPE, input, world)
				.orElse(null);
		if (recipeEntry == null) return null;

		MixerShapelessRecipe recipe = recipeEntry.value();
		ItemStack cooked = recipe.craft(input, world.getRegistryManager());

		List<ItemStack> seasonings = getTagFilteredSeasonings(recipe);
		recipe.applySeasoning(cooked, seasonings);

		Integer shakeTint = MixerColors.shakeTint(cooked);
		if (shakeTint != null) return shakeTint;
		return MixerColors.resultColour(cooked);
	}

	public void serverTick() {
		if (world == null) return;

		CraftingRecipeInput input = CraftingRecipeInput.create(2, 3, items.subList(CRAFTING_START, CRAFTING_END + 1));

		boolean active = running && hasRequiredPower();

		RecipeEntry<MixerShapelessRecipe> recipeEntry = !active ? null : world.getRecipeManager()
				.getFirstMatch(CobbleCuisineRecipeTypes.MIXER_SHAPELESS_RECIPE, input, world)
				.orElse(null);

		boolean isCooking = false;
		boolean crafted = false;

		if (recipeEntry != null) {
			MixerShapelessRecipe recipe = recipeEntry.value();
			ItemStack cooked = recipe.craft(input, world.getRegistryManager());
			recipe.applySeasoning(cooked, getTagFilteredSeasonings(recipe));

			ItemStack outSlot = items.get(RESULT_SLOT);
			boolean resultFits = outSlot.isEmpty()
					|| (ItemStack.areItemsAndComponentsEqual(outSlot, cooked)
						&& outSlot.getCount() + cooked.getCount() <= outSlot.getMaxCount());

			if (resultFits) {
				isCooking = true;
				cookingProgress++;

				if (cookingProgress % 20 == 0) {
					world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW,
							SoundCategory.BLOCKS, 0.3f, 1.2f);
				}

				if (cookingProgress >= cookingTime) {
					cookingProgress = 0;

					if (outSlot.isEmpty()) {
						items.set(RESULT_SLOT, cooked);
					} else {
						outSlot.increment(cooked.getCount());
					}

					consumeIngredients(recipe);
					setLastRecipe(recipeEntry);
					crafted = true;

					world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW,
							SoundCategory.BLOCKS, 0.5f, 1.5f);
				}
			}
		}

		if (!isCooking) {
			cookingProgress = 0;
		}

		boolean hasResult = !items.get(RESULT_SLOT).isEmpty();
		BlockState currentState = world.getBlockState(pos);
		if (currentState.get(MixerBlock.COOKING) != isCooking ||
				currentState.get(MixerBlock.HAS_RESULT) != hasResult) {
			world.setBlockState(pos, currentState
							.with(MixerBlock.COOKING, isCooking)
							.with(MixerBlock.HAS_RESULT, hasResult),
					Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
		}

		if (crafted) {
			onItemUpdate();
		} else if (isCooking) {
			markDirty();
		}
	}

	private List<ItemStack> getTagFilteredSeasonings(MixerShapelessRecipe recipe) {
		List<ItemStack> seasonings = new ArrayList<>();
		for (int i = SEASONING_START; i <= SEASONING_END; i++) {
			ItemStack seasoning = items.get(i);
			if (!seasoning.isEmpty() && seasoning.isIn(recipe.getSeasoningTag())) {
				seasonings.add(seasoning);
			}
		}
		return seasonings;
	}

	private void consumeIngredients(MixerShapelessRecipe recipe) {
		Map<Item, Integer> remainders = new LinkedHashMap<>();

		for (int i = CRAFTING_START; i <= CRAFTING_END; i++) {
			ItemStack stack = items.get(i);
			if (stack.isEmpty()) continue;
			if (stack.getItem().hasRecipeRemainder()) {
				remainders.merge(stack.getItem().getRecipeRemainder(), 1, Integer::sum);
			}
			stack.decrement(1);
			if (stack.isEmpty()) items.set(i, ItemStack.EMPTY);
		}

		for (int i = SEASONING_START; i <= SEASONING_END; i++) {
			ItemStack seasoning = items.get(i);
			if (seasoning.isEmpty() || !seasoning.isIn(recipe.getSeasoningTag())) continue;

			boolean shouldConsume = false;
			for (SeasoningProcessor processor : recipe.getSeasoningProcessors()) {
				if (processor.consumesItem(seasoning)) {
					shouldConsume = true;
					break;
				}
			}
			if (shouldConsume) {
				if (seasoning.getItem().hasRecipeRemainder()) {
					remainders.merge(seasoning.getItem().getRecipeRemainder(), 1, Integer::sum);
				}
				seasoning.decrement(1);
				if (seasoning.isEmpty()) items.set(i, ItemStack.EMPTY);
			}
		}

		for (Map.Entry<Item, Integer> remainder : remainders.entrySet()) {
			ItemStack remainderStack = new ItemStack(remainder.getKey(), remainder.getValue());

			for (Direction side : REMAINDER_OUTPUT_ORDER) {
				Inventory destination = HopperBlockEntity.getInventoryAt(world, pos.offset(side));
				if (destination == null) continue;
				remainderStack = HopperBlockEntity.transfer(this, destination, remainderStack, side.getOpposite());
				if (remainderStack.isEmpty()) break;
			}

			if (remainderStack.isEmpty()) continue;

			ItemEntity itemEntity = new ItemEntity(world,
					pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, remainderStack);
			itemEntity.setVelocity(0.0, 0.1, 0.0);
			world.spawnEntity(itemEntity);
		}
	}

	private void onItemUpdate() {
		markDirty();
		if (world != null && !world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
			world.updateComparators(pos, getCachedState().getBlock());
		}
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, RESULT_SLOT};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		if (dir == Direction.UP && Seasonings.INSTANCE.isSeasoning(stack)) {
			return slot >= SEASONING_START && slot <= SEASONING_END;
		}
		return slot >= CRAFTING_START && slot <= CRAFTING_END;
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return slot == RESULT_SLOT;
	}


	@Override
	public void setLastRecipe(@Nullable RecipeEntry<?> recipe) {
		if (recipe == null) return;
		recipesUsed.merge(recipe.id(), 1, Integer::sum);
		this.lastRecipe = recipe;
	}

	@Override
	public @Nullable RecipeEntry<?> getLastRecipe() {
		return this.lastRecipe;
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		return items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getStack(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack removed = Inventories.splitStack(items, slot, amount);
		if (!removed.isEmpty()) onItemUpdate();
		return removed;
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack removed = Inventories.removeStack(items, slot);
		if (!removed.isEmpty()) onItemUpdate();
		return removed;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		items.set(slot, stack);
		onItemUpdate();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return true;
	}

	@Override
	public void clear() {
		Collections.fill(items, ItemStack.EMPTY);
		onItemUpdate();
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("container.cobblecuisine.mixer");
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new MixerMenu(syncId, inv, this, propertyDelegate);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		Inventories.writeNbt(nbt, items, registries);
		nbt.putInt("CookingProgress", cookingProgress);
		nbt.putBoolean("Running", running);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		Collections.fill(items, ItemStack.EMPTY);
		Inventories.readNbt(nbt, items, registries);
		cookingProgress = nbt.getInt("CookingProgress");
		running = nbt.getBoolean("Running");
		if (world != null && world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
		}
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
		return createNbt(registries);
	}
}
