package com.horrormod.world;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;

/**
 * Deletes one in six overworld chunks, including bedrock, leaving the column empty.
 */
public final class VoidChunks {
    private static final ThreadLocal<FriendlyByteBuf> AIR_SECTION = ThreadLocal.withInitial(VoidChunks::airSection);

    private VoidChunks() {
    }

    public static boolean isBaseVoid(long seed, int chunkX, int chunkZ) {
        long mixed = seed ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L);
        mixed = (mixed ^ (mixed >>> 33)) * 0xff51afd7ed558ccdL;
        mixed = (mixed ^ (mixed >>> 33)) * 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        return Math.floorMod(mixed, 6) == 0;
    }

    public static boolean isRemoved(ServerLevel level, int chunkX, int chunkZ) {
        if (level.dimension() != Level.OVERWORLD) {
            return false;
        }
        long seed = level.getSeed();
        if (!isBaseVoid(seed, chunkX, chunkZ)) {
            return false;
        }
        ChunkPos spawn = SpawnPlains.spawnChunk(level);
        if (SpawnPlains.isSpawnPad(spawn, chunkX, chunkZ)) {
            return false;
        }
        return !ObeliskPlacement.preservesChunk(seed, spawn, chunkX, chunkZ);
    }

    public static void clear(ServerLevel level, LevelChunk chunk, boolean generated) {
        ChunkPos pos = chunk.getPos();
        if (!isRemoved(level, pos.x(), pos.z())) {
            return;
        }

        if (generated) {
            boolean cleared = false;
            FriendlyByteBuf air = AIR_SECTION.get();
            for (LevelChunkSection section : chunk.getSections()) {
                if (section.hasOnlyAir()) {
                    continue;
                }
                air.readerIndex(0);
                section.getStates().read(air);
                section.recalcBlockCounts();
                cleared = true;
            }
            if (cleared) {
                chunk.clearAllBlockEntities();
                clearTicks(chunk.getBlockTicks());
                clearTicks(chunk.getFluidTicks());
                chunk.markUnsaved();
            }
        }
        if (generated || onlyAir(chunk)) {
            if (dropHeightmaps(chunk)) {
                chunk.markUnsaved();
            }
        }
    }

    private static boolean onlyAir(LevelChunk chunk) {
        for (LevelChunkSection section : chunk.getSections()) {
            if (!section.hasOnlyAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * An empty column's height is the world bottom. Priming never lowers a map, so a cleared
     * chunk used to keep the terrain height and the server treated the player as standing on air.
     */
    private static boolean dropHeightmaps(LevelChunk chunk) {
        List<Map.Entry<Heightmap.Types, Heightmap>> maps = new ArrayList<>(chunk.getHeightmaps());
        boolean raised = false;
        for (Map.Entry<Heightmap.Types, Heightmap> entry : maps) {
            for (long word : entry.getValue().getRawData()) {
                if (word != 0L) {
                    raised = true;
                    break;
                }
            }
            if (raised) {
                break;
            }
        }
        if (!raised) {
            return false;
        }
        for (Map.Entry<Heightmap.Types, Heightmap> entry : maps) {
            chunk.setHeightmap(entry.getKey(), new long[entry.getValue().getRawData().length]);
        }
        return true;
    }

    private static FriendlyByteBuf airSection() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(8));
        buf.writeByte(0);
        buf.writeVarInt(Block.getId(Blocks.AIR.defaultBlockState()));
        return buf;
    }

    private static void clearTicks(TickContainerAccess<?> ticks) {
        if (ticks instanceof LevelChunkTicks<?> chunkTicks) {
            chunkTicks.removeIf(scheduled -> true);
        }
    }
}
