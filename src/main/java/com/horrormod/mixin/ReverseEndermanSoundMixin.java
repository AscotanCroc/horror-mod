package com.horrormod.mixin;

import com.horrormod.entity.ReverseEnderman;
import com.horrormod.sound.ReverseEndermanSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.monster.Enderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Enderman.class)
public class ReverseEndermanSoundMixin {
    @Redirect(
            method = "playStareSound",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/sounds/SoundEvents;ENDERMAN_STARE:Lnet/minecraft/sounds/SoundEvent;"
            )
    )
    private SoundEvent horror$reverseStare() {
        if ((Object) this instanceof ReverseEnderman) {
            return ReverseEndermanSounds.STARE;
        }
        return SoundEvents.ENDERMAN_STARE;
    }

    @Redirect(
            method = "teleport(DDD)Z",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/sounds/SoundEvents;ENDERMAN_TELEPORT:Lnet/minecraft/sounds/SoundEvent;",
                    ordinal = 0
            )
    )
    private SoundEvent horror$reverseTeleportStart() {
        return horror$teleportSound();
    }

    @Redirect(
            method = "teleport(DDD)Z",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/sounds/SoundEvents;ENDERMAN_TELEPORT:Lnet/minecraft/sounds/SoundEvent;",
                    ordinal = 1
            )
    )
    private SoundEvent horror$reverseTeleportEnd() {
        return horror$teleportSound();
    }

    private SoundEvent horror$teleportSound() {
        if ((Object) this instanceof ReverseEnderman) {
            return ReverseEndermanSounds.TELEPORT;
        }
        return SoundEvents.ENDERMAN_TELEPORT;
    }
}
