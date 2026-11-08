package com.horrormod.client.mixin;

import com.horrormod.client.sound.ReverseSpiderAudio;
import com.horrormod.sound.ReverseSpiderSounds;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class ReverseSpiderCutMixin {
    @Shadow
    private float calculateVolume(float volume, SoundSource source) {
        return 0.0F;
    }

    @Inject(method = "play", at = @At("RETURN"))
    private void horror$cutSpider(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> callback) {
        ReverseSpiderAudio.maybeCut((SoundEngine) (Object) this, sound);
    }

    @Inject(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F", at = @At("HEAD"), cancellable = true)
    private void horror$loudSpider(SoundInstance sound, CallbackInfoReturnable<Float> callback) {
        if (ReverseSpiderSounds.glitches(sound.getIdentifier())) {
            callback.setReturnValue(this.calculateVolume(1.0F, sound.getSource()) * 3.4F);
        } else if (ReverseSpiderSounds.KILL.location().equals(sound.getIdentifier())) {
            callback.setReturnValue(this.calculateVolume(1.0F, sound.getSource()) * 1.45F);
        }
    }
}
