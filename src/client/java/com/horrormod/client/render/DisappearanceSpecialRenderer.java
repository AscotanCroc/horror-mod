package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * The disappearance icon, in the hand and on the ground.
 * One frame of the shot's strip, at the same rate as the shot.
 */
public final class DisappearanceSpecialRenderer implements NoDataSpecialModelRenderer {
    private static final Identifier TEXTURE = HorrorMod.id("textures/entity/glitch_square.png");
    /** Sky 15 and block 15, matching the shot. */
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucentEmissive(TEXTURE);
    private static final float FRONT = 8.5F / 16.0F;
    private static final float BACK = 7.5F / 16.0F;

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline) {
        int frame = GlitchFrame.index();
        float v0 = frame / (float) GlitchFrame.COUNT;
        float v1 = (frame + 1) / (float) GlitchFrame.COUNT;
        collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, consumer) -> {
            face(consumer, pose, FRONT, v0, v1, 1.0F);
            face(consumer, pose, BACK, v0, v1, -1.0F);
        });
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        for (float x : new float[] {0.0F, 1.0F}) {
            for (float y : new float[] {0.0F, 1.0F}) {
                consumer.accept(new Vector3f(x, y, FRONT));
                consumer.accept(new Vector3f(x, y, BACK));
            }
        }
    }

    private static void face(VertexConsumer consumer, PoseStack.Pose pose, float z, float v0, float v1, float normal) {
        if (normal > 0.0F) {
            vertex(consumer, pose, 0.0F, 0.0F, z, 0.0F, v1, normal);
            vertex(consumer, pose, 0.0F, 1.0F, z, 0.0F, v0, normal);
            vertex(consumer, pose, 1.0F, 1.0F, z, 1.0F, v0, normal);
            vertex(consumer, pose, 1.0F, 0.0F, z, 1.0F, v1, normal);
        } else {
            vertex(consumer, pose, 0.0F, 0.0F, z, 0.0F, v1, normal);
            vertex(consumer, pose, 1.0F, 0.0F, z, 1.0F, v1, normal);
            vertex(consumer, pose, 1.0F, 1.0F, z, 1.0F, v0, normal);
            vertex(consumer, pose, 0.0F, 1.0F, z, 0.0F, v0, normal);
        }
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            float normal
    ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(0xFFFFFFFF)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, normal);
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new DisappearanceSpecialRenderer();
        }
    }
}
