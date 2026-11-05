package com.horrormod.client.mixin;

import com.horrormod.client.event.HorrorClientEffects;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void horror$noStars(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo callback) {
        if (HorrorClientEffects.swapCelestial()) {
            float sun = state.sunAngle;
            state.sunAngle = state.moonAngle;
            state.moonAngle = sun;
        }
        state.starBrightness = HorrorClientEffects.starBrightness();
        state.moonPhase = MoonPhase.FULL_MOON;
    }
}
