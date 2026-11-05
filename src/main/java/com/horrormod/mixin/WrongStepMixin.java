package com.horrormod.mixin;

import com.horrormod.event.HorrorEventActions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class WrongStepMixin {
    @Redirect(
            method = "playStepSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SoundType;getPitch()F")
    )
    private float horror$wrongPitch(SoundType type) {
        float pitch = type.getPitch();
        if ((Object) this instanceof Player player) {
            return HorrorEventActions.adjustStepPitch(player, pitch);
        }
        return pitch;
    }
}
