package com.horrormod.mixin;

import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeehiveDecorator.class)
public class BeehiveDecoratorMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void horror$noNaturalBeehives(TreeDecorator.Context context, CallbackInfo callback) {
        callback.cancel();
    }
}
