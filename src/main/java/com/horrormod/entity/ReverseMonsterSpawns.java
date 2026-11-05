package com.horrormod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Shared natural-spawn rules for every reverse monster, so each is equally rare.
 */
public final class ReverseMonsterSpawns {
    /** Half the previous average weight, shared by all three. */
    public static final int WEIGHT = 4;

    private static final int NIGHT_START = 13000;
    private static final int NIGHT_END = 23000;
    /** Below this Y is the deep underground band. */
    private static final int DEEP_Y = -16;
    /** Twice as strict as the old reverse-zombie surface roll. */
    private static final int SURFACE_CHANCE = 48;
    /** Twice as strict as the old reverse-zombie deep roll. */
    private static final int DEEP_CHANCE = 8;

    private ReverseMonsterSpawns() {
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
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        boolean onSurface = pos.getY() >= surface - 1 && pos.getY() <= surface + 1;
        if (onSurface) {
            int time = Math.floorMod((int) level.getLevel().getOverworldClockTime(), 24000);
            boolean night = time >= NIGHT_START && time < NIGHT_END;
            return night && random.nextInt(SURFACE_CHANCE) == 0;
        }
        if (pos.getY() <= DEEP_Y) {
            return random.nextInt(DEEP_CHANCE) == 0;
        }
        return false;
    }
}
