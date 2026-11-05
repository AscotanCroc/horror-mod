package com.horrormod.world;

import com.horrormod.content.HorrorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * A hollow cobblestone pillar with a floor, a spawn-facing oak door, and a lectern.
 */
public class ObeliskPiece extends StructurePiece {
    private static final int HALF_WIDTH = 4;
    private static final int BURIED = 4;
    private static final int HEIGHT = 48;
    private static final int EXTRA_FOUNDATION = 12;

    private final int centerX;
    private final int surfaceY;
    private final int centerZ;
    private final Direction facing;

    public ObeliskPiece(BlockPos surfaceCenter, int minY, Direction facing) {
        super(HorrorStructures.OBELISK_PIECE, 0, boundingBox(surfaceCenter, minY));
        this.centerX = surfaceCenter.getX();
        this.surfaceY = surfaceCenter.getY();
        this.centerZ = surfaceCenter.getZ();
        this.facing = horizontal(facing);
    }

    public ObeliskPiece(CompoundTag tag) {
        super(HorrorStructures.OBELISK_PIECE, tag);
        this.centerX = tag.getIntOr("CX", 0);
        this.surfaceY = tag.getIntOr("SY", 64);
        this.centerZ = tag.getIntOr("CZ", 0);
        this.facing = horizontal(Direction.from3DDataValue(tag.getIntOr("Face", Direction.NORTH.get3DDataValue())));
    }

    public static ObeliskPiece load(StructurePieceSerializationContext context, CompoundTag tag) {
        return new ObeliskPiece(tag);
    }

    private static BoundingBox boundingBox(BlockPos surfaceCenter, int minY) {
        int x = surfaceCenter.getX();
        int y = surfaceCenter.getY();
        int z = surfaceCenter.getZ();
        return new BoundingBox(
                x - HALF_WIDTH,
                Math.max(minY, y - BURIED - EXTRA_FOUNDATION),
                z - HALF_WIDTH,
                x + HALF_WIDTH,
                y + HEIGHT - 1,
                z + HALF_WIDTH
        );
    }

    private static Direction horizontal(Direction facing) {
        return facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.centerX);
        tag.putInt("SY", this.surfaceY);
        tag.putInt("CZ", this.centerZ);
        tag.putInt("Face", this.facing.get3DDataValue());
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
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        int bottom = Math.max(level.getMinY(), this.surfaceY - BURIED);
        int top = Math.min(level.getMaxY() - 1, this.surfaceY + HEIGHT - 1);

        for (int y = bottom; y <= top; y++) {
            int above = y - this.surfaceY;
            int radius = above < 0 ? HALF_WIDTH : radius(above);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (isDoorway(dx, dz, y)) {
                        continue;
                    }
                    // surfaceY is the air above the ground, so the floor is the cobble one block down.
                    boolean interior = above >= 0 && radius >= 1 && Math.abs(dx) <= radius - 1 && Math.abs(dz) <= radius - 1;
                    place(level, chunkBox, this.centerX + dx, y, this.centerZ + dz, interior ? air : cobble);
                }
            }
        }

        extendFoundation(level, chunkBox, cobble);
        placeDoor(level, chunkBox);
        place(
                level,
                chunkBox,
                this.centerX,
                this.surfaceY,
                this.centerZ,
                HorrorBlocks.STONE_LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, this.facing)
        );
        placeTorch(level, chunkBox);
    }

    private void placeTorch(WorldGenLevel level, BoundingBox chunkBox) {
        int choice = Math.floorMod(this.centerX * 73428767 ^ this.centerZ * 9127841, 48);
        int seen = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (seen++ != choice) {
                    continue;
                }
                place(
                        level,
                        chunkBox,
                        this.centerX + dx,
                        this.surfaceY,
                        this.centerZ + dz,
                        Blocks.REDSTONE_TORCH.defaultBlockState()
                );
                return;
            }
        }
    }

    private void placeDoor(WorldGenLevel level, BoundingBox chunkBox) {
        int x = this.centerX + this.facing.getStepX() * HALF_WIDTH;
        int z = this.centerZ + this.facing.getStepZ() * HALF_WIDTH;
        BlockState lower = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, this.facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.POWERED, false);
        place(level, chunkBox, x, this.surfaceY, z, lower);
        place(level, chunkBox, x, this.surfaceY + 1, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    private boolean isDoorway(int dx, int dz, int y) {
        if (y != this.surfaceY && y != this.surfaceY + 1) {
            return false;
        }
        return switch (this.facing) {
            case NORTH -> dx == 0 && dz == -HALF_WIDTH;
            case SOUTH -> dx == 0 && dz == HALF_WIDTH;
            case WEST -> dz == 0 && dx == -HALF_WIDTH;
            case EAST -> dz == 0 && dx == HALF_WIDTH;
            default -> false;
        };
    }

    private void extendFoundation(WorldGenLevel level, BoundingBox chunkBox, BlockState foundation) {
        int start = this.surfaceY - BURIED - 1;
        int floor = Math.max(level.getMinY(), start - EXTRA_FOUNDATION);
        for (int dx = -HALF_WIDTH; dx <= HALF_WIDTH; dx++) {
            for (int dz = -HALF_WIDTH; dz <= HALF_WIDTH; dz++) {
                int x = this.centerX + dx;
                int z = this.centerZ + dz;
                for (int y = start; y >= floor; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!chunkBox.isInside(pos) || !level.getBlockState(pos).isAir()) {
                        break;
                    }
                    level.setBlock(pos, foundation, 2);
                }
            }
        }
    }

    private static void place(WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private static int radius(int aboveSurface) {
        if (aboveSurface < 12) {
            return 4;
        }
        if (aboveSurface < 22) {
            return 3;
        }
        if (aboveSurface < 32) {
            return 2;
        }
        if (aboveSurface < 42) {
            return 1;
        }
        return 0;
    }
}
