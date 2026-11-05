package com.horrormod.client.mixin;

import com.horrormod.HorrorMod;
import com.horrormod.client.screen.RecipeScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class LecternPostEffectMixin {
    @Inject(method = "update", at = @At("RETURN"))
    private void horror$lecternEffect(DeltaTracker tracker, CallbackInfo callback) {
        if (Minecraft.getInstance().gui.screen() instanceof RecipeScreen) {
            ((GameRenderer) (Object) this).getRequestedPostEffects().add(HorrorMod.id("lectern"));
        }
    }
}
