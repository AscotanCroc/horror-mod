package com.horrormod.content;

import com.horrormod.mixin.LevelTickListAccessor;
import com.horrormod.network.WhiteFlashPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.ticks.LevelChunkTicks;

/**
 * A cobweb that does not catch anything walking through it.
 * Spider-placed webs flash for a second, sit for the rest of a minute, then flash
 * again and vanish. Breaking one starts that leaving flash at the broken web
 * and rolls it outward through the cluster.
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

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel server && !state.getValue(LEAVING)) {
            vanishFrom(server, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * The leaving flash starts at the broken web and rolls outward through the cluster.
     */
    public static void vanishFrom(ServerLevel level, BlockPos center) {
        int radius = 12;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (!state.is(HorrorBlocks.BLACK_COBWEB) || state.getValue(LEAVING)) {
                        continue;
                    }
                    int wave = wave(center, cursor.immutable());
                    BlockPos target = cursor.immutable();
                    level.setBlock(target, state.setValue(WAVE, wave).setValue(AGE, FLASH_TICKS).setValue(LEAVING, false), 3);
                    reschedule(level, target, Math.max(1, wave * RIPPLE_TICKS));
                }
            }
        }
    }

    /**
     * A cobweb already has its lifetime tick waiting. A second schedule is ignored,
     * so the old one has to go before the leaving ripple can start.
     */
    @SuppressWarnings("unchecked")
    private static void reschedule(ServerLevel level, BlockPos pos, int delay) {
        Object chunkTicks = ((LevelTickListAccessor) (Object) level.getBlockTicks())
                .horror$allContainers()
                .get(ChunkPos.pack(pos));
        if (chunkTicks instanceof LevelChunkTicks<?> ticks) {
            ((LevelChunkTicks<Block>) ticks).removeIf(tick -> tick.pos().equals(pos) && tick.type() == HorrorBlocks.BLACK_COBWEB);
        }
        level.scheduleTick(pos, HorrorBlocks.BLACK_COBWEB, delay);
    }

    public static void flash(ServerLevel level, double x, double y, double z) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(x, y, z) <= 48.0 * 48.0) {
                ServerPlayNetworking.send(player, WhiteFlashPayload.INSTANCE);
            }
        }
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
