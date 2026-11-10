package com.horrormod.client.mixin;

import com.horrormod.client.event.HorrorClientEffects;
import com.horrormod.client.event.VeilRitual;
import net.minecraft.client.resources.sounds.SoundInstance;
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
    private void horror$blockPlay(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> callback) {
        if (VeilRitual.isScream(sound)) {
            return;
        }
        if (HorrorClientEffects.silencing()) {
            callback.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }

    @Inject(method = "stop()V", at = @At("HEAD"), cancellable = true)
    private void horror$keepScream(CallbackInfo callback) {
        if (VeilRitual.active()) {
            callback.cancel();
        }
    }

    @Inject(method = {"playDelayed", "queueTickingSound"}, at = @At("HEAD"), cancellable = true)
    private void horror$blockQueued(CallbackInfo callback) {
        if (HorrorClientEffects.silencing()) {
            callback.cancel();
        }
    }
}
