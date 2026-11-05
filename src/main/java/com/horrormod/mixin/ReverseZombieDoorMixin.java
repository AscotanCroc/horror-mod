package com.horrormod.mixin;

import com.horrormod.entity.ReverseZombie;
import com.horrormod.sound.ReverseZombieSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BreakDoorGoal.class)
public class ReverseZombieDoorMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"
            )
    )
    private void horror$reverseDoorSounds(Level level, int event, BlockPos pos, int data) {
        Mob mob = ((DoorInteractGoalAccessor) this).horror$mob();
        if (mob instanceof ReverseZombie && event == 1019) {
            level.playSound(null, pos, ReverseZombieSounds.ATTACK_DOOR, SoundSource.HOSTILE, 2.0F, 1.0F);
            return;
        }
        if (mob instanceof ReverseZombie && event == 1021) {
            level.playSound(null, pos, ReverseZombieSounds.BREAK_DOOR, SoundSource.HOSTILE, 2.0F, 1.0F);
            return;
        }
        level.levelEvent(event, pos, data);
    }
}
