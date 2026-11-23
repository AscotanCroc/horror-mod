package com.horrormod.mixin;

import com.horrormod.content.GlitchDrops;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class GlitchDeathMixin {
    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void horror$deleteHeldGlitch(ServerLevel level, CallbackInfo callback) {
        if (level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }
        Player player = (Player) (Object) this;
        AbstractContainerMenu menu = player.containerMenu;
        if (GlitchDrops.is(menu.getCarried())) {
            menu.setCarried(ItemStack.EMPTY);
        }
        if (!(menu instanceof InventoryMenu)) {
            return;
        }
        for (Slot slot : menu.slots) {
            if (slot.container != player.getInventory() && GlitchDrops.is(slot.getItem())) {
                slot.set(ItemStack.EMPTY);
            }
        }
    }
}
