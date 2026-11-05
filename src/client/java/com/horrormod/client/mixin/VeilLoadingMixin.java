package com.horrormod.client.mixin;

import com.horrormod.client.event.VeilRitual;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public class VeilLoadingMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void horror$coverLoad(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        VeilRitual.render(graphics);
    }
}
