package com.horrormod.world;

import com.horrormod.HorrorMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Every block a player has placed in one level. Event memory only keeps a short list, so the reverse creeper uses this.
 */
public final class PlayerPlacedBlocks extends SavedData {
    public record Placed(BlockPos pos, String block) {
        public static final Codec<Placed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Placed::pos),
                Codec.STRING.fieldOf("block").forGetter(Placed::block)
        ).apply(instance, Placed::new));
    }

    public static final Codec<PlayerPlacedBlocks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Placed.CODEC.listOf().optionalFieldOf("blocks", List.of()).forGetter(PlayerPlacedBlocks::entries)
    ).apply(instance, PlayerPlacedBlocks::new));

    public static final SavedDataType<PlayerPlacedBlocks> TYPE = new SavedDataType<>(
            HorrorMod.id("placed_blocks"),
            PlayerPlacedBlocks::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<BlockPos, String> blocks = new HashMap<>();

    public PlayerPlacedBlocks() {
    }

    private PlayerPlacedBlocks(List<Placed> placed) {
        for (Placed entry : placed) {
            this.blocks.put(entry.pos(), entry.block());
        }
    }

    public static PlayerPlacedBlocks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void record(BlockPos pos, String blockId) {
        this.blocks.put(pos.immutable(), blockId);
        setDirty();
    }

    public boolean contains(BlockPos pos) {
        return this.blocks.containsKey(pos);
    }

    /**
     * True while the stored id is still the block in the world. A broken or replaced block is forgotten.
     */
    public boolean matches(ServerLevel level, BlockPos pos) {
        String stored = this.blocks.get(pos);
        if (stored == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        Identifier current = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (current != null && current.toString().equals(stored)) {
            return true;
        }
        this.blocks.remove(pos);
        setDirty();
        return false;
    }

    public void remove(BlockPos pos) {
        if (this.blocks.remove(pos) != null) {
            setDirty();
        }
    }

    private List<Placed> entries() {
        List<Placed> list = new ArrayList<>(this.blocks.size());
        for (Map.Entry<BlockPos, String> entry : this.blocks.entrySet()) {
            list.add(new Placed(entry.getKey(), entry.getValue()));
        }
        return list;
    }
}
