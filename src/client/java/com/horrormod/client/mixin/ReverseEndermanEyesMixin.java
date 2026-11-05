package com.horrormod.client.mixin;

import com.horrormod.client.render.ReverseEndermanRenderer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyesLayer.class)
public class ReverseEndermanEyesMixin {
    @Inject(method = "renderType", at = @At("HEAD"), cancellable = true)
    private void horror$reverseEyes(CallbackInfoReturnable<RenderType> callback) {
        if (((RenderLayerAccessor) this).horror$renderer() instanceof ReverseEndermanRenderer) {
            callback.setReturnValue(RenderTypes.eyes(ReverseEndermanRenderer.EYES));
        }
    }
}
