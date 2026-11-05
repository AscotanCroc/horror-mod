package com.horrormod.client.mixin;

import com.horrormod.HorrorMod;
import com.horrormod.client.render.VeilItemPipeline;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFeatureRenderer.class)
public class VeilHeldItemMixin {
    private static final Identifier SPRITE = HorrorMod.id("item/veil");

    @Inject(
            method = "getLayerColorSafe([ILnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void horror$rainbow(int[] tints, BakedQuad.MaterialInfo info, CallbackInfoReturnable<Integer> callback) {
        if (SPRITE.equals(info.sprite().contents().name())) {
            callback.setReturnValue(VeilItemPipeline.color());
        }
    }
}
