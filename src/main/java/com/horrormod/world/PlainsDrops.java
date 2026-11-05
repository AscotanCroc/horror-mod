package com.horrormod.world;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

/**
 * Plains and sunflower plains break short grass into a seed, and oak leaves drop apples more often.
 */
public final class PlainsDrops {
    private static final ResourceKey<LootTable> SHORT_GRASS = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.withDefaultNamespace("blocks/short_grass")
    );
    private static final ResourceKey<LootTable> OAK_LEAVES = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.withDefaultNamespace("blocks/oak_leaves")
    );

    private PlainsDrops() {
    }

    public static void initialize() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin() || (!key.equals(SHORT_GRASS) && !key.equals(OAK_LEAVES))) {
                return;
            }
            var biomes = registries.lookupOrThrow(Registries.BIOME);
            HolderSet<Biome> plains = HolderSet.direct(
                    biomes.getOrThrow(Biomes.PLAINS),
                    biomes.getOrThrow(Biomes.SUNFLOWER_PLAINS)
            );
            if (key.equals(SHORT_GRASS)) {
                table.withPool(LootPool.lootPool()
                        .setRolls(one())
                        .when(inPlains(plains))
                        .add(LootItem.lootTableItem(Items.WHEAT_SEEDS)));
                return;
            }
            var predicates = registries.lookupOrThrow(Registries.PREDICATE);
            Holder<Enchantment> fortune = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
            LootItemCondition.Builder bareLeaves = InvertedLootItemCondition.invert(predicates.getOrThrow(LootPredicates.TOOL_CAN_SHEAR))
                    .and(InvertedLootItemCondition.invert(predicates.getOrThrow(LootPredicates.TOOL_CAN_SILK_TOUCH)));
            table.withPool(LootPool.lootPool()
                    .setRolls(one())
                    .when(inPlains(plains))
                    .when(bareLeaves)
                    .add(LootItem.lootTableItem(Items.APPLE)
                            .when(ExplosionCondition.survivesExplosion())
                            .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                    fortune,
                                    0.015F,
                                    0.016666668F,
                                    0.01875F,
                                    0.025F,
                                    0.075F
                            ))));
        });
    }

    private static LootItemCondition.Builder inPlains(HolderSet<Biome> plains) {
        return LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiomes(plains));
    }

    private static Holder<ContextIntProvider> one() {
        return Holder.direct(new ConstantValue(1));
    }
}
