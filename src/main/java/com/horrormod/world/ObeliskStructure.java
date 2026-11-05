package com.horrormod.world;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class ObeliskStructure extends Structure {
    public static final MapCodec<ObeliskStructure> CODEC = simpleCodec(ObeliskStructure::new);

    public ObeliskStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos spawn = SpawnPlains.spawnChunk(context.seed(), context.chunkGenerator(), context.randomState());
        ChunkPos target = ObeliskPlacement.chunk(context.seed(), spawn);
        if (!context.chunkPos().equals(target)) {
            return Optional.empty();
        }

        BlockPos origin = chooseSite(context, spawn, target);
        Direction facing = facingSpawn(origin.getX(), origin.getZ(), spawn);
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(
                new ObeliskPiece(origin, context.heightAccessor().getMinY(), facing)
        )));
    }

    /**
     * Samples a few noise columns around the planned spot. Lower score is a drier, flatter site
     * whose spawn-facing door opens onto ground at the same height.
     */
    private static BlockPos chooseSite(GenerationContext context, ChunkPos spawn, ChunkPos nominal) {
        int originX = nominal.getMiddleBlockX();
        int originZ = nominal.getMiddleBlockZ();
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int dx = -32; dx <= 32; dx += 8) {
            for (int dz = -32; dz <= 32; dz += 8) {
                int x = originX + dx;
                int z = originZ + dz;
                int chunkX = Math.floorDiv(x, 16);
                int chunkZ = Math.floorDiv(z, 16);
                if (!withinSpawn(spawn, x, z) || SpawnPlains.isSpawnPad(spawn, chunkX, chunkZ)) {
                    continue;
                }
                if (VoidChunks.isBaseVoid(context.seed(), chunkX, chunkZ)) {
                    continue;
                }
                int surface = column(context, x, z, Heightmap.Types.WORLD_SURFACE_WG);
                int floor = column(context, x, z, Heightmap.Types.OCEAN_FLOOR_WG);
                int score = siteScore(context, x, z, surface, floor, spawn);
                if (score < bestScore) {
                    bestScore = score;
                    best = new BlockPos(x, surface, z);
                }
            }
        }
        if (best != null) {
            return best;
        }
        int x = originX;
        int z = originZ;
        return new BlockPos(x, column(context, x, z, Heightmap.Types.WORLD_SURFACE_WG), z);
    }

    private static int siteScore(GenerationContext context, int x, int z, int surface, int floor, ChunkPos spawn) {
        int score = 0;
        if (surface > floor) {
            score += 500;
        }
        int lowest = surface;
        int highest = surface;
        for (int[] offset : FOOTPRINT) {
            int sample = column(context, x + offset[0], z + offset[1], Heightmap.Types.WORLD_SURFACE_WG);
            int sampleFloor = column(context, x + offset[0], z + offset[1], Heightmap.Types.OCEAN_FLOOR_WG);
            if (sample > sampleFloor) {
                score += 80;
            }
            lowest = Math.min(lowest, sample);
            highest = Math.max(highest, sample);
        }
        score += (highest - lowest) * 12;
        Direction facing = facingSpawn(x, z, spawn);
        int doorX = x + facing.getStepX() * 5;
        int doorZ = z + facing.getStepZ() * 5;
        int outside = column(context, doorX, doorZ, Heightmap.Types.WORLD_SURFACE_WG);
        int outsideFloor = column(context, doorX, doorZ, Heightmap.Types.OCEAN_FLOOR_WG);
        if (outside > outsideFloor) {
            score += 200;
        }
        if (outside > surface) {
            score += (outside - surface) * 20;
        } else if (outside < surface - 1) {
            score += (surface - 1 - outside) * 6;
        }
        return score;
    }

    private static int column(GenerationContext context, int x, int z, Heightmap.Types type) {
        return context.chunkGenerator().getBaseHeight(
                x,
                z,
                type,
                context.heightAccessor(),
                context.randomState()
        );
    }

    private static boolean withinSpawn(ChunkPos spawn, int x, int z) {
        long dx = x - spawn.getMiddleBlockX();
        long dz = z - spawn.getMiddleBlockZ();
        long limit = ObeliskPlacement.MAX_DISTANCE_FROM_SPAWN;
        return dx * dx + dz * dz <= limit * limit;
    }

    private static final int[][] FOOTPRINT = {
            {4, 0},
            {-4, 0},
            {0, 4},
            {0, -4}
    };

    private static Direction facingSpawn(int x, int z, ChunkPos spawn) {
        int dx = spawn.getMiddleBlockX() - x;
        int dz = spawn.getMiddleBlockZ() - z;
        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx >= 0 ? Direction.EAST : Direction.WEST;
        }
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    @Override
    public StructureType<?> type() {
        return HorrorStructures.OBELISK_TYPE;
    }
}
