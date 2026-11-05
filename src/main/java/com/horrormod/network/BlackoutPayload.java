package com.horrormod.network;

import com.horrormod.HorrorMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Tells a client to go black, silent, and ignore gameplay controls until {@code endTick}.
 */
public record BlackoutPayload(long endTick) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BlackoutPayload> TYPE = new CustomPacketPayload.Type<>(HorrorMod.id("blackout"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlackoutPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            BlackoutPayload::endTick,
            BlackoutPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
