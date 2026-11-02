package com.fyre.cobblecuisine.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CobbleCuisineConfigScreen extends Screen {
	private static final int CONTROL_WIDTH = 150;
	private static final int RESET_WIDTH = 40;
	private static final int HELP_WIDTH = 14;
	private static final double PERCENT_STEP = 0.0025;
	private static final int ROW_HEIGHT = 24;
	private static final int TICKS_PER_SECOND = 20;
	private static final int SUGGESTION_HEIGHT = 20;
	private static final int MAX_SUGGESTIONS = 8;
	private static final Map<String, String> ACRONYMS = Map.of("iv", "IV", "ivs", "IVs", "exp", "EXP", "ev", "EV");

	private final Screen parent;
	private final List<Row> rows = new ArrayList<>();
	private ConfigList configList;
	private Text saveError;
	private CobbleCuisineConfigData candidate;
	private boolean classicShown;
	private ItemListRow expandedRow;
	private boolean expandedIncluded;
	private SearchRow searchRow;
	private ItemCatalog catalog;
	private List<ItemCatalog.Candidate> suggestions = List.of();
	private int highlighted;
	private final List<Tab> tabs = new ArrayList<>();
	private int currentTab;
	private int buildingTab;
	private final List<ButtonWidget> tabButtons = new ArrayList<>();
	private final List<Integer> tabButtonTargets = new ArrayList<>();
	private final List<String> tabButtonLabels = new ArrayList<>();
	private List<Integer> shownTabs = List.of();
	private boolean tabBarDirty = true;

	private record Tab(String id, Text title, Text shortTitle, Mode mode) {
	}

	public CobbleCuisineConfigScreen(Screen parent) {
		super(Text.literal("CobbleCuisine Config"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		saveError = null;
		int listWidth = Math.min(this.width - 20, 480);
		configList = new ConfigList(this.client, listWidth, this.height - 104, 46, ROW_HEIGHT);
		configList.setX((this.width - listWidth) / 2);

		if (candidate == null) {
			candidate = CobbleCuisineConfig.copyData();
			try {
				populate(candidate, new CobbleCuisineConfigData(), "", 0, Mode.ALWAYS);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("CobbleCuisine >> Failed to build config screen!", e);
			}
			classicShown = candidate.globalSettings.classicMealPowers;
		}
		tabButtons.clear();
		tabBarDirty = true;
		refreshVisibility(classicShown);
		this.addDrawableChild(configList);
		rebuildTabBar();

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Exit"), button -> save(candidate))
				.dimensions(this.width / 2 - 104, this.height - 32, 100, 20)
				.build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> close())
				.dimensions(this.width / 2 + 4, this.height - 32, 100, 20)
				.build());
	}

	private void save(CobbleCuisineConfigData candidate) {
		boolean valid = true;
		for (Row row : rows) {
			if (row instanceof ValueRow valueRow && !valueRow.apply()) valid = false;
		}
		if (!valid) {
			saveError = Text.literal("Invalid number. Settings were not applied.");
			return;
		}
		if (!CobbleCuisineConfig.save(candidate)) {
			saveError = Text.literal("Save failed. Settings were not applied; see log.");
			return;
		}
		close();
	}

	@Override
	public void close() {
		if (this.client != null) this.client.setScreen(parent);
	}

	private void populate(Object container, Object defaults, String path, int depth, Mode inherited) throws ReflectiveOperationException {
		for (Field field : container.getClass().getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())
					|| field.isAnnotationPresent(CobbleCuisineConfigData.HiddenFromEditor.class)) continue;
			field.setAccessible(true);
			Object value = field.get(container);
			if (value == null) continue;
			String key = path.isEmpty() ? field.getName() : path + "." + field.getName();
			Object defaultValue = field.get(defaults);
			Mode mode = field.isAnnotationPresent(CobbleCuisineConfigData.ClassicOnly.class) ? Mode.CLASSIC
					: field.isAnnotationPresent(CobbleCuisineConfigData.StandardOnly.class) ? Mode.STANDARD
					: inherited;

			if (value instanceof Number || value instanceof Boolean) {
				ValueRow valueRow = valueRow(field, container, defaultValue, label(key, field.getName()));
				valueRow.describe(description(key, valueRow.label));
				valueRow.mode = mode;
				addRow(valueRow);
				continue;
			}
			if (value instanceof CobbleCuisineConfigData.FullnessItems) {
				Text label = label(key, field.getName());
				ItemListRow listRow = new ItemListRow(label, field, container, defaultValue, textRenderer, this::toggleList, this::listChanged);
				listRow.describe(description(key, label));
				listRow.mode = mode;
				addRow(listRow);
				continue;
			}
			if (depth == 0) {
				CobbleCuisineConfigData.EditorTab grouped = field.getAnnotation(CobbleCuisineConfigData.EditorTab.class);
				String tabId = grouped != null ? grouped.value() : field.getName();
				buildingTab = tabIndex(tabId);
				if (buildingTab < 0) {
					Text title = tabId.equals(field.getName()) ? sectionTitle(key, field.getName())
							: Text.translatableWithFallback("config.cobblecuisine.tab." + tabId.toLowerCase(), humanize(tabId));
					Text shortTitle = Text.translatableWithFallback("config.cobblecuisine.tab.short." + tabId.toLowerCase(), title.getString());
					tabs.add(new Tab(tabId, title, shortTitle, mode));
					buildingTab = tabs.size() - 1;
				}
				if (grouped != null) {
					HeaderRow header = new HeaderRow(sectionTitle(key, field.getName()), true, textRenderer);
					header.mode = mode;
					addRow(header);
				}
			} else if (!field.isAnnotationPresent(CobbleCuisineConfigData.EditorInline.class)) {
				HeaderRow header = new HeaderRow(sectionTitle(key, field.getName()), false, textRenderer);
				header.mode = mode;
				addRow(header);
			}
			populate(value, defaultValue, key, depth + 1, mode);
		}
	}

	private void toggleList(ItemListRow row, boolean included) {
		boolean closing = expandedRow == row && expandedIncluded == included;
		if (expandedRow != null) expandedRow.setExpanded(null);
		expandedRow = closing ? null : row;
		expandedIncluded = included;
		suggestions = List.of();
		if (expandedRow != null) {
			if (catalog == null) catalog = ItemCatalog.build();
			searchRow = new SearchRow();
			expandedRow.setExpanded(included);
		} else {
			searchRow = null;
		}
		refreshVisibility(classicShown);
	}

	private void addRow(Row row) {
		row.tab = buildingTab;
		rows.add(row);
	}

	private int tabIndex(String id) {
		for (int index = 0; index < tabs.size(); index++) {
			if (tabs.get(index).id().equals(id)) return index;
		}
		return -1;
	}

	private boolean tabVisible(int tab) {
		Mode mode = tabs.get(tab).mode();
		return mode == Mode.ALWAYS || (mode == Mode.CLASSIC) == classicShown;
	}

	private List<Integer> visibleTabs() {
		List<Integer> visible = new ArrayList<>();
		for (int tab = 0; tab < tabs.size(); tab++) {
			if (tabVisible(tab)) visible.add(tab);
		}
		return visible;
	}

	private void selectTab(int tab) {
		currentTab = tab;
		refreshVisibility(classicShown);
		configList.setScrollAmount(0);
	}

	private void rebuildTabBar() {
		tabButtons.forEach(this::remove);
		tabButtons.clear();
		tabButtonTargets.clear();
		tabButtonLabels.clear();
		shownTabs = visibleTabs();
		int count = shownTabs.size();
		if (count == 0) return;
		int gap = 2;
		int available = this.width - 20 - gap * (count - 1);
		int[] widths = new int[count];
		int natural = 0;
		for (int index = 0; index < count; index++) {
			widths[index] = textRenderer.getWidth(tabs.get(shownTabs.get(index)).shortTitle()) + 12;
			natural += widths[index];
		}
		if (natural > available) {
			for (int index = 0; index < count; index++) widths[index] = Math.max(24, widths[index] * available / natural);
		}
		int total = gap * (count - 1);
		for (int width : widths) total += width;
		int x = (this.width - total) / 2;
		for (int index = 0; index < count; index++) {
			int tab = shownTabs.get(index);
			String label = tabs.get(tab).shortTitle().getString();
			if (textRenderer.getWidth(label) > widths[index] - 8) {
				label = textRenderer.trimToWidth(label, widths[index] - 8 - textRenderer.getWidth("…")) + "…";
			}
			ButtonWidget button = ButtonWidget.builder(Text.literal(label), widget -> selectTab(tab))
					.dimensions(x, 6, widths[index], 20)
					.build();
			tabButtons.add(this.addDrawableChild(button));
			tabButtonTargets.add(tab);
			tabButtonLabels.add(label);
			x += widths[index] + gap;
		}
		updateTabLabels();
		tabBarDirty = false;
	}

	private void updateTabLabels() {
		for (int index = 0; index < tabButtons.size(); index++) {
			boolean selected = tabButtonTargets.get(index) == currentTab;
			tabButtons.get(index).setMessage(Text.literal(tabButtonLabels.get(index)).formatted(selected ? Formatting.GOLD : Formatting.WHITE));
		}
	}

	private void refreshVisibility(boolean classic) {
		classicShown = classic;
		if (!tabs.isEmpty() && !tabVisible(currentTab)) {
			List<Integer> visible = visibleTabs();
			int replacement = visible.isEmpty() ? 0 : visible.getFirst();
			for (int tab : visible) {
				if (tab < currentTab) replacement = tab;
			}
			currentTab = replacement;
		}
		if (!visibleTabs().equals(shownTabs)) tabBarDirty = true;
		updateTabLabels();
		List<Row> visible = new ArrayList<>();
		for (Row row : rows) {
			if (row.tab != currentTab) continue;
			if (row.mode != Mode.ALWAYS && (row.mode == Mode.CLASSIC) != classic) continue;
			visible.add(row);
			if (row == expandedRow) {
				visible.add(searchRow);
				List<ListEntry> entries = expandedList();
				for (ListEntry entry : entries) visible.add(new EntryRow(entry, entries));
			}
		}
		configList.show(visible);
	}

	private List<ListEntry> expandedList() {
		return expandedIncluded ? expandedRow.included : expandedRow.excluded;
	}

	private void listChanged() {
		if (expandedRow != null) expandedRow.updateMessage();
		refreshVisibility(classicShown);
	}

	private void updateSuggestions() {
		String query = searchRow == null ? "" : searchRow.field.getText();
		suggestions = query.isBlank() || catalog == null ? List.of() : catalog.search(query, MAX_SUGGESTIONS);
		highlighted = 0;
	}

	private boolean suggestionsVisible() {
		return searchRow != null && searchRow.shown && searchRow.field.isFocused() && !suggestions.isEmpty();
	}

	private void addEntry(String value) {
		String trimmed = value.trim();
		if (trimmed.isEmpty() || expandedRow == null) return;
		List<ListEntry> target = expandedList();
		if (target.stream().noneMatch(entry -> entry.value.trim().equalsIgnoreCase(trimmed))) target.add(new ListEntry(trimmed));
		searchRow.field.setText("");
		suggestions = List.of();
		listChanged();
		configList.setFocused(searchRow);
		searchRow.setFocused(searchRow.field);
		searchRow.field.setFocused(true);
	}

	private ValueRow valueRow(Field field, Object container, Object defaultValue, Text label) throws IllegalAccessException {
		Class<?> type = field.getType();
		CobbleCuisineConfigData.EditorRange range = field.getAnnotation(CobbleCuisineConfigData.EditorRange.class);
		if (type == boolean.class) {
			boolean toggle = field.isAnnotationPresent(CobbleCuisineConfigData.ClassicToggle.class);
			return new BooleanRow(label, field, container, defaultValue, textRenderer, toggle ? this::refreshVisibility : value -> { });
		}
		CobbleCuisineConfigData.EditorOptions options = field.getAnnotation(CobbleCuisineConfigData.EditorOptions.class);
		if (options != null && type == int.class) {
			return new OptionRow(label, field, container, defaultValue, options.value(), textRenderer);
		}
		if (range != null && (type == int.class || type == float.class || type == double.class)) {
			return new SliderRow(label, field, container, defaultValue, range.min(), range.max(), textRenderer);
		}
		boolean seconds = type == int.class && field.isAnnotationPresent(CobbleCuisineConfigData.EditorTicksAsSeconds.class);
		return new TextRow(label, field, container, defaultValue, seconds ? TICKS_PER_SECOND : 1, textRenderer);
	}

	private static Text label(String key, String fieldName) {
		String translationKey = "config.cobblecuisine." + key.toLowerCase();
		return I18n.hasTranslation(translationKey) ? Text.translatable(translationKey) : Text.literal(humanize(fieldName));
	}

	private static Text description(String key, Text fallback) {
		String exact = "config.cobblecuisine." + key.toLowerCase() + ".description";
		if (I18n.hasTranslation(exact)) return Text.translatable(exact);
		String[] parts = key.split("\\.");
		if (parts.length == 3) {
			String shared = "config.cobblecuisine." + parts[0].toLowerCase() + ".*." + parts[2].toLowerCase() + ".description";
			if (I18n.hasTranslation(shared)) return Text.translatable(shared, sectionTitle(parts[0] + "." + parts[1], parts[1]));
		}
		return fallback;
	}

	private static Text sectionTitle(String key, String fieldName) {
		String translationKey = "config.cobblecuisine.section." + key.toLowerCase();
		return I18n.hasTranslation(translationKey) ? Text.translatable(translationKey) : Text.literal(humanize(fieldName));
	}

	private static String humanize(String name) {
		String[] words = name.replaceAll("([a-z])([A-Z0-9])", "$1 $2").split(" ");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (word.isEmpty()) continue;
			if (!result.isEmpty()) result.append(' ');
			String acronym = ACRONYMS.get(word.toLowerCase());
			result.append(acronym != null ? acronym : Character.toUpperCase(word.charAt(0)) + word.substring(1));
		}
		return result.toString();
	}

	private static String format(double value, Class<?> type) {
		if (type == int.class) return Integer.toString((int) Math.round(value));
		if (type == float.class) return new BigDecimal(Float.toString((float) value)).stripTrailingZeros().toPlainString();
		return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
	}

	private static double read(Field field, Object container) throws IllegalAccessException {
		Object value = field.get(container);
		return value instanceof Number number ? number.doubleValue() : 0;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (tabBarDirty) rebuildTabBar();
		if (searchRow != null) searchRow.shown = false;
		super.render(context, mouseX, mouseY, delta);
		if (!tabs.isEmpty()) {
			context.drawCenteredTextWithShadow(textRenderer, tabs.get(currentTab).title().copy().formatted(Formatting.GOLD, Formatting.BOLD),
					this.width / 2, 32, 0xFFFFFF);
		}
		if (saveError != null) {
			context.drawCenteredTextWithShadow(textRenderer, saveError, this.width / 2, this.height - 48, 0xFF5555);
		}
		if (suggestionsVisible()) renderSuggestions(context, mouseX, mouseY);
	}

	private void renderSuggestions(DrawContext context, int mouseX, int mouseY) {
		int left = searchRow.fieldX;
		int top = searchRow.fieldY + 22;
		int width = searchRow.fieldWidth;
		int height = suggestions.size() * SUGGESTION_HEIGHT;
		context.getMatrices().push();
		context.getMatrices().translate(0, 0, 400);
		context.fill(left, top, left + width, top + height, 0xF0101010);
		context.drawBorder(left, top, width, height, 0xFF808080);
		for (int index = 0; index < suggestions.size(); index++) {
			ItemCatalog.Candidate candidate = suggestions.get(index);
			int y = top + index * SUGGESTION_HEIGHT;
			boolean hovered = mouseX >= left && mouseX < left + width && mouseY >= y && mouseY < y + SUGGESTION_HEIGHT;
			if (hovered || index == highlighted) context.fill(left + 1, y, left + width - 1, y + SUGGESTION_HEIGHT, 0xFF303050);
			context.drawItem(ItemCatalog.icon(candidate.icons()), left + 2, y + 2);
			String detail = candidate.tag()
					? (candidate.icons().isEmpty() ? "#" : Text.translatable("config.cobblecuisine.fullness.items", candidate.icons().size()).getString())
					: candidate.value();
			int detailWidth = textRenderer.getWidth(detail);
			String name = textRenderer.trimToWidth(candidate.name(), Math.max(10, width - 26 - detailWidth - 8));
			context.drawTextWithShadow(textRenderer, Text.literal(name).formatted(candidate.tag() ? Formatting.AQUA : Formatting.WHITE),
					left + 22, y + 6, 0xFFFFFF);
			context.drawTextWithShadow(textRenderer, Text.literal(detail).formatted(Formatting.GRAY),
					left + width - detailWidth - 4, y + 6, 0xFFFFFF);
		}
		context.getMatrices().pop();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (suggestionsVisible()) {
			int left = searchRow.fieldX;
			int top = searchRow.fieldY + 22;
			if (mouseX >= left && mouseX < left + searchRow.fieldWidth
					&& mouseY >= top && mouseY < top + suggestions.size() * SUGGESTION_HEIGHT) {
				addEntry(suggestions.get((int) ((mouseY - top) / SUGGESTION_HEIGHT)).value());
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (searchRow != null && searchRow.field.isFocused()) {
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				addEntry(suggestions.isEmpty() ? searchRow.field.getText() : suggestions.get(highlighted).value());
				return true;
			}
			if (!suggestions.isEmpty() && keyCode == GLFW.GLFW_KEY_DOWN) {
				highlighted = (highlighted + 1) % suggestions.size();
				return true;
			}
			if (!suggestions.isEmpty() && keyCode == GLFW.GLFW_KEY_UP) {
				highlighted = (highlighted + suggestions.size() - 1) % suggestions.size();
				return true;
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private static class ConfigList extends ElementListWidget<Row> {
		ConfigList(MinecraftClient client, int width, int height, int y, int itemHeight) {
			super(client, width, height, y, itemHeight);
		}

		void show(List<Row> visible) {
			this.replaceEntries(visible);
			this.setScrollAmount(this.getScrollAmount());
		}

		@Override
		public int getRowWidth() {
			return this.getWidth() - 20;
		}

		@Override
		protected int getScrollbarX() {
			return this.getX() + this.getWidth() - 6;
		}
	}

	private enum Mode {
		ALWAYS,
		CLASSIC,
		STANDARD
	}

	private abstract static class Row extends ElementListWidget.Entry<Row> {
		Mode mode = Mode.ALWAYS;
		int tab;
	}

	private static class HeaderRow extends Row {
		private final Text title;
		private final boolean section;
		private final TextRenderer textRenderer;

		HeaderRow(Text title, boolean section, TextRenderer textRenderer) {
			this.title = section ? title.copy().formatted(Formatting.GOLD, Formatting.BOLD) : title.copy().formatted(Formatting.YELLOW);
			this.section = section;
			this.textRenderer = textRenderer;
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
				int mouseX, int mouseY, boolean hovered, float tickDelta) {
			int textY = y + (entryHeight - 9) / 2;
			if (section) context.drawCenteredTextWithShadow(textRenderer, title, x + entryWidth / 2, textY, 0xFFFFFF);
			else context.drawTextWithShadow(textRenderer, title, x, textY, 0xFFFFFF);
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}
	}

	private abstract static class ValueRow extends Row {
		final Text label;
		final Field field;
		final Object container;
		final Object defaultValue;
		final TextRenderer textRenderer;
		ClickableWidget control;
		final ButtonWidget help;
		final ButtonWidget reset;
		final String prefix;

		ValueRow(Text label, Field field, Object container, Object defaultValue, TextRenderer textRenderer) {
			this.label = label;
			this.field = field;
			this.container = container;
			this.defaultValue = defaultValue;
			this.textRenderer = textRenderer;
			CobbleCuisineConfigData.EditorPrefix shownPrefix = field.getAnnotation(CobbleCuisineConfigData.EditorPrefix.class);
			this.prefix = shownPrefix == null ? "" : shownPrefix.value();
			this.help = ButtonWidget.builder(Text.literal("?"), button -> { })
					.size(HELP_WIDTH, 20)
					.build();
			this.reset = ButtonWidget.builder(Text.translatable("controls.reset"), button -> resetToDefault())
					.size(RESET_WIDTH, 20)
					.build();
		}

		void finish(ClickableWidget control) {
			this.control = control;
		}

		void describe(Text description) {
			help.setTooltip(Tooltip.of(Text.empty()
					.append(description)
					.append(Text.literal("\nDefault: " + prefix + defaultText()).formatted(Formatting.GRAY))));
		}

		String defaultText() {
			return defaultValue instanceof Boolean flag
					? (flag ? "ON" : "OFF")
					: format(((Number) defaultValue).doubleValue(), field.getType());
		}

		abstract boolean apply();

		abstract void resetToDefault();

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
				int mouseX, int mouseY, boolean hovered, float tickDelta) {
			int resetX = x + entryWidth - RESET_WIDTH;
			int helpX = resetX - 4 - HELP_WIDTH;
			int controlX = helpX - 4 - CONTROL_WIDTH;
			int labelWidth = controlX - x - 4 - prefixWidth();
			Text shown = textRenderer.getWidth(label) > labelWidth
					? Text.literal(textRenderer.trimToWidth(label.getString(), labelWidth - textRenderer.getWidth("…")) + "…")
					: label;
			context.drawTextWithShadow(textRenderer, shown, x, y + (entryHeight - 9) / 2, 0xFFFFFF);
			help.setPosition(helpX, y + (entryHeight - 20) / 2);
			help.render(context, mouseX, mouseY, tickDelta);
			if (!prefix.isEmpty()) context.drawTextWithShadow(textRenderer, prefix, controlX - prefixWidth(), y + (entryHeight - 9) / 2, 0xFFFFFF);
			renderControl(context, controlX, y + (entryHeight - 20) / 2, mouseX, mouseY, tickDelta);
			reset.setPosition(resetX, y + (entryHeight - 20) / 2);
			reset.render(context, mouseX, mouseY, tickDelta);
		}

		int prefixWidth() {
			return prefix.isEmpty() ? 0 : textRenderer.getWidth(prefix) + 4;
		}

		void renderControl(DrawContext context, int x, int y, int mouseX, int mouseY, float tickDelta) {
			control.setPosition(x, y);
			control.render(context, mouseX, mouseY, tickDelta);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(control, help, reset);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(control, help, reset);
		}
	}

	private static class BooleanRow extends ValueRow {
		private final CyclingButtonWidget<Boolean> button;
		private final Consumer<Boolean> onChange;

		BooleanRow(Text label, Field field, Object container, Object defaultValue, TextRenderer textRenderer,
				Consumer<Boolean> onChange) throws IllegalAccessException {
			super(label, field, container, defaultValue, textRenderer);
			this.onChange = onChange;
			this.button = CyclingButtonWidget.onOffBuilder(field.getBoolean(container))
					.omitKeyText()
					.build(0, 0, CONTROL_WIDTH, 20, label, (widget, value) -> onChange.accept(value));
			finish(button);
		}

		@Override
		boolean apply() {
			try {
				field.setBoolean(container, button.getValue());
				return true;
			} catch (IllegalAccessException e) {
				return false;
			}
		}

		@Override
		void resetToDefault() {
			button.setValue((Boolean) defaultValue);
			onChange.accept((Boolean) defaultValue);
		}
	}

	private static class OptionRow extends ValueRow {
		private final String[] names;
		private final CyclingButtonWidget<Integer> button;

		OptionRow(Text label, Field field, Object container, Object defaultValue, String[] names, TextRenderer textRenderer) throws IllegalAccessException {
			super(label, field, container, defaultValue, textRenderer);
			this.names = names;
			List<Integer> indices = new ArrayList<>();
			for (int index = 0; index < names.length; index++) indices.add(index);
			this.button = CyclingButtonWidget.<Integer>builder(this::name)
					.values(indices)
					.initially(clampIndex(field.getInt(container)))
					.omitKeyText()
					.build(0, 0, CONTROL_WIDTH, 20, label);
			finish(button);
		}

		private int clampIndex(int value) {
			return Math.max(0, Math.min(names.length - 1, value));
		}

		private Text name(int index) {
			String name = names[index];
			int detail = name.indexOf(" (");
			String word = detail < 0 ? name : name.substring(0, detail);
			Text shown = Text.translatableWithFallback("config.cobblecuisine.option." + word.toLowerCase(), word);
			return detail < 0 ? shown : Text.empty().append(shown).append(name.substring(detail));
		}

		@Override
		String defaultText() {
			return name(clampIndex((Integer) defaultValue)).getString();
		}

		@Override
		boolean apply() {
			try {
				field.setInt(container, button.getValue());
				return true;
			} catch (IllegalAccessException e) {
				return false;
			}
		}

		@Override
		void resetToDefault() {
			button.setValue(clampIndex((Integer) defaultValue));
		}
	}

	private static class SliderRow extends ValueRow {
		private final RangeSlider slider;

		SliderRow(Text label, Field field, Object container, Object defaultValue, double min, double max, TextRenderer textRenderer) throws IllegalAccessException {
			super(label, field, container, defaultValue, textRenderer);
			this.slider = new RangeSlider(field.getType(), min, max, read(field, container));
			finish(slider);
		}

		@Override
		boolean apply() {
			try {
				double value = slider.actual();
				if (field.getType() == int.class) field.setInt(container, (int) Math.round(value));
				else if (field.getType() == float.class) field.setFloat(container, (float) value);
				else field.setDouble(container, value);
				return true;
			} catch (IllegalAccessException e) {
				return false;
			}
		}

		@Override
		void resetToDefault() {
			slider.setActual(((Number) defaultValue).doubleValue());
		}
	}

	private static class RangeSlider extends SliderWidget {
		private final Class<?> type;
		private final double min;
		private final double max;
		private double actual;

		RangeSlider(Class<?> type, double min, double max, double initial) {
			super(0, 0, CONTROL_WIDTH, 20, Text.empty(), 0);
			this.type = type;
			this.min = min;
			this.max = max;
			setActual(initial);
		}

		double actual() {
			return actual;
		}

		void setActual(double value) {
			actual = Math.max(min, Math.min(max, value));
			this.value = (actual - min) / (max - min);
			updateMessage();
		}

		private boolean percent() {
			return type != int.class && min == 0 && max == 1;
		}

		private double step() {
			if (type == int.class) return 1;
			return percent() ? PERCENT_STEP : 0.25;
		}

		private double snap(double raw) {
			double stepped = min + Math.round((raw - min) / step()) * step();
			return Math.max(min, Math.min(max, Math.round(stepped * 1_000_000) / 1_000_000.0));
		}

		@Override
		protected void updateMessage() {
			if (percent()) {
				setMessage(Text.literal(BigDecimal.valueOf(actual * 100).setScale(2, RoundingMode.HALF_UP)
						.stripTrailingZeros().toPlainString() + "%"));
			} else {
				setMessage(Text.literal(format(actual, type)));
			}
		}

		@Override
		protected void applyValue() {
			actual = snap(min + this.value * (max - min));
			this.value = (actual - min) / (max - min);
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			double before = actual;
			boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
			if (handled && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) {
				setActual(snap(before + (keyCode == GLFW.GLFW_KEY_LEFT ? -step() : step())));
			}
			return handled;
		}
	}

	private static class TextRow extends ValueRow {
		private final TextFieldWidget input;
		private final int unit;

		TextRow(Text label, Field field, Object container, Object defaultValue, int unit, TextRenderer textRenderer) throws IllegalAccessException {
			super(label, field, container, defaultValue, textRenderer);
			this.unit = unit;
			this.input = new TextFieldWidget(textRenderer, 0, 0, CONTROL_WIDTH, 20, label);
			this.input.setText(shown(read(field, container)));
			finish(input);
		}

		private String shown(double value) {
			return unit == 1 ? format(value, field.getType()) : format(value / unit, double.class);
		}

		@Override
		String defaultText() {
			return shown(((Number) defaultValue).doubleValue());
		}

		@Override
		boolean apply() {
			String text = input.getText().trim();
			try {
				Class<?> type = field.getType();
				if (unit != 1) {
					double seconds = Double.parseDouble(text);
					if (!Double.isFinite(seconds)) throw new NumberFormatException("Non-finite value");
					field.setInt(container, Math.toIntExact(Math.round(seconds * unit)));
				} else if (type == int.class) field.setInt(container, Integer.parseInt(text));
				else if (type == float.class) {
					float value = Float.parseFloat(text);
					if (!Float.isFinite(value)) throw new NumberFormatException("Non-finite value");
					field.setFloat(container, value);
				} else if (type == double.class) {
					double value = Double.parseDouble(text);
					if (!Double.isFinite(value)) throw new NumberFormatException("Non-finite value");
					field.setDouble(container, value);
				} else if (Number.class.isAssignableFrom(type)) {
					field.set(container, type.getConstructor(String.class).newInstance(text));
				}
			} catch (Exception e) {
				input.setEditableColor(0xFF5555);
				return false;
			}
			input.setEditableColor(0xFFFFFF);
			return true;
		}

		@Override
		void resetToDefault() {
			input.setText(shown(((Number) defaultValue).doubleValue()));
			input.setEditableColor(0xFFFFFF);
		}
	}

	private static final class ListEntry {
		String value;

		ListEntry(String value) {
			this.value = value;
		}
	}

	private static class ItemListRow extends ValueRow {
		private final ButtonWidget includedButton;
		private final ButtonWidget excludedButton;
		private final Runnable onChanged;
		private Boolean expanded;
		final List<ListEntry> included = new ArrayList<>();
		final List<ListEntry> excluded = new ArrayList<>();

		ItemListRow(Text label, Field field, Object container, Object defaultValue, TextRenderer textRenderer,
				BiConsumer<ItemListRow, Boolean> toggle, Runnable onChanged) throws IllegalAccessException {
			super(label, field, container, defaultValue, textRenderer);
			this.onChanged = onChanged;
			CobbleCuisineConfigData.FullnessItems current = (CobbleCuisineConfigData.FullnessItems) field.get(container);
			fill(included, current.included);
			fill(excluded, current.excluded);
			int half = (CONTROL_WIDTH - 2) / 2;
			this.includedButton = ButtonWidget.builder(Text.empty(), widget -> toggle.accept(this, true))
					.size(half, 20)
					.build();
			this.excludedButton = ButtonWidget.builder(Text.empty(), widget -> toggle.accept(this, false))
					.size(CONTROL_WIDTH - 2 - half, 20)
					.build();
			updateMessage();
			finish(includedButton);
		}

		private static void fill(List<ListEntry> target, List<String> values) {
			target.clear();
			for (String value : ItemCatalog.clean(values)) target.add(new ListEntry(value));
		}

		private static List<String> values(List<ListEntry> entries) {
			return ItemCatalog.clean(entries.stream().map(entry -> entry.value).toList());
		}

		void setExpanded(Boolean included) {
			expanded = included;
			updateMessage();
		}

		void updateMessage() {
			includedButton.setMessage(Text.translatable("config.cobblecuisine.fullness.included_count", included.size())
					.append(Boolean.TRUE.equals(expanded) ? " ▴" : " ▾"));
			excludedButton.setMessage(Text.translatable("config.cobblecuisine.fullness.excluded_count", excluded.size())
					.append(Boolean.FALSE.equals(expanded) ? " ▴" : " ▾"));
		}

		@Override
		void renderControl(DrawContext context, int x, int y, int mouseX, int mouseY, float tickDelta) {
			includedButton.setPosition(x, y);
			includedButton.render(context, mouseX, mouseY, tickDelta);
			excludedButton.setPosition(x + includedButton.getWidth() + 2, y);
			excludedButton.render(context, mouseX, mouseY, tickDelta);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(includedButton, excludedButton, help, reset);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(includedButton, excludedButton, help, reset);
		}

		@Override
		String defaultText() {
			CobbleCuisineConfigData.FullnessItems defaults = (CobbleCuisineConfigData.FullnessItems) defaultValue;
			return Text.translatable("config.cobblecuisine.fullness.count",
					ItemCatalog.clean(defaults.included).size(), ItemCatalog.clean(defaults.excluded).size()).getString();
		}

		@Override
		boolean apply() {
			try {
				CobbleCuisineConfigData.FullnessItems target = (CobbleCuisineConfigData.FullnessItems) field.get(container);
				target.included = values(included);
				target.excluded = values(excluded);
				return true;
			} catch (IllegalAccessException e) {
				return false;
			}
		}

		@Override
		void resetToDefault() {
			CobbleCuisineConfigData.FullnessItems defaults = (CobbleCuisineConfigData.FullnessItems) defaultValue;
			fill(included, defaults.included);
			fill(excluded, defaults.excluded);
			updateMessage();
			onChanged.run();
		}
	}

	private class SearchRow extends Row {
		final TextFieldWidget field;
		boolean shown;
		int fieldX;
		int fieldY;
		int fieldWidth;

		SearchRow() {
			this.field = new TextFieldWidget(textRenderer, 0, 0, 100, 20, Text.translatable("config.cobblecuisine.fullness.search"));
			this.field.setMaxLength(256);
			this.field.setPlaceholder(Text.translatable("config.cobblecuisine.fullness.search").formatted(Formatting.DARK_GRAY));
			this.field.setChangedListener(text -> updateSuggestions());
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
				int mouseX, int mouseY, boolean hovered, float tickDelta) {
			fieldX = x;
			fieldY = y + (entryHeight - 20) / 2;
			fieldWidth = entryWidth;
			field.setPosition(fieldX, fieldY);
			field.setWidth(fieldWidth);
			field.render(context, mouseX, mouseY, tickDelta);
			shown = true;
		}

		@Override
		public List<? extends Element> children() {
			return List.of(field);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(field);
		}
	}

	private class EntryRow extends Row {
		private final ListEntry entry;
		private final TextFieldWidget input;
		private final ButtonWidget remove;

		EntryRow(ListEntry entry, List<ListEntry> owner) {
			this.entry = entry;
			this.input = new TextFieldWidget(textRenderer, 0, 0, 100, 20, Text.literal(entry.value));
			this.input.setMaxLength(256);
			this.input.setText(entry.value);
			this.input.setCursorToStart(false);
			this.input.setChangedListener(text -> entry.value = text);
			this.remove = ButtonWidget.builder(Text.literal("✕"), button -> {
						owner.remove(entry);
						listChanged();
					})
					.size(20, 20)
					.tooltip(Tooltip.of(Text.translatable("config.cobblecuisine.fullness.remove")))
					.build();
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
				int mouseX, int mouseY, boolean hovered, float tickDelta) {
			int top = y + (entryHeight - 20) / 2;
			int left = x;
			boolean known = catalog != null && catalog.known(entry.value);
			context.drawItem(ItemCatalog.icon(catalog == null ? List.of() : catalog.icons(entry.value)), left + 2, top + 2);
			int inputWidth = entryWidth - 22 - 14 - 24;
			input.setPosition(left + 22, top);
			input.setWidth(inputWidth);
			input.setEditableColor(known ? 0xFFFFFF : 0xFF5555);
			input.render(context, mouseX, mouseY, tickDelta);
			Text mark = Text.literal(known ? "✔" : "✖").formatted(known ? Formatting.GREEN : Formatting.RED);
			int gapLeft = left + 22 + inputWidth;
			int gapWidth = (x + entryWidth - 20) - gapLeft;
			int markX = gapLeft + (gapWidth - textRenderer.getWidth(mark)) / 2;
			context.drawTextWithShadow(textRenderer, mark, markX, top + 6, 0xFFFFFF);
			if (!known && mouseX >= gapLeft && mouseX < gapLeft + gapWidth && mouseY >= top && mouseY < top + 20) {
				context.drawTooltip(textRenderer, Text.translatable("config.cobblecuisine.fullness.unknown"), mouseX, mouseY);
			}
			remove.setPosition(x + entryWidth - 20, top);
			remove.render(context, mouseX, mouseY, tickDelta);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(input, remove);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(input, remove);
		}
	}
}
