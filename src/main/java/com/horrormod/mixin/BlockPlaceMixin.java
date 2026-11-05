package com.horrormod.mixin;

import com.horrormod.event.HorrorEventScheduler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockPlaceMixin {
    @Inject(method = "place", at = @At("RETURN"))
    private void horror$recordPlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> callback) {
        if (callback.getReturnValue().consumesAction()) {
            HorrorEventScheduler.recordPlacement(context);
        }
    }
}
