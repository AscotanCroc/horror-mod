package com.horrormod.content;

import com.horrormod.entity.ReverseMarker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Creative item. Using it hangs a through-wall marker over every loaded reverse mob.
 */
public class ReverseTrackerItem extends Item {
    public ReverseTrackerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server && ReverseMarker.markAll(server.getServer()) == 0) {
            player.sendOverlayMessage(Component.translatable("item.horror_mod.reverse_tracker.none"));
        }
        return InteractionResult.SUCCESS;
    }
}
