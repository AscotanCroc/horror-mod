package com.horrormod.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * One hard white frame, gone a few ticks later.
 */
public final class WhiteFlash {
    private static int remaining;

    private WhiteFlash() {
    }

    public static void begin() {
        remaining = 3;
    }

    public static void tick() {
        if (remaining > 0) {
            remaining--;
        }
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (remaining <= 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        graphics.fill(0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), 0xFFFFFFFF);
    }
}
