package com.fyre.cobblecuisine.client.gui;

import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.inventory.MixerMenu;
import com.fyre.cobblecuisine.inventory.MixerSeasoningSlot;
import com.fyre.cobblecuisine.util.MixerColors;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MixerScreen extends HandledScreen<MixerMenu> {
	private static final Identifier[] TEXTURES = {
			Identifier.of(CobbleCuisine.MOD_ID, "textures/gui/mixer_red.png"),
			Identifier.of(CobbleCuisine.MOD_ID, "textures/gui/mixer_blue.png"),
			Identifier.of(CobbleCuisine.MOD_ID, "textures/gui/mixer_green.png"),
			Identifier.of(CobbleCuisine.MOD_ID, "textures/gui/mixer_yellow.png"),
			Identifier.of(CobbleCuisine.MOD_ID, "textures/gui/mixer_pink.png"),
			Identifier.of(CobbleCuisine.MOD_ID, "textures/gui/mixer_white.png"),
			Identifier.of(CobbleCuisine.MOD_ID, "textures/gui/mixer_black.png")
	};

	private Identifier texture() {
		int i = this.handler.getColorIndex();
		return TEXTURES[i >= 0 && i < TEXTURES.length ? i : 0];
	}

	private final MixerRecipePanel recipePanel = new MixerRecipePanel();
	private boolean narrow;
	private ButtonWidget runButton;

	public MixerScreen(MixerMenu handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = 176;
		this.backgroundHeight = 174;
		this.playerInventoryTitleY = this.backgroundHeight - 94;
	}

	@Override
	protected void init() {
		super.init();
		this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

		this.narrow = this.width < 379;
		this.recipePanel.initialize(this.width, this.height, this.client, this.narrow, this.handler);
		this.x = this.recipePanel.findLeftEdge(this.width, this.backgroundWidth);

		this.addDrawableChild(new TexturedButtonWidget(this.x + 4, this.y + 37, 20, 18,
				MixerRecipePanel.BOOK_BUTTON_TEXTURES, button -> {
			this.recipePanel.toggleOpen();
			this.x = this.recipePanel.findLeftEdge(this.width, this.backgroundWidth);
			button.setPosition(this.x + 4, this.y + 37);
		}));

		this.runButton = ButtonWidget.builder(Text.literal("▶"), button -> {
			if (this.client != null && this.client.interactionManager != null) {
				this.client.interactionManager.clickButton(this.handler.syncId, MixerMenu.TOGGLE_RUNNING_BUTTON_ID);
			}
		}).dimensions(this.x + 61, this.y + 37, 18, 18).build();
		this.addDrawableChild(this.runButton);
	}

	private void updateRunButton() {
		if (this.runButton == null) return;
		boolean running = this.handler.isRunning();
		this.runButton.setMessage(Text.literal(running ? "■" : "▶"));
		Text tooltip;
		if (running && !this.handler.isPowered()) {
			tooltip = Text.translatable("gui.cobblecuisine.mixer.needs_power");
		} else {
			tooltip = Text.translatable(running ? "gui.cobblecuisine.mixer.stop" : "gui.cobblecuisine.mixer.start");
		}
		this.runButton.setTooltip(Tooltip.of(tooltip));
		this.runButton.setPosition(this.x + 61, this.y + 37);
	}

	@Override
	protected void handledScreenTick() {
		super.handledScreenTick();
		this.recipePanel.update();
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		Identifier texture = texture();
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, texture);

		int x = this.x;
		int y = (this.height - this.backgroundHeight) / 2;

		context.drawTexture(texture, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

		int progress = this.handler.getPropertyDelegate().get(0);
		int totalTime = this.handler.getPropertyDelegate().get(1);

		if (totalTime > 0 && progress > 0) {
			int mixerWidth = 64;
			int mixerHeight = 64;
			int lightsHeight = 16;
			int fillHeight = mixerHeight - lightsHeight;

			int screenX = x + 111;
			int screenY = y + 12;

			int textureU = 176;
			int textureV = 0;

			context.drawTexture(texture,
					screenX,
					screenY + fillHeight,
					textureU,
					textureV + fillHeight,
					mixerWidth,
					lightsHeight
			);

			int scaledHeight = (progress * fillHeight) / totalTime;

			int currentY = (screenY + fillHeight) - scaledHeight;
			int currentV = (textureV + fillHeight) - scaledHeight;

			setTint(getResultColor(progress));

			context.drawTexture(texture,
					screenX,
					currentY,
					textureU,
					currentV,
					mixerWidth,
					scaledHeight
			);

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	private static void setTint(int argb) {
		RenderSystem.setShaderColor(
				((argb >> 16) & 0xFF) / 255.0F,
				((argb >> 8) & 0xFF) / 255.0F,
				(argb & 0xFF) / 255.0F,
				1.0F
		);
	}

	private ItemStack tintSource(int currentProgress) {
		if (currentProgress > 0) return this.handler.getPreviewItem();
		return this.handler.slots.get(MixerMenu.RESULT_SLOT_INDEX).getStack();
	}

	private int getResultColor(int currentProgress) {
		ItemStack stack = tintSource(currentProgress);
		if (stack.isEmpty()) return 0xFFFFFFFF;

		Integer shakeTint = MixerColors.shakeTint(stack);
		return MixerColors.boost(shakeTint != null ? shakeTint : MixerColors.resultColour(stack));
	}


	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		updateRunButton();
		if (this.recipePanel.isOpen() && this.narrow) {
			this.renderBackground(context, mouseX, mouseY, delta);
			this.recipePanel.render(context, mouseX, mouseY, delta);
		} else {
			this.renderBackground(context, mouseX, mouseY, delta);
			super.render(context, mouseX, mouseY, delta);
			this.recipePanel.render(context, mouseX, mouseY, delta);
			this.recipePanel.drawGhostSlots(context, this.x, this.y, delta);
			renderResultPreview(context, mouseX, mouseY);
		}
		this.drawMouseoverTooltip(context, mouseX, mouseY);
		this.recipePanel.drawTooltip(context, this.x, this.y, mouseX, mouseY);
	}

	private void renderResultPreview(DrawContext context, int mouseX, int mouseY) {
		Slot resultSlot = this.handler.slots.get(MixerMenu.RESULT_SLOT_INDEX);
		if (resultSlot.hasStack() || this.handler.getActiveRecipe() == null) return;

		ItemStack preview = this.handler.getPreviewItem();
		if (preview.isEmpty()) return;

		int slotX = this.x + resultSlot.x;
		int slotY = this.y + resultSlot.y;

		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);

		context.drawItemWithoutEntity(preview, slotX, slotY);
		context.drawItemInSlot(this.textRenderer, preview, slotX, slotY);

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();

		if (isPointWithinBounds(resultSlot.x, resultSlot.y, 16, 16, mouseX, mouseY)
				&& this.handler.getCursorStack().isEmpty()) {
			context.drawTooltip(this.textRenderer, getTooltipFromItem(this.client, preview),
					preview.getTooltipData(), mouseX, mouseY);
		}
	}

	@Override
	protected void drawSlot(DrawContext context, Slot slot) {
		if (slot instanceof MixerSeasoningSlot
				&& (isRejectedSeasoning(this.handler.getCursorStack()) || isRejectedSeasoning(slot.getStack()))) {
			context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x30FF0000);
		}
		super.drawSlot(context, slot);
	}

	private boolean isRejectedSeasoning(ItemStack stack) {
		if (stack.isEmpty()) return false;
		var recipe = this.handler.getActiveRecipe();
		return recipe == null || !stack.isIn(recipe.value().getSeasoningTag());
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
		context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x404040, false);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.recipePanel.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if (this.narrow && this.recipePanel.isOpen()) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
		boolean outsideGui = mouseX < (double) left || mouseY < (double) top
				|| mouseX >= (double) (left + this.backgroundWidth)
				|| mouseY >= (double) (top + this.backgroundHeight);
		return this.recipePanel.isClickOutsideBounds(mouseX, mouseY, this.x, this.y,
				this.backgroundWidth, this.backgroundHeight) && outsideGui;
	}

	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		super.onMouseClick(slot, slotId, button, actionType);
		this.recipePanel.slotClicked(slot);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return this.recipePanel.keyPressed(keyCode, scanCode, modifiers)
				|| super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		return this.recipePanel.charTyped(chr, modifiers)
				|| super.charTyped(chr, modifiers);
	}
}
