package com.horrormod.mixin;

import com.horrormod.entity.ReverseEnderman;
import com.horrormod.sound.ReverseEndermanSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Enderman.class)
public class ReverseEndermanSoundMixin {
    @Redirect(
            method = "playStareSound",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"
            )
    )
    private void horror$loudStare(Level level, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, boolean distanceDelay) {
        if ((Object) this instanceof ReverseEnderman) {
            level.playLocalSound(x, y, z, ReverseEndermanSounds.STARE, source, volume * 2.2F, pitch * 1.7F, distanceDelay);
            return;
        }
        level.playLocalSound(x, y, z, sound, source, volume, pitch, distanceDelay);
    }

    @Redirect(
            method = "teleport(DDD)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
            )
    )
    private void horror$loudTeleport(Level level, Entity entity, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch) {
        if ((Object) this instanceof ReverseEnderman) {
            level.playSound(entity, x, y, z, ReverseEndermanSounds.TELEPORT, source, volume * 2.4F, pitch * 1.6F);
            return;
        }
        level.playSound(entity, x, y, z, sound, source, volume, pitch);
    }

    @Redirect(
            method = "teleport(DDD)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Enderman;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
            )
    )
    private void horror$loudTeleportSelf(Enderman enderman, SoundEvent sound, float volume, float pitch) {
        SoundEvent played = enderman instanceof ReverseEnderman ? ReverseEndermanSounds.TELEPORT : sound;
        float loud = enderman instanceof ReverseEnderman ? volume * 2.6F : volume;
        float high = enderman instanceof ReverseEnderman ? Math.min(2.3F, pitch * 1.65F) : pitch;
        enderman.level().playSound(null, enderman.getX(), enderman.getY(), enderman.getZ(), played, enderman.getSoundSource(), loud, high);
    }
}
