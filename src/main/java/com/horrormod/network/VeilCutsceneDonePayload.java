package com.horrormod.network;

import com.horrormod.HorrorMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The client sends this when the veil video has finished, so the player can move.
 */
public record VeilCutsceneDonePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<VeilCutsceneDonePayload> TYPE = new CustomPacketPayload.Type<>(HorrorMod.id("veil_cutscene_done"));
    public static final VeilCutsceneDonePayload INSTANCE = new VeilCutsceneDonePayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, VeilCutsceneDonePayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
