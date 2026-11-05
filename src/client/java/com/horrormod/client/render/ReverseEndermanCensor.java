package com.horrormod.client.render;

import com.horrormod.entity.ReverseEnderman;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A screen-space bar over each reverse enderman's face. The bar stays put until the face
 * is about to leave it, then snaps to the new spot. It is hidden only when the whole face
 * is behind full opaque cubes.
 */
public final class ReverseEndermanCensor {
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float HEAD_PIVOT_Y = -13.0F * PIXEL;
    private static final float CREEPY_LIFT = -5.0F * PIXEL;
    private static final float FACE_HALF = 4.0F * PIXEL;
    private static final float FACE_Z = -4.0F * PIXEL;
    private static final int SAMPLES = 3;
    private static final double SAMPLE_INSET = 0.15;
    private static final int BAR_COLOR = 0xFF000000;

    private static final Map<Integer, Rect> BARS = new HashMap<>();

    private ReverseEndermanCensor() {
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            BARS.clear();
            return;
        }
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        Vec3 camera = minecraft.gameRenderer.mainCamera().position();
        Vector3f forward = new Vector3f(minecraft.gameRenderer.mainCamera().forwardVector());
        Set<Integer> seen = new HashSet<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ReverseEnderman enderman)) {
                continue;
            }
            Rect face = projectFace(minecraft, enderman, partialTick, camera, forward, guiWidth, guiHeight);
            if (face == null) {
                continue;
            }
            int id = enderman.getId();
            seen.add(id);
            Rect bar = BARS.get(id);
            if (bar == null || !bar.contains(face, 3.0F)) {
                bar = face.padded();
                BARS.put(id, bar);
            }
            graphics.fill(Mth.floor(bar.left), Mth.floor(bar.top), Mth.ceil(bar.right), Mth.ceil(bar.bottom), BAR_COLOR);
        }
        BARS.keySet().retainAll(seen);
    }

    private static Rect projectFace(
            Minecraft minecraft,
            ReverseEnderman enderman,
            float partialTick,
            Vec3 camera,
            Vector3f forward,
            int guiWidth,
            int guiHeight
    ) {
        float headYaw = Mth.rotLerp(partialTick, enderman.yHeadRotO, enderman.yHeadRot);
        float bodyYaw = Mth.rotLerp(partialTick, enderman.yBodyRotO, enderman.yBodyRot);
        float relativeYaw = Mth.wrapDegrees(headYaw - bodyYaw) * Mth.DEG_TO_RAD;
        float pitch = enderman.getXRot(partialTick) * Mth.DEG_TO_RAD;
        float death = enderman.deathTime > 0 ? enderman.deathTime + partialTick : 0.0F;
        float fall = death <= 0.0F ? 0.0F : Mth.sqrt(Math.min(1.0F, (death - 1.0F) / 20.0F * 1.6F)) * 90.0F;
        float pivotY = HEAD_PIVOT_Y + (enderman.isCreepy() ? CREEPY_LIFT : 0.0F);
        Vec3 origin = enderman.getPosition(partialTick);
        float scale = enderman.getScale();

        Vec3 center = toWorld(origin, scale, bodyYaw, fall, pitch, relativeYaw, pivotY, 0.0F, -FACE_HALF, FACE_Z);
        Vec3 normal = toDirection(bodyYaw, fall, pitch, relativeYaw, 0.0F, 0.0F, -1.0F);
        if (normal.dot(camera.subtract(center)) <= 0.0) {
            return null;
        }
        if (!anyFaceVisible(minecraft.level, camera, origin, scale, bodyYaw, fall, pitch, relativeYaw, pivotY)) {
            return null;
        }

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        boolean any = false;
        for (int corner = 0; corner < 4; corner++) {
            float localX = (corner & 1) == 0 ? -FACE_HALF : FACE_HALF;
            float localY = (corner & 2) == 0 ? -FACE_HALF * 2.0F : 0.0F;
            Vec3 world = toWorld(origin, scale, bodyYaw, fall, pitch, relativeYaw, pivotY, localX, localY, FACE_Z);
            world = inFrontOfCamera(world, center, camera, forward);
            if (forward.dot(toVector(world.subtract(camera))) <= 0.05F) {
                continue;
            }
            Vec3 screen = minecraft.gameRenderer.projectPointToScreen(world);
            float x = (float) ((screen.x * 0.5 + 0.5) * guiWidth);
            float y = (float) ((1.0 - (screen.y * 0.5 + 0.5)) * guiHeight);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            any = true;
        }
        if (!any || maxX < 0.0F || maxY < 0.0F || minX > guiWidth || minY > guiHeight) {
            return null;
        }
        return new Rect(minX, minY, maxX, maxY);
    }

    private static boolean anyFaceVisible(
            ClientLevel level,
            Vec3 camera,
            Vec3 origin,
            float scale,
            float bodyYaw,
            float fall,
            float pitch,
            float relativeYaw,
            float pivotY
    ) {
        boolean anyClear = false;
        for (int y = 0; y < SAMPLES; y++) {
            for (int x = 0; x < SAMPLES; x++) {
                double tx = SAMPLE_INSET + (1.0 - 2.0 * SAMPLE_INSET) * x / (SAMPLES - 1.0);
                double ty = SAMPLE_INSET + (1.0 - 2.0 * SAMPLE_INSET) * y / (SAMPLES - 1.0);
                float localX = (float) Mth.lerp(tx, -FACE_HALF, FACE_HALF);
                float localY = (float) Mth.lerp(ty, -FACE_HALF * 2.0F, 0.0);
                Vec3 sample = toWorld(origin, scale, bodyYaw, fall, pitch, relativeYaw, pivotY, localX, localY, FACE_Z);
                if (!blockedBySolid(level, camera, sample)) {
                    anyClear = true;
                }
            }
        }
        return anyClear;
    }

    private static boolean blockedBySolid(ClientLevel level, Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        if (dx * dx + dy * dy + dz * dz < 1.0E-8) {
            return occludes(level, Mth.floor(to.x), Mth.floor(to.y), Mth.floor(to.z));
        }
        int x = Mth.floor(from.x);
        int y = Mth.floor(from.y);
        int z = Mth.floor(from.z);
        int endX = Mth.floor(to.x);
        int endY = Mth.floor(to.y);
        int endZ = Mth.floor(to.z);
        int stepX = dx > 0.0 ? 1 : (dx < 0.0 ? -1 : 0);
        int stepY = dy > 0.0 ? 1 : (dy < 0.0 ? -1 : 0);
        int stepZ = dz > 0.0 ? 1 : (dz < 0.0 ? -1 : 0);
        double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
        double tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
        double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);
        double tMaxX = stepX > 0 ? (x + 1.0 - from.x) * tDeltaX : (stepX < 0 ? (from.x - x) * tDeltaX : Double.POSITIVE_INFINITY);
        double tMaxY = stepY > 0 ? (y + 1.0 - from.y) * tDeltaY : (stepY < 0 ? (from.y - y) * tDeltaY : Double.POSITIVE_INFINITY);
        double tMaxZ = stepZ > 0 ? (z + 1.0 - from.z) * tDeltaZ : (stepZ < 0 ? (from.z - z) * tDeltaZ : Double.POSITIVE_INFINITY);
        for (int guard = 0; guard < 64 && (x != endX || y != endY || z != endZ); guard++) {
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                if (tMaxX > 1.0) {
                    break;
                }
                x += stepX;
                tMaxX += tDeltaX;
            } else if (tMaxY < tMaxZ) {
                if (tMaxY > 1.0) {
                    break;
                }
                y += stepY;
                tMaxY += tDeltaY;
            } else {
                if (tMaxZ > 1.0) {
                    break;
                }
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
            if ((x != endX || y != endY || z != endZ) && occludes(level, x, y, z)) {
                return true;
            }
        }
        return occludes(level, endX, endY, endZ);
    }

    private static boolean occludes(ClientLevel level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        return state.canOcclude() && level.getBlockEntity(pos) == null;
    }

    private static Vec3 toWorld(
            Vec3 origin,
            float scale,
            float bodyYaw,
            float fallDegrees,
            float pitch,
            float relativeYaw,
            float pivotY,
            float localX,
            float localY,
            float localZ
    ) {
        Vector3f local = new Vector3f(localX, localY, localZ);
        local.rotate(new Quaternionf().rotationZYX(0.0F, relativeYaw, pitch));
        local.add(0.0F, pivotY, 0.0F);
        local.add(0.0F, -1.501F, 0.0F);
        local.set(-local.x, -local.y, local.z);
        if (fallDegrees != 0.0F) {
            local.rotateZ(fallDegrees * Mth.DEG_TO_RAD);
        }
        local.rotateY((180.0F - bodyYaw) * Mth.DEG_TO_RAD);
        local.mul(scale);
        return origin.add(local.x, local.y, local.z);
    }

    private static Vec3 toDirection(
            float bodyYaw,
            float fallDegrees,
            float pitch,
            float relativeYaw,
            float localX,
            float localY,
            float localZ
    ) {
        Vector3f local = new Vector3f(localX, localY, localZ);
        local.rotate(new Quaternionf().rotationZYX(0.0F, relativeYaw, pitch));
        local.set(-local.x, -local.y, local.z);
        if (fallDegrees != 0.0F) {
            local.rotateZ(fallDegrees * Mth.DEG_TO_RAD);
        }
        local.rotateY((180.0F - bodyYaw) * Mth.DEG_TO_RAD);
        return new Vec3(local.x, local.y, local.z);
    }

    private static Vec3 inFrontOfCamera(Vec3 point, Vec3 center, Vec3 camera, Vector3f forward) {
        float pointDot = forward.dot(toVector(point.subtract(camera)));
        if (pointDot > 0.05F) {
            return point;
        }
        float centerDot = forward.dot(toVector(center.subtract(camera)));
        if (centerDot <= 0.05F) {
            return point;
        }
        double t = (0.05F - pointDot) / (centerDot - pointDot);
        return point.add(center.subtract(point).scale(t));
    }

    private static Vector3f toVector(Vec3 vec) {
        return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
    }

    private record Rect(float left, float top, float right, float bottom) {
        private boolean contains(Rect face, float margin) {
            return face.left >= this.left + margin
                    && face.top >= this.top + margin
                    && face.right <= this.right - margin
                    && face.bottom <= this.bottom - margin;
        }

        private Rect padded() {
            float width = this.right - this.left;
            float height = this.bottom - this.top;
            float padX = Math.max(10.0F, width * 0.55F);
            float padY = Math.max(6.0F, height * 0.40F);
            return new Rect(this.left - padX, this.top - padY, this.right + padX, this.bottom + padY);
        }
    }
}
