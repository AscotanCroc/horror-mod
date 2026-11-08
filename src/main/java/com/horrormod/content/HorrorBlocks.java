package com.horrormod.content;

import com.horrormod.HorrorMod;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import com.horrormod.sound.ReverseSpiderSounds;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class HorrorBlocks {
    public static final BlockItemId SAMPLE_BLOCK_ID = BlockItemId.create(
            HorrorMod.id("sample_block"),
            HorrorMod.id("sample_block")
    );

    public static final Block SAMPLE_BLOCK = register(
            SAMPLE_BLOCK_ID,
            Block::new,
            BlockBehaviour.Properties.of().sound(SoundType.STONE).strength(1.5F, 6.0F)
    );

    public static final BlockItemId STONE_LECTERN_ID = BlockItemId.create(
            HorrorMod.id("stone_lectern"),
            HorrorMod.id("stone_lectern")
    );

    public static final SoundType BLACK_COBWEB_SOUNDS = new SoundType(
            1.0F,
            1.0F,
            ReverseSpiderSounds.COBWEB_BREAK,
            ReverseSpiderSounds.COBWEB_STEP,
            ReverseSpiderSounds.COBWEB_BREAK,
            ReverseSpiderSounds.COBWEB_STEP,
            ReverseSpiderSounds.COBWEB_STEP
    );

    public static final BlockItemId BLACK_COBWEB_ID = BlockItemId.create(
            HorrorMod.id("black_cobweb"),
            HorrorMod.id("black_cobweb")
    );

    public static final Block BLACK_COBWEB = register(
            BLACK_COBWEB_ID,
            BlackCobwebBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollision()
                    .noOcclusion()
                    .sound(BLACK_COBWEB_SOUNDS)
                    .strength(0.15F)
    );

    public static final Block STONE_LECTERN = register(
            STONE_LECTERN_ID,
            StoneLecternBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .sound(SoundType.STONE)
                    .strength(-1.0F, 3600000.0F)
                    .pushReaction(PushReaction.IMMOVEABLE)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(StoneLecternBlock.CHARGED) ? 15 : 0)
    );

    private HorrorBlocks() {
    }

    public static void initialize() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
                !state.is(STONE_LECTERN) || player.isCreative()
        );
        HorrorMod.LOGGER.info("Registered sample block {}", SAMPLE_BLOCK_ID.block());
        HorrorMod.LOGGER.info("Registered stone lectern {}", STONE_LECTERN_ID.block());
        HorrorMod.LOGGER.info("Registered black cobweb {}", BLACK_COBWEB_ID.block());
    }

    private static Block register(BlockItemId id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        Block block = register(id.block(), factory, properties);
        Item.Properties itemProperties = new Item.Properties().useBlockDescriptionPrefix().setId(id.item());
        Registry.register(BuiltInRegistries.ITEM, id.item(), new BlockItem(block, itemProperties));
        return block;
    }

    private static Block register(ResourceKey<Block> key, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        Block block = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }
}
