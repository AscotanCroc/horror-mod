package com.horrormod.entity;

import net.minecraft.world.level.ServerLevelAccessor;

/**
 * After any reverse zombie or reverse enderman appears, neither can appear again until a minute has passed.
 */
public final class ReverseSpawnCooldown {
    private static final long GAP_TICKS = 20L * 60L;
    private static long nextAllowed = Long.MIN_VALUE;

    private ReverseSpawnCooldown() {
    }

    public static boolean isBlocked(ServerLevelAccessor level) {
        return level.getLevel().getGameTime() < nextAllowed;
    }

    public static void mark(ServerLevelAccessor level) {
        nextAllowed = level.getLevel().getGameTime() + GAP_TICKS;
    }
}
