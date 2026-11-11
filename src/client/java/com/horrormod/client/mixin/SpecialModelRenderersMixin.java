package com.horrormod.client.mixin;

import com.horrormod.HorrorMod;
import com.horrormod.client.render.DisappearanceSpecialRenderer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpecialModelRenderers.class)
public abstract class SpecialModelRenderersMixin {
    @Shadow
    private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpecialModelRenderer.Unbaked<?>>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("RETURN"))
    private static void horror$disappearance(CallbackInfo callback) {
        ID_MAPPER.put(HorrorMod.id("dissapearance"), DisappearanceSpecialRenderer.Unbaked.MAP_CODEC);
    }
}
