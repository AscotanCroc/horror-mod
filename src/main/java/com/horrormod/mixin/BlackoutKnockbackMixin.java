package com.horrormod.mixin;

import com.horrormod.entity.ReverseCreeperBlackout;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class BlackoutKnockbackMixin {
    @Inject(method = "knockback(DDDLnet/minecraft/world/damagesource/DamageSource;F)V", at = @At("HEAD"), cancellable = true)
    private void horror$noKnockback(CallbackInfo callback) {
        cancelKnockback(callback);
    }

    @Inject(method = "knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V", at = @At("HEAD"), cancellable = true)
    private void horror$noKnockbackForced(CallbackInfo callback) {
        cancelKnockback(callback);
    }

    @Inject(method = "dealDefaultKnockback", at = @At("HEAD"), cancellable = true)
    private void horror$noDefaultKnockback(CallbackInfo callback) {
        cancelKnockback(callback);
    }

    @Inject(method = "causeExtraKnockback", at = @At("HEAD"), cancellable = true)
    private void horror$noExtraKnockback(CallbackInfo callback) {
        cancelKnockback(callback);
    }

    private void cancelKnockback(CallbackInfo callback) {
        if ((Object) this instanceof Player player && ReverseCreeperBlackout.isFrozen(player)) {
            callback.cancel();
        }
    }
}