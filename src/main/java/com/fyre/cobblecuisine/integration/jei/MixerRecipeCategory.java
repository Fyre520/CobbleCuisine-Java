package com.fyre.cobblecuisine.integration.jei;

import com.cobblemon.mod.common.api.cooking.Seasonings;
import com.fyre.cobblecuisine.CobbleCuisine;
import com.fyre.cobblecuisine.block.CobbleCuisineBlocks;
import com.fyre.cobblecuisine.crafting.recipe.MixerShapelessRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MixerRecipeCategory implements IRecipeCategory<MixerShapelessRecipe> {
	public static final RecipeType<MixerShapelessRecipe> RECIPE_TYPE =
			RecipeType.create(CobbleCuisine.MOD_ID, "mixer", MixerShapelessRecipe.class);
	private static final Identifier TEXTURE = Identifier.of(CobbleCuisine.MOD_ID, "textures/gui/mixer_red.png");
	private static final int LEFT = 20;
	private static final int TOP = 12;
	private static final int WIDTH = 156;
	private static final int HEIGHT = 66;
	private static final int[][] SEASONING_SLOTS = {{80, 20}, {98, 38}, {80, 56}};

	private final IDrawable background;
	private final IDrawable icon;
	private List<ItemStack> seasonings;

	public MixerRecipeCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.drawableBuilder(TEXTURE, LEFT, TOP, WIDTH, HEIGHT).setTextureSize(256, 256).build();
		this.icon = guiHelper.createDrawableItemStack(new ItemStack(CobbleCuisineBlocks.MIXER_RED));
	}

	@Override
	public RecipeType<MixerShapelessRecipe> getRecipeType() {
		return RECIPE_TYPE;
	}

	@Override
	public Text getTitle() {
		return Text.translatable("container.cobblecuisine.mixer");
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, MixerShapelessRecipe recipe, IFocusGroup focuses) {
		List<Ingredient> ingredients = recipe.getIngredients();
		for (int i = 0; i < ingredients.size(); i++) {
			builder.addSlot(RecipeIngredientRole.INPUT, 26 + (i % 2) * 18 - LEFT, 20 + (i / 2) * 18 - TOP)
					.addIngredients(ingredients.get(i));
		}
		if (!recipe.getSeasoningProcessors().isEmpty()) {
			List<ItemStack> valid = seasoningsFor(recipe);
			for (int[] slot : SEASONING_SLOTS) {
				List<ItemStack> shuffled = new ArrayList<>(valid);
				Collections.shuffle(shuffled);
				builder.addSlot(RecipeIngredientRole.INPUT, slot[0] - LEFT, slot[1] - TOP)
						.addIngredients(VanillaTypes.ITEM_STACK, shuffled);
			}
		}
		if (MinecraftClient.getInstance().world != null) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, 134 - LEFT, 31 - TOP)
					.addItemStack(recipe.getResult(MinecraftClient.getInstance().world.getRegistryManager()));
		}
	}

	@Override
	public void draw(MixerShapelessRecipe recipe, IRecipeSlotsView recipeSlotsView, DrawContext guiGraphics, double mouseX, double mouseY) {
		background.draw(guiGraphics);
	}

	private List<ItemStack> seasoningsFor(MixerShapelessRecipe recipe) {
		if (seasonings == null) {
			seasonings = Registries.ITEM.stream().map(ItemStack::new).filter(Seasonings.INSTANCE::isSeasoning).toList();
		}
		return seasonings.stream()
				.filter(stack -> stack.isIn(recipe.getSeasoningTag()))
				.filter(stack -> recipe.getSeasoningProcessors().stream().allMatch(processor -> processor.consumesItem(stack)))
				.toList();
	}
}
