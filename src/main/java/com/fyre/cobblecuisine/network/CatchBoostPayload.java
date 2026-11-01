package com.fyre.cobblecuisine.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CatchBoostPayload(float multiplier) implements CustomPayload {
	public static final Id<CatchBoostPayload> ID = new Id<>(Identifier.of("cobblecuisine", "catch_boost_v1"));
	public static final PacketCodec<RegistryByteBuf, CatchBoostPayload> CODEC =
			PacketCodec.tuple(PacketCodecs.FLOAT, CatchBoostPayload::multiplier, CatchBoostPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() { return ID; }
}
