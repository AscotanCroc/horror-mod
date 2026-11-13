package com.horrormod.entity;

import com.horrormod.event.HorrorEventActions;
import com.horrormod.event.HorrorMemory;
import com.horrormod.event.HorrorMemory.PlayerMemory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Reverse special drops go into the killer's inventory instead of the world.
 * An open inventory holds the item until that screen closes.
 */
public final class ReverseRewards {
    private ReverseRewards() {
    }

    public static void queue(LivingEntity mob, DamageSource source) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        ReverseSpawnOrder.Kind kind = ReverseSpawnOrder.Kind.of(mob.getType());
        if (kind == null) {
            return;
        }
        ServerPlayer player = killer(mob, source);
        if (player == null) {
            return;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(kind.drop());
        if (id == null) {
            return;
        }
        PlayerMemory memory = HorrorMemory.get(level.getServer().overworld()).player(player.getUUID());
        HorrorMemory.get(level.getServer().overworld()).queueReward(player.getUUID(), id.toString());
        if (!memory.inventoryVisible()) {
            HorrorEventActions.deliverRewards(player);
        }
    }

    private static ServerPlayer killer(LivingEntity mob, DamageSource source) {
        if (mob.getKillCredit() instanceof ServerPlayer player) {
            return player;
        }
        if (source != null && source.getEntity() instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }
}
