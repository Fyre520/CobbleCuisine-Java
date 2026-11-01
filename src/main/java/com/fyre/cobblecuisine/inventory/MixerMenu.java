package com.fyre.cobblecuisine.inventory;

import com.cobblemon.mod.common.api.cooking.Seasonings;
import com.fyre.cobblecuisine.block.entity.MixerBlockEntity;
import com.fyre.cobblecuisine.crafting.CobbleCuisineRecipeTypes;
import com.fyre.cobblecuisine.crafting.recipe.MixerShapelessRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MixerMenu extends AbstractRecipeScreenHandler<CraftingRecipeInput, MixerShapelessRecipe> {
	public static final int RESULT_SLOT_INDEX = 0;
	public static final int GRID_SLOTS_START = 1;
	public static final int GRID_SLOTS_END = 7;
	public static final int SEASONING_SLOTS_START = 7;
	public static final int SEASONING_SLOTS_END = 10;
	private static final int PLAYER_SLOTS_START = 10;
	private static final int PLAYER_SLOTS_END = 46;

	public static final int TOGGLE_RUNNING_BUTTON_ID = 0;

	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;
	private final World world;

	public MixerMenu(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(10), new ArrayPropertyDelegate(5));
	}

	public MixerMenu(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
		super(CobbleCuisineScreenHandlers.MIXER_MENU_TYPE, syncId);
		checkSize(inventory, 10);
		this.inventory = inventory;
		this.propertyDelegate = propertyDelegate;
		this.world = playerInventory.player.getWorld();
		inventory.onOpen(playerInventory.player);
		this.addProperties(propertyDelegate);

		this.addSlot(new MixerResultSlot(inventory, 9, 134, 31));

		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 2; ++col) {
				this.addSlot(new Slot(inventory, col + row * 2, 26 + col * 18, 20 + row * 18));
			}
		}

		this.addSlot(new MixerSeasoningSlot(this, inventory, 6, 80, 20));
		this.addSlot(new MixerSeasoningSlot(this, inventory, 7, 98, 38));
		this.addSlot(new MixerSeasoningSlot(this, inventory, 8, 80, 56));

		int playerInvY = 92;
		int playerInvX = 8;

		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, playerInvX + col * 18, playerInvY + row * 18));
			}
		}

		for (int col = 0; col < 9; ++col) {
			this.addSlot(new Slot(playerInventory, col, playerInvX + col * 18, playerInvY + 58));
		}
	}

	@Nullable
	public RecipeEntry<MixerShapelessRecipe> getActiveRecipe() {
		return world.getRecipeManager()
				.getFirstMatch(CobbleCuisineRecipeTypes.MIXER_SHAPELESS_RECIPE, createInput(), world)
				.orElse(null);
	}

	public ItemStack getPreviewItem() {
		RecipeEntry<MixerShapelessRecipe> recipeEntry = getActiveRecipe();
		if (recipeEntry == null) return ItemStack.EMPTY;

		MixerShapelessRecipe recipe = recipeEntry.value();
		ItemStack preview = recipe.craft(createInput(), world.getRegistryManager());

		List<ItemStack> seasonings = new ArrayList<>();
		for (int i = MixerBlockEntity.SEASONING_START; i <= MixerBlockEntity.SEASONING_END; i++) {
			ItemStack seasoning = inventory.getStack(i);
			if (!seasoning.isEmpty() && seasoning.isIn(recipe.getSeasoningTag())) {
				seasonings.add(seasoning);
			}
		}
		recipe.applySeasoning(preview, seasonings);
		return preview;
	}

	private CraftingRecipeInput createInput() {
		List<ItemStack> grid = new ArrayList<>(6);
		for (int i = MixerBlockEntity.CRAFTING_START; i <= MixerBlockEntity.CRAFTING_END; i++) {
			grid.add(inventory.getStack(i));
		}
		return CraftingRecipeInput.create(2, 3, grid);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int invSlot) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if (slot != null && slot.hasStack()) {
			ItemStack originalStack = slot.getStack();
			newStack = originalStack.copy();

			if (invSlot == RESULT_SLOT_INDEX) {
				if (!this.insertItem(originalStack, PLAYER_SLOTS_START, PLAYER_SLOTS_END, true)) {
					return ItemStack.EMPTY;
				}
			} else if (invSlot >= PLAYER_SLOTS_START) {
				if (Seasonings.INSTANCE.isSeasoning(originalStack)) {
					if (!this.insertItem(originalStack, GRID_SLOTS_START, GRID_SLOTS_END, false)
							&& !this.insertItem(originalStack, SEASONING_SLOTS_START, SEASONING_SLOTS_END, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.insertItem(originalStack, GRID_SLOTS_START, GRID_SLOTS_END, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItem(originalStack, PLAYER_SLOTS_START, PLAYER_SLOTS_END, false)) {
				return ItemStack.EMPTY;
			}

			if (originalStack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}

			if (originalStack.getCount() == newStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTakeItem(player, originalStack);
		}
		return newStack;
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (id == TOGGLE_RUNNING_BUTTON_ID) {
			if (this.inventory instanceof MixerBlockEntity mixer) {
				mixer.setRunning(!mixer.isRunning());
			}
			return true;
		}
		return false;
	}

	public boolean isRunning() {
		return this.propertyDelegate.get(2) != 0;
	}

	public boolean isPowered() {
		return this.propertyDelegate.get(4) != 0;
	}

	public int getColorIndex() {
		return this.propertyDelegate.get(3);
	}

	@Override
	public boolean canUse(PlayerEntity player) { return this.inventory.canPlayerUse(player); }

	public PropertyDelegate getPropertyDelegate() { return propertyDelegate; }


	@Override
	public void populateRecipeFinder(RecipeMatcher finder) {
		for (int i = MixerBlockEntity.CRAFTING_START; i <= MixerBlockEntity.SEASONING_END; i++) {
			finder.addUnenchantedInput(inventory.getStack(i));
		}
	}

	@Override
	public void clearCraftingSlots() {
		for (int i = MixerBlockEntity.CRAFTING_START; i <= MixerBlockEntity.CRAFTING_END; i++) {
			inventory.setStack(i, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean matches(RecipeEntry<MixerShapelessRecipe> recipeEntry) {
		Recipe<?> recipe = ((RecipeEntry<?>) recipeEntry).value();
		if (!(recipe instanceof MixerShapelessRecipe mixerRecipe)) return false;
		return mixerRecipe.matches(createInput(), world);
	}

	@Override
	public void fillInputSlots(boolean craftAll, RecipeEntry<?> recipe, ServerPlayerEntity player) {
		if (recipe.value() instanceof MixerShapelessRecipe) {
			super.fillInputSlots(craftAll, recipe, player);
		}
	}

	@Override
	public int getCraftingResultSlotIndex() {
		return RESULT_SLOT_INDEX;
	}

	@Override
	public int getCraftingWidth() {
		return 2;
	}

	@Override
	public int getCraftingHeight() {
		return 3;
	}

	@Override
	public int getCraftingSlotCount() {
		return 10;
	}

	@Override
	public RecipeBookCategory getCategory() {
		return RecipeBookCategory.CRAFTING;
	}

	@Override
	public boolean canInsertIntoSlot(int index) {
		return index < SEASONING_SLOTS_START || index >= SEASONING_SLOTS_END;
	}
}
