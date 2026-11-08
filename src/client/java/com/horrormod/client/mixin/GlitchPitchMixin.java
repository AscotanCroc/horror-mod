package com.horrormod.client.mixin;

import com.horrormod.sound.HorrorSounds;
import com.horrormod.sound.ReverseSpiderSounds;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public class GlitchPitchMixin {
    @WrapOperation(method = "calculatePitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float horror$glitchSpeed(float value, float min, float max, Operation<Float> original, SoundInstance sound) {
        if (ReverseSpiderSounds.KILL.location().equals(sound.getIdentifier())) {
            return Mth.clamp(value, 0.5F, 2.5F);
        }
        if (HorrorSounds.GLITCH_TRANSITION.location().equals(sound.getIdentifier())) {
            return Mth.clamp(value, 0.5F, 3.0F);
        }
        return original.call(value, min, max);
    }
}
