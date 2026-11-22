package com.horrormod.content;

import com.horrormod.network.OpenImpuritySheetPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Right-click opens the impurity detector recipe. The sheet stays in the inventory.
 */
public class ImpuritySheetItem extends Item {
    public ImpuritySheetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, OpenImpuritySheetPayload.INSTANCE);
        }
        return InteractionResult.SUCCESS;
    }
}
