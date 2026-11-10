package com.horrormod;

import com.horrormod.content.HorrorBlocks;
import com.horrormod.content.HorrorTabs;
import com.horrormod.entity.HorrorEntities;
import com.horrormod.entity.HorrorParticles;
import com.horrormod.event.HorrorEventScheduler;
import com.horrormod.network.BlackoutPayload;
import com.horrormod.network.HorrorEffectPayload;
import com.horrormod.network.InventoryVisiblePayload;
import com.horrormod.network.OpenRecipePayload;
import com.horrormod.network.SpiderSnapPayload;
import com.horrormod.network.VeilCutsceneDonePayload;
import com.horrormod.network.VeilRitualPayload;
import com.horrormod.network.WhiteFlashPayload;
import com.horrormod.sound.HorrorSounds;
import com.horrormod.sound.ReverseCreeperSounds;
import com.horrormod.sound.ReverseEndermanSounds;
import com.horrormod.sound.ReverseSkeletonSounds;
import com.horrormod.sound.ReverseSpiderSounds;
import com.horrormod.sound.ReverseZombieSounds;
import com.horrormod.world.HorrorWorld;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HorrorMod implements ModInitializer {
    public static final String MOD_ID = "horror_mod";

    /**
     * Resource path of the passthrough post chain. Nothing applies it yet.
     */
    public static final String PIPELINE_PATH = "pipeline";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        HorrorBlocks.initialize();
        HorrorTabs.initialize();
        HorrorSounds.initialize();
        ReverseZombieSounds.initialize();
        ReverseEndermanSounds.initialize();
        ReverseCreeperSounds.initialize();
        ReverseSkeletonSounds.initialize();
        ReverseSpiderSounds.initialize();
        HorrorParticles.initialize();
        HorrorEntities.initialize();
        PayloadTypeRegistry.clientboundPlay().register(OpenRecipePayload.TYPE, OpenRecipePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HorrorEffectPayload.TYPE, HorrorEffectPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlackoutPayload.TYPE, BlackoutPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(VeilRitualPayload.TYPE, VeilRitualPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SpiderSnapPayload.TYPE, SpiderSnapPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WhiteFlashPayload.TYPE, WhiteFlashPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(InventoryVisiblePayload.TYPE, InventoryVisiblePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(VeilCutsceneDonePayload.TYPE, VeilCutsceneDonePayload.CODEC);
        HorrorWorld.initialize();
        HorrorEventScheduler.initialize();
    }
}
