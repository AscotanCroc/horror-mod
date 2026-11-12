package com.horrormod.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Hard-cuts the view to a new direction each tick. No blend between cuts.
 */
public final class SpiderSnap {
    private static final int TICKS = 20;
    private static int remaining;

    private SpiderSnap() {
    }

    public static void begin() {
        remaining = TICKS;
        snap();
    }

    public static boolean active() {
        return remaining > 0;
    }

    /**
     * Three ticks of white, then one tick of the new view, until the cuts stop.
     */
    public static boolean flashing() {
        return remaining > 0 && (remaining & 3) != 0;
    }

    public static void tick(Minecraft minecraft) {
        if (remaining <= 0) {
            return;
        }
        remaining--;
        snap();
    }

    private static void snap() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            remaining = 0;
            return;
        }
        float yaw = player.getRandom().nextFloat() * 360.0F - 180.0F;
        float pitch = player.getRandom().nextFloat() * 140.0F - 70.0F;
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
        player.setYHeadRot(yaw);
        player.yHeadRotO = yaw;
        player.yBodyRot = yaw;
        player.yBodyRotO = yaw;
    }
}
