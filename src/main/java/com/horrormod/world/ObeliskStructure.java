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
        if (ObeliskPlacement.isComputing()) {
            return Optional.empty();
        }
        ChunkPos spawn = SpawnPlains.spawnChunk(context.seed(), context.chunkGenerator(), context.randomState());
        ChunkPos target = ObeliskPlacement.chunk(
                context.seed(),
                spawn,
                context.chunkGenerator(),
                context.randomState(),
                context.heightAccessor()
        );
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
     * Samples the ground around the planned chunk. Lower score is a drier, flatter site
     * whose spawn-facing door opens onto ground at the same height.
     */
    private static BlockPos chooseSite(GenerationContext context, ChunkPos spawn, ChunkPos nominal) {
        int originX = nominal.getMiddleBlockX();
        int originZ = nominal.getMiddleBlockZ();
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        int search = ObeliskPlacement.SITE_SEARCH_RADIUS;
        for (int dx = -search; dx <= search; dx += 4) {
            for (int dz = -search; dz <= search; dz += 4) {
                int x = originX + dx;
                int z = originZ + dz;
                int chunkX = Math.floorDiv(x, 16);
                int chunkZ = Math.floorDiv(z, 16);
                if (!withinSpawn(spawn, x, z) || SpawnPlains.isSpawnPad(spawn, chunkX, chunkZ)) {
                    continue;
                }
                if (!ObeliskPlacement.footprintClear(context.seed(), x, z)) {
                    continue;
                }
                int score = ObeliskPlacement.score(
                        (columnX, columnZ, type) -> column(context, columnX, columnZ, type),
                        x,
                        z,
                        spawn
                );
                if (score < bestScore) {
                    bestScore = score;
                    best = new BlockPos(x, column(context, x, z, Heightmap.Types.WORLD_SURFACE_WG), z);
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

    private static Direction facingSpawn(int x, int z, ChunkPos spawn) {
        return ObeliskPlacement.facingSpawn(x, z, spawn);
    }

    @Override
    public StructureType<?> type() {
        return HorrorStructures.OBELISK_TYPE;
    }
}
