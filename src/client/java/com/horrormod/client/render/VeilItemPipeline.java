package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.awt.Color;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Rainbow used by the key icon and by the held and dropped model.
 */
public final class VeilItemPipeline {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(HorrorMod.id("pipeline/veil_item"))
            .withFragmentShader(HorrorMod.id("core/veil_item"))
            .withUsePipelineDrawModeForGui(true)
            .build();

    private VeilItemPipeline() {
    }

    public static void initialize() {
        HorrorMod.LOGGER.info("Registered veil item pipeline {}", PIPELINE.getLocation());
    }

    public static int color() {
        float seconds = (System.nanoTime() % 1_000_000_000_000L) / 1_000_000_000.0F;
        float hue = (seconds * 0.15F) % 1.0F;
        return 0xFF000000 | (Color.HSBtoRGB(hue, 0.9F, 1.0F) & 0xFFFFFF);
    }

    public static int color(float hue, int alpha) {
        return (alpha << 24) | (Color.HSBtoRGB(hue, 0.95F, 1.0F) & 0xFFFFFF);
    }
}
