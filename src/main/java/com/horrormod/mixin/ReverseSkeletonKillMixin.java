package com.horrormod.mixin;

import com.horrormod.entity.GlitchSquare;
import com.horrormod.entity.ReverseSkeleton;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class ReverseSkeletonKillMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void horror$eraseKiller(DamageSource source, CallbackInfo callback) {
        if (!((Object) this instanceof Player player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (source.getEntity() instanceof ReverseSkeleton skeleton) {
            GlitchSquare.erase(level, skeleton);
        }
    }
}
