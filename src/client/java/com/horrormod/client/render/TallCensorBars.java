package com.horrormod.client.render;

import com.horrormod.entity.TallCensor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * A sticky black bar over a tall censor's whole body, using the same screen-space bar as the reverse enderman's face.
 */
public final class TallCensorBars {
    private static final int BAR_COLOR = 0xFF000000;
    private static final Map<Integer, Rect> BARS = new HashMap<>();

    private TallCensorBars() {
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
            if (!(entity instanceof TallCensor censor)) {
                continue;
            }
            Rect body = project(minecraft, censor, partialTick, camera, forward, guiWidth, guiHeight);
            if (body == null) {
                continue;
            }
            int id = censor.getId();
            seen.add(id);
            Rect bar = BARS.get(id);
            if (bar == null || !bar.contains(body, 3.0F)) {
                bar = body.padded();
                BARS.put(id, bar);
            }
            graphics.fill(Mth.floor(bar.left), Mth.floor(bar.top), Mth.ceil(bar.right), Mth.ceil(bar.bottom), BAR_COLOR);
        }
        BARS.keySet().retainAll(seen);
    }

    private static Rect project(
            Minecraft minecraft,
            TallCensor censor,
            float partialTick,
            Vec3 camera,
            Vector3f forward,
            int guiWidth,
            int guiHeight
    ) {
        Vec3 pos = censor.getPosition(partialTick);
        AABB box = censor.getBoundingBox().move(pos.subtract(censor.position()));
        if (!anyClear(minecraft, camera, box)) {
            return null;
        }
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        boolean any = false;
        for (int corner = 0; corner < 8; corner++) {
            Vec3 point = new Vec3(
                    (corner & 1) == 0 ? box.minX : box.maxX,
                    (corner & 2) == 0 ? box.minY : box.maxY,
                    (corner & 4) == 0 ? box.minZ : box.maxZ
            );
            if (forward.dot(toVector(point.subtract(camera))) <= 0.05F) {
                continue;
            }
            Vec3 screen = minecraft.gameRenderer.projectPointToScreen(point);
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

    private static boolean anyClear(Minecraft minecraft, Vec3 camera, AABB box) {
        ClientLevel level = minecraft.level;
        for (int index = 0; index < 6; index++) {
            double y = Mth.lerp(index / 5.0, box.minY + 1.0, box.maxY - 1.0);
            Vec3 point = new Vec3(box.getCenter().x, y, box.getCenter().z);
            BlockHitResult hit = level.clip(new ClipContext(camera, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, minecraft.player));
            if (hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(point) <= 2.25) {
                return true;
            }
        }
        return false;
    }

    private static Vector3f toVector(Vec3 vec) {
        return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
    }

    private record Rect(float left, float top, float right, float bottom) {
        private boolean contains(Rect body, float margin) {
            return body.left >= this.left + margin
                    && body.top >= this.top + margin
                    && body.right <= this.right - margin
                    && body.bottom <= this.bottom - margin;
        }

        private Rect padded() {
            float width = this.right - this.left;
            float height = this.bottom - this.top;
            float padX = Math.min(28.0F, Math.max(8.0F, width * 0.12F));
            float padY = Math.min(20.0F, Math.max(6.0F, height * 0.08F));
            return new Rect(this.left - padX, this.top - padY, this.right + padX, this.bottom + padY);
        }
    }
}
