package com.horrormod.mixin;

import com.horrormod.entity.ReverseCreeper;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public class CreeperExplodeMixin {
    @Inject(method = "explodeCreeper", at = @At("HEAD"), cancellable = true)
    private void horror$reverseBurst(CallbackInfo callback) {
        if ((Object) this instanceof ReverseCreeper creeper) {
            creeper.burst();
            callback.cancel();
        }
    }
}
