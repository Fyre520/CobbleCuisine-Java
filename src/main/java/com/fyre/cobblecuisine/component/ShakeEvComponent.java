package com.fyre.cobblecuisine.component;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.pokemon.EVs;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Map;

public record ShakeEvComponent(Map<Stat, Integer> extraEvs) {
	public static final ShakeEvComponent EMPTY = new ShakeEvComponent(Map.of());

	public static final Codec<ShakeEvComponent> CODEC = Codec
			.unboundedMap(Stat.Companion.getPERMANENT_ONLY_CODEC(), Codec.intRange(0, EVs.MAX_STAT_VALUE))
			.xmap(ShakeEvComponent::new, ShakeEvComponent::extraEvs);

	public static final PacketCodec<RegistryByteBuf, ShakeEvComponent> PACKET_CODEC =
			PacketCodecs.codec(CODEC).cast();

	public ShakeEvComponent {
		extraEvs = Map.copyOf(extraEvs);
	}

	public boolean isEmpty() {
		return extraEvs.isEmpty();
	}

	public int extra(Stat stat) {
		return extraEvs.getOrDefault(stat, 0);
	}
}
