package com.horrormod.content;

import com.horrormod.entity.HorrorEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Rotten &amp;&amp; never becomes a dropped item. A player's throw goes back into that inventory.
 * Anything else that would drop it, including a broken container, deletes it.
 */
public final class GlitchDrops {
    private GlitchDrops() {
    }

    public static boolean is(ItemStack stack) {
        return stack.is(HorrorEntities.GLITCH);
    }

    /**
     * @return true when this entity must not enter the world
     */
    public static boolean keepOutOfWorld(Entity entity) {
        if (!(entity instanceof ItemEntity item) || !is(item.getItem())) {
            return false;
        }
        if (item.getOwner() instanceof ServerPlayer player && player.isAlive() && !player.hasDisconnected()) {
            ItemStack stack = item.getItem();
            if (!stack.isEmpty() && !player.getInventory().add(stack) && player.containerMenu.getCarried().isEmpty()) {
                player.containerMenu.setCarried(stack);
            }
        }
        return true;
    }
}
