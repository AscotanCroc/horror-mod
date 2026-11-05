package com.horrormod.mixin;

import com.horrormod.event.HorrorEventActions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public class ChestOpenMixin {
    @Inject(method = "startOpen", at = @At("TAIL"))
    private void horror$rememberChest(ContainerUser user, CallbackInfo callback) {
        if (user.getLivingEntity() instanceof ServerPlayer player) {
            HorrorEventActions.rememberChest(player, ((ChestBlockEntity) (Object) this).getBlockPos());
        }
    }
}
