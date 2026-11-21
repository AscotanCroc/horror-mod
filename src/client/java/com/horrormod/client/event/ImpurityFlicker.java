package com.horrormod.client.event;

import com.horrormod.entity.ImpurityDetector;
import com.horrormod.network.ImpuritySignalPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

/**
 * Transparent chromatic pixels in the direction of the nearest reverse mob.
 * Faint and rare at the edge of range, constant beside it.
 */
public final class ImpurityFlicker {
    private static final int[] CHANNELS = {0xFF2040, 0x40FFD0, 0x2060FF};

    private static boolean present;
    private static double mobX;
    private static double mobY;
    private static double mobZ;
    private static long receivedAt;
    private static int itemTicks;
    private static float itemShaped;
    private static final List<Flash> flashes = new ArrayList<>();

    private ImpurityFlicker() {
    }

    public static void receive(ImpuritySignalPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        present = payload.present();
        mobX = payload.x();
        mobY = payload.y();
        mobZ = payload.z();
        receivedAt = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        if (!present) {
            flashes.clear();
            itemTicks = 0;
            itemShaped = 0.0F;
        }
    }

    /** How hard the item shader should split, or zero while the item is still. */
    public static float itemGlitch() {
        return itemTicks > 0 ? itemShaped : 0.0F;
    }

    public static void tick(Minecraft minecraft) {
        flashes.removeIf(flash -> --flash.ticks <= 0);
        if (minecraft.level == null || minecraft.player == null) {
            present = false;
            flashes.clear();
            return;
        }
        if (present && minecraft.level.getGameTime() - receivedAt > 40L) {
            present = false;
            flashes.clear();
            itemTicks = 0;
            itemShaped = 0.0F;
            return;
        }
        if (itemTicks > 0) {
            itemTicks--;
        }
        if (!present) {
            return;
        }
        Vec3 eyes = minecraft.player.getEyePosition();
        double dx = mobX - eyes.x;
        double dy = mobY - eyes.y;
        double dz = mobZ - eyes.z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float closeness = (float) (1.0 - Mth.clamp(distance / ImpurityDetector.RANGE, 0.0, 1.0));
        float shaped = closeness * closeness;
        float itemChance = 0.012F + 0.988F * shaped;
        if (shaped > 0.85F) {
            itemTicks = Math.max(itemTicks, 2);
            itemShaped = shaped;
        } else if (itemTicks <= 0 && minecraft.player.getRandom().nextFloat() < itemChance) {
            itemTicks = 2 + minecraft.player.getRandom().nextInt(3);
            itemShaped = shaped;
        }
        float chance = 0.004F + 0.16F * shaped;
        if (minecraft.player.getRandom().nextFloat() >= chance) {
            return;
        }
        flashes.add(new Flash(2 + minecraft.player.getRandom().nextInt(2), shaped));
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (!present || flashes.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        int[] point = project(minecraft, mobX, mobY, mobZ);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        for (Flash flash : flashes) {
            int alpha = Math.round(Mth.lerp(flash.shaped, 10.0F, 52.0F));
            int count = Math.round(Mth.lerp(flash.shaped, 2.0F, 5.0F));
            for (int pixel = 0; pixel < count; pixel++) {
                int channel = pixel % 3;
                int ox = (channel - 1) * 2 + (pixel / 3) % 3 - 1;
                int oy = (pixel * 2) % 5 - 2;
                int x = Mth.clamp(point[0] + ox, 1, width - 2);
                int y = Mth.clamp(point[1] + oy, 1, height - 2);
                int color = (alpha << 24) | CHANNELS[channel];
                graphics.fill(x, y, x + 1, y + 1, color);
            }
        }
    }

    private static int[] project(Minecraft minecraft, double x, double y, double z) {
        Camera camera = minecraft.gameRenderer.mainCamera();
        Vec3 cam = camera.position();
        Vector3fc forward = camera.forwardVector();
        Vector3fc up = camera.upVector();
        Vector3fc left = camera.leftVector();
        double dx = x - cam.x;
        double dy = y - cam.y;
        double dz = z - cam.z;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-4) {
            length = 1.0;
        }
        dx /= length;
        dy /= length;
        dz /= length;
        double facing = dx * forward.x() + dy * forward.y() + dz * forward.z();
        double right = -(dx * left.x() + dy * left.y() + dz * left.z());
        double screenUp = dx * up.x() + dy * up.y() + dz * up.z();
        double sx = right;
        double sy = -screenUp;
        double mag = Math.hypot(sx, sy);
        if (mag < 1.0E-4) {
            sx = 0.0;
            sy = facing <= 0.05 ? 1.0 : 0.0;
            mag = 1.0;
        } else {
            sx /= mag;
            sy /= mag;
        }
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        float edge = Math.max(12.0F, Math.min(centerX, centerY) - 6.0F);
        float radius;
        if (facing <= 0.05) {
            radius = edge;
        } else if (mag < 1.0E-4) {
            radius = 0.0F;
        } else {
            float angle = (float) Math.acos(Mth.clamp(facing, -1.0, 1.0));
            radius = edge * Math.min(1.0F, angle / (float) (Math.PI / 2.0));
        }
        int px = Math.round(centerX + (float) sx * radius);
        int py = Math.round(centerY + (float) sy * radius);
        return new int[] {
                Mth.clamp(px, 2, width - 3),
                Mth.clamp(py, 2, height - 3)
        };
    }

    private static final class Flash {
        private int ticks;
        private final float shaped;

        private Flash(int ticks, float shaped) {
            this.ticks = ticks;
            this.shaped = shaped;
        }
    }
}
