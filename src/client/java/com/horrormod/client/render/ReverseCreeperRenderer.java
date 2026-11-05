package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

public class ReverseCreeperRenderer extends CreeperRenderer {
    private static final Identifier TEXTURE = HorrorMod.id("textures/entity/reverse_creeper.png");

    public ReverseCreeperRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(CreeperRenderState state) {
        return TEXTURE;
    }
}
