package com.horrormod.content;

import com.horrormod.entity.HorrorEntities;
import com.horrormod.network.OpenRecipePayload;
import com.horrormod.world.VeilPassage;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A stone lectern with no book. Using it asks the client to open the recipe screen.
 * Using [veil on it opens the passage. [veil carried nearby charges the lectern.
 */
public class StoneLecternBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty CHARGED = BooleanProperty.create("charged");
    /** Horizontal reach of a carried [veil, in blocks. */
    public static final int VEIL_REACH = 32;
    public static final int VEIL_REACH_VERTICAL = 16;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            Block.box(4.0, 2.0, 4.0, 12.0, 10.0, 12.0),
            Block.box(1.0, 9.0, 2.0, 15.0, 16.0, 15.0)
    );

    public StoneLecternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CHARGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(CHARGED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    /**
     * Visits loaded lecterns around {@code center}. The position handed to {@code consumer} is a copy.
     */
    public static void nearby(Level level, BlockPos center, Consumer<BlockPos> consumer) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int x0 = center.getX();
        int y0 = center.getY();
        int z0 = center.getZ();
        for (int y = y0 - VEIL_REACH_VERTICAL; y <= y0 + VEIL_REACH_VERTICAL; y++) {
            for (int x = x0 - VEIL_REACH; x <= x0 + VEIL_REACH; x++) {
                for (int z = z0 - VEIL_REACH; z <= z0 + VEIL_REACH; z++) {
                    cursor.set(x, y, z);
                    if (!level.isLoaded(cursor) || !level.getBlockState(cursor).is(HorrorBlocks.STONE_LECTERN)) {
                        continue;
                    }
                    consumer.accept(cursor.immutable());
                }
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!stack.is(HorrorEntities.VEIL)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            VeilPassage.begin(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, OpenRecipePayload.INSTANCE);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CHARGED);
    }
}
