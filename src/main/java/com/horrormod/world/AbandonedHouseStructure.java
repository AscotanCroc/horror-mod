package com.horrormod.world;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Places the saved abandoned house. The building is the template; only its chest loot is changed.
 */
public class AbandonedHouseStructure extends Structure {
    public static final MapCodec<AbandonedHouseStructure> CODEC = simpleCodec(AbandonedHouseStructure::new);

    public AbandonedHouseStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        if (VoidChunks.isBaseVoid(context.seed(), chunk.x(), chunk.z())) {
            return Optional.empty();
        }
        int minY = context.heightAccessor().getMinY();
        int height = context.heightAccessor().getHeight();
        BlockPos origin = new BlockPos(chunk.getMiddleBlockX(), minY, chunk.getMiddleBlockZ());
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new AbandonedHousePiece(chunk, minY, height))));
    }

    @Override
    public StructureType<?> type() {
        return HorrorStructures.ABANDONED_HOUSE_TYPE;
    }
}
