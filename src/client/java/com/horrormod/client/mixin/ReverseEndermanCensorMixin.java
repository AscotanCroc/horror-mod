package com.horrormod.client.mixin;

import com.horrormod.client.render.ReverseEndermanCensor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class ReverseEndermanCensorMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void horror$censorFace(GuiGraphicsExtractor graphics, DeltaTracker tracker, CallbackInfo callback) {
        ReverseEndermanCensor.render(graphics, tracker.getGameTimeDeltaPartialTick(false));
    }
}
