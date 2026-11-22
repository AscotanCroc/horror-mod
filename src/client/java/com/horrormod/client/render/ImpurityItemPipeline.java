package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Chromatic split for the impurity detector. A full vertex alpha means the item is still.
 */
public final class ImpurityItemPipeline {
    public static final RenderPipeline WORLD = RenderPipeline.builder()
            .withLocation(HorrorMod.id("pipeline/impurity_item"))
            .withVertexShader("core/item")
            .withFragmentShader(HorrorMod.id("core/impurity_item"))
            .withBindGroupLayout(BindGroupLayouts.GLOBALS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.FOG)
            .withBindGroupLayout(BindGroupLayouts.LIGHTING)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2)
            .withVertexBinding(0, DefaultVertexFormat.ENTITY)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .build();

    public static final RenderPipeline GUI = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(HorrorMod.id("pipeline/impurity_gui"))
            .withFragmentShader(HorrorMod.id("core/impurity_gui"))
            .withUsePipelineDrawModeForGui(true)
            .build();

    private ImpurityItemPipeline() {
    }

    public static void initialize() {
        HorrorMod.LOGGER.info("Registered impurity item pipeline {}", WORLD.getLocation());
    }
}
