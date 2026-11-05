package com.horrormod.world;

import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Terrain deepslate is removed by the underground material rule. This covers ores and
 * other deepslate blocks placed directly during generation.
 */
public final class DeepslateStone {
    private static final Map<Block, Block> ORES = Map.of(
            Blocks.DEEPSLATE_COAL_ORE, Blocks.COAL_ORE,
            Blocks.DEEPSLATE_IRON_ORE, Blocks.IRON_ORE,
            Blocks.DEEPSLATE_COPPER_ORE, Blocks.COPPER_ORE,
            Blocks.DEEPSLATE_GOLD_ORE, Blocks.GOLD_ORE,
            Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.REDSTONE_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE, Blocks.EMERALD_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE, Blocks.LAPIS_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DIAMOND_ORE
    );

    private DeepslateStone() {
    }

    public static BlockState toStone(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.DEEPSLATE
                || block == Blocks.POLISHED_DEEPSLATE
                || block == Blocks.DEEPSLATE_BRICKS
                || block == Blocks.DEEPSLATE_TILES
                || block == Blocks.CHISELED_DEEPSLATE
                || block == Blocks.CRACKED_DEEPSLATE_BRICKS
                || block == Blocks.CRACKED_DEEPSLATE_TILES
                || block == Blocks.REINFORCED_DEEPSLATE) {
            return Blocks.STONE.defaultBlockState();
        }
        if (block == Blocks.COBBLED_DEEPSLATE) {
            return Blocks.COBBLESTONE.defaultBlockState();
        }
        if (block == Blocks.INFESTED_DEEPSLATE) {
            return Blocks.INFESTED_STONE.defaultBlockState();
        }
        Block ore = ORES.get(block);
        if (ore != null) {
            return ore.defaultBlockState();
        }
        return state;
    }
}
