package com.horrormod.content;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Left behind when a reverse enderman dies. The texture ignores light, and the block itself emits none.
 * It breaks like a mushroom block and drops raw meat instead of itself.
 */
public class VeinBlock extends Block {
    private static final int RADIUS = 4;
    private static final int SCARS = 5;

    public VeinBlock(Properties properties) {
        super(properties);
    }

    public static void scar(ServerLevel level, BlockPos origin) {
        List<BlockPos> open = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz > RADIUS * RADIUS) {
                        continue;
                    }
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (canReplace(level, cursor)) {
                        open.add(cursor.immutable());
                    }
                }
            }
        }
        RandomSource random = level.getRandom();
        int left = Math.min(SCARS, open.size());
        while (left > 0 && !open.isEmpty()) {
            BlockPos picked = open.remove(random.nextInt(open.size()));
            if (canReplace(level, picked)) {
                level.setBlock(picked, HorrorBlocks.VEIN.defaultBlockState(), 3);
                left--;
            }
        }
    }

    private static boolean canReplace(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(HorrorBlocks.VEIN)) {
            return false;
        }
        if (state.getDestroySpeed(level, pos) < 0.0F || level.getBlockEntity(pos) != null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).isAir()) {
                return true;
            }
        }
        return false;
    }
}
