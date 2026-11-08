package com.horrormod.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * A cobweb that does not catch anything walking through it.
 * Spider-placed webs flash for a second, sit for the rest of a minute, then flash
 * again and vanish. The second flash starts at the death position and rolls outward.
 */
public class BlackCobwebBlock extends Block {
    public static final int FLASH_TICKS = 20;
    public static final int LIFE_TICKS = 20 * 60;
    /** How long each block of distance waits before the leaving flash. */
    public static final int RIPPLE_TICKS = 8;
    public static final int MAX_WAVE = 7;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, FLASH_TICKS);
    public static final IntegerProperty WAVE = IntegerProperty.create("wave", 0, MAX_WAVE);
    public static final BooleanProperty LEAVING = BooleanProperty.create("leaving");

    public BlackCobwebBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, FLASH_TICKS)
                .setValue(WAVE, 0)
                .setValue(LEAVING, false));
    }

    public static int wave(BlockPos origin, BlockPos pos) {
        int dx = Math.abs(pos.getX() - origin.getX());
        int dy = Math.abs(pos.getY() - origin.getY());
        int dz = Math.abs(pos.getZ() - origin.getZ());
        return Math.min(MAX_WAVE, Math.max(dx, Math.max(dy, dz)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, WAVE, LEAVING);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean leaving = state.getValue(LEAVING);
        int age = state.getValue(AGE);
        if (leaving) {
            if (age >= FLASH_TICKS - 1) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return;
            }
            level.setBlock(pos, state.setValue(AGE, age + 1), 3);
            level.scheduleTick(pos, this, 1);
            return;
        }
        if (age < FLASH_TICKS) {
            int nextAge = age + 1;
            level.setBlock(pos, state.setValue(AGE, nextAge), 3);
            if (nextAge < FLASH_TICKS) {
                level.scheduleTick(pos, this, 1);
            } else {
                int wait = LIFE_TICKS - FLASH_TICKS + state.getValue(WAVE) * RIPPLE_TICKS;
                level.scheduleTick(pos, this, wait);
            }
            return;
        }
        level.setBlock(pos, state.setValue(LEAVING, true).setValue(AGE, 0), 3);
        level.scheduleTick(pos, this, 1);
    }
}
