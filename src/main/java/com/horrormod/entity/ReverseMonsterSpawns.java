package com.horrormod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.WeatherData;

/**
 * Shared natural-spawn rules for every reverse monster, so each is equally rare.
 */
public final class ReverseMonsterSpawns {
    /** Half the previous average weight, shared by every reverse monster. */
    public static final int WEIGHT = 4;

    /** Surface reverse monsters can spawn from this time through {@link #SURFACE_NIGHT_END}. */
    public static final int SURFACE_NIGHT_START = 13000;
    public static final int SURFACE_NIGHT_END = 23000;
    /** Below this Y is the deep underground band. */
    private static final int DEEP_Y = -16;
    /** Twice as strict as the old reverse-zombie surface roll. */
    private static final int SURFACE_CHANCE = 48;
    /** A thunderstorm makes the surface roll a little more common. */
    private static final int STORM_SURFACE_CHANCE = 40;
    /** Twice as strict as the old reverse-zombie deep roll. */
    private static final int DEEP_CHANCE = 8;
    /** A thunderstorm makes the deep roll a little more common. */
    private static final int STORM_DEEP_CHANCE = 7;

    private ReverseMonsterSpawns() {
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

    public static boolean allow(ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
            return true;
        }
        if (ReverseSpawnCooldown.isBlocked(level)) {
            return false;
        }
        if (level.getBrightness(LightLayer.BLOCK, pos) > 0) {
            return false;
        }
        boolean storm = storming(level);
        boolean onSurface = onSurface(level.getLevel(), pos);
        if (onSurface) {
            if (occupied(level, true)) {
                return false;
            }
            int chance = storm ? STORM_SURFACE_CHANCE : SURFACE_CHANCE;
            return surfaceNight(level.getLevel()) && random.nextInt(chance) == 0;
        }
        if (pos.getY() <= DEEP_Y) {
            if (occupied(level, false)) {
                return false;
            }
            int chance = storm ? STORM_DEEP_CHANCE : DEEP_CHANCE;
            return random.nextInt(chance) == 0;
        }
        return false;
    }

    /**
     * The ground slot already has its one reverse monster.
     */
    public static boolean surfaceOccupied(ServerLevel level) {
        return occupied(level, true);
    }

    /**
     * Standing on the ground, including under trees. Anywhere lower and unlit is the dark slot.
     */
    private static boolean onSurface(Level level, BlockPos pos) {
        return near(level, pos, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)
                || near(level, pos, Heightmap.Types.WORLD_SURFACE);
    }

    private static boolean near(Level level, BlockPos pos, Heightmap.Types heightmap) {
        int surface = level.getHeight(heightmap, pos.getX(), pos.getZ());
        return pos.getY() >= surface - 1 && pos.getY() <= surface + 1;
    }

    /**
     * @param surfaceSlot true for the single surface monster, false for the single dark one below ground
     */
    private static boolean occupied(ServerLevelAccessor level, boolean surfaceSlot) {
        if (!(level.getLevel() instanceof ServerLevel server)) {
            return false;
        }
        for (Entity entity : server.getAllEntities()) {
            if (!entity.isAlive() || !HorrorEntities.isReverse(entity)) {
                continue;
            }
            BlockPos pos = entity.blockPosition();
            if (onSurface(server, pos)) {
                if (surfaceSlot) {
                    return true;
                }
            } else if (!surfaceSlot && server.getBrightness(LightLayer.BLOCK, pos) == 0) {
                return true;
            }
        }
        return false;
    }
}
