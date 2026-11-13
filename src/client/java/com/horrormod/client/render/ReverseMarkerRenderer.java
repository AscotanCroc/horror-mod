package com.horrormod.client.render;

import com.horrormod.entity.ReverseMarker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * A red diamond above a reverse mob. The quad does not depth-test, so a wall does not hide it.
 */
public class ReverseMarkerRenderer extends EntityRenderer<ReverseMarker, EntityRenderState> {
    private static final int COLOR = 0xFFFF3030;
    private static final float SCALE = 0.9F;

    public ReverseMarkerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float scale = scale(state);
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.rotate(camera.orientation);
        collector.submitCustomGeometry(poseStack, ReverseMarkerRenderType.get(), (pose, consumer) -> quad(consumer, pose));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    /** Keeps the diamond the size it has at 24 blocks, so distance does not shrink it away. */
    private static float scale(EntityRenderState state) {
        double distance = Math.sqrt(state.distanceToCameraSq);
        if (distance <= 24.0) {
            return SCALE;
        }
        return (float) (SCALE * distance / 24.0);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose) {
        vertex(consumer, pose, 0.0F, -0.55F);
        vertex(consumer, pose, 0.42F, 0.0F);
        vertex(consumer, pose, 0.0F, 0.55F);
        vertex(consumer, pose, -0.42F, 0.0F);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y) {
        consumer.addVertex(pose, x, y, 0.0F).setColor(COLOR);
    }
}
