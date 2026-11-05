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
 * The face is {@code assets/horror_mod/textures/gui/veil_face.png}, 512 by 512.
 * Its opacity eases from nothing up to {@link #PEAK_ALPHA}, holds, then the picture cuts.
 */
public final class VeilRitual {
    public static final Identifier FACE = HorrorMod.id("textures/gui/veil_face.png");
    public static final int FACE_SIZE = 512;
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
        silence();
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.gui.screen() instanceof LevelLoadingScreen)) {
            minecraft.gui.setScreen(null);
        }
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
        int size = Math.max(1, Math.round(Math.min(width, height) * 0.72F));
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                FACE,
                x,
                y,
                0.0F,
                0.0F,
                size,
                size,
                FACE_SIZE,
                FACE_SIZE,
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

    private static void silence() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSoundManager().stop();
        minecraft.getMusicManager().stopPlaying();
    }
}
