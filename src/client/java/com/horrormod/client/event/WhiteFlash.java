package com.horrormod.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * One hard white frame, gone a few ticks later.
 * While the view is cutting, that flash keeps going until the cuts stop.
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
        if (!showing()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        graphics.fill(0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), 0xFFFFFFFF);
    }

    private static boolean showing() {
        if (SpiderSnap.active()) {
            return SpiderSnap.flashing();
        }
        return remaining > 0;
    }
}
