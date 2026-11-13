package com.horrormod.mixin;

import com.horrormod.sound.StepVolume;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Level.class)
public class StepVolumeMixin {
    @ModifyVariable(
            method = "playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float horror$stepVolume(float volume) {
        if (!StepVolume.fromStep()) {
            return volume;
        }
        return volume * StepVolume.SCALE;
    }
}
