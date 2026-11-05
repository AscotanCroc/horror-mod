package com.horrormod.world;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Forces a plains disk around the origin large enough to contain vanilla's spawn search.
 */
public final class SpawnPlains {
    public static final int RADIUS_BLOCKS = 3072;

    private static final ThreadLocal<Boolean> RESOLVING_SPAWN = ThreadLocal.withInitial(() -> false);

    private static long cachedSeed = Long.MIN_VALUE;
    private static ChunkPos cachedSpawn;

    private SpawnPlains() {
    }

    public static boolean containsQuart(int quartX, int quartZ) {
        long blockX = QuartPos.toBlock(quartX);
        long blockZ = QuartPos.toBlock(quartZ);
        long radius = RADIUS_BLOCKS;
        return blockX * blockX + blockZ * blockZ <= radius * radius;
    }

    public static Holder<Biome> plains(BiomeSource source) {
        for (Holder<Biome> biome : source.possibleBiomes()) {
            if (biome.is(Biomes.PLAINS)) {
                return biome;
            }
        }
        return null;
    }

    public static ChunkPos spawnChunk(ServerLevel level) {
        return spawnChunk(level.getSeed(), level.getChunkSource().getGenerator(), level.getChunkSource().randomState());
    }

    public static ChunkPos spawnChunk(long seed, ChunkGenerator generator, RandomState randomState) {
        synchronized (SpawnPlains.class) {
            if (cachedSpawn != null && cachedSeed == seed) {
                return cachedSpawn;
            }
            if (RESOLVING_SPAWN.get()) {
                return ChunkPos.ZERO;
            }

            RESOLVING_SPAWN.set(true);
            try {
                ChunkPos resolved = ChunkPos.ZERO;
                if (generator instanceof NoiseBasedChunkGenerator noise) {
                    resolved = noise.getOrigin(randomState);
                }
                cachedSeed = seed;
                cachedSpawn = resolved;
                return resolved;
            } finally {
                RESOLVING_SPAWN.set(false);
            }
        }
    }

    public static boolean isResolvingSpawn() {
        return RESOLVING_SPAWN.get();
    }

    public static boolean containsChunk(ChunkPos chunk) {
        long centerX = chunk.getMiddleBlockX();
        long centerZ = chunk.getMiddleBlockZ();
        long radius = RADIUS_BLOCKS;
        return centerX * centerX + centerZ * centerZ <= radius * radius;
    }

    public static boolean isSpawnPad(ChunkPos spawn, int chunkX, int chunkZ) {
        return Math.abs(chunkX - spawn.x()) <= 1 && Math.abs(chunkZ - spawn.z()) <= 1;
    }
}
