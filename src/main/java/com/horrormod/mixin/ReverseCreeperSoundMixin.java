package com.horrormod.mixin;

import com.horrormod.entity.ReverseCreeper;
import com.horrormod.sound.ReverseCreeperSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Creeper.class)
public class ReverseCreeperSoundMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Creeper;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
            )
    )
    private void horror$reversePrimed(Creeper creeper, SoundEvent sound, float volume, float pitch) {
        if (creeper instanceof ReverseCreeper) {
            creeper.level().playSound(null, creeper.getX(), creeper.getY(), creeper.getZ(), ReverseCreeperSounds.PRIMED, SoundSource.HOSTILE, volume * 2.6F, 1.2F);
            return;
        }
        creeper.level().playSound(null, creeper.getX(), creeper.getY(), creeper.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }
}
