package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import com.horrormod.entity.ReverseSkeleton;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class ReverseSkeletonRenderer extends AbstractSkeletonRenderer<ReverseSkeleton, SkeletonRenderState> {
    private static final Identifier TEXTURE = HorrorMod.id("textures/entity/reverse_skeleton.png");
    private static final Identifier EMISSIVE = HorrorMod.id("textures/entity/reverse_skeleton_emissive.png");

    public ReverseSkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.SKELETON_ARMOR, new ReverseSkeletonModel(context.bakeLayer(ReverseSkeletonModel.LAYER_LOCATION)));
        this.addLayer(new GlowLayer(this));
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return TEXTURE;
    }

    private static final class GlowLayer extends EyesLayer<SkeletonRenderState, SkeletonModel<SkeletonRenderState>> {
        private static final RenderType RENDER_TYPE = RenderTypes.eyes(EMISSIVE);

        private GlowLayer(ReverseSkeletonRenderer renderer) {
            super(renderer);
        }

        @Override
        public RenderType renderType() {
            return RENDER_TYPE;
        }
    }
}
