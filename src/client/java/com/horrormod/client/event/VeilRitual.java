package com.horrormod.client.event;

import com.horrormod.HorrorMod;
import com.horrormod.client.sound.HorrorAmbience;
import com.horrormod.network.VeilRitualPayload;
import com.horrormod.world.HorrorDimensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Black screen over the trip into the veil, including the loading screen.
 * The picture is {@code assets/horror_mod/textures/gui/veil_face.png}.
 * Its opacity eases from nothing up to {@link #PEAK_ALPHA}, holds, then the picture cuts.
 */
public final class VeilRitual {
    public static final Identifier FACE = HorrorMod.id("textures/gui/veil_face.png");
    public static final int FACE_WIDTH = 768;
    public static final int FACE_HEIGHT = 1024;
    /** About 22% opacity. This is as bright as the face is allowed to get. */
    public static final int PEAK_ALPHA = 56;
    private static final long REVEAL_NANOS = 8_000_000_000L;
    private static final long HOLD_NANOS = 2_000_000_000L;
    private static final long CAP_NANOS = 15_000_000_000L;

    private static boolean running;
    private static long startedAt;

    private VeilRitual() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(VeilRitual::tick);
        ClientPlayNetworking.registerGlobalReceiver(VeilRitualPayload.TYPE, (payload, context) ->
                context.client().execute(VeilRitual::begin)
        );
    }

    public static void begin() {
        if (active()) {
            return;
        }
        running = true;
        startedAt = System.nanoTime();
        hideCursor();
        silence();
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.gui.screen() instanceof LevelLoadingScreen)) {
            minecraft.gui.setScreen(null);
        }
        hideCursor();
    }

    public static boolean active() {
        if (!running) {
            return false;
        }
        long elapsed = System.nanoTime() - startedAt;
        if (elapsed >= CAP_NANOS) {
            return false;
        }
        if (elapsed < REVEAL_NANOS + HOLD_NANOS) {
            return true;
        }
        return !arrived();
    }

    public static void tick(Minecraft minecraft) {
        if (!running) {
            return;
        }
        if (active()) {
            hideCursor();
            silence();
            return;
        }
        running = false;
        HorrorAmbience.resumeAfterReload();
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (!active()) {
            return;
        }
        silence();
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        graphics.fill(0, 0, width, height, 0xFF000000);
        int alpha = faceAlpha();
        if (alpha <= 0) {
            return;
        }
        int max = Math.max(1, Math.round(Math.min(width, height) * 0.72F));
        int drawHeight = max;
        int drawWidth = Math.max(1, Math.round(max * (FACE_WIDTH / (float) FACE_HEIGHT)));
        int x = (width - drawWidth) / 2;
        int y = (height - drawHeight) / 2;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                FACE,
                x,
                y,
                0.0F,
                0.0F,
                drawWidth,
                drawHeight,
                FACE_WIDTH,
                FACE_HEIGHT,
                (alpha << 24) | 0xFFFFFF
        );
    }

    private static int faceAlpha() {
        long elapsed = System.nanoTime() - startedAt;
        if (elapsed >= REVEAL_NANOS) {
            return PEAK_ALPHA;
        }
        float t = elapsed / (float) REVEAL_NANOS;
        float curved = t * t * t;
        return Math.round(PEAK_ALPHA * curved);
    }

    private static boolean arrived() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null
                && !(minecraft.gui.screen() instanceof LevelLoadingScreen)
                && HorrorDimensions.VEIL.equals(minecraft.level.dimension());
    }

    private static void hideCursor() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.mouseHandler.isMouseGrabbed()) {
            minecraft.mouseHandler.grabMouse();
        }
    }

    private static void silence() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSoundManager().stop();
        minecraft.getMusicManager().stopPlaying();
    }
}
