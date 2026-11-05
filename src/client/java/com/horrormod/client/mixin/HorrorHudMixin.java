package com.horrormod.client.mixin;

import com.horrormod.client.event.HorrorClientEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HorrorHudMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void horror$events(GuiGraphicsExtractor graphics, DeltaTracker tracker, CallbackInfo callback) {
        HorrorClientEffects.render(graphics);
    }
}
