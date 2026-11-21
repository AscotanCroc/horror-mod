package com.horrormod.world;

import com.horrormod.HorrorMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * The abandoned house template, unchanged except the chest loot.
 */
public class AbandonedHousePiece extends StructurePiece {
    private static final int HOUSE_X = 7;
    private static final int HOUSE_Y = 6;
    private static final int HOUSE_Z = 7;
    private static final int BELOW_SURFACE = 8;

    private final int chunkX;
    private final int chunkZ;
    private final int minY;
    private final int height;

    public AbandonedHousePiece(ChunkPos chunk, int minY, int height) {
        super(HorrorStructures.ABANDONED_HOUSE_PIECE, 0, column(chunk, minY, height));
        this.chunkX = chunk.x();
        this.chunkZ = chunk.z();
        this.minY = minY;
        this.height = height;
    }

    public AbandonedHousePiece(CompoundTag tag) {
        super(HorrorStructures.ABANDONED_HOUSE_PIECE, tag);
        this.chunkX = tag.getIntOr("CX", 0);
        this.chunkZ = tag.getIntOr("CZ", 0);
        this.minY = tag.getIntOr("MinY", 0);
        this.height = tag.getIntOr("Height", 384);
    }

    public static AbandonedHousePiece load(StructurePieceSerializationContext context, CompoundTag tag) {
        return new AbandonedHousePiece(tag);
    }

    private static BoundingBox column(ChunkPos chunk, int minY, int height) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        return new BoundingBox(x, minY, z, x + 15, minY + height - 1, z + 15);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.chunkX);
        tag.putInt("CZ", this.chunkZ);
        tag.putInt("MinY", this.minY);
        tag.putInt("Height", this.height);
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            net.minecraft.world.level.StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBox,
            ChunkPos chunkPos,
            BlockPos structurePos
    ) {
        if (chunkPos.x() != this.chunkX || chunkPos.z() != this.chunkZ) {
            return;
        }
        ServerLevel server = level.getLevel();
        StructureTemplate template = server.getStructureTemplateManager().get(HorrorMod.id("abandoned_house")).orElse(null);
        if (template == null) {
            HorrorMod.LOGGER.warn("Missing abandoned house template");
            return;
        }
        Vec3i size = template.getSize();
        int originX = chunkPos.getMiddleBlockX() - size.getX() / 2;
        int originZ = chunkPos.getMiddleBlockZ() - size.getZ() / 2;
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, originX, originZ);
        int originY = floorY(level, originX, originZ, surface, size.getY());
        BlockPos origin = new BlockPos(originX, originY, originZ);
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true).setFinalizeEntities(true);
        template.placeInWorld(level, origin, origin, settings, random, 2);
        BlockPos end = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
        for (BlockPos pos : BlockPos.betweenClosed(origin, end)) {
            if (!level.getBlockState(pos).is(Blocks.CHEST)) {
                continue;
            }
            net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, pos.immutable(), AbandonedHouseLoot.TABLE);
        }
    }

    /**
     * Sit the saved floor on a dry cave floor when this column has one. Otherwise bury the whole house.
     */
    private int floorY(WorldGenLevel level, int originX, int originZ, int surface, int houseHeight) {
        int buried = Math.max(this.minY + 4, surface - BELOW_SURFACE - houseHeight);
        int bottom = this.minY + 4;
        for (int y = surface - BELOW_SURFACE; y >= bottom; y--) {
            BlockPos feet = new BlockPos(originX + HOUSE_X / 2, y, originZ + HOUSE_Z / 2);
            BlockPos ground = feet.below();
            if (!level.getBlockState(feet).isAir() || !level.getFluidState(feet).isEmpty()) {
                continue;
            }
            if (level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
                return ground.getY();
            }
        }
        return buried;
    }
}
