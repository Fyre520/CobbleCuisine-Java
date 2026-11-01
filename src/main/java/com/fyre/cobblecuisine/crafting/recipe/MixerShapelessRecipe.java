package com.fyre.cobblecuisine.crafting.recipe;

import com.cobblemon.mod.common.api.tags.CobblemonItemTags;
import com.cobblemon.mod.common.item.crafting.CookingPotBookCategory;
import com.cobblemon.mod.common.item.crafting.SeasoningProcessor;
import com.fyre.cobblecuisine.block.CobbleCuisineBlocks;
import com.fyre.cobblecuisine.crafting.CobbleCuisineRecipeSerializers;
import com.fyre.cobblecuisine.crafting.CobbleCuisineRecipeTypes;
import com.fyre.cobblecuisine.item.food.ShakeBlendMath;
import com.fyre.cobblecuisine.item.food.ShakeItem;
import com.fyre.cobblecuisine.item.food.ShakeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MixerShapelessRecipe implements Recipe<CraftingRecipeInput> {
	private static final int MAX_INGREDIENTS = 6;

	private final String groupName;
	private final CookingPotBookCategory category;
	private final ItemStack result;
	private final DefaultedList<Ingredient> ingredients;
	private final TagKey<Item> seasoningTag;
	private final List<SeasoningProcessor> seasoningProcessors;

	public MixerShapelessRecipe(
			String groupName,
			CookingPotBookCategory category,
			ItemStack result,
			DefaultedList<Ingredient> ingredients,
			TagKey<Item> seasoningTag,
			List<SeasoningProcessor> seasoningProcessors
	) {
		this.groupName = groupName;
		this.category = category;
		this.result = result;
		this.ingredients = ingredients;
		this.seasoningTag = seasoningTag;
		this.seasoningProcessors = seasoningProcessors;
	}

	@Override
	public RecipeType<?> getType() {
		return CobbleCuisineRecipeTypes.MIXER_SHAPELESS_RECIPE;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return CobbleCuisineRecipeSerializers.MIXER_SHAPELESS_RECIPE;
	}

	@Override
	public DefaultedList<Ingredient> getIngredients() {
		return this.ingredients;
	}

	@Override
	public boolean matches(CraftingRecipeInput input, @NotNull World world) {
		List<Ingredient> remaining = new ArrayList<>(this.ingredients);

		for (ItemStack item : input.getStacks()) {
			if (item.isEmpty()) continue;

			int matchIndex = -1;
			for (int i = 0; i < remaining.size(); i++) {
				if (remaining.get(i).test(item)) {
					matchIndex = i;
					break;
				}
			}

			if (matchIndex != -1) {
				remaining.remove(matchIndex);
			} else {
				return false;
			}
		}

		return remaining.isEmpty();
	}

	@Override
	public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
		if (this.result.getItem() instanceof ShakeItem) {
			List<ItemStack> berries = input.getStacks().stream()
					.filter(stack -> !stack.isEmpty() && ShakeBlendMath.colourOf(stack) != null)
					.toList();
			if (berries.size() == 3) {
				ShakeType blended = ShakeBlendMath.resolve(berries.get(0), berries.get(1), berries.get(2));
				if (blended != null) return new ItemStack(blended.item);
			}
		}
		return this.result.copy();
	}

	@Override
	public boolean fits(int width, int height) {
		return width * height >= this.ingredients.size();
	}

	public @NotNull ItemStack getResult() {
		return this.result;
	}

	@Override
	public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
		return this.result;
	}

	@Override
	public @NotNull String getGroup() {
		return this.groupName;
	}

	public @NotNull String getGroupName() {
		return this.groupName;
	}

	public @NotNull CookingPotBookCategory getCategory() {
		return this.category;
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(CobbleCuisineBlocks.MIXER_RED);
	}

	public @NotNull TagKey<Item> getSeasoningTag() {
		return this.seasoningTag;
	}

	public @NotNull List<SeasoningProcessor> getSeasoningProcessors() {
		return this.seasoningProcessors;
	}

	public void applySeasoning(ItemStack stack, List<ItemStack> seasonings) {
		for (SeasoningProcessor processor : this.seasoningProcessors) {
			processor.apply(stack, seasonings);
		}
	}

	public static class Serializer implements RecipeSerializer<MixerShapelessRecipe> {
		private static final MapCodec<MixerShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.groupName),
						CookingPotBookCategory.Companion.getCODEC()
								.fieldOf("category").orElse(CookingPotBookCategory.MISC).forGetter(r -> r.category),
						ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(r -> r.result),
						Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("ingredients").flatXmap(
								list -> {
									Ingredient[] array = list.stream()
											.filter(ing -> !ing.isEmpty())
											.toArray(Ingredient[]::new);
									if (array.length == 0) {
										return DataResult.error(() -> "No ingredients for shapeless recipe");
									}
									if (array.length > MAX_INGREDIENTS) {
										return DataResult.error(() -> "Too many ingredients for a 2x3 mixer recipe: "
												+ array.length + " (max " + MAX_INGREDIENTS + ")");
									}
									DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(array.length, Ingredient.empty());
									for (int i = 0; i < array.length; i++) {
										ingredients.set(i, array[i]);
									}
									return DataResult.success(ingredients);
								},
								DataResult::success
						).forGetter(r -> r.ingredients),
						TagKey.unprefixedCodec(Registries.ITEM.getKey()).fieldOf("seasoningTag").orElse(CobblemonItemTags.EMPTY).forGetter(r -> r.seasoningTag),
						createSeasoningProcessorCodec().listOf().fieldOf("seasoningProcessors").forGetter(r -> r.seasoningProcessors)
				).apply(instance, MixerShapelessRecipe::new)
		);

		private static Codec<SeasoningProcessor> createSeasoningProcessorCodec() {
			return Codec.STRING.comapFlatMap(
					type -> {
						SeasoningProcessor processor = SeasoningProcessor.Companion.getProcessors().get(type);
						if (processor == null) {
							return DataResult.error(() -> "Unknown seasoning processor: " + type);
						}
						return DataResult.success(processor);
					},
					SeasoningProcessor::getType
			);
		}

		private static final PacketCodec<RegistryByteBuf, MixerShapelessRecipe> PACKET_CODEC =
				new PacketCodec<RegistryByteBuf, MixerShapelessRecipe>() {
					@Override
					public MixerShapelessRecipe decode(RegistryByteBuf buf) {
						return fromNetwork(buf);
					}

					@Override
					public void encode(RegistryByteBuf buf, MixerShapelessRecipe recipe) {
						toNetwork(buf, recipe);
					}
				};

		private static MixerShapelessRecipe fromNetwork(RegistryByteBuf buf) {
			String group = buf.readString();
			String categoryName = buf.readString();
			CookingPotBookCategory category = CookingPotBookCategory.valueOf(categoryName);
			Identifier tagId = buf.readIdentifier();
			TagKey<Item> seasoningTag = TagKey.of(Registries.ITEM.getKey(), tagId);

			int size = buf.readVarInt();
			DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(size, Ingredient.empty());
			for (int i = 0; i < size; i++) {
				ingredients.set(i, Ingredient.PACKET_CODEC.decode(buf));
			}

			ItemStack result = ItemStack.PACKET_CODEC.decode(buf);

			int processorCount = buf.readVarInt();
			List<SeasoningProcessor> processors = new ArrayList<>();
			Map<String, SeasoningProcessor> processorMap = SeasoningProcessor.Companion.getProcessors();
			for (int i = 0; i < processorCount; i++) {
				String type = buf.readString();
				SeasoningProcessor processor = processorMap.get(type);
				if (processor == null) {
					throw new IllegalArgumentException("Unknown seasoning processor: " + type);
				}
				processors.add(processor);
			}

			return new MixerShapelessRecipe(group, category, result, ingredients, seasoningTag, processors);
		}

		private static void toNetwork(RegistryByteBuf buf, MixerShapelessRecipe recipe) {
			buf.writeString(recipe.groupName);
			buf.writeString(recipe.category.name());
			buf.writeIdentifier(recipe.seasoningTag.id());

			buf.writeVarInt(recipe.ingredients.size());
			for (Ingredient ingredient : recipe.ingredients) {
				Ingredient.PACKET_CODEC.encode(buf, ingredient);
			}

			ItemStack.PACKET_CODEC.encode(buf, recipe.result);

			buf.writeVarInt(recipe.seasoningProcessors.size());
			for (SeasoningProcessor processor : recipe.seasoningProcessors) {
				buf.writeString(processor.getType());
			}
		}

		@Override
		public MapCodec<MixerShapelessRecipe> codec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, MixerShapelessRecipe> packetCodec() {
			return PACKET_CODEC;
		}
	}
}