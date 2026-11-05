package com.horrormod.entity;

import com.horrormod.world.PlayerPlacedBlocks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BonemealableFeaturePlacerBlock;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.NyliumBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

/**
 * Block rewrite for a reverse creeper blast. Rolls are per block inside a 16-block sphere.
 */
public final class ReverseCreeperBurst {
    static final double RADIUS = 16.0;
    /** Moss bonemeal can place plants this far from the moss block, including past the blast. */
    private static final int GROWTH_MARGIN = 6;
    /** Long enough for blocks broken by the blast to finish dropping their items. */
    private static final int ITEM_CLEAR_DELAY = 2;
    private static final List<ItemSweep> itemSweeps = new ArrayList<>();
    private static final int FLAGS = Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;
    private static final Block[] SOILS = {
            Blocks.PODZOL,
            Blocks.DIRT,
            Blocks.COARSE_DIRT,
            Blocks.MOSS_BLOCK,
            Blocks.GRAVEL
    };
    private static final Map<Block, List<Block>> WEATHERED = Map.ofEntries(
            Map.entry(Blocks.COBBLESTONE, List.of(Blocks.MOSSY_COBBLESTONE)),
            Map.entry(Blocks.COBBLESTONE_STAIRS, List.of(Blocks.MOSSY_COBBLESTONE_STAIRS)),
            Map.entry(Blocks.COBBLESTONE_SLAB, List.of(Blocks.MOSSY_COBBLESTONE_SLAB)),
            Map.entry(Blocks.COBBLESTONE_WALL, List.of(Blocks.MOSSY_COBBLESTONE_WALL)),
            Map.entry(Blocks.STONE_BRICKS, List.of(Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS)),
            Map.entry(Blocks.STONE_BRICK_STAIRS, List.of(Blocks.MOSSY_STONE_BRICK_STAIRS)),
            Map.entry(Blocks.STONE_BRICK_SLAB, List.of(Blocks.MOSSY_STONE_BRICK_SLAB)),
            Map.entry(Blocks.STONE_BRICK_WALL, List.of(Blocks.MOSSY_STONE_BRICK_WALL)),
            Map.entry(Blocks.DEEPSLATE_BRICKS, List.of(Blocks.CRACKED_DEEPSLATE_BRICKS)),
            Map.entry(Blocks.DEEPSLATE_TILES, List.of(Blocks.CRACKED_DEEPSLATE_TILES)),
            Map.entry(Blocks.NETHER_BRICKS, List.of(Blocks.CRACKED_NETHER_BRICKS)),
            Map.entry(Blocks.POLISHED_BLACKSTONE_BRICKS, List.of(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS))
    );

    private ReverseCreeperBurst() {
    }

    public static void initialize() {
        ServerTickEvents.END_LEVEL_TICK.register(ReverseCreeperBurst::clearDroppedItems);
    }

    /**
     * Removes floor items a moment after the blast, including loot the explosion itself just dropped.
     */
    public static void clearItemsSoon(ServerLevel level, Vec3 center) {
        itemSweeps.add(new ItemSweep(level, center, level.getGameTime() + ITEM_CLEAR_DELAY));
    }

    private static void clearDroppedItems(ServerLevel level) {
        if (itemSweeps.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        Iterator<ItemSweep> sweeps = itemSweeps.iterator();
        while (sweeps.hasNext()) {
            ItemSweep sweep = sweeps.next();
            if (sweep.level != level) {
                continue;
            }
            if (now < sweep.at) {
                continue;
            }
            sweeps.remove();
            AABB area = new AABB(sweep.center, sweep.center).inflate(RADIUS + 1.0);
            for (ItemEntity item : level.getEntities(EntityTypes.ITEM, area, entity -> true)) {
                item.discard();
            }
        }
    }

    public static void apply(ServerLevel level, ReverseCreeper creeper) {
        double x = creeper.getX();
        double y = creeper.getY();
        double z = creeper.getZ();
        RandomSource random = creeper.getRandom();
        PlayerPlacedBlocks index = PlayerPlacedBlocks.get(level);
        List<BlockPos> grass = new ArrayList<>();
        List<BlockPos> area = new ArrayList<>();
        BlockPos min = BlockPos.containing(x - RADIUS, y - RADIUS, z - RADIUS);
        BlockPos max = BlockPos.containing(x + RADIUS, y + RADIUS, z + RADIUS);
        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            if (!inside(cursor, x, y, z) || !level.isLoaded(cursor)) {
                continue;
            }
            BlockPos pos = cursor.immutable();
            BlockState state = level.getBlockState(pos);
            if (unbreakable(level, pos, state)) {
                continue;
            }
            area.add(pos);
            if (state.is(Blocks.GRASS_BLOCK)) {
                grass.add(pos);
            }
        }

        Set<BlockPos> removed = new HashSet<>();
        List<BlockPos> kept = new ArrayList<>();
        for (BlockPos pos : area) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || natural(state)) {
                continue;
            }
            if (random.nextBoolean()) {
                BlockState replacement = random.nextFloat() < 0.15F
                        ? Blocks.COBWEB.defaultBlockState()
                        : Blocks.AIR.defaultBlockState();
                set(level, pos, replacement);
                index.remove(pos);
                removed.add(pos);
            } else {
                kept.add(pos);
            }
        }

        for (BlockPos pos : area) {
            BlockState state = level.getBlockState(pos);
            if (!removed.contains(pos) && !natural(state)) {
                weather(level, pos, random, index);
            }
        }

        List<BlockPos> formed = new ArrayList<>();
        for (BlockPos pos : kept) {
            if (random.nextFloat() >= 0.25F) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            BlockState shaped = shape(state.getBlock(), random);
            if (shaped == null) {
                continue;
            }
            set(level, pos, shaped);
            remember(index, pos, shaped);
            formed.add(pos);
        }

        Set<BlockPos> water = placeWater(level, grass, random, index);
        for (BlockPos pos : grass) {
            if (water.contains(pos) || !level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                continue;
            }
            if (random.nextFloat() >= 0.15F) {
                continue;
            }
            clearPlant(level, pos.above(), index);
            Block soil = SOILS[random.nextInt(SOILS.length)];
            set(level, pos, soil.defaultBlockState());
            index.remove(pos);
        }

        Set<BlockPos> planted = new HashSet<>();
        for (BlockPos pos : grass) {
            if (water.contains(pos) || !level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                continue;
            }
            BlockPos above = pos.above();
            if (!level.isInWorldBounds(above) || !level.isLoaded(above) || !plantSpace(level, above)) {
                continue;
            }
            float roll = random.nextFloat();
            if (roll < 0.60F) {
                clearPlant(level, above, index);
            } else if (roll < 0.88F) {
                placeSmall(level, above, Blocks.SHORT_GRASS, index, planted);
            } else if (roll < 0.96F) {
                placeTall(level, above, index, planted);
            } else if (roll < 0.98F) {
                placeSmall(level, above, Blocks.POPPY, index, planted);
            } else {
                placeSmall(level, above, Blocks.DANDELION, index, planted);
            }
        }

        waterlog(level, formed, random);
        placeVines(level, area, planted, formed, random, x, y, z);
        growPlants(level, x, y, z, random);
    }

    /**
     * Runs after the other block changes, so only plants that are still standing, and able to grow, mature.
     */
    private static void growPlants(ServerLevel level, double x, double y, double z, RandomSource random) {
        List<BlockPos> plants = new ArrayList<>();
        BlockPos min = BlockPos.containing(x - RADIUS, y - RADIUS, z - RADIUS);
        BlockPos max = BlockPos.containing(x + RADIUS, y + RADIUS, z + RADIUS);
        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            if (!inside(cursor, x, y, z) || !level.isLoaded(cursor)) {
                continue;
            }
            BlockPos pos = cursor.immutable();
            if (grows(level.getBlockState(pos).getBlock())) {
                plants.add(pos);
            }
        }
        for (BlockPos pos : plants) {
            grow(level, pos, random);
        }
    }

    private static Map<BlockPos, BlockState> snapshot(ServerLevel level, BlockPos center) {
        Map<BlockPos, BlockState> before = new HashMap<>();
        BlockPos min = new BlockPos(center.getX() - GROWTH_MARGIN, center.getY() - GROWTH_MARGIN, center.getZ() - GROWTH_MARGIN);
        BlockPos max = new BlockPos(center.getX() + GROWTH_MARGIN, center.getY() + GROWTH_MARGIN, center.getZ() + GROWTH_MARGIN);
        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            if (!level.isInWorldBounds(cursor) || !level.isLoaded(cursor)) {
                continue;
            }
            before.put(cursor.immutable(), level.getBlockState(cursor));
        }
        return before;
    }

    /**
     * Moss growth rolls azalea and flowering azalea. Put back whatever block was there.
     */
    private static void clearNewAzalea(ServerLevel level, Map<BlockPos, BlockState> before) {
        for (Map.Entry<BlockPos, BlockState> entry : before.entrySet()) {
            if (azaleaBush(entry.getValue())) {
                continue;
            }
            BlockPos pos = entry.getKey();
            if (azaleaBush(level.getBlockState(pos))) {
                set(level, pos, entry.getValue());
            }
        }
    }

    private static boolean azaleaBush(BlockState state) {
        return state.is(Blocks.AZALEA) || state.is(Blocks.FLOWERING_AZALEA);
    }

    private static boolean grows(Block block) {
        if (block instanceof GrassBlock || block instanceof NyliumBlock) {
            return false;
        }
        return block instanceof SaplingBlock || block instanceof CropBlock || block instanceof BonemealableBlock;
    }

    private static void grow(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof SaplingBlock sapling) {
            if (!sapling.isValidBonemealTarget(level, pos, state, BonemealSource.MOB)) {
                return;
            }
            BlockState ready = state.getValue(SaplingBlock.STAGE) == 0 ? state.setValue(SaplingBlock.STAGE, 1) : state;
            sapling.advanceTree(level, pos, ready, random);
            return;
        }
        if (block instanceof CropBlock crop) {
            if (crop.isMaxAge(state) || !crop.isValidBonemealTarget(level, pos, state, BonemealSource.MOB)) {
                return;
            }
            level.setBlock(pos, crop.getStateForAge(crop.getMaxAge()), Block.UPDATE_ALL);
            return;
        }
        if (!(block instanceof BonemealableBlock)) {
            return;
        }
        int limit = block instanceof GrowingPlantHeadBlock || block instanceof BambooStalkBlock ? 16 : 1;
        Map<BlockPos, BlockState> spread = block instanceof BonemealableFeaturePlacerBlock ? snapshot(level, pos) : Map.of();
        try {
            for (int attempt = 0; attempt < limit; attempt++) {
                state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof BonemealableBlock growable)) {
                    return;
                }
                if (growable instanceof GrassBlock || growable instanceof NyliumBlock) {
                    return;
                }
                if (!growable.isValidBonemealTarget(level, pos, state, BonemealSource.MOB)) {
                    return;
                }
                BlockState before = state;
                BlockState aboveBefore = level.getBlockState(pos.above());
                growable.performBonemeal(level, random, pos, state, BonemealSource.MOB);
                if (level.getBlockState(pos).equals(before) && level.getBlockState(pos.above()).equals(aboveBefore)) {
                    return;
                }
            }
        } finally {
            clearNewAzalea(level, spread);
        }
    }

    /**
     * Mossy or cracked forms are chosen before a block is cut into a slab or stair, so the new shape keeps that variant.
     */
    private static void weather(ServerLevel level, BlockPos pos, RandomSource random, PlayerPlacedBlocks index) {
        BlockState state = level.getBlockState(pos);
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id != null && id.getPath().contains("infested")) {
            return;
        }
        List<Block> variants = WEATHERED.get(state.getBlock());
        if (variants == null || random.nextFloat() >= 0.5F) {
            return;
        }
        Block variant = variants.get(random.nextInt(variants.size()));
        BlockState weathered = withSharedProperties(state, variant);
        boolean placedHere = index.contains(pos);
        set(level, pos, weathered);
        if (placedHere) {
            remember(index, pos, weathered);
        }
    }

    private static void placeVines(
            ServerLevel level,
            List<BlockPos> area,
            Set<BlockPos> planted,
            List<BlockPos> formed,
            RandomSource random,
            double x,
            double y,
            double z
    ) {
        Set<BlockPos> reserved = new HashSet<>(planted);
        reserved.addAll(formed);
        List<VineFace> faces = new ArrayList<>();
        for (BlockPos pos : area) {
            for (Direction outward : Direction.Plane.HORIZONTAL) {
                Direction attach = outward.getOpposite();
                if (!VineBlock.isAcceptableNeighbour(level, pos, attach)) {
                    continue;
                }
                BlockPos air = pos.relative(outward);
                if (!inside(air, x, y, z) || !level.isLoaded(air) || reserved.contains(air)) {
                    continue;
                }
                BlockState occupying = level.getBlockState(air);
                if (!occupying.isAir() && !occupying.is(Blocks.VINE)) {
                    continue;
                }
                faces.add(new VineFace(air.immutable(), attach));
            }
        }
        int count = Math.min(6, faces.size());
        for (int i = 0; i < count; i++) {
            int pick = i + random.nextInt(faces.size() - i);
            VineFace face = faces.get(pick);
            faces.set(pick, faces.get(i));
            BlockState occupying = level.getBlockState(face.air());
            BlockState vine = occupying.is(Blocks.VINE) ? occupying : Blocks.VINE.defaultBlockState();
            set(level, face.air(), vine.setValue(VineBlock.getPropertyForFace(face.attach()), true));
        }
    }

    private static void waterlog(ServerLevel level, List<BlockPos> formed, RandomSource random) {
        for (BlockPos pos : formed) {
            BlockState state = level.getBlockState(pos);
            if (!state.hasProperty(BlockStateProperties.WATERLOGGED) || random.nextFloat() >= 0.33F || !enclosed(level, pos)) {
                continue;
            }
            set(level, pos, state.setValue(BlockStateProperties.WATERLOGGED, true));
        }
    }

    private static boolean enclosed(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) {
                continue;
            }
            BlockPos neighbor = pos.relative(direction);
            BlockState state = level.getBlockState(neighbor);
            if (!state.isFaceSturdy(level, neighbor, direction.getOpposite())) {
                return false;
            }
        }
        return true;
    }

    private static Set<BlockPos> placeWater(ServerLevel level, List<BlockPos> grass, RandomSource random, PlayerPlacedBlocks index) {
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : grass) {
            if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                candidates.add(pos);
            }
        }
        Set<BlockPos> placed = new HashSet<>();
        if (candidates.isEmpty()) {
            return placed;
        }
        BlockPos first = candidates.get(random.nextInt(candidates.size()));
        pour(level, first, index);
        placed.add(first);
        if (!random.nextBoolean()) {
            return placed;
        }
        List<BlockPos> separated = new ArrayList<>();
        for (BlockPos pos : candidates) {
            if (!touches(pos, first)) {
                separated.add(pos);
            }
        }
        if (separated.isEmpty()) {
            return placed;
        }
        BlockPos second = separated.get(random.nextInt(separated.size()));
        pour(level, second, index);
        placed.add(second);
        return placed;
    }

    private static void pour(ServerLevel level, BlockPos pos, PlayerPlacedBlocks index) {
        clearPlant(level, pos.above(), index);
        set(level, pos, Blocks.WATER.defaultBlockState());
        index.remove(pos);
    }

    private static void placeTall(ServerLevel level, BlockPos above, PlayerPlacedBlocks index, Set<BlockPos> planted) {
        BlockPos upper = above.above();
        if (!level.isInWorldBounds(upper) || !level.isLoaded(upper) || !plantSpace(level, upper)) {
            placeSmall(level, above, Blocks.SHORT_GRASS, index, planted);
            return;
        }
        clearPlant(level, above, index);
        clearPlant(level, upper, index);
        DoublePlantBlock.placeAt(level, Blocks.TALL_GRASS.defaultBlockState(), above, FLAGS);
        planted.add(above);
        planted.add(upper);
    }

    private static void placeSmall(ServerLevel level, BlockPos pos, Block block, PlayerPlacedBlocks index, Set<BlockPos> planted) {
        clearPlant(level, pos, index);
        set(level, pos, block.defaultBlockState());
        planted.add(pos);
    }

    private static void clearPlant(ServerLevel level, BlockPos pos, PlayerPlacedBlocks index) {
        if (!level.isInWorldBounds(pos) || !level.isLoaded(pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.TALL_GRASS)) {
            BlockPos lower = state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            BlockPos upper = lower.above();
            if (level.getBlockState(lower).is(Blocks.TALL_GRASS)) {
                set(level, lower, Blocks.AIR.defaultBlockState());
                index.remove(lower);
            }
            if (level.isInWorldBounds(upper) && level.getBlockState(upper).is(Blocks.TALL_GRASS)) {
                set(level, upper, Blocks.AIR.defaultBlockState());
                index.remove(upper);
            }
            return;
        }
        if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.POPPY) || state.is(Blocks.DANDELION)) {
            set(level, pos, Blocks.AIR.defaultBlockState());
            index.remove(pos);
        }
    }

    private static boolean plantSpace(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.POPPY)
                || state.is(Blocks.DANDELION);
    }

    private static BlockState shape(Block block, RandomSource random) {
        Block slab = form(block, "slab");
        Block stair = form(block, "stairs");
        if (slab == null && stair == null) {
            return null;
        }
        Block chosen;
        if (slab != null && stair != null) {
            chosen = random.nextBoolean() ? slab : stair;
        } else {
            chosen = slab != null ? slab : stair;
        }
        BlockState state = chosen.defaultBlockState();
        if (state.hasProperty(StairBlock.FACING)) {
            return state
                    .setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
                    .setValue(StairBlock.HALF, random.nextBoolean() ? Half.TOP : Half.BOTTOM)
                    .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT)
                    .setValue(StairBlock.WATERLOGGED, false);
        }
        if (state.hasProperty(SlabBlock.TYPE)) {
            return state
                    .setValue(SlabBlock.TYPE, random.nextBoolean() ? SlabType.TOP : SlabType.BOTTOM)
                    .setValue(SlabBlock.WATERLOGGED, false);
        }
        return null;
    }

    private static Block form(Block block, String suffix) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null) {
            return null;
        }
        for (String stem : stems(id.getPath())) {
            Identifier candidate = Identifier.fromNamespaceAndPath(id.getNamespace(), stem + "_" + suffix);
            if (BuiltInRegistries.BLOCK.containsKey(candidate)) {
                return BuiltInRegistries.BLOCK.getValue(candidate);
            }
        }
        return null;
    }

    private static List<String> stems(String path) {
        List<String> stems = new ArrayList<>();
        stems.add(path);
        if (path.endsWith("_planks")) {
            stems.add(path.substring(0, path.length() - "_planks".length()));
        }
        if (path.endsWith("_block")) {
            stems.add(path.substring(0, path.length() - "_block".length()));
        }
        if (path.endsWith("_bricks")) {
            stems.add(path.substring(0, path.length() - "s".length()));
        } else if (path.equals("bricks")) {
            stems.add("brick");
        }
        return stems;
    }

    private static BlockState withSharedProperties(BlockState from, Block toBlock) {
        BlockState to = toBlock.defaultBlockState();
        for (Property<?> property : from.getProperties()) {
            to = copyProperty(from, to, property);
        }
        return to;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        if (to.hasProperty(property)) {
            return to.setValue(property, from.getValue(property));
        }
        return to;
    }

    private static void remember(PlayerPlacedBlocks index, BlockPos pos, BlockState state) {
        if (!index.contains(pos)) {
            return;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null) {
            index.remove(pos);
        } else {
            index.record(pos, id.toString());
        }
    }

    private static void set(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) != null) {
            level.removeBlockEntity(pos);
        }
        level.setBlock(pos, state, FLAGS);
    }

    private static boolean unbreakable(ServerLevel level, BlockPos pos, BlockState state) {
        return state.getDestroySpeed(level, pos) < 0.0F;
    }

    /**
     * Terrain and the plants that grow on it. Built blocks, such as planks, bricks, and cobblestone, are not natural.
     */
    private static boolean natural(BlockState state) {
        if (state.isAir() || state.getBlock() instanceof LiquidBlock) {
            return true;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id != null && id.getPath().contains("infested")) {
            return true;
        }
        if (state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(BlockTags.ORES)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS)
                || state.is(BlockTags.NYLIUM)
                || state.is(BlockTags.ICE)
                || state.is(BlockTags.SNOW)
                || state.is(BlockTags.CORALS)
                || state.is(BlockTags.CORAL_BLOCKS)
                || state.is(BlockTags.CORAL_PLANTS)
                || state.is(BlockTags.BADLANDS_TERRACOTTA)
                || state.is(BlockTags.REPLACEABLE)
                || state.is(BlockTags.OVERWORLD_NATURAL_LOGS)) {
            return true;
        }
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.DIRT_PATH)
                || state.is(Blocks.FARMLAND)
                || state.is(Blocks.MUD)
                || state.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.PALE_MOSS_BLOCK)
                || state.is(Blocks.MOSS_CARPET)
                || state.is(Blocks.PALE_MOSS_CARPET)
                || state.is(Blocks.CALCITE)
                || state.is(Blocks.DRIPSTONE_BLOCK)
                || state.is(Blocks.POINTED_DRIPSTONE)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.END_STONE)
                || state.is(Blocks.OBSIDIAN)
                || state.is(Blocks.CRYING_OBSIDIAN)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.SOUL_SAND)
                || state.is(Blocks.SOUL_SOIL)
                || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.BUDDING_AMETHYST)
                || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.SCULK)
                || state.is(Blocks.SCULK_VEIN)
                || state.is(Blocks.SCULK_CATALYST)
                || state.is(Blocks.SCULK_SENSOR)
                || state.is(Blocks.SCULK_SHRIEKER)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.VINE)
                || state.is(Blocks.GLOW_LICHEN)
                || state.is(Blocks.HANGING_ROOTS)
                || state.is(Blocks.SPORE_BLOSSOM)
                || state.is(Blocks.BAMBOO)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.CACTUS_FLOWER)
                || state.is(Blocks.KELP)
                || state.is(Blocks.KELP_PLANT)
                || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.TALL_SEAGRASS)
                || state.is(Blocks.BROWN_MUSHROOM)
                || state.is(Blocks.RED_MUSHROOM)
                || state.is(Blocks.MUSHROOM_STEM)
                || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                || state.is(Blocks.RED_MUSHROOM_BLOCK)
                || state.is(Blocks.NETHER_WART)
                || state.is(Blocks.CRIMSON_ROOTS)
                || state.is(Blocks.WARPED_ROOTS)
                || state.is(Blocks.NETHER_SPROUTS)
                || state.is(Blocks.WEEPING_VINES)
                || state.is(Blocks.WEEPING_VINES_PLANT)
                || state.is(Blocks.TWISTING_VINES)
                || state.is(Blocks.TWISTING_VINES_PLANT)
                || state.is(Blocks.CAVE_VINES)
                || state.is(Blocks.CAVE_VINES_PLANT)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.PUMPKIN)
                || state.is(Blocks.MELON);
    }

    private static boolean inside(BlockPos pos, double x, double y, double z) {
        double dx = pos.getX() + 0.5 - x;
        double dy = pos.getY() + 0.5 - y;
        double dz = pos.getZ() + 0.5 - z;
        return dx * dx + dy * dy + dz * dz <= RADIUS * RADIUS;
    }

    private static boolean touches(BlockPos a, BlockPos b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());
        return Math.max(dx, Math.max(dy, dz)) <= 1;
    }

    private record VineFace(BlockPos air, Direction attach) {
    }

    private record ItemSweep(ServerLevel level, Vec3 center, long at) {
    }
}
