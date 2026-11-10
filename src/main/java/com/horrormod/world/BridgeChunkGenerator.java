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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Stamps {@link BridgeShape} through the veil. Everywhere else stays empty.
 * Min y and height match {@code dimension_type/veil.json}.
 */
public class BridgeChunkGenerator extends ChunkGenerator {
    /** Same vertical span as the veil dimension type. */
    private static final int MIN_Y = -64;
    private static final int HEIGHT = 384;

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
        int y0 = Math.max(chunk.getMinY(), BridgeShape.minBlockY());
        int y1 = Math.min(chunk.getMinY() + chunk.getHeight() - 1, BridgeShape.maxBlockY());
        if (y0 > y1) {
            return CompletableFuture.completedFuture(chunk);
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        for (int localX = 0; localX < 16; localX++) {
            int x = minX + localX;
            if (!BridgeShape.onBridge(x)) {
                continue;
            }
            for (int localZ = 0; localZ < 16; localZ++) {
                int z = minZ + localZ;
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
        return BridgeShape.WALK_Y + 1;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int minY = heightAccessor.getMinY();
        if (!BridgeShape.onBridge(x)) {
            return minY;
        }
        Predicate<BlockState> opaque = type.isOpaque();
        int y1 = Math.min(heightAccessor.getMaxY() - 1, BridgeShape.maxBlockY());
        int y0 = Math.max(minY, BridgeShape.minBlockY());
        for (int y = y1; y >= y0; y--) {
            BlockState state = BridgeShape.block(x, y, z);
            if (state != null && opaque.test(state)) {
                return y + 1;
            }
        }
        return minY;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int minY = heightAccessor.getMinY();
        BlockState[] states = new BlockState[heightAccessor.getHeight()];
        Arrays.fill(states, Blocks.AIR.defaultBlockState());
        if (BridgeShape.onBridge(x)) {
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
