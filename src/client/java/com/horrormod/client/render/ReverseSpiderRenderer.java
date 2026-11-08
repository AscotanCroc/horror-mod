package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import com.horrormod.entity.ReverseSpider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class ReverseSpiderRenderer extends MobRenderer<ReverseSpider, LivingEntityRenderState, SpiderModel> {
    private static final Identifier WHITE = HorrorMod.id("textures/entity/reverse_spider_white.png");
    private static final Identifier BLACK = HorrorMod.id("textures/entity/reverse_spider_black.png");

    public ReverseSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.8F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return ((int) (state.ageInTicks * 2.0F) & 1) == 0 ? WHITE : BLACK;
    }

    @Override
    protected int getBlockLightLevel(ReverseSpider entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(ReverseSpider entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0F;
    }
}
