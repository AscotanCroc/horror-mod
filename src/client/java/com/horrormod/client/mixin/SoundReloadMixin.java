package com.horrormod.client.mixin;

import com.horrormod.client.sound.HorrorAmbience;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class SoundReloadMixin {
    @Inject(method = "reload", at = @At("RETURN"))
    private void horror$resumeMusic(CallbackInfo callback) {
        HorrorAmbience.resumeAfterReload();
    }
}
