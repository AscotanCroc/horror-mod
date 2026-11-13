package com.horrormod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.WeatherData;

/**
 * One reverse monster on the surface and one underground.
 * The underground roll is the common one. A thunderstorm uses that same roll on the surface.
 * {@link ReverseZombieNights} is the only natural source, so a locked kind cannot fail a biome pick.
 */
public final class ReverseMonsterSpawns {
    /** Kept off the hostile pack list. The timed attempt does not use this weight. */
    public static final int WEIGHT = 3;

    /** Surface reverse monsters can spawn from this time through {@link #SURFACE_NIGHT_END}. */
    public static final int SURFACE_NIGHT_START = 13000;
    public static final int SURFACE_NIGHT_END = 23000;
    /** Underground, and the surface while a thunderstorm is up. */
    public static final int UNDERGROUND_CHANCE = 6;
    /** Clear nights on the surface. Rarer than {@link #UNDERGROUND_CHANCE}. */
    public static final int SURFACE_CHANCE = 18;

    private ReverseMonsterSpawns() {
    }

    public enum Band {
        SURFACE,
        UNDERGROUND
    }

    /**
     * False until the clock reaches the first surface night. Later days stay open.
     */
    public static boolean firstNightStarted(Level level) {
        return level.getOverworldClockTime() >= SURFACE_NIGHT_START;
    }

    /**
     * The clock window where a reverse monster is allowed on the surface.
     */
    public static boolean surfaceNight(Level level) {
        int time = Math.floorMod((int) level.getOverworldClockTime(), 24000);
        return time >= SURFACE_NIGHT_START && time < SURFACE_NIGHT_END;
    }

    /**
     * True during a thunderstorm. The saved thunder flag counts while rain is up, so an ordinary
     * thunderstorm counts as soon as the weather says so, and so does rain promoted into one.
     */
    public static boolean storming(ServerLevelAccessor level) {
        Level world = level.getLevel();
        if (world.isThundering()) {
            return true;
        }
        if (world instanceof ServerLevel server) {
            WeatherData weather = server.getWeatherData();
            return weather.isThundering() && (weather.isRaining() || world.isRaining());
        }
        return false;
    }

    /**
     * Clear surface nights use the rarer roll. A storm uses the underground roll in both bands.
     */
    public static int appearanceChance(boolean surface, boolean storm) {
        if (surface && !storm) {
            return SURFACE_CHANCE;
        }
        return UNDERGROUND_CHANCE;
    }

    /**
     * Eggs, commands, and events skip the timed attempt. Biome and chunk rolls do not place one,
     * or a locked kind would fail that pick and the timed attempt would stack on top.
     */
    public static boolean allow(
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random,
            EntityType<?> type
    ) {
        return reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION;
    }

    /**
     * The ground slot already has its one reverse monster.
     */
    public static boolean surfaceOccupied(ServerLevel level) {
        return occupied(level, true);
    }

    /**
     * The underground slot already has its one reverse monster.
     */
    public static boolean darkOccupied(ServerLevel level) {
        return occupied(level, false);
    }

    /**
     * At or above the ground is the surface slot, including while off the ground.
     * Below the ground is the underground slot.
     */
    public static Band band(Level level, BlockPos pos) {
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if (pos.getY() >= surface - 1) {
            return Band.SURFACE;
        }
        return Band.UNDERGROUND;
    }

    /**
     * @param surfaceSlot true for the single surface monster, false for the single underground one
     */
    private static boolean occupied(ServerLevelAccessor level, boolean surfaceSlot) {
        if (!(level.getLevel() instanceof ServerLevel server)) {
            return false;
        }
        for (Entity entity : server.getAllEntities()) {
            if (!entity.isAlive() || !HorrorEntities.isReverse(entity)) {
                continue;
            }
            Band band = band(server, entity.blockPosition());
            if (band == Band.SURFACE && surfaceSlot) {
                return true;
            }
            if (band == Band.UNDERGROUND && !surfaceSlot) {
                return true;
            }
        }
        return false;
    }
}
