package com.horrormod.network;

import com.horrormod.HorrorMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Tells one client to snap the view to a new direction every tick for a short burst.
 */
public record SpiderSnapPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpiderSnapPayload> TYPE = new CustomPacketPayload.Type<>(HorrorMod.id("spider_snap"));
    public static final SpiderSnapPayload INSTANCE = new SpiderSnapPayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, SpiderSnapPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
