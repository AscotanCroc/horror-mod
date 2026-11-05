package com.horrormod.mixin;

import com.horrormod.entity.ReverseEnderman;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Enderman.class)
public class ReverseEndermanParticleMixin {
    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void horror$ambientParticles(
            Level level,
            ParticleOptions particle,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
    ) {
        if ((Object) this instanceof ReverseEnderman) {
            return;
        }
        level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Redirect(
            method = "teleport(DDD)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"
            )
    )
    private void horror$teleportParticles(Level level, int event, BlockPos pos, int data) {
        if ((Object) this instanceof ReverseEnderman) {
            return;
        }
        level.levelEvent(event, pos, data);
    }
}
