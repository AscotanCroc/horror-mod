package com.horrormod.network;

import com.horrormod.HorrorMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Starts or refreshes one client-only event. {@code endTick} is the overworld game time when it stops.
 */
public record HorrorEffectPayload(String effect, long endTick, float x, float y, float stretchX, float stretchY) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HorrorEffectPayload> TYPE = new CustomPacketPayload.Type<>(HorrorMod.id("horror_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HorrorEffectPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            HorrorEffectPayload::effect,
            ByteBufCodecs.VAR_LONG,
            HorrorEffectPayload::endTick,
            ByteBufCodecs.FLOAT,
            HorrorEffectPayload::x,
            ByteBufCodecs.FLOAT,
            HorrorEffectPayload::y,
            ByteBufCodecs.FLOAT,
            HorrorEffectPayload::stretchX,
            ByteBufCodecs.FLOAT,
            HorrorEffectPayload::stretchY,
            HorrorEffectPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
