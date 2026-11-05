package com.horrormod.client.mixin;

import com.horrormod.client.event.HorrorClientEffects;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class SoundMuteMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void horror$blockPlay(CallbackInfoReturnable<SoundEngine.PlayResult> callback) {
        if (HorrorClientEffects.silencing()) {
            callback.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }

    @Inject(method = {"playDelayed", "queueTickingSound"}, at = @At("HEAD"), cancellable = true)
    private void horror$blockQueued(CallbackInfo callback) {
        if (HorrorClientEffects.silencing()) {
            callback.cancel();
        }
    }
}
