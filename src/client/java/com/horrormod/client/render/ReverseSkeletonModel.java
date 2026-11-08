package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.util.Mth;

/**
 * The Blockbench skeleton. The aim bone has no cubes. The heads shift together and keep their angles.
 */
public class ReverseSkeletonModel extends SkeletonModel<SkeletonRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(HorrorMod.id("reverse_skeleton"), "main");
    private static final String[] HEAD_NAMES = {"head_r1", "head_r2", "head_r3", "head_r4"};

    private final ModelPart[] shakingHeads;
    private final float[] baseX = new float[HEAD_NAMES.length];
    private final float[] baseY = new float[HEAD_NAMES.length];
    private final float[] baseZ = new float[HEAD_NAMES.length];
    private final float[] baseXRot = new float[HEAD_NAMES.length];
    private final float[] baseYRot = new float[HEAD_NAMES.length];
    private final float[] baseZRot = new float[HEAD_NAMES.length];

    public ReverseSkeletonModel(ModelPart root) {
        super(root);
        this.head.visible = false;
        this.hat.visible = false;
        ModelPart heads = root.getChild("shaking_heads");
        this.shakingHeads = new ModelPart[HEAD_NAMES.length];
        for (int index = 0; index < HEAD_NAMES.length; index++) {
            ModelPart skull = heads.getChild(HEAD_NAMES[index]);
            this.shakingHeads[index] = skull;
            this.baseX[index] = skull.x;
            this.baseY[index] = skull.y;
            this.baseZ[index] = skull.z;
            this.baseXRot[index] = skull.xRot;
            this.baseYRot[index] = skull.yRot;
            this.baseZRot[index] = skull.zRot;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition shakingHeads = partdefinition.addOrReplaceChild("shaking_heads", CubeListBuilder.create(), PartPose.ZERO);
        shakingHeads.addOrReplaceChild(
                "head_r1",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, -1.0F, 2.0F, -2.685F, 0.0806F, 2.8616F)
        );
        shakingHeads.addOrReplaceChild(
                "head_r2",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.0F, -1.0F, -3.0F, -0.4583F, 0.1153F, -0.3762F)
        );
        shakingHeads.addOrReplaceChild(
                "head_r3",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.0F, -1.0F, -1.0F, 0.2835F, 0.1153F, -0.3762F)
        );
        shakingHeads.addOrReplaceChild(
                "head_r4",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.3491F)
        );

        partdefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-0.75F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.25F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(2.0F, 12.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 12.0F, 0.0F)
        );

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(SkeletonRenderState state) {
        super.setupAnim(state);
        float time = state.ageInTicks;
        float dx = Mth.sin(time * 3.4F) * 0.45F;
        float dy = Mth.sin(time * 5.1F) * 0.3F;
        float dz = Mth.cos(time * 4.2F) * 0.35F;
        for (int index = 0; index < this.shakingHeads.length; index++) {
            ModelPart skull = this.shakingHeads[index];
            skull.x = this.baseX[index] + dx;
            skull.y = this.baseY[index] + dy;
            skull.z = this.baseZ[index] + dz;
            skull.xRot = this.baseXRot[index];
            skull.yRot = this.baseYRot[index];
            skull.zRot = this.baseZRot[index];
        }
    }
}
