package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;

public class ReverseEndermanRenderer extends EndermanRenderer {
    public static final Identifier TEXTURE = HorrorMod.id("textures/entity/reverse_enderman/reverse_enderman.png");
    public static final Identifier EYES = HorrorMod.id("textures/entity/reverse_enderman/reverse_enderman_eyes.png");

    public ReverseEndermanRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(EndermanRenderState state) {
        return TEXTURE;
    }
}
