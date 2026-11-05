package com.horrormod.client.mixin;

import com.horrormod.client.event.HorrorClientEffects;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class ScreenLockMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void horror$blockScreens(Screen screen, CallbackInfo callback) {
        if (!HorrorClientEffects.lockingControls()) {
            return;
        }
        if (screen == null
                || screen instanceof PauseScreen
                || screen instanceof DeathScreen
                || screen instanceof LevelLoadingScreen) {
            return;
        }
        callback.cancel();
    }
}
