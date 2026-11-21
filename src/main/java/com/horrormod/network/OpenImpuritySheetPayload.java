package com.horrormod.network;

import com.horrormod.HorrorMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Asks one client to open the impurity sheet.
 */
public record OpenImpuritySheetPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenImpuritySheetPayload> TYPE = new CustomPacketPayload.Type<>(HorrorMod.id("open_impurity_sheet"));
    public static final OpenImpuritySheetPayload INSTANCE = new OpenImpuritySheetPayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenImpuritySheetPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
