package com.horrormod.network;

import com.horrormod.HorrorMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A white frame on the client's screen.
 */
public record WhiteFlashPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WhiteFlashPayload> TYPE = new CustomPacketPayload.Type<>(HorrorMod.id("white_flash"));
    public static final WhiteFlashPayload INSTANCE = new WhiteFlashPayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, WhiteFlashPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
