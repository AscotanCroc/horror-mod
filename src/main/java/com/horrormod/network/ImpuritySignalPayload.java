package com.horrormod.network;

import com.horrormod.HorrorMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The nearest reverse mob a carried impurity detector can see, or none.
 */
public record ImpuritySignalPayload(boolean present, double x, double y, double z) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ImpuritySignalPayload> TYPE = new CustomPacketPayload.Type<>(HorrorMod.id("impurity_signal"));
    public static final ImpuritySignalPayload NONE = new ImpuritySignalPayload(false, 0.0, 0.0, 0.0);
    public static final StreamCodec<RegistryFriendlyByteBuf, ImpuritySignalPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ImpuritySignalPayload::present,
            ByteBufCodecs.DOUBLE,
            ImpuritySignalPayload::x,
            ByteBufCodecs.DOUBLE,
            ImpuritySignalPayload::y,
            ByteBufCodecs.DOUBLE,
            ImpuritySignalPayload::z,
            ImpuritySignalPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
