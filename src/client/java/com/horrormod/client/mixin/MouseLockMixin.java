package com.horrormod.client.mixin;

import com.horrormod.client.event.HorrorClientEffects;
import com.horrormod.client.event.SpiderSnap;
import com.horrormod.client.event.VeilRitual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseLockMixin {
    @Inject(method = "releaseMouse", at = @At("HEAD"), cancellable = true)
    private void horror$keepCursorHidden(CallbackInfo callback) {
        if (VeilRitual.active()) {
            callback.cancel();
        }
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void horror$freezeLook(CallbackInfo callback) {
        if (HorrorClientEffects.lockingControls() || SpiderSnap.active()) {
            callback.cancel();
        }
    }

    @Inject(method = {"onButton", "onScroll"}, at = @At("HEAD"), cancellable = true)
    private void horror$freezeClicks(CallbackInfo callback) {
        if (!HorrorClientEffects.lockingControls()) {
            return;
        }
        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen instanceof PauseScreen || screen instanceof DeathScreen) {
            return;
        }
        callback.cancel();
    }
}
