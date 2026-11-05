package com.horrormod.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void horror$noPhantomsAtNight(ServerLevel level, boolean spawnEnemies, CallbackInfo callback) {
        if (level.dimension() == Level.OVERWORLD) {
            callback.cancel();
        }
    }
}
