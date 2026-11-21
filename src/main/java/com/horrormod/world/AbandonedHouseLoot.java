package com.horrormod.world;

import com.horrormod.HorrorMod;
import com.horrormod.entity.HorrorEntities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The abandoned house chest. The sheet is always present. Everything else is a chance.
 * Whatever appears is kept in list order, then dropped into random slots that still read
 * from the top left toward the bottom right.
 */
public final class AbandonedHouseLoot {
    public static final ResourceKey<LootTable> TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            HorrorMod.id("abandoned_house_chest")
    );

    private static final List<List<Item>> ORDER = List.of(
            List.of(Items.STONE_SWORD, Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_HOE, Items.STONE_SPEAR),
            List.of(Items.WOODEN_SWORD, Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE, Items.WOODEN_SPEAR),
            List.of(Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_SPEAR),
            List.of(Items.RAW_IRON),
            List.of(Items.IRON_INGOT),
            List.of(Items.COAL),
            List.of(Items.APPLE),
            List.of(Items.OAK_SAPLING),
            List.of(Items.STICK),
            List.of(Items.COBBLESTONE),
            List.of(Items.DIORITE),
            List.of(Items.ANDESITE),
            List.of(Items.GRANITE),
            List.of(Items.GRAVEL),
            List.of(Items.FLINT),
            List.of(Items.DIRT),
            List.of(Items.DIAMOND),
            List.of(Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_SPEAR),
            List.of(HorrorEntities.IMPURITY_SHEET),
            List.of(Items.ROTTEN_FLESH),
            List.of(Items.BONE),
            List.of(Items.SPIDER_EYE),
            List.of(Items.STRING),
            List.of(Items.GUNPOWDER),
            List.of(Items.ENDER_PEARL),
            List.of(HorrorEntities.GLITCH, HorrorEntities.NULL_POINTER, HorrorEntities.SAVIOR, HorrorEntities.DISAPPEARANCE, HorrorEntities.EYE),
            List.of(Items.SHIELD),
            List.of(Items.RAW_GOLD),
            List.of(Items.GOLD_INGOT),
            List.of(Items.RAW_COPPER),
            List.of(Items.COPPER_INGOT),
            List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS),
            List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS),
            List.of(Items.REDSTONE),
            List.of(Items.LAPIS_LAZULI),
            List.of(Items.OAK_PLANKS),
            List.of(Items.OAK_LOG),
            List.of(Items.SAND),
            List.of(Items.GLASS),
            List.of(Items.GLASS_PANE),
            List.of(Items.CLAY_BALL),
            List.of(Items.GRASS_BLOCK),
            List.of(Items.BUCKET),
            List.of(Items.TORCH),
            List.of(Items.OBSIDIAN),
            List.of(Items.WATER_BUCKET),
            List.of(Items.LAVA_BUCKET),
            List.of(Items.WHEAT_SEEDS)
    );

    private static final ThreadLocal<Boolean> PENDING = new ThreadLocal<>();

    private AbandonedHouseLoot() {
    }

    public static void note(ResourceKey<LootTable> table) {
        PENDING.set(TABLE.equals(table));
    }

    public static boolean consumeNote() {
        Boolean house = PENDING.get();
        PENDING.remove();
        return Boolean.TRUE.equals(house);
    }

    public static void arrange(Container container, RandomSource random) {
        List<ItemStack> found = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                wear(stack, random);
                found.add(stack);
                container.setItem(slot, ItemStack.EMPTY);
            }
        }
        found.sort((left, right) -> Integer.compare(order(left), order(right)));
        int count = Math.min(found.size(), container.getContainerSize());
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            slots.add(slot);
        }
        for (int index = 0; index < count; index++) {
            int swap = index + random.nextInt(slots.size() - index);
            Collections.swap(slots, index, swap);
        }
        List<Integer> chosen = new ArrayList<>(slots.subList(0, count));
        Collections.sort(chosen);
        for (int index = 0; index < count; index++) {
            container.setItem(chosen.get(index), found.get(index));
        }
    }

    private static int order(ItemStack stack) {
        Item item = stack.getItem();
        for (int index = 0; index < ORDER.size(); index++) {
            if (ORDER.get(index).contains(item)) {
                return index;
            }
        }
        return ORDER.size();
    }

    /** Four fifths of the time the piece is worn. The rest stay undamaged. */
    private static void wear(ItemStack stack, RandomSource random) {
        if (!stack.isDamageableItem() || random.nextFloat() < 0.2F) {
            return;
        }
        int max = stack.getMaxDamage();
        if (max <= 1) {
            return;
        }
        stack.setDamageValue(1 + random.nextInt(max - 1));
    }
}
