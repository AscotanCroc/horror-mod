package com.horrormod.mixin;

import com.horrormod.content.GlitchDrops;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class GlitchMenuDropMixin {
    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void horror$keepGlitch(int slotId, int button, ContainerInput input, Player player, CallbackInfo callback) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (input == ContainerInput.THROW && slotId >= 0 && slotId < menu.slots.size()) {
            if (GlitchDrops.is(menu.slots.get(slotId).getItem())) {
                callback.cancel();
            }
            return;
        }
        if (slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && GlitchDrops.is(menu.getCarried())) {
            callback.cancel();
        }
    }
}
