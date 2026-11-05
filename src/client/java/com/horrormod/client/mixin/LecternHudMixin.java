package com.horrormod.client.mixin;

import com.horrormod.client.screen.RecipeScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class LecternHudMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void horror$hideHud(GuiGraphicsExtractor graphics, DeltaTracker tracker, CallbackInfo callback) {
        if (Minecraft.getInstance().gui.screen() instanceof RecipeScreen) {
            callback.cancel();
        }
    }
}
