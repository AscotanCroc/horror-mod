package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.Optional;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * Position-color quads with no depth test, and OIT stages so the entity pass accepts them.
 * Debug quads have OIT but still depth-test, so a wall hides the marker.
 */
public final class ReverseMarkerRenderType {
    private static final Identifier SHADER = Identifier.withDefaultNamespace("core/position_color");
    private static RenderType type;

    private ReverseMarkerRenderType() {
    }

    public static void initialize() {
        RenderPipeline pipeline = RenderPipeline.builder()
                .withLocation(HorrorMod.id("pipeline/reverse_marker"))
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withVertexShader(SHADER)
                .withFragmentShader(SHADER)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withDepthStencilState(Optional.empty())
                .withCull(false)
                .build();
        RenderPipelines.register(pipeline);

        OitPipelineSet orderIndependent = OitPipelineSet.builder(
                "reverse_marker",
                RenderPipeline.builder()
                        .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                        .withVertexShader(SHADER)
                        .withFragmentShader(SHADER)
                        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                        .withPrimitiveTopology(PrimitiveTopology.QUADS)
                        .withCull(false)
        ).withoutDepthTest().build();
        RenderPipelines.register(orderIndependent);

        type = RenderType.create(
                "horror_reverse_marker",
                RenderSetup.builder(pipeline).setOitPipelines(orderIndependent).sortOnUpload().createRenderSetup()
        );
    }

    public static RenderType get() {
        return type;
    }
}
