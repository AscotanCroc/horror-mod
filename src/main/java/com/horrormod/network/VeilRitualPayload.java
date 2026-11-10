package com.horrormod.network;

import com.horrormod.HorrorMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Tells one client to play the veil video over the dimension change.
 */
public record VeilRitualPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<VeilRitualPayload> TYPE = new CustomPacketPayload.Type<>(HorrorMod.id("veil_ritual"));
    public static final VeilRitualPayload INSTANCE = new VeilRitualPayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, VeilRitualPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
