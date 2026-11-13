package com.horrormod.mixin;

import com.horrormod.content.GlitchDrops;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class GlitchItemMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void horror$voidGlitch(CallbackInfo callback) {
        ItemEntity item = (ItemEntity) (Object) this;
        if (!GlitchDrops.is(item.getItem())) {
            return;
        }
        item.discard();
        callback.cancel();
    }
}
