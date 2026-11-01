package com.fyre.cobblecuisine.meal;

import com.cobblemon.mod.common.api.cooking.Seasonings;
import com.cobblemon.mod.common.item.crafting.SeasoningProcessor;
import com.fyre.cobblecuisine.component.CobbleCuisineDataComponents;
import com.fyre.cobblecuisine.component.MealEffectsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MealSeasoningProcessor implements SeasoningProcessor {
	public static final String TYPE = "cobblecuisine:meal_effects";
	public static final MealSeasoningProcessor INSTANCE = new MealSeasoningProcessor();

	private MealSeasoningProcessor() {
	}

	public static void register() {
		SeasoningProcessor.Companion.getProcessors().put(TYPE, INSTANCE);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void apply(ItemStack result, List<ItemStack> seasoning) {
		List<List<String>> effectSlots = seasoning.stream()
				.map(stack -> Seasonings.INSTANCE.getMobEffectsFromItemStack(stack).stream()
						.map(effect -> effect.getEffect().toString())
						.toList())
				.toList();
		MealEffectsComponent contributions = fromEffectSlots(effectSlots);
		MealPotency potency = MealPotencyIngredients.fromStacks(seasoning);
		int occupiedSlots = (int) seasoning.stream().filter(this::consumesItem).count();
		MealEffectsComponent existing = result.getOrDefault(
				CobbleCuisineDataComponents.MEAL_EFFECTS,
				MealEffectsComponent.EMPTY
		);
		FoodComponent foodComponent = result.get(DataComponentTypes.FOOD);
		result.set(CobbleCuisineDataComponents.MEAL_EFFECTS, new MealEffectsComponent(
				existing.primary().or(() -> MealConsumption.primaryFromFood(foodComponent)),
				contributions.seasonings(),
				occupiedSlots,
				potency
		));
	}

	@Override
	public boolean consumesItem(ItemStack seasoning) {
		return MealPotencyIngredients.recognizes(seasoning) || Seasonings.INSTANCE.getMobEffectsFromItemStack(seasoning).stream()
				.map(effect -> effect.getEffect().toString())
				.anyMatch(effect -> MealEffectMappings.fromStatusEffectId(effect).isPresent());
	}

	static MealEffectsComponent fromEffectSlots(List<List<String>> effectSlots) {
		if (effectSlots.size() > 3) throw new IllegalArgumentException("The Cooking Pot has only three seasoning slots");

		Map<EffectKey, Integer> counts = new LinkedHashMap<>();
		int occupiedSlots = 0;
		for (List<String> effectSlot : effectSlots) {
			Set<EffectKey> slotEffects = new LinkedHashSet<>();
			effectSlot.forEach(effect -> MealEffectMappings.fromStatusEffectId(effect)
					.map(entry -> new EffectKey(entry.type(), entry.target()))
					.ifPresent(slotEffects::add));
			if (slotEffects.isEmpty()) continue;
			occupiedSlots++;
			slotEffects.forEach(effect -> counts.merge(effect, 1, Integer::sum));
		}

		List<MealEffectEntry> entries = new ArrayList<>();
		counts.forEach((effect, count) -> entries.add(new MealEffectEntry(effect.type(), effect.target(), count)));
		return new MealEffectsComponent(Optional.empty(), entries, occupiedSlots);
	}

	private record EffectKey(MealEffectType type, String target) {
	}
}
