package com.horrormod.mixin;

import com.horrormod.content.GlitchDrops;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class GlitchDropMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void horror$voidGlitch(Entity entity, CallbackInfoReturnable<Boolean> callback) {
        if (GlitchDrops.keepOutOfWorld(entity)) {
            callback.setReturnValue(false);
        }
    }
}
