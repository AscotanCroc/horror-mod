package com.horrormod.entity;

import net.minecraft.world.level.ServerLevelAccessor;

/**
 * A natural reverse mob starts a one-minute gap. A death holds the next one for six minutes.
 */
public final class ReverseSpawnCooldown {
    private static final long GAP_TICKS = 20L * 60L;
    private static final long DEATH_GAP_TICKS = 20L * 60L * 6L;
    private static long nextAllowed = Long.MIN_VALUE;

    private ReverseSpawnCooldown() {
    }

    public static boolean isBlocked(ServerLevelAccessor level) {
        return ticksRemaining(level) > 0L;
    }

    public static long ticksRemaining(ServerLevelAccessor level) {
        return Math.max(0L, nextAllowed - level.getLevel().getGameTime());
    }

    public static void mark(ServerLevelAccessor level) {
        hold(level, GAP_TICKS);
    }

    /** No reverse mob can appear until six minutes after this death. */
    public static void markKilled(ServerLevelAccessor level) {
        hold(level, DEATH_GAP_TICKS);
    }

    private static void hold(ServerLevelAccessor level, long gap) {
        long at = level.getLevel().getGameTime() + gap;
        if (at > nextAllowed) {
            nextAllowed = at;
        }
    }
}
