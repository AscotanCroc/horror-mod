package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import com.horrormod.entity.GlitchSquare;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * A camera-facing square. The texture is a vertical strip of frames, and the frame changes twice a tick.
 */
public class GlitchSquareRenderer extends EntityRenderer<GlitchSquare, GlitchSquareRenderer.State> {
    static final int FRAMES = 32;
    private static final Identifier TEXTURE = HorrorMod.id("textures/entity/glitch_square.png");
    /** Sky 15 and block 15, so the square ignores world light. */
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucentEmissive(TEXTURE);
    private static final float SCALE = 0.5F;

    public GlitchSquareRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GlitchSquare entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.frame = (int) ((entity.tickCount + partialTick) * 2.0F);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.rotate(camera.orientation);
        int frame = Math.floorMod(state.frame, FRAMES);
        float v0 = frame / (float) FRAMES;
        float v1 = (frame + 1) / (float) FRAMES;
        collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, consumer) -> quad(consumer, pose, FULL_BRIGHT, v0, v1));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, int light, float v0, float v1) {
        vertex(consumer, pose, light, -0.5F, -0.5F, 0.0F, v1);
        vertex(consumer, pose, light, 0.5F, -0.5F, 1.0F, v1);
        vertex(consumer, pose, light, 0.5F, 0.5F, 1.0F, v0);
        vertex(consumer, pose, light, -0.5F, 0.5F, 0.0F, v0);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int light, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(0xFFFFFFFF)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    public static final class State extends EntityRenderState {
        public int frame;
    }
}
