package com.horrormod.client.event;

import com.horrormod.HorrorMod;
import com.horrormod.client.sound.HorrorAmbience;
import com.horrormod.event.HorrorEvent;
import com.horrormod.network.BlackoutPayload;
import com.horrormod.network.HorrorEffectPayload;
import com.horrormod.network.InventoryVisiblePayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Screen, sky, and light timers for the events that do not change the world.
 */
public final class HorrorClientEffects {
    private static final Identifier GLITCH_TEXTURE = HorrorMod.id("textures/item/glitch.png");

    private static String effect = "";
    private static long endTick;
    private static float anchorX;
    private static float anchorY;
    private static float stretchX = 1.0F;
    private static float stretchY = 1.0F;
    private static ClientLevel trackedLevel;
    private static boolean inventoryVisible;
    private static long blackoutUntil;
    private static boolean silenced;

    private HorrorClientEffects() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(HorrorEffectPayload.TYPE, (payload, context) ->
                context.client().execute(() -> show(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(BlackoutPayload.TYPE, (payload, context) ->
                context.client().execute(() -> showBlackout(payload.endTick()))
        );
        ClientTickEvents.END_CLIENT_TICK.register(HorrorClientEffects::tick);
    }

    public static boolean lockingControls() {
        return VeilRitual.active() || blackoutActive();
    }

    public static boolean silencing() {
        return VeilRitual.active() || (silenced && blackoutActive());
    }

    public static void render(GuiGraphicsExtractor graphics) {
        WhiteFlash.render(graphics);
        if (VeilRitual.active()) {
            VeilRitual.render(graphics);
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        if (lockingControls()) {
            graphics.fill(0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), 0xFF000000);
            return;
        }
        if (!active(minecraft)) {
            return;
        }
        if (effect.equals(HorrorEvent.FLICKER.id()) && ((minecraft.level.getGameTime() / 2L) & 1L) == 0L) {
            graphics.fill(0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), 0xFF000000);
            return;
        }
        if (!effect.equals(HorrorEvent.STRETCHED_FLESH.id())) {
            return;
        }
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        int drawWidth = Math.max(16, Math.round(16.0F * stretchX));
        int drawHeight = Math.max(16, Math.round(16.0F * stretchY));
        int x = Math.round(anchorX * Math.max(0, guiWidth - drawWidth));
        int y = Math.round(anchorY * Math.max(0, guiHeight - drawHeight));
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                GLITCH_TEXTURE,
                x,
                y,
                0.0F,
                0.0F,
                drawWidth,
                drawHeight,
                16,
                16,
                16,
                16
        );
    }

    public static boolean swapCelestial() {
        return active(Minecraft.getInstance()) && effect.equals(HorrorEvent.CELESTIAL.id());
    }

    public static float starBrightness() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!active(minecraft) || !effect.equals(HorrorEvent.STARS.id()) || minecraft.level == null) {
            return 0.0F;
        }
        return (minecraft.level.getGameTime() & 1L) == 0L ? 1.0F : 0.0F;
    }

    public static boolean fullBrightness() {
        return active(Minecraft.getInstance()) && effect.equals(HorrorEvent.BRIGHTNESS.id());
    }

    private static void show(HorrorEffectPayload payload) {
        effect = payload.effect();
        endTick = payload.endTick();
        anchorX = payload.x();
        anchorY = payload.y();
        stretchX = payload.stretchX();
        stretchY = payload.stretchY();
    }

    private static void showBlackout(long endTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level.getGameTime() >= endTick) {
            return;
        }
        blackoutUntil = Math.max(blackoutUntil, endTick);
        silenced = true;
        minecraft.getSoundManager().stop();
        Screen screen = minecraft.gui.screen();
        if (!(screen instanceof PauseScreen) && !(screen instanceof DeathScreen)) {
            minecraft.gui.setScreen(null);
        }
    }

    private static void tick(Minecraft minecraft) {
        if (minecraft.level != trackedLevel) {
            boolean resume = silenced && minecraft.level != null && !VeilRitual.active();
            trackedLevel = minecraft.level;
            clear();
            if (resume) {
                HorrorAmbience.resumeAfterReload();
            }
            sendVisibility(minecraft);
            return;
        }
        if (silenced && minecraft.level != null && minecraft.level.getGameTime() >= blackoutUntil) {
            silenced = false;
            blackoutUntil = 0L;
            if (!VeilRitual.active()) {
                HorrorAmbience.resumeAfterReload();
            }
        }
        boolean visible = minecraft.gui.screen() instanceof AbstractContainerScreen;
        if (visible != inventoryVisible) {
            inventoryVisible = visible;
            sendVisibility(minecraft);
        }
    }

    private static void sendVisibility(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.getConnection() == null) {
            return;
        }
        inventoryVisible = minecraft.gui.screen() instanceof AbstractContainerScreen;
        ClientPlayNetworking.send(new InventoryVisiblePayload(inventoryVisible));
    }

    private static boolean blackoutActive() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && blackoutUntil > minecraft.level.getGameTime();
    }

    private static boolean active(Minecraft minecraft) {
        return minecraft.level != null && !effect.isEmpty() && minecraft.level.getGameTime() < endTick;
    }

    private static void clear() {
        effect = "";
        endTick = 0L;
        silenced = false;
        blackoutUntil = 0L;
    }
}
