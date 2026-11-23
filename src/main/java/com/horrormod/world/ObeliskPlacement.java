package com.horrormod.world;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Picks the one obelisk chunk near the world spawn, on the flattest dry ground in range.
 */
public final class ObeliskPlacement {
    public static final int MAX_DISTANCE_FROM_SPAWN = 100;
    /** How far a site may sit from the nominal chunk center. Matches the structure search. */
    public static final int SITE_SEARCH_RADIUS = 32;

    private static final long SALT = 0x4F42454C4C49534BL;
    private static final int MIN_DISTANCE = 48;
    private static final int PREFERRED_MAX_DISTANCE = 80;
    private static final int SAMPLE_STEP = 8;
    /** One wet column outranks any slope the search can see. */
    private static final int WET_COLUMN = 1_000_000;
    private static final int WET_DOOR = 100_000;

    private static final int[][] FOOTPRINT = {
            {0, 0},
            {4, 0},
            {-4, 0},
            {0, 4},
            {0, -4},
            {4, 4},
            {4, -4},
            {-4, 4},
            {-4, -4}
    };

    private static final ThreadLocal<Boolean> COMPUTING = ThreadLocal.withInitial(() -> false);

    private static long cachedSeed = Long.MIN_VALUE;
    private static ChunkPos cachedSpawn;
    private static ChunkPos cachedChunk;

    private ObeliskPlacement() {
    }

    public static boolean isComputing() {
        return COMPUTING.get();
    }

    public static ChunkPos chunk(ServerLevel level, ChunkPos spawn) {
        return chunk(
                level.getSeed(),
                spawn,
                level.getChunkSource().getGenerator(),
                level.getChunkSource().randomState(),
                level
        );
    }

    public static ChunkPos chunk(
            long seed,
            ChunkPos spawn,
            ChunkGenerator generator,
            RandomState randomState,
            LevelHeightAccessor heights
    ) {
        if (SpawnPlains.isResolvingSpawn() || COMPUTING.get() || generator == null || randomState == null || heights == null) {
            return computeAngle(seed, spawn);
        }
        synchronized (ObeliskPlacement.class) {
            if (cachedChunk != null && cachedSeed == seed && spawn.equals(cachedSpawn)) {
                return cachedChunk;
            }
        }
        COMPUTING.set(true);
        try {
            ChunkPos resolved = computeTerrain(seed, spawn, generator, randomState, heights);
            synchronized (ObeliskPlacement.class) {
                cachedSeed = seed;
                cachedSpawn = spawn;
                cachedChunk = resolved;
            }
            return resolved;
        } finally {
            COMPUTING.set(false);
        }
    }

    /**
     * Chunks the pillar can occupy. The site may sit {@link #SITE_SEARCH_RADIUS} blocks from the
     * nominal chunk center, and the cobble extends {@link ObeliskPiece#HALF_WIDTH} past that.
     */
    public static boolean preservesChunk(ServerLevel level, int chunkX, int chunkZ) {
        ChunkPos spawn = SpawnPlains.spawnChunk(level);
        if (COMPUTING.get()) {
            int reach = Math.floorDiv(PREFERRED_MAX_DISTANCE + SITE_SEARCH_RADIUS + ObeliskPiece.HALF_WIDTH, 16) + 1;
            return Math.abs(chunkX - spawn.x()) <= reach && Math.abs(chunkZ - spawn.z()) <= reach;
        }
        ChunkPos obelisk = chunk(level, spawn);
        int reach = Math.floorDiv(8 + SITE_SEARCH_RADIUS + ObeliskPiece.HALF_WIDTH, 16);
        return Math.abs(chunkX - obelisk.x()) <= reach && Math.abs(chunkZ - obelisk.z()) <= reach;
    }

    /**
     * Lower is better. Wet ground loses to any dry site, and a level footprint beats a slope.
     */
    public static int score(ColumnSampler columns, int x, int z, ChunkPos spawn) {
        int wet = 0;
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int[] offset : FOOTPRINT) {
            int surface = columns.height(x + offset[0], z + offset[1], Heightmap.Types.WORLD_SURFACE_WG);
            int floor = columns.height(x + offset[0], z + offset[1], Heightmap.Types.OCEAN_FLOOR_WG);
            if (surface > floor) {
                wet++;
            }
            lowest = Math.min(lowest, surface);
            highest = Math.max(highest, surface);
        }
        int center = columns.height(x, z, Heightmap.Types.WORLD_SURFACE_WG);
        Direction facing = facingSpawn(x, z, spawn);
        int doorX = x + facing.getStepX() * (ObeliskPiece.HALF_WIDTH + 1);
        int doorZ = z + facing.getStepZ() * (ObeliskPiece.HALF_WIDTH + 1);
        int outside = columns.height(doorX, doorZ, Heightmap.Types.WORLD_SURFACE_WG);
        int outsideFloor = columns.height(doorX, doorZ, Heightmap.Types.OCEAN_FLOOR_WG);
        int doorWet = outside > outsideFloor ? 1 : 0;
        int doorStep = 0;
        if (outside > center) {
            doorStep = outside - center;
        } else if (outside < center - 1) {
            doorStep = center - 1 - outside;
        }
        return wet * WET_COLUMN + doorWet * WET_DOOR + (highest - lowest) * 40 + doorStep * 20;
    }

    public static boolean footprintClear(long seed, int x, int z) {
        int radius = ObeliskPiece.HALF_WIDTH;
        int minX = Math.floorDiv(x - radius, 16);
        int maxX = Math.floorDiv(x + radius, 16);
        int minZ = Math.floorDiv(z - radius, 16);
        int maxZ = Math.floorDiv(z + radius, 16);
        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                if (VoidChunks.isBaseVoid(seed, chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Direction facingSpawn(int x, int z, ChunkPos spawn) {
        int dx = spawn.getMiddleBlockX() - x;
        int dz = spawn.getMiddleBlockZ() - z;
        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx >= 0 ? Direction.EAST : Direction.WEST;
        }
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static ChunkPos computeTerrain(
            long seed,
            ChunkPos spawn,
            ChunkGenerator generator,
            RandomState randomState,
            LevelHeightAccessor heights
    ) {
        ColumnSampler columns = (x, z, type) -> generator.getBaseHeight(x, z, type, heights, randomState);
        int originX = spawn.getMiddleBlockX();
        int originZ = spawn.getMiddleBlockZ();
        ChunkPos bestChunk = null;
        int bestScore = Integer.MAX_VALUE;
        for (int distance = MIN_DISTANCE; distance <= PREFERRED_MAX_DISTANCE; distance += SAMPLE_STEP) {
            int steps = Math.max(8, (int) Math.round((Math.PI * 2.0 * distance) / SAMPLE_STEP));
            for (int step = 0; step < steps; step++) {
                double angle = step * (Math.PI * 2.0) / steps;
                int blockX = originX + (int) Math.round(Math.cos(angle) * distance);
                int blockZ = originZ + (int) Math.round(Math.sin(angle) * distance);
                int chunkX = Math.floorDiv(blockX, 16);
                int chunkZ = Math.floorDiv(blockZ, 16);
                if (!withinSpawn(originX, originZ, chunkX, chunkZ) || SpawnPlains.isSpawnPad(spawn, chunkX, chunkZ)) {
                    continue;
                }
                if (!footprintClear(seed, blockX, blockZ)) {
                    continue;
                }
                int score = score(columns, blockX, blockZ, spawn);
                if (score < bestScore) {
                    bestScore = score;
                    bestChunk = new ChunkPos(chunkX, chunkZ);
                }
            }
        }
        if (bestChunk != null) {
            return bestChunk;
        }
        return computeAngle(seed, spawn);
    }

    private static ChunkPos computeAngle(long seed, ChunkPos spawn) {
        RandomSource random = RandomSource.create(seed ^ SALT);
        double angle = random.nextDouble() * Math.PI * 2.0;
        int distance = MIN_DISTANCE + random.nextInt(PREFERRED_MAX_DISTANCE - MIN_DISTANCE);
        int originX = spawn.getMiddleBlockX();
        int originZ = spawn.getMiddleBlockZ();
        ChunkPos fallback = null;
        for (int step = 0; step < 16; step++) {
            int walked = Math.min(PREFERRED_MAX_DISTANCE, distance + step * 4);
            int blockX = originX + (int) Math.round(Math.cos(angle) * walked);
            int blockZ = originZ + (int) Math.round(Math.sin(angle) * walked);
            int chunkX = Math.floorDiv(blockX, 16);
            int chunkZ = Math.floorDiv(blockZ, 16);
            if (!withinSpawn(originX, originZ, chunkX, chunkZ) || SpawnPlains.isSpawnPad(spawn, chunkX, chunkZ)) {
                continue;
            }
            ChunkPos candidate = new ChunkPos(chunkX, chunkZ);
            if (fallback == null) {
                fallback = candidate;
            }
            if (!VoidChunks.isBaseVoid(seed, chunkX, chunkZ)) {
                return candidate;
            }
        }
        if (fallback != null) {
            return fallback;
        }
        return new ChunkPos(spawn.x() + 3, spawn.z());
    }

    private static boolean withinSpawn(int originX, int originZ, int chunkX, int chunkZ) {
        long dx = chunkX * 16L + 8L - originX;
        long dz = chunkZ * 16L + 8L - originZ;
        long limit = MAX_DISTANCE_FROM_SPAWN;
        return dx * dx + dz * dz <= limit * limit;
    }

    @FunctionalInterface
    public interface ColumnSampler {
        int height(int x, int z, Heightmap.Types type);
    }
}
