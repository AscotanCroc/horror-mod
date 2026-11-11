package com.horrormod.world;

import com.horrormod.HorrorMod;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Stamps {@link BridgeShape} through the veil, including towers that replace some spans,
 * and a floor across the bottom of the dimension.
 * Everywhere else stays empty.
 * Min y and height match {@code dimension_type/veil.json}.
 */
public class BridgeChunkGenerator extends ChunkGenerator {
    /** Same vertical span as the veil dimension type. */
    private static final int MIN_Y = BridgeShape.FLOOR_Y;
    private static final int HEIGHT = 384;
    /** One solid layer at the dimension's minimum y. */
    private static final BlockState FLOOR = Blocks.BLACKSTONE.defaultBlockState();

    public static final MapCodec<BridgeChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(BridgeChunkGenerator::getBiomeSource)
    ).apply(instance, BridgeChunkGenerator::new));

    public BridgeChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, HorrorMod.id("bridges"), CODEC);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(
            ChunkAccess chunk,
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            BiomeManager biomeManager,
            WorldGenRegion carverBiomeRegion,
            Set<Holder<Biome>> possibleBiomes
    ) {
        ChunkPos pos = chunk.getPos();
        int minY = chunk.getMinY();
        int maxY = minY + chunk.getHeight() - 1;
        int floorY = minY;
        boolean placeFloor = floorY <= maxY;
        int y0 = Math.max(minY, BridgeShape.minBlockY());
        int y1 = Math.min(maxY, BridgeShape.maxBlockY());
        if (!placeFloor && y0 > y1) {
            return CompletableFuture.completedFuture(chunk);
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        for (int localX = 0; localX < 16; localX++) {
            int x = minX + localX;
            boolean structure = y0 <= y1 && BridgeShape.nearRow(x);
            for (int localZ = 0; localZ < 16; localZ++) {
                int z = minZ + localZ;
                if (placeFloor) {
                    cursor.set(x, floorY, z);
                    chunk.setBlockState(cursor, FLOOR);
                    updateHeight(chunk, localX, floorY, localZ, FLOOR);
                }
                if (!structure || !BridgeShape.generates(x, z)) {
                    continue;
                }
                for (int y = y0; y <= y1; y++) {
                    BlockState state = BridgeShape.block(x, y, z);
                    if (state == null) {
                        continue;
                    }
                    cursor.set(x, y, z);
                    chunk.setBlockState(cursor, state);
                    updateHeight(chunk, localX, y, localZ, state);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    /**
     * Fills a floor into veil chunks that were generated before the floor existed.
     */
    public static void ensureFloor(ServerLevel level, LevelChunk chunk, boolean generated) {
        if (generated || !HorrorDimensions.VEIL.equals(level.dimension())) {
            return;
        }
        int floorY = level.getMinY();
        if (floorY < chunk.getMinY() || floorY >= chunk.getMinY() + chunk.getHeight()) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                cursor.set(pos.getMinBlockX() + localX, floorY, pos.getMinBlockZ() + localZ);
                if (!chunk.getBlockState(cursor).isAir()) {
                    continue;
                }
                chunk.setBlockState(cursor, FLOOR, Block.UPDATE_NONE);
            }
        }
        connectPiers(chunk, floorY);
    }

    /**
     * Drops each stilt through the air under it until it meets the floor.
     * Chunks generated before the floor have a pier that stops short of it.
     */
    private static void connectPiers(LevelChunk chunk, int floorY) {
        ChunkPos pos = chunk.getPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        int top = BridgeShape.maxBlockY();
        for (int localX = 0; localX < 16; localX++) {
            int x = minX + localX;
            if (!BridgeShape.onBridge(x)) {
                continue;
            }
            for (int localZ = 0; localZ < 16; localZ++) {
                int z = minZ + localZ;
                for (int y = floorY + 1; y <= top; y++) {
                    BlockState want = BridgeShape.bridgeOnly(x, y, z);
                    if (want == null) {
                        continue;
                    }
                    cursor.set(x, y, z);
                    if (!chunk.getBlockState(cursor).isAir()) {
                        break;
                    }
                    chunk.setBlockState(cursor, want, Block.UPDATE_NONE);
                }
            }
        }
    }

    /** Standing height on the floor. */
    public static double feetY(LevelHeightAccessor level) {
        return level.getMinY() + 1.0;
    }

    private static void updateHeight(ChunkAccess chunk, int localX, int y, int localZ, BlockState state) {
        for (Heightmap.Types type : Heightmap.Types.values()) {
            chunk.getOrCreateHeightmapUnprimed(type).update(localX, y, localZ, state);
        }
    }

    @Override
    public void createStructures(
            RegistryAccess registryAccess,
            ChunkGeneratorStructureState state,
            StructureManager structureManager,
            ChunkAccess centerChunk,
            StructureTemplateManager structureTemplateManager,
            ResourceKey<Level> level
    ) {
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return MIN_Y;
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor heightAccessor) {
        return heightAccessor.getMinY() + 1;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int minY = heightAccessor.getMinY();
        Predicate<BlockState> opaque = type.isOpaque();
        if (BridgeShape.generates(x, z)) {
            int y1 = Math.min(heightAccessor.getMaxY() - 1, BridgeShape.maxBlockY());
            int y0 = Math.max(minY, BridgeShape.minBlockY());
            for (int y = y1; y >= y0; y--) {
                BlockState state = BridgeShape.block(x, y, z);
                if (state != null && opaque.test(state)) {
                    return y + 1;
                }
            }
        }
        if (opaque.test(FLOOR)) {
            return minY + 1;
        }
        return minY;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int minY = heightAccessor.getMinY();
        BlockState[] states = new BlockState[heightAccessor.getHeight()];
        Arrays.fill(states, Blocks.AIR.defaultBlockState());
        if (states.length > 0) {
            states[0] = FLOOR;
        }
        if (BridgeShape.generates(x, z)) {
            int y0 = Math.max(minY, BridgeShape.minBlockY());
            int y1 = Math.min(minY + states.length - 1, BridgeShape.maxBlockY());
            for (int y = y0; y <= y1; y++) {
                BlockState state = BridgeShape.block(x, y, z);
                if (state != null) {
                    states[y - minY] = state;
                }
            }
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
        result.add("horror_mod:bridges");
    }
}
