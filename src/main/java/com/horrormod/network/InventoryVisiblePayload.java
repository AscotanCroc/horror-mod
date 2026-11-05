package com.horrormod.network;

import com.horrormod.HorrorMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The server does not know when the local inventory screen is open, so the client says so.
 */
public record InventoryVisiblePayload(boolean visible) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InventoryVisiblePayload> TYPE = new CustomPacketPayload.Type<>(HorrorMod.id("inventory_visible"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryVisiblePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            InventoryVisiblePayload::visible,
            InventoryVisiblePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
