package com.horrormod.content;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Placing rot sends five curving arms through exposed solid blocks, including diagonal steps.
 * A third of the blocks can later grow one wither rose.
 */
public class RotBlock extends Block {
    private static final int MIN_WAIT = 20 * 60 * 2;
    private static final int MAX_WAIT = 20 * 60 * 60;
    private static final ThreadLocal<Boolean> SPREADING = ThreadLocal.withInitial(() -> false);

    public RotBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (oldState.is(this) || !(level instanceof ServerLevel server)) {
            return;
        }
        if (server.getRandom().nextFloat() < 0.33F) {
            int wait = MIN_WAIT + server.getRandom().nextInt(MAX_WAIT - MIN_WAIT);
            server.scheduleTick(pos, this, wait);
        }
        if (SPREADING.get()) {
            return;
        }
        SPREADING.set(true);
        try {
            spread(server, pos);
        } finally {
            SPREADING.set(false);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.is(this)) {
            return;
        }
        BlockPos above = pos.above();
        BlockState rose = Blocks.WITHER_ROSE.defaultBlockState();
        if (level.getBlockState(above).isAir() && rose.canSurvive(level, above)) {
            level.setBlock(above, rose, 3);
        }
    }

    private static void spread(ServerLevel level, BlockPos origin) {
        RandomSource random = level.getRandom();
        List<Step> directions = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        directions.add(new Step(x, y, z));
                    }
                }
            }
        }
        for (int index = directions.size() - 1; index > 0; index--) {
            int swap = random.nextInt(index + 1);
            Step hold = directions.get(index);
            directions.set(index, directions.get(swap));
            directions.set(swap, hold);
        }
        for (int arm = 0; arm < 5; arm++) {
            growArm(level, origin, directions.get(arm), 8 + random.nextInt(6));
        }
    }

    private static void growArm(ServerLevel level, BlockPos origin, Step direction, int length) {
        RandomSource random = level.getRandom();
        BlockPos current = origin;
        Step facing = direction;
        for (int step = 0; step < length; step++) {
            if (random.nextFloat() < 0.35F) {
                facing = turn(facing, random);
            }
            BlockPos next = current.offset(facing.x, facing.y, facing.z);
            if (!canReplace(level, next)) {
                Step turned = turn(facing, random);
                next = current.offset(turned.x, turned.y, turned.z);
                if (!canReplace(level, next)) {
                    return;
                }
                facing = turned;
            }
            level.setBlock(next, HorrorBlocks.ROT.defaultBlockState(), 3);
            current = next;
        }
    }

    private static Step turn(Step facing, RandomSource random) {
        int x = facing.x;
        int y = facing.y;
        int z = facing.z;
        int axis = random.nextInt(3);
        int delta = random.nextBoolean() ? 1 : -1;
        if (axis == 0) {
            x = clamp(x + delta);
        } else if (axis == 1) {
            y = clamp(y + delta);
        } else {
            z = clamp(z + delta);
        }
        if (x == 0 && y == 0 && z == 0) {
            return facing;
        }
        return new Step(x, y, z);
    }

    private static int clamp(int value) {
        return Math.max(-1, Math.min(1, value));
    }

    private record Step(int x, int y, int z) {
    }

    private static boolean canReplace(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.is(HorrorBlocks.ROT) || state.isAir() || !state.isSolid()) {
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
