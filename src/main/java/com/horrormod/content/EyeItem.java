package com.horrormod.content;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The reverse spider's drop. The name is empty on purpose.
 */
public class EyeItem extends Item {
    public EyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.empty();
    }
}
