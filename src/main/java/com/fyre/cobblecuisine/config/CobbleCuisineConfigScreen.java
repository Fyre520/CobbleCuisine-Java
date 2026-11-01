package com.fyre.cobblecuisine.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CobbleCuisineConfigScreen extends Screen {
	private final Screen parent;
	private ConfigListWidget configList;
	private ButtonWidget saveButton;

	public CobbleCuisineConfigScreen(Screen parent) {
		super(Text.literal("CobbleCuisine Config"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.clearChildren();

		int listWidth = 420;
		int listHeight = this.height - 110;
		int listLeft = (this.width - listWidth) / 2;
		int listTop = 40;
		int entryHeight = 25;

		configList = new ConfigListWidget(this.client, listWidth, listHeight, listTop, entryHeight);
		configList.setX(listLeft);
		this.addSelectableChild(configList);

		try {
			Object root = CobbleCuisineConfig.data;
			populateFieldsRecursive(root, "");
		} catch (Exception e) {
			throw new RuntimeException("cobbleCuisine >> Failed to build config screen!", e);
		}

		saveButton = ButtonWidget.builder(Text.literal("Save & Exit"), btn -> {
					for (ConfigListWidget.ConfigEntry entry : configList.children()) {
						try {
							String txt = entry.input.getText();
							Field f = entry.field;
							Object ctr = entry.container;
							Class<?> type = f.getType();
							if (type == int.class) f.setInt(ctr, Integer.parseInt(txt));
							else if (type == float.class) f.setFloat(ctr, Float.parseFloat(txt));
							else if (type == double.class) f.setDouble(ctr, Double.parseDouble(txt));
							else if (Number.class.isAssignableFrom(type)) f.set(ctr, type.getConstructor(String.class).newInstance(txt));
						} catch (Exception ignored) {}
					}
					CobbleCuisineConfig.save(CobbleCuisineConfig.data);
					if (client != null) client.setScreen(parent);
				})
				.dimensions(this.width/2 - 50, this.height - 40, 100, 20)
				.build();
		this.addDrawableChild(saveButton);
	}

	private void populateFieldsRecursive(Object container, String pathPrefix) throws IllegalAccessException {
		Class<?> cls = container.getClass();
		for (Field f : cls.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers())) continue;
			f.setAccessible(true);
			Object val = f.get(container);
			if (val == null) continue;

			String keyName = pathPrefix.isEmpty() ? f.getName() : (pathPrefix + "." + f.getName());

			if (val instanceof Number) {
				String transKey = "config.cobblecuisine." + keyName.toLowerCase();
				Text label = Text.translatable(transKey);
				TextFieldWidget input = new TextFieldWidget(this.textRenderer, 0, 0, 100, 20, label);
				input.setText(val.toString());
				this.addDrawableChild(input);
				this.addSelectableChild(input);
				configList.addConfigEntry(new ConfigListWidget.ConfigEntry(label, input, f, container, textRenderer));
			} else {
				populateFieldsRecursive(val, keyName);
			}
		}
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderBackground(ctx, mouseX, mouseY, delta);
		ctx.drawCenteredTextWithShadow(textRenderer, this.title, this.width/2, 10, 0xFFFFFF);
		configList.render(ctx, mouseX, mouseY, delta);
		saveButton.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double hx, double vy) {
		return configList.mouseScrolled(mx, my, hx, vy) || super.mouseScrolled(mx, my, hx, vy);
	}

	@Override
	public boolean shouldPause() { return false; }

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (ConfigListWidget.ConfigEntry entry : configList.children()) {
			if (entry.input.mouseClicked(mouseX, mouseY, button)) {
				this.setFocused(entry.input);
				return true;
			}
		}
		this.setFocused(null);
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.getFocused() instanceof TextFieldWidget tf)
			if (tf.keyPressed(keyCode, scanCode, modifiers)) return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (this.getFocused() instanceof TextFieldWidget tf)
			if (tf.charTyped(chr, modifiers)) return true;
		return super.charTyped(chr, modifiers);
	}

	private static class ConfigListWidget extends EntryListWidget<ConfigListWidget.ConfigEntry> {
		public ConfigListWidget(MinecraftClient c, int w, int h, int top, int itemH) { super(c, w, h, top, itemH); }
		@Override public int getRowLeft() { return this.getX() + 10; }
		@Override protected int getScrollbarX() { return this.getX() + this.getWidth(); }
		@Override protected void appendClickableNarrations(NarrationMessageBuilder b) { }

		void addConfigEntry(ConfigEntry e) { this.addEntry(e); }

		public static class ConfigEntry extends Entry<ConfigEntry> {
			final Text label; final TextFieldWidget input;
			final Field field; final Object container; final TextRenderer tr;

			public ConfigEntry(Text lab, TextFieldWidget in, Field f, Object ctr, TextRenderer tr) {
				this.label = lab; this.input = in; this.field = f; this.container = ctr; this.tr = tr;
			}

			@Override
			public void render(DrawContext ctx, int idx, int y, int x, int w, int h, int mx, int my, boolean hov, float delta) {
				int labelY = y + (h - 9) / 2;
				ctx.drawText(tr, label, x, labelY, 0xFFFFFF, false);
				input.setX(x + 250);
				input.setY(y + (h - 20) / 2);
				input.render(ctx, mx, my, delta);
			}

			@Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return input.mouseClicked(mouseX, mouseY, button); }
			@Override public boolean charTyped(char chr, int modifiers) { return input.charTyped(chr, modifiers); }
			@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return input.keyPressed(keyCode, scanCode, modifiers); }
			@Override public boolean isMouseOver(double mx, double my) { return input.isMouseOver(mx,my); }
		}
	}
}
