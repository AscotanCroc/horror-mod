package com.horrormod.client.render;

import com.horrormod.client.event.VeilRitual;
import com.horrormod.content.HorrorBlocks;
import com.horrormod.content.StoneLecternBlock;
import com.horrormod.entity.HorrorEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws a charged lectern itself, rattling in place, with the ender dragon's death light on its body.
 */
public final class VeilLecternRender {
    private static final Identifier EXPLODING = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon_exploding.png");
    private static final RenderType GLOW = RenderTypes.entityTranslucentEmissive(EXPLODING);
    private static final RenderType RAYS = RenderTypes.dragonRays();
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0) / 2.0);
    /** Same length as the dragon's death, then it begins again. */
    private static final float DEATH_TICKS = 200.0F;
    private static final int MAGENTA = 16711935;
    private static final int SCAN_INTERVAL = 5;

    private static final List<BlockPos> charged = new ArrayList<>();
    private static final MovingBlockRenderState moving = new MovingBlockRenderState();
    private static int cooldown = SCAN_INTERVAL;

    private VeilLecternRender() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(VeilLecternRender::tick);
        LevelRenderEvents.COLLECT_SUBMITS.register(VeilLecternRender::submit);
    }

    private static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            charged.clear();
            return;
        }
        if (--cooldown > 0) {
            return;
        }
        cooldown = SCAN_INTERVAL;
        if (!minecraft.player.getInventory().contains(stack -> stack.is(HorrorEntities.VEIL))) {
            return;
        }
        List<BlockPos> found = new ArrayList<>();
        StoneLecternBlock.nearby(minecraft.level, minecraft.player.blockPosition(), pos -> {
            BlockState state = minecraft.level.getBlockState(pos);
            if (state.is(HorrorBlocks.STONE_LECTERN) && state.getValue(StoneLecternBlock.CHARGED)) {
                found.add(pos);
            }
        });
        charged.clear();
        charged.addAll(found);
    }

    private static void submit(LevelRenderContext context) {
        if (charged.isEmpty() || VeilRitual.active()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.level instanceof ClientLevel level)) {
            return;
        }
        Vec3 camera = context.levelState().cameraRenderState.pos;
        float partial = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float seconds = (level.getGameTime() + partial) / 20.0F;
        float death = (Math.floorMod(level.getGameTime(), (long) DEATH_TICKS) + partial) / DEATH_TICKS;
        PoseStack pose = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();
        Iterator<BlockPos> remaining = charged.iterator();
        while (remaining.hasNext()) {
            BlockPos pos = remaining.next();
            BlockState state = level.getBlockState(pos);
            if (!state.is(HorrorBlocks.STONE_LECTERN) || !state.getValue(StoneLecternBlock.CHARGED)) {
                remaining.remove();
                continue;
            }
            pose.pushPose();
            pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            shake(pose, seconds, pos);
            submitBody(collector, pose, level, pos);
            submitGlow(collector, pose, death);
            submitRays(collector, pose, death);
            pose.popPose();
        }
    }

    private static void shake(PoseStack pose, float seconds, BlockPos pos) {
        float phase = pos.getX() * 0.17F + pos.getZ() * 0.13F;
        float jx = (float) (Math.sin(seconds * 37.0 + phase) * 0.55 + Math.sin(seconds * 71.0) * 0.45) * 0.045F;
        float jy = (float) Math.cos(seconds * 29.0 + phase) * 0.018F;
        float jz = (float) (Math.sin(seconds * 43.0 + phase) * 0.55 + Math.cos(seconds * 67.0) * 0.45) * 0.045F;
        float yaw = (float) Math.sin(seconds * 33.0 + phase) * 1.6F;
        float pitch = (float) Math.cos(seconds * 41.0 + phase) * 0.9F;
        pose.translate(0.5 + jx, 0.55 + jy, 0.5 + jz);
        pose.rotateDegrees(com.mojang.math.Axis.YP, yaw);
        pose.rotateDegrees(com.mojang.math.Axis.ZP, pitch);
        pose.translate(-0.5, -0.55, -0.5);
    }

    private static void submitBody(SubmitNodeCollector collector, PoseStack pose, ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(HorrorBlocks.STONE_LECTERN)) {
            return;
        }
        state = state.setValue(StoneLecternBlock.CHARGED, false);
        moving.randomSeedPos = pos;
        moving.blockPos = pos;
        moving.blockState = state;
        moving.biome = level.getBiome(pos);
        moving.cardinalLighting = level.cardinalLighting();
        moving.lightEngine = level.getLightEngine();
        collector.submitMovingBlock(pose, moving, 0);
    }

    private static void submitGlow(SubmitNodeCollector collector, PoseStack pose, float death) {
        int color = ARGB.white(1.0F - death);
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.scale(1.04F, 1.04F, 1.04F);
        pose.translate(-0.5F, -0.5F, -0.5F);
        collector.submitCustomGeometry(pose, GLOW, (modelPose, buffer) -> {
            box(buffer, modelPose, 0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F, color);
            box(buffer, modelPose, 0.25F, 0.125F, 0.25F, 0.75F, 0.7F, 0.75F, color);
            box(buffer, modelPose, 0.02F, 0.72F, 0.18F, 0.98F, 0.98F, 0.98F, color);
        });
        pose.popPose();
    }

    /**
     * The rays drawn on the dragon while {@code deathTime / 200} climbs and the body rises.
     * Scaled down so they burst from the lectern instead of filling the sky.
     */
    private static void submitRays(SubmitNodeCollector collector, PoseStack pose, float death) {
        pose.pushPose();
        pose.translate(0.5F, 0.55F, 0.5F);
        pose.scale(0.12F, 0.12F, 0.12F);
        collector.submitCustomGeometry(pose, RAYS, (modelPose, buffer) -> rays(modelPose, buffer, death));
        pose.popPose();
    }

    private static void rays(PoseStack.Pose pose, VertexConsumer buffer, float death) {
        float fade = Math.min(death > 0.8F ? (death - 0.8F) / 0.2F : 0.0F, 1.0F);
        int white = ARGB.colorFromFloat(1.0F - fade, 1.0F, 1.0F, 1.0F);
        RandomSource random = RandomSource.create(432L);
        Quaternionf rotation = new Quaternionf();
        Vector3f origin = new Vector3f();
        Vector3f left = new Vector3f();
        Vector3f right = new Vector3f();
        Vector3f tip = new Vector3f();
        int count = Mth.floor((death + death * death) / 2.0F * 60.0F);
        PoseStack.Pose base = pose.copy();
        for (int ray = 0; ray < count; ray++) {
            rotation.rotationXYZ(
                    random.nextFloat() * Mth.TWO_PI,
                    random.nextFloat() * Mth.TWO_PI,
                    random.nextFloat() * Mth.TWO_PI
            ).rotateXYZ(
                    random.nextFloat() * Mth.TWO_PI,
                    random.nextFloat() * Mth.TWO_PI,
                    random.nextFloat() * Mth.TWO_PI + death * Mth.HALF_PI
            );
            PoseStack.Pose spun = base.copy();
            spun.rotate(rotation);
            float length = random.nextFloat() * 20.0F + 5.0F + fade * 10.0F;
            float width = random.nextFloat() * 2.0F + 1.0F + fade * 2.0F;
            left.set(-HALF_SQRT_3 * width, length, -0.5F * width);
            right.set(HALF_SQRT_3 * width, length, -0.5F * width);
            tip.set(0.0F, length, width);
            buffer.addVertex(spun, origin).setColor(white);
            buffer.addVertex(spun, left).setColor(MAGENTA);
            buffer.addVertex(spun, right).setColor(MAGENTA);
            buffer.addVertex(spun, origin).setColor(white);
            buffer.addVertex(spun, right).setColor(MAGENTA);
            buffer.addVertex(spun, tip).setColor(MAGENTA);
            buffer.addVertex(spun, origin).setColor(white);
            buffer.addVertex(spun, tip).setColor(MAGENTA);
            buffer.addVertex(spun, left).setColor(MAGENTA);
        }
    }

    private static void box(VertexConsumer buffer, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        quad(buffer, pose, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0.0F, 0.0F, 1.0F, color);
        quad(buffer, pose, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, 0.0F, 0.0F, -1.0F, color);
        quad(buffer, pose, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, 0.0F, 1.0F, 0.0F, color);
        quad(buffer, pose, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, 0.0F, -1.0F, 0.0F, color);
        quad(buffer, pose, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, 1.0F, 0.0F, 0.0F, color);
        quad(buffer, pose, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, -1.0F, 0.0F, 0.0F, color);
    }

    private static void quad(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float nx,
            float ny,
            float nz,
            int color
    ) {
        vertex(buffer, pose, x0, y0, z0, 0.0F, 1.0F, nx, ny, nz, color);
        vertex(buffer, pose, x1, y1, z1, 1.0F, 1.0F, nx, ny, nz, color);
        vertex(buffer, pose, x2, y2, z2, 1.0F, 0.0F, nx, ny, nz, color);
        vertex(buffer, pose, x0, y0, z0, 0.0F, 1.0F, nx, ny, nz, color);
        vertex(buffer, pose, x2, y2, z2, 1.0F, 0.0F, nx, ny, nz, color);
        vertex(buffer, pose, x3, y3, z3, 0.0F, 0.0F, nx, ny, nz, color);
    }

    private static void vertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            float nx,
            float ny,
            float nz,
            int color
    ) {
        buffer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }
}
