package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class WatdatmeanModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(HorrorMod.id("watdatmean"), "main");

    private final KeyframeAnimation walk;

    public WatdatmeanModel(ModelPart root) {
        super(root);
        this.walk = WatdatmeanAnimation.WALK.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        PartDefinition mouse = root.addOrReplaceChild("the sihtf mouse", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        mouse.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -10.0F, -6.0F, 12.0F, 8.0F, 13.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        PartDefinition toes = mouse.addOrReplaceChild("toes", CubeListBuilder.create(), PartPose.offset(-5.0F, 0.0F, 6.0F));
        toes.addOrReplaceChild(
                "bottom left",
                CubeListBuilder.create().texOffs(10, 34).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(10.0F, -2.0F, 0.0F)
        );
        toes.addOrReplaceChild(
                "top right",
                CubeListBuilder.create().texOffs(10, 30).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, -11.0F)
        );
        toes.addOrReplaceChild(
                "top left",
                CubeListBuilder.create().texOffs(30, 26).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(10.0F, -2.0F, -11.0F)
        );
        toes.addOrReplaceChild(
                "bottom right",
                CubeListBuilder.create().texOffs(30, 30).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, 0.0F)
        );

        PartDefinition ears = mouse.addOrReplaceChild("ears", CubeListBuilder.create(), PartPose.offset(-2.0F, -10.0F, -3.0F));
        ears.addOrReplaceChild(
                "right",
                CubeListBuilder.create().texOffs(18, 28).addBox(-3.0F, -4.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(6.0F, 0.0F, 0.0F)
        );
        ears.addOrReplaceChild(
                "left",
                CubeListBuilder.create().texOffs(18, 21).addBox(-3.0F, -5.0F, -1.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        PartDefinition nose = mouse.addOrReplaceChild(
                "nose",
                CubeListBuilder.create()
                        .texOffs(18, 34).addBox(-4.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(22, 34).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, -5.0F, -7.0F)
        );
        PartDefinition tip = nose.addOrReplaceChild("tip", CubeListBuilder.create(), PartPose.offset(-2.0F, -1.0F, -5.0F));
        tip.addOrReplaceChild(
                "cube_r1",
                CubeListBuilder.create().texOffs(0, 30).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.7418F, 0.0F, 0.0F)
        );
        nose.addOrReplaceChild(
                "shaft",
                CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 0.0F, 0.0F)
        );

        mouse.addOrReplaceChild(
                "tails",
                CubeListBuilder.create().texOffs(30, 21).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -4.0F, 8.0F)
        );
        mouse.addOrReplaceChild(
                "utters",
                CubeListBuilder.create()
                        .texOffs(26, 34).addBox(-4.0F, -2.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 34).addBox(-1.0F, -2.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 35).addBox(-4.0F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(34, 34).addBox(-1.0F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 0.0F, 4.0F)
        );

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.walk.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 2.0F, 2.5F);
    }
}
