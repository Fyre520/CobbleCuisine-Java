package com.fyre.cobblecuisine.client.gui;

import com.cobblemon.mod.common.item.crafting.CookingPotBookCategory;
import com.fyre.cobblecuisine.crafting.CobbleCuisineRecipeTypes;
import com.fyre.cobblecuisine.crafting.recipe.MixerShapelessRecipe;
import com.fyre.cobblecuisine.inventory.MixerMenu;
import com.fyre.cobblecuisine.item.food.DrinkItem;
import com.fyre.cobblecuisine.item.food.FancyShakeItem;
import com.fyre.cobblecuisine.item.food.ShakeBlendMath;
import com.fyre.cobblecuisine.item.food.ShakeItem;
import com.fyre.cobblecuisine.item.food.ShakeType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeGridAligner;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MixerRecipePanel implements RecipeGridAligner<Ingredient> {
	private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/recipe_book.png");
	public static final ButtonTextures BOOK_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.ofVanilla("recipe_book/button"), Identifier.ofVanilla("recipe_book/button_highlighted"));
	private static final ButtonTextures FILTER_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.ofVanilla("recipe_book/filter_enabled"),
			Identifier.ofVanilla("recipe_book/filter_disabled"),
			Identifier.ofVanilla("recipe_book/filter_enabled_highlighted"),
			Identifier.ofVanilla("recipe_book/filter_disabled_highlighted"));
	private static final ButtonTextures PAGE_FORWARD_TEXTURES = new ButtonTextures(
			Identifier.ofVanilla("recipe_book/page_forward"), Identifier.ofVanilla("recipe_book/page_forward_highlighted"));
	private static final ButtonTextures PAGE_BACKWARD_TEXTURES = new ButtonTextures(
			Identifier.ofVanilla("recipe_book/page_backward"), Identifier.ofVanilla("recipe_book/page_backward_highlighted"));
	private static final ButtonTextures TAB_TEXTURES = new ButtonTextures(
			Identifier.ofVanilla("recipe_book/tab"), Identifier.ofVanilla("recipe_book/tab_selected"));
	private static final Identifier SLOT_CRAFTABLE_TEXTURE = Identifier.ofVanilla("recipe_book/slot_craftable");
	private static final Identifier SLOT_UNCRAFTABLE_TEXTURE = Identifier.ofVanilla("recipe_book/slot_uncraftable");

	private static final Text SEARCH_HINT_TEXT =
			Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
	private static final Text TOGGLE_CRAFTABLE_RECIPES_TEXT = Text.translatable("gui.recipebook.toggleRecipes.craftable");
	private static final Text TOGGLE_ALL_RECIPES_TEXT = Text.translatable("gui.recipebook.toggleRecipes.all");

	private static final int WIDTH = 147;
	private static final int HEIGHT = 166;
	private static final int RESULTS_PER_PAGE = 20;

	private static boolean sessionOpen = false;
	private static boolean sessionFilterCraftable = false;

	private static final class Tab extends ToggleButtonWidget {
		@Nullable final CookingPotBookCategory category;
		ItemStack icon = ItemStack.EMPTY;
		float bounce;

		Tab(@Nullable CookingPotBookCategory category) {
			super(0, 0, 35, 27, false);
			this.category = category;
			this.setTextures(TAB_TEXTURES);
		}

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			if (this.bounce > 0.0F) {
				float f = 1.0F + 0.1F * (float) Math.sin(this.bounce / 15.0F * (float) Math.PI);
				context.getMatrices().push();
				context.getMatrices().translate(this.getX() + 8, this.getY() + 12, 0.0F);
				context.getMatrices().scale(1.0F, f, 1.0F);
				context.getMatrices().translate(-(this.getX() + 8), -(this.getY() + 12), 0.0F);
			}

			Identifier tex = TAB_TEXTURES.get(true, this.toggled);
			int x = this.getX();
			if (this.toggled) x -= 2;
			context.drawGuiTexture(tex, x, this.getY(), this.width, this.height);
			int iconOffset = this.toggled ? -2 : 0;
			context.drawItemWithoutEntity(this.icon, this.getX() + 9 + iconOffset, this.getY() + 5);

			if (this.bounce > 0.0F) {
				context.getMatrices().pop();
				this.bounce -= delta;
			}
		}
	}

	private final class ResultButton extends ClickableWidget {
		@Nullable RecipeEntry<MixerShapelessRecipe> recipe;
		ItemStack result = ItemStack.EMPTY;
		boolean craftable;
		float bounce;

		ResultButton() {
			super(0, 0, 25, 25, Text.empty());
		}

		void show(RecipeEntry<MixerShapelessRecipe> recipe, ItemStack result, boolean craftable, boolean newlyDisplayed) {
			this.recipe = recipe;
			this.result = result;
			this.craftable = craftable;
			if (newlyDisplayed) this.bounce = 15.0F;
		}

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			if (this.recipe == null) return;
			boolean bouncing = this.bounce > 0.0F;
			if (bouncing) {
				float f = 1.0F + 0.1F * (float) Math.sin(this.bounce / 15.0F * (float) Math.PI);
				context.getMatrices().push();
				context.getMatrices().translate(this.getX() + 8, this.getY() + 12, 0.0F);
				context.getMatrices().scale(f, f, 1.0F);
				context.getMatrices().translate(-(this.getX() + 8), -(this.getY() + 12), 0.0F);
				this.bounce -= delta;
			}

			context.drawGuiTexture(this.craftable ? SLOT_CRAFTABLE_TEXTURE : SLOT_UNCRAFTABLE_TEXTURE,
					this.getX(), this.getY(), this.width, this.height);
			context.drawItemWithoutEntity(this.result, this.getX() + 4, this.getY() + 4);

			if (bouncing) {
				context.getMatrices().pop();
			}
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {
			this.appendDefaultNarrations(builder);
		}
	}

	private static final class GhostSlot {
		final Ingredient ingredient;
		final int x;
		final int y;

		GhostSlot(Ingredient ingredient, int x, int y) {
			this.ingredient = ingredient;
			this.x = x;
			this.y = y;
		}

		ItemStack currentStack(float time) {
			ItemStack[] stacks = this.ingredient.getMatchingStacks();
			return stacks.length == 0 ? ItemStack.EMPTY : stacks[MathHelper.floor(time / 30.0F) % stacks.length];
		}
	}

	private record Displayed(RecipeEntry<MixerShapelessRecipe> entry, ItemStack result, int tierOrder) {}

	private static int bookRank(ItemStack result) {
		if (result.getItem() instanceof DrinkItem) return 0;
		if (result.getItem() instanceof ShakeItem) return 1;
		if (result.getItem() instanceof FancyShakeItem) return 2;
		return 3;
	}

	private static List<ItemStack> possibleResults(RecipeEntry<MixerShapelessRecipe> entry) {
		ItemStack declared = entry.value().getResult();
		if (declared.getItem() instanceof ShakeItem shake) {
			ShakeBlendMath.Colour colour = ShakeBlendMath.colourFor(shake.stat());
			if (colour != null) {
				List<ItemStack> tiers = new ArrayList<>(3);
				for (int tier = 1; tier <= 3; tier++) {
					ShakeType type = ShakeBlendMath.shakeFor(colour, tier);
					if (type != null) tiers.add(new ItemStack(type.item));
				}
				if (!tiers.isEmpty()) return tiers;
			}
		}
		return List.of(declared);
	}

	private MinecraftClient client;
	private MixerMenu menu;
	private ClientRecipeBook recipeBook;
	private int parentWidth;
	private int parentHeight;
	private boolean narrow;
	private int leftOffset;

	private final List<Tab> tabs = new ArrayList<>();
	private Tab currentTab;
	private ToggleButtonWidget toggleCraftableButton;
	private TextFieldWidget searchField;
	private String searchText = "";
	private final List<ResultButton> resultButtons = new ArrayList<>();
	private ToggleButtonWidget nextPageButton;
	private ToggleButtonWidget prevPageButton;
	private int pageCount;
	private int currentPage;
	@Nullable private ResultButton hoveredResultButton;

	private final RecipeMatcher recipeFinder = new RecipeMatcher();
	private List<Displayed> filteredRecipes = List.of();
	private final Set<Identifier> craftableIds = new HashSet<>();
	private int cachedInvChangeCount;
	private boolean searching;

	@Nullable private RecipeEntry<MixerShapelessRecipe> ghostRecipe;
	private final List<GhostSlot> ghostSlots = new ArrayList<>();
	private float ghostTime;

	public MixerRecipePanel() {
		for (int i = 0; i < RESULTS_PER_PAGE; i++) {
			this.resultButtons.add(new ResultButton());
		}
	}

	public void initialize(int parentWidth, int parentHeight, MinecraftClient client, boolean narrow, MixerMenu menu) {
		this.client = client;
		this.parentWidth = parentWidth;
		this.parentHeight = parentHeight;
		this.menu = menu;
		this.narrow = narrow;
		this.recipeBook = client.player.getRecipeBook();
		this.cachedInvChangeCount = client.player.getInventory().getChangeCount();
		if (this.isOpen()) {
			this.reset();
		}
	}

	private void reset() {
		this.leftOffset = this.narrow ? 0 : 86;
		int i = (this.parentWidth - WIDTH) / 2 - this.leftOffset;
		int j = (this.parentHeight - HEIGHT) / 2;
		this.refreshInputMatcher();

		String previousSearch = this.searchField != null ? this.searchField.getText() : "";
		this.searchField = new TextFieldWidget(this.client.textRenderer, i + 25, j + 13, 81, 14,
				Text.translatable("itemGroup.search"));
		this.searchField.setMaxLength(50);
		this.searchField.setVisible(true);
		this.searchField.setEditableColor(0xFFFFFF);
		this.searchField.setText(previousSearch);
		this.searchField.setPlaceholder(SEARCH_HINT_TEXT);

		for (int k = 0; k < this.resultButtons.size(); k++) {
			this.resultButtons.get(k).setPosition(i + 11 + 25 * (k % 5), j + 31 + 25 * (k / 5));
		}
		this.nextPageButton = new ToggleButtonWidget(i + 93, j + 137, 12, 17, false);
		this.nextPageButton.setTextures(PAGE_FORWARD_TEXTURES);
		this.prevPageButton = new ToggleButtonWidget(i + 38, j + 137, 12, 17, true);
		this.prevPageButton.setTextures(PAGE_BACKWARD_TEXTURES);

		this.toggleCraftableButton = new ToggleButtonWidget(i + 110, j + 12, 26, 16, sessionFilterCraftable);
		this.toggleCraftableButton.setTextures(FILTER_BUTTON_TEXTURES);
		this.updateFilterTooltip();

		this.rebuildTabs();
		this.refreshResults(false);
		this.refreshTabPositions();
	}

	private void updateFilterTooltip() {
		this.toggleCraftableButton.setTooltip(
				this.toggleCraftableButton.isToggled() ? Tooltip.of(TOGGLE_CRAFTABLE_RECIPES_TEXT) : Tooltip.of(TOGGLE_ALL_RECIPES_TEXT));
	}

	private void rebuildTabs() {
		CookingPotBookCategory previous = this.currentTab != null ? this.currentTab.category : null;
		boolean hadTab = this.currentTab != null;
		this.tabs.clear();

		Tab searchTab = new Tab(null);
		searchTab.icon = new ItemStack(Items.COMPASS);
		this.tabs.add(searchTab);

		List<Displayed> all = this.allUnlockedRecipes();
		for (CookingPotBookCategory category : CookingPotBookCategory.values()) {
			ItemStack icon = ItemStack.EMPTY;
			for (Displayed displayed : all) {
				if (displayed.entry().value().getCategory() == category) {
					if (icon.isEmpty()) {
						icon = displayed.result();
					}
				}
			}
			if (!icon.isEmpty()) {
				Tab tab = new Tab(category);
				tab.icon = icon;
				this.tabs.add(tab);
			}
		}

		this.currentTab = null;
		if (hadTab) {
			for (Tab tab : this.tabs) {
				if (tab.category == previous) {
					this.currentTab = tab;
					break;
				}
			}
		}
		if (this.currentTab == null) {
			this.currentTab = this.tabs.get(0);
		}
		this.currentTab.setToggled(true);
	}

	private void refreshTabPositions() {
		int i = (this.parentWidth - WIDTH) / 2 - this.leftOffset - 30;
		int j = (this.parentHeight - HEIGHT) / 2 + 3;
		int l = 0;
		for (Tab tab : this.tabs) {
			tab.visible = true;
			tab.setPosition(i, j + 27 * l++);
		}
	}

	private List<Displayed> allUnlockedRecipes() {
		List<Displayed> list = new ArrayList<>();
		if (this.client.world == null) return list;
		for (RecipeEntry<MixerShapelessRecipe> entry :
				this.client.world.getRecipeManager().listAllOfType(CobbleCuisineRecipeTypes.MIXER_SHAPELESS_RECIPE)) {
			if (!this.recipeBook.contains(entry)) continue;
			List<ItemStack> results = possibleResults(entry);
			for (int i = 0; i < results.size(); i++) {
				list.add(new Displayed(entry, results.get(i), i));
			}
		}
		list.sort(Comparator
				.comparingInt((Displayed displayed) -> bookRank(displayed.result()))
				.thenComparing(displayed -> displayed.entry().value().getResult().getName().getString())
				.thenComparingInt(Displayed::tierOrder));
		return list;
	}

	private void refreshInputMatcher() {
		this.recipeFinder.clear();
		this.client.player.getInventory().populateRecipeFinder(this.recipeFinder);
		this.menu.populateRecipeFinder(this.recipeFinder);
	}

	private void refreshResults(boolean resetCurrentPage) {
		List<Displayed> list = this.allUnlockedRecipes();

		this.craftableIds.clear();
		List<RecipeEntry<MixerShapelessRecipe>> newlyDisplayed = new ArrayList<>();
		for (RecipeEntry<MixerShapelessRecipe> entry : list.stream().map(Displayed::entry).distinct().toList()) {
			if (this.recipeFinder.match(entry.value(), null)) {
				this.craftableIds.add(entry.id());
			}
			if (this.recipeBook.shouldDisplay(entry)) {
				newlyDisplayed.add(entry);
			}
		}

		if (this.currentTab != null && this.currentTab.category != null) {
			list.removeIf(displayed -> displayed.entry().value().getCategory() != this.currentTab.category);
		}
		String query = this.searchField != null ? this.searchField.getText().toLowerCase(Locale.ROOT).trim() : "";
		if (!query.isEmpty()) {
			list.removeIf(displayed -> !displayed.result().getName().getString().toLowerCase(Locale.ROOT).contains(query));
		}
		if (sessionFilterCraftable) {
			list.removeIf(displayed -> !this.craftableIds.contains(displayed.entry().id()));
		}

		this.filteredRecipes = list;
		this.pageCount = (int) Math.ceil(list.size() / (double) RESULTS_PER_PAGE);
		if (this.pageCount <= this.currentPage || resetCurrentPage) {
			this.currentPage = 0;
		}
		this.refreshResultButtons(newlyDisplayed);

		for (RecipeEntry<MixerShapelessRecipe> entry : newlyDisplayed) {
			this.client.player.onRecipeDisplayed(entry);
		}
	}

	private void refreshResultButtons(List<RecipeEntry<MixerShapelessRecipe>> newlyDisplayed) {
		int offset = RESULTS_PER_PAGE * this.currentPage;
		for (int i = 0; i < this.resultButtons.size(); i++) {
			ResultButton button = this.resultButtons.get(i);
			if (offset + i < this.filteredRecipes.size()) {
				Displayed displayed = this.filteredRecipes.get(offset + i);
				button.show(displayed.entry(), displayed.result(),
						this.craftableIds.contains(displayed.entry().id()),
						newlyDisplayed.contains(displayed.entry()));
				button.visible = true;
			} else {
				button.recipe = null;
				button.visible = false;
			}
		}
		this.nextPageButton.visible = this.pageCount > 1 && this.currentPage < this.pageCount - 1;
		this.prevPageButton.visible = this.pageCount > 1 && this.currentPage > 0;
	}


	public boolean isOpen() {
		return sessionOpen;
	}

	public void toggleOpen() {
		this.setOpen(!this.isOpen());
	}

	private void setOpen(boolean open) {
		if (open) {
			this.reset();
		}
		sessionOpen = open;
	}

	public int findLeftEdge(int width, int backgroundWidth) {
		return this.isOpen() && !this.narrow
				? 177 + (width - backgroundWidth - 200) / 2
				: (width - backgroundWidth) / 2;
	}

	private boolean isWide() {
		return this.leftOffset == 86;
	}


	public void update() {
		if (!this.isOpen()) return;
		if (this.cachedInvChangeCount != this.client.player.getInventory().getChangeCount()) {
			this.refreshInputs();
			this.cachedInvChangeCount = this.client.player.getInventory().getChangeCount();
		}
		if (this.ghostRecipe != null) {
			RecipeEntry<MixerShapelessRecipe> active = this.menu.getActiveRecipe();
			if (active != null && active.id().equals(this.ghostRecipe.id())) {
				this.clearGhostSlots();
			}
		}
	}

	private void refreshInputs() {
		this.refreshInputMatcher();
		this.refreshResults(false);
	}

	public void slotClicked(@Nullable Slot slot) {
		if (slot != null && slot.id < this.menu.getCraftingSlotCount()) {
			this.clearGhostSlots();
			if (this.isOpen()) {
				this.refreshInputs();
			}
		}
	}

	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (!this.isOpen()) return;
		context.getMatrices().push();
		context.getMatrices().translate(0.0F, 0.0F, 100.0F);
		int i = (this.parentWidth - WIDTH) / 2 - this.leftOffset;
		int j = (this.parentHeight - HEIGHT) / 2;
		context.drawTexture(TEXTURE, i, j, 1, 1, WIDTH, HEIGHT);
		this.searchField.render(context, mouseX, mouseY, delta);

		for (Tab tab : this.tabs) {
			tab.render(context, mouseX, mouseY, delta);
		}
		this.toggleCraftableButton.render(context, mouseX, mouseY, delta);

		if (this.pageCount > 1) {
			Text text = Text.translatable("gui.recipebook.page", this.currentPage + 1, this.pageCount);
			int textWidth = this.client.textRenderer.getWidth(text);
			context.drawText(this.client.textRenderer, text, i - textWidth / 2 + 73, j + 141, 0xFFFFFFFF, false);
		}
		this.hoveredResultButton = null;
		for (ResultButton button : this.resultButtons) {
			button.render(context, mouseX, mouseY, delta);
			if (button.visible && button.isSelected()) {
				this.hoveredResultButton = button;
			}
		}
		this.prevPageButton.render(context, mouseX, mouseY, delta);
		this.nextPageButton.render(context, mouseX, mouseY, delta);
		context.getMatrices().pop();
	}

	public void drawTooltip(DrawContext context, int x, int y, int mouseX, int mouseY) {
		if (this.isOpen() && this.client.currentScreen != null && this.hoveredResultButton != null
				&& this.hoveredResultButton.recipe != null) {
			context.drawTooltip(this.client.textRenderer,
					Screen.getTooltipFromItem(this.client, this.hoveredResultButton.result),
					mouseX, mouseY);
		}
		this.drawGhostSlotTooltip(context, x, y, mouseX, mouseY);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.isOpen() || this.client.player.isSpectator()) return false;

		for (ResultButton resultButton : this.resultButtons) {
			if (resultButton.visible && resultButton.recipe != null && resultButton.mouseClicked(mouseX, mouseY, button)) {
				if (button == 0) {
					this.onRecipeClicked(resultButton.recipe, resultButton.result);
				}
				return true;
			}
		}
		if (this.nextPageButton.visible && this.nextPageButton.mouseClicked(mouseX, mouseY, button)) {
			this.currentPage++;
			this.refreshResultButtons(List.of());
			return true;
		}
		if (this.prevPageButton.visible && this.prevPageButton.mouseClicked(mouseX, mouseY, button)) {
			this.currentPage--;
			this.refreshResultButtons(List.of());
			return true;
		}
		if (this.searchField.mouseClicked(mouseX, mouseY, button)) {
			this.searchField.setFocused(true);
			return true;
		}
		this.searchField.setFocused(false);
		if (this.toggleCraftableButton.mouseClicked(mouseX, mouseY, button)) {
			sessionFilterCraftable = !sessionFilterCraftable;
			this.toggleCraftableButton.setToggled(sessionFilterCraftable);
			this.updateFilterTooltip();
			this.refreshResults(false);
			return true;
		}
		for (Tab tab : this.tabs) {
			if (tab.mouseClicked(mouseX, mouseY, button)) {
				if (this.currentTab != tab) {
					if (this.currentTab != null) {
						this.currentTab.setToggled(false);
					}
					this.currentTab = tab;
					this.currentTab.setToggled(true);
					this.refreshResults(true);
				}
				return true;
			}
		}
		return false;
	}

	private void onRecipeClicked(RecipeEntry<MixerShapelessRecipe> entry, ItemStack result) {
		boolean craftable = this.craftableIds.contains(entry.id());
		if (!craftable && this.ghostRecipe != null && this.ghostRecipe.id().equals(entry.id())) {
			return;
		}
		this.clearGhostSlots();
		this.client.interactionManager.clickRecipe(this.menu.syncId, entry, Screen.hasShiftDown());
		if (!craftable) {
			this.showGhostRecipe(entry, result);
		}
		if (!this.isWide()) {
			this.setOpen(false);
		}
	}

	public boolean isClickOutsideBounds(double mouseX, double mouseY, int x, int y, int backgroundWidth, int backgroundHeight) {
		if (!this.isOpen()) return true;
		boolean outsideGui = mouseX < x || mouseY < y || mouseX >= x + backgroundWidth || mouseY >= y + backgroundHeight;
		boolean insidePanel = x - WIDTH < mouseX && mouseX < x && y < mouseY && mouseY < y + backgroundHeight;
		boolean onTab = false;
		for (Tab tab : this.tabs) {
			if (tab.isSelected()) {
				onTab = true;
				break;
			}
		}
		return outsideGui && !insidePanel && !onTab;
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		this.searching = false;
		if (!this.isOpen() || this.client.player.isSpectator()) return false;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE && !this.isWide()) {
			this.setOpen(false);
			return true;
		}
		if (this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
			this.refreshSearchResults();
			return true;
		}
		if (this.searchField.isFocused() && this.searchField.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
			return true;
		}
		if (this.client.options.chatKey.matchesKey(keyCode, scanCode) && !this.searchField.isFocused()) {
			this.searching = true;
			this.searchField.setFocused(true);
			return true;
		}
		return false;
	}

	public boolean charTyped(char chr, int modifiers) {
		if (this.searching) return false;
		if (!this.isOpen() || this.client.player.isSpectator()) return false;
		if (this.searchField.charTyped(chr, modifiers)) {
			this.refreshSearchResults();
			return true;
		}
		return false;
	}

	private void refreshSearchResults() {
		String query = this.searchField.getText().toLowerCase(Locale.ROOT);
		if (!query.equals(this.searchText)) {
			this.refreshResults(false);
			this.searchText = query;
		}
	}


	private void showGhostRecipe(RecipeEntry<MixerShapelessRecipe> entry, ItemStack result) {
		this.ghostRecipe = entry;
		this.ghostTime = 0.0F;
		Slot resultSlot = this.menu.slots.get(MixerMenu.RESULT_SLOT_INDEX);
		this.ghostSlots.add(new GhostSlot(Ingredient.ofStacks(result), resultSlot.x, resultSlot.y));
		this.alignRecipeToGrid(this.menu.getCraftingWidth(), this.menu.getCraftingHeight(),
				this.menu.getCraftingResultSlotIndex(), entry, entry.value().getIngredients().iterator(), 0);
	}

	@Override
	public void acceptAlignedInput(Ingredient ingredient, int slotIndex, int amount, int gridX, int gridY) {
		if (!ingredient.isEmpty()) {
			Slot slot = this.menu.slots.get(slotIndex);
			this.ghostSlots.add(new GhostSlot(ingredient, slot.x, slot.y));
		}
	}

	private void clearGhostSlots() {
		this.ghostRecipe = null;
		this.ghostSlots.clear();
		this.ghostTime = 0.0F;
	}

	public void drawGhostSlots(DrawContext context, int x, int y, float delta) {
		if (!Screen.hasControlDown()) {
			this.ghostTime += delta;
		}
		for (int i = 0; i < this.ghostSlots.size(); i++) {
			GhostSlot ghostSlot = this.ghostSlots.get(i);
			int j = ghostSlot.x + x;
			int k = ghostSlot.y + y;
			context.fill(j, k, j + 16, k + 16, 0x30FF0000);
			ItemStack stack = ghostSlot.currentStack(this.ghostTime);
			context.drawItemWithoutEntity(stack, j, k);
			context.fill(RenderLayer.getGuiGhostRecipeOverlay(), j, k, j + 16, k + 16, 0x30FFFFFF);
			if (i == 0) {
				context.drawItemInSlot(this.client.textRenderer, stack, j, k);
			}
		}
	}

	private void drawGhostSlotTooltip(DrawContext context, int x, int y, int mouseX, int mouseY) {
		ItemStack hovered = null;
		for (GhostSlot ghostSlot : this.ghostSlots) {
			int j = ghostSlot.x + x;
			int k = ghostSlot.y + y;
			if (mouseX >= j && mouseY >= k && mouseX < j + 16 && mouseY < k + 16) {
				hovered = ghostSlot.currentStack(this.ghostTime);
			}
		}
		if (hovered != null && this.client.currentScreen != null) {
			context.drawTooltip(this.client.textRenderer, Screen.getTooltipFromItem(this.client, hovered), mouseX, mouseY);
		}
	}
}
