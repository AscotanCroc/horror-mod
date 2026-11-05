package com.horrormod.network;

import com.horrormod.HorrorMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Asks one client to open the stone lectern recipe screen.
 */
public record OpenRecipePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenRecipePayload> TYPE = new CustomPacketPayload.Type<>(HorrorMod.id("open_recipe"));
    public static final OpenRecipePayload INSTANCE = new OpenRecipePayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRecipePayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
