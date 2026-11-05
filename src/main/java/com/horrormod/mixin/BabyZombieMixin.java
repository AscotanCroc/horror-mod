package com.horrormod.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public class BabyZombieMixin {
    @Inject(method = "getSpawnAsBabyOdds", at = @At("HEAD"), cancellable = true)
    private static void horror$noNaturalBabies(RandomSource random, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(false);
    }
}
