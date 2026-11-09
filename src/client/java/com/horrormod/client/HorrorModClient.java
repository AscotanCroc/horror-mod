package com.horrormod.client;

import com.horrormod.HorrorMod;
import com.horrormod.client.entity.ClientNullClone;
import com.horrormod.client.event.HorrorClientEffects;
import com.horrormod.client.event.SpiderSnap;
import com.horrormod.client.event.WhiteFlash;
import com.horrormod.client.particle.BlackSmokeParticle;
import com.horrormod.client.particle.BlackSquareParticle;
import com.horrormod.client.particle.GlitchSquareParticle;
import com.horrormod.entity.HorrorParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import com.horrormod.client.event.VeilRitual;
import com.horrormod.client.render.NullCloneRenderers;
import com.horrormod.client.render.VeilItemPipeline;
import com.horrormod.client.render.VeilLecternRender;
import com.horrormod.client.render.ReverseEndermanRenderer;
import com.horrormod.entity.NullClone;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import com.horrormod.client.render.GlitchSquareRenderer;
import com.horrormod.client.render.ReverseCreeperRenderer;
import com.horrormod.client.render.ReverseSkeletonModel;
import com.horrormod.client.render.ReverseSkeletonRenderer;
import com.horrormod.client.render.ReverseZombieRenderer;
import com.horrormod.client.render.WatdatmeanModel;
import com.horrormod.client.render.WatdatmeanRenderer;
import com.horrormod.client.screen.RecipeScreen;
import com.horrormod.client.render.ReverseSpiderRenderer;
import com.horrormod.client.sound.HorrorAmbience;
import com.horrormod.client.sound.ReverseSpiderAudio;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.horrormod.entity.HorrorEntities;
import com.horrormod.network.OpenRecipePayload;
import com.horrormod.network.SpiderSnapPayload;
import com.horrormod.network.WhiteFlashPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public final class HorrorModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HorrorAmbience.initialize();
        HorrorClientEffects.initialize();
        VeilRitual.initialize();
        ParticleProviderRegistry.getInstance().register(HorrorParticles.GLITCH_SQUARE, GlitchSquareParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(HorrorParticles.BLACK_SQUARE, BlackSquareParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(HorrorParticles.BLACK_SMOKE, BlackSmokeParticle.Provider::new);
        ClientPlayNetworking.registerGlobalReceiver(SpiderSnapPayload.TYPE, (payload, context) ->
                context.client().execute(SpiderSnap::begin)
        );
        ClientPlayNetworking.registerGlobalReceiver(WhiteFlashPayload.TYPE, (payload, context) ->
                context.client().execute(WhiteFlash::begin)
        );
        VeilItemPipeline.initialize();
        VeilLecternRender.initialize();
        EntityRendererRegistry.register(HorrorEntities.REVERSE_ZOMBIE, ReverseZombieRenderer::new);
        EntityRendererRegistry.register(HorrorEntities.REVERSE_ENDERMAN, ReverseEndermanRenderer::new);
        EntityRendererRegistry.register(HorrorEntities.REVERSE_CREEPER, ReverseCreeperRenderer::new);
        NullClone.clientFactory = ClientNullClone::new;
        EntityRendererRegistry.register(HorrorEntities.NULL_CLONE, NullCloneRenderers::create);
        EntityRendererRegistry.register(HorrorEntities.THROWN_NULL_POINTER, ThrownItemRenderer::new);
        ModelLayerRegistry.registerModelLayer(WatdatmeanModel.LAYER_LOCATION, WatdatmeanModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ReverseSkeletonModel.LAYER_LOCATION, ReverseSkeletonModel::createBodyLayer);
        EntityRendererRegistry.register(HorrorEntities.REVERSE_SKELETON, ReverseSkeletonRenderer::new);
        EntityRendererRegistry.register(HorrorEntities.REVERSE_SPIDER, ReverseSpiderRenderer::new);
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            ReverseSpiderAudio.tick();
            SpiderSnap.tick(minecraft);
            WhiteFlash.tick();
        });
        EntityRendererRegistry.register(HorrorEntities.GLITCH_SQUARE, GlitchSquareRenderer::new);
        EntityRendererRegistry.register(HorrorEntities.WATDATMEAN, WatdatmeanRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(OpenRecipePayload.TYPE, (payload, context) ->
                context.client().execute(() -> context.client().setScreenAndShow(new RecipeScreen()))
        );
        HorrorMod.LOGGER.info(
                "Horror Mod client loaded. Post chain {} is present and not applied.",
                HorrorMod.id(HorrorMod.PIPELINE_PATH)
        );
    }
}
