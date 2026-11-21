package com.horrormod.client.mixin;

import com.horrormod.HorrorMod;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.rendertype.ImpurityItemTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.blaze3d.platform.Transparency;

@Mixin(BakedQuad.MaterialInfo.class)
public class ImpurityMaterialMixin {
    private static final Identifier SPRITE = HorrorMod.id("item/impurity_detector");

    @Inject(method = "of", at = @At("RETURN"), cancellable = true)
    private static void horror$detectorShader(
            Material.Baked baked,
            Transparency transparency,
            int tint,
            Direction direction,
            int light,
            CallbackInfoReturnable<BakedQuad.MaterialInfo> callback
    ) {
        BakedQuad.MaterialInfo info = callback.getReturnValue();
        if (info.sprite() == null || !SPRITE.equals(info.sprite().contents().name())) {
            return;
        }
        callback.setReturnValue(new BakedQuad.MaterialInfo(
                info.sprite(),
                info.layer(),
                ImpurityItemTypes.RENDER_TYPE,
                info.itemGlintRenderType(),
                info.itemGlintSpecialRenderType(),
                info.tintIndex(),
                info.shadeDirectionOverride(),
                info.lightEmission()
        ));
    }
}
