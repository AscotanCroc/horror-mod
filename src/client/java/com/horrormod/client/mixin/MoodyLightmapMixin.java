package com.horrormod.client.mixin;

import com.horrormod.client.event.HorrorClientEffects;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class MoodyLightmapMixin {
    @Inject(method = "extract", at = @At("RETURN"))
    private void horror$forceMoody(LightmapRenderState state, float partialTick, CallbackInfo callback) {
        state.brightness = HorrorClientEffects.fullBrightness() ? 1.0F : 0.0F;
    }
}
