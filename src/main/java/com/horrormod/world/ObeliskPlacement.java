package com.horrormod.world;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;

/**
 * Picks the one obelisk chunk near the world spawn.
 */
public final class ObeliskPlacement {
    public static final int MAX_DISTANCE_FROM_SPAWN = 100;

    private static final long SALT = 0x4F42454C4C49534BL;
    private static final int MIN_DISTANCE = 48;
    private static final int PREFERRED_MAX_DISTANCE = 80;

    private static long cachedSeed = Long.MIN_VALUE;
    private static ChunkPos cachedSpawn;
    private static ChunkPos cachedChunk;

    private ObeliskPlacement() {
    }

    public static ChunkPos chunk(long seed, ChunkPos spawn) {
        if (SpawnPlains.isResolvingSpawn()) {
            return compute(seed, spawn);
        }
        synchronized (ObeliskPlacement.class) {
            if (cachedChunk != null && cachedSeed == seed && spawn.equals(cachedSpawn)) {
                return cachedChunk;
            }
            ChunkPos resolved = compute(seed, spawn);
            cachedSeed = seed;
            cachedSpawn = spawn;
            cachedChunk = resolved;
            return resolved;
        }
    }

    public static boolean isObeliskChunk(long seed, ChunkPos spawn, int chunkX, int chunkZ) {
        ChunkPos obelisk = chunk(seed, spawn);
        return obelisk.x() == chunkX && obelisk.z() == chunkZ;
    }

    private static ChunkPos compute(long seed, ChunkPos spawn) {
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
}
