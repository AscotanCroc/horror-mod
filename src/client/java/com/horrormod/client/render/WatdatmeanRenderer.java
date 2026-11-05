package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import com.horrormod.entity.Watdatmean;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class WatdatmeanRenderer extends MobRenderer<Watdatmean, LivingEntityRenderState, WatdatmeanModel> {
    private static final Identifier TEXTURE = HorrorMod.id("textures/entity/watdatmean.png");

    public WatdatmeanRenderer(EntityRendererProvider.Context context) {
        super(context, new WatdatmeanModel(context.bakeLayer(WatdatmeanModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
