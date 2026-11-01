package com.fyre.cobblecuisine.network;

import com.fyre.cobblecuisine.config.TooltipSettings;
import com.fyre.cobblecuisine.meal.MealEffectType;
import com.fyre.cobblecuisine.meal.MealLoadout;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public record TooltipSettingsPayload(TooltipSettings.Snapshot snapshot) implements CustomPayload {
	public static final Id<TooltipSettingsPayload> ID = new Id<>(Identifier.of("cobblecuisine", "tooltip_settings_v8"));
	public static final PacketCodec<RegistryByteBuf, TooltipSettingsPayload> CODEC = new PacketCodec<>() {
		@Override
		public TooltipSettingsPayload decode(RegistryByteBuf buf) {
			boolean classic = buf.readBoolean();
			TooltipSettings.Feeding puffFriendship = readFeeding(buf);
			TooltipSettings.Feeding malasadaFriendship = readFeeding(buf);
			TooltipSettings.Feeding puffHealing = readFeeding(buf);
			TooltipSettings.Feeding malasadaHealing = readFeeding(buf);
			int size = buf.readVarInt();
			if (size < 0 || size > MealEffectType.values().length) throw new IllegalArgumentException("Invalid tooltip strength count");
			Map<MealEffectType, TooltipSettings.Strength> strengths = new EnumMap<>(MealEffectType.class);
			for (int i = 0; i < size; i++) {
				MealEffectType type = MealEffectType.valueOf(buf.readString(64));
				strengths.put(type, new TooltipSettings.Strength(readAmount(buf), readAmount(buf), readAmount(buf)));
			}
			size = buf.readVarInt();
			if (size < 0 || size > 256) throw new IllegalArgumentException("Invalid tooltip weight count");
			Map<String, MealLoadout.WeightSettings> weights = new HashMap<>();
			for (int i = 0; i < size; i++) {
				weights.put(buf.readString(64), new MealLoadout.WeightSettings(readAmount(buf), readAmount(buf), readAmount(buf)));
			}
			size = buf.readVarInt();
			if (size < 0 || size > MealEffectType.values().length) throw new IllegalArgumentException("Invalid tooltip scale count");
			Map<MealEffectType, MealLoadout.Scale> scales = new EnumMap<>(MealEffectType.class);
			for (int i = 0; i < size; i++) {
				scales.put(MealEffectType.valueOf(buf.readString(64)), new MealLoadout.Scale(readAmount(buf), readAmount(buf)));
			}
			MealLoadout.Scale ivMaximum = new MealLoadout.Scale(readAmount(buf), readAmount(buf));
			int ivMaxValue = readCount(buf);
			size = buf.readVarInt();
			if (size < 0 || size > 256) throw new IllegalArgumentException("Invalid tooltip item count");
			Map<String, Integer> items = new HashMap<>();
			for (int i = 0; i < size; i++) items.put(buf.readString(128), readCount(buf));
			return new TooltipSettingsPayload(new TooltipSettings.Snapshot(classic, puffFriendship, malasadaFriendship,
					puffHealing, malasadaHealing, strengths, weights, scales, ivMaximum, ivMaxValue, items));
		}

		@Override
		public void encode(RegistryByteBuf buf, TooltipSettingsPayload payload) {
			TooltipSettings.Snapshot values = payload.snapshot();
			buf.writeBoolean(values.classic());
			writeFeeding(buf, values.pokePuffFriendship());
			writeFeeding(buf, values.malasadaFriendship());
			writeFeeding(buf, values.pokePuffHealing());
			writeFeeding(buf, values.malasadaHealing());
			buf.writeVarInt(values.strengths().size());
			values.strengths().forEach((type, strength) -> {
				buf.writeString(type.name(), 64);
				buf.writeFloat(strength.direct());
				buf.writeFloat(strength.seasoning());
				buf.writeFloat(strength.cap());
			});
			buf.writeVarInt(values.weights().size());
			values.weights().forEach((key, weight) -> {
				buf.writeString(key, 64);
				buf.writeFloat(weight.spawn());
				buf.writeFloat(weight.seasoning());
				buf.writeFloat(weight.nonWeight());
			});
			buf.writeVarInt(values.scales().size());
			values.scales().forEach((type, scale) -> {
				buf.writeString(type.name(), 64);
				buf.writeFloat(scale.base());
				buf.writeFloat(scale.perSeasoning());
			});
			buf.writeFloat(values.ivMaximum().base());
			buf.writeFloat(values.ivMaximum().perSeasoning());
			buf.writeVarInt(values.ivMaxValue());
			buf.writeVarInt(values.itemAmounts().size());
			values.itemAmounts().forEach((id, amount) -> {
				buf.writeString(id, 128);
				buf.writeVarInt(amount);
			});
		}
	};

	private static TooltipSettings.Feeding readFeeding(RegistryByteBuf buf) {
		return new TooltipSettings.Feeding(readAmount(buf), readAmount(buf), readAmount(buf));
	}

	private static void writeFeeding(RegistryByteBuf buf, TooltipSettings.Feeding feeding) {
		buf.writeFloat(feeding.amount());
		buf.writeFloat(feeding.baseMultiplier());
		buf.writeFloat(feeding.perSeasoningMultiplier());
	}

	private static float readAmount(RegistryByteBuf buf) {
		float amount = buf.readFloat();
		if (!Float.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid tooltip amount");
		return amount;
	}

	private static int readCount(RegistryByteBuf buf) {
		int amount = buf.readVarInt();
		if (amount < 0) throw new IllegalArgumentException("Invalid tooltip amount");
		return amount;
	}

	@Override
	public Id<? extends CustomPayload> getId() { return ID; }
}
