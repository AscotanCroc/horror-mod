package com.horrormod.entity;

import com.horrormod.HorrorMod;
import com.horrormod.mixin.SpawnPlacementsInvoker;
import java.util.List;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import com.horrormod.content.EyeItem;
import com.horrormod.content.NullPointerItem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.levelgen.Heightmap;

public final class HorrorEntities {
    public static final ResourceKey<EntityType<?>> REVERSE_ZOMBIE_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            HorrorMod.id("reverse_zombie")
    );
    public static final ResourceKey<Item> REVERSE_ZOMBIE_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            HorrorMod.id("reverse_zombie_spawn_egg")
    );

    public static final EntityType<ReverseZombie> REVERSE_ZOMBIE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            REVERSE_ZOMBIE_KEY,
            EntityType.Builder.of(ReverseZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .passengerAttachments(2.0125F)
                    .ridingOffset(-0.7F)
                    .clientTrackingRange(8)
                    .notInPeaceful()
                    .build(REVERSE_ZOMBIE_KEY)
    );

    public static final ResourceKey<EntityType<?>> REVERSE_ENDERMAN_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            HorrorMod.id("reverse_enderman")
    );
    public static final ResourceKey<Item> REVERSE_ENDERMAN_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            HorrorMod.id("reverse_enderman_spawn_egg")
    );
    public static final ResourceKey<Item> NULL_POINTER_KEY = ResourceKey.create(
            Registries.ITEM,
            HorrorMod.id("null_pointer")
    );

    public static final EntityType<ReverseEnderman> REVERSE_ENDERMAN = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            REVERSE_ENDERMAN_KEY,
            EntityType.Builder.of(ReverseEnderman::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.9F)
                    .eyeHeight(2.55F)
                    .passengerAttachments(2.80625F)
                    .clientTrackingRange(8)
                    .notInPeaceful()
                    .build(REVERSE_ENDERMAN_KEY)
    );

    public static final ResourceKey<EntityType<?>> REVERSE_CREEPER_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            HorrorMod.id("reverse_creeper")
    );
    public static final ResourceKey<Item> REVERSE_CREEPER_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            HorrorMod.id("reverse_creeper_spawn_egg")
    );

    public static final EntityType<ReverseCreeper> REVERSE_CREEPER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            REVERSE_CREEPER_KEY,
            EntityType.Builder.of(ReverseCreeper::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.7F)
                    .eyeHeight(1.445F)
                    .clientTrackingRange(8)
                    .notInPeaceful()
                    .build(REVERSE_CREEPER_KEY)
    );

    public static final Item REVERSE_CREEPER_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            REVERSE_CREEPER_SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().setId(REVERSE_CREEPER_SPAWN_EGG_KEY).spawnEgg(REVERSE_CREEPER))
    );

    public static final ResourceKey<Item> SAVIOR_KEY = ResourceKey.create(Registries.ITEM, HorrorMod.id("savior"));

    public static final Item SAVIOR = Registry.register(
            BuiltInRegistries.ITEM,
            SAVIOR_KEY,
            new Item(new Item.Properties().setId(SAVIOR_KEY))
    );

    public static final ResourceKey<Item> VEIL_KEY = ResourceKey.create(Registries.ITEM, HorrorMod.id("veil"));

    public static final Item VEIL = Registry.register(
            BuiltInRegistries.ITEM,
            VEIL_KEY,
            new Item(new Item.Properties().setId(VEIL_KEY))
    );

    public static final ResourceKey<Item> DISSAPEARANCE_KEY = ResourceKey.create(Registries.ITEM, HorrorMod.id("dissapearance"));

    public static final Item DISSAPEARANCE = Registry.register(
            BuiltInRegistries.ITEM,
            DISSAPEARANCE_KEY,
            new Item(new Item.Properties().setId(DISSAPEARANCE_KEY))
    );

    public static final ResourceKey<Item> EYE_KEY = ResourceKey.create(Registries.ITEM, HorrorMod.id("eye"));

    public static final Item EYE = Registry.register(
            BuiltInRegistries.ITEM,
            EYE_KEY,
            new EyeItem(new Item.Properties().setId(EYE_KEY))
    );

    /** Crafted from the five reverse drops, then used in place of savior for the veil. */
    public static final ResourceKey<Item> FIVE_KEY = ResourceKey.create(Registries.ITEM, HorrorMod.id("five"));

    public static final Item FIVE = Registry.register(
            BuiltInRegistries.ITEM,
            FIVE_KEY,
            new Item(new Item.Properties().setId(FIVE_KEY))
    );

    public static final EntityType<NullClone> NULL_CLONE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceKey.create(Registries.ENTITY_TYPE, HorrorMod.id("null_clone")),
            EntityType.Builder.of(NullClone::create, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .eyeHeight(1.62F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, HorrorMod.id("null_clone")))
    );

    public static final EntityType<ThrownNullPointer> THROWN_NULL_POINTER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceKey.create(Registries.ENTITY_TYPE, HorrorMod.id("thrown_null_pointer")),
            EntityType.Builder.<ThrownNullPointer>of(ThrownNullPointer::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, HorrorMod.id("thrown_null_pointer")))
    );

    public static final Item NULL_POINTER = Registry.register(
            BuiltInRegistries.ITEM,
            NULL_POINTER_KEY,
            new NullPointerItem(new Item.Properties().setId(NULL_POINTER_KEY).useCooldown(1.0F))
    );

    public static final Item REVERSE_ENDERMAN_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            REVERSE_ENDERMAN_SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().setId(REVERSE_ENDERMAN_SPAWN_EGG_KEY).spawnEgg(REVERSE_ENDERMAN))
    );

    public static final ResourceKey<Item> GLITCH_KEY = ResourceKey.create(Registries.ITEM, HorrorMod.id("glitch"));

    public static final ResourceKey<Item> ERROR_ICON_KEY = ResourceKey.create(Registries.ITEM, HorrorMod.id("error_icon"));

    public static final Item ERROR_ICON = Registry.register(
            BuiltInRegistries.ITEM,
            ERROR_ICON_KEY,
            new Item(new Item.Properties().setId(ERROR_ICON_KEY))
    );

    public static final Item GLITCH = Registry.register(
            BuiltInRegistries.ITEM,
            GLITCH_KEY,
            new Item(new Item.Properties()
                    .setId(GLITCH_KEY)
                    .food(
                            new FoodProperties(20, 20.0F, true),
                            Consumable.builder()
                                    .onConsume(new ApplyStatusEffectsConsumeEffect(
                                            new MobEffectInstance(MobEffects.WEAKNESS, 76 * 20, 179)
                                    ))
                                    .build()
                    ))
    );

    public static final Item REVERSE_ZOMBIE_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            REVERSE_ZOMBIE_SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().setId(REVERSE_ZOMBIE_SPAWN_EGG_KEY).spawnEgg(REVERSE_ZOMBIE))
    );

    public static final ResourceKey<EntityType<?>> WATDATMEAN_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            HorrorMod.id("watdatmean")
    );
    public static final ResourceKey<Item> WATDATMEAN_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            HorrorMod.id("watdatmean_spawn_egg")
    );

    public static final EntityType<Watdatmean> WATDATMEAN = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            WATDATMEAN_KEY,
            EntityType.Builder.of(Watdatmean::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.9F)
                    .eyeHeight(0.55F)
                    .clientTrackingRange(8)
                    .notInPeaceful()
                    .build(WATDATMEAN_KEY)
    );

    public static final Item WATDATMEAN_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            WATDATMEAN_SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().setId(WATDATMEAN_SPAWN_EGG_KEY).spawnEgg(WATDATMEAN))
    );

    public static final ResourceKey<EntityType<?>> REVERSE_SKELETON_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            HorrorMod.id("reverse_skeleton")
    );
    public static final ResourceKey<Item> REVERSE_SKELETON_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            HorrorMod.id("reverse_skeleton_spawn_egg")
    );

    public static final EntityType<ReverseSkeleton> REVERSE_SKELETON = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            REVERSE_SKELETON_KEY,
            EntityType.Builder.of(ReverseSkeleton::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.99F)
                    .eyeHeight(1.74F)
                    .ridingOffset(-0.7F)
                    .clientTrackingRange(8)
                    .notInPeaceful()
                    .build(REVERSE_SKELETON_KEY)
    );

    public static final Item REVERSE_SKELETON_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            REVERSE_SKELETON_SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().setId(REVERSE_SKELETON_SPAWN_EGG_KEY).spawnEgg(REVERSE_SKELETON))
    );

    public static final ResourceKey<EntityType<?>> REVERSE_SPIDER_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            HorrorMod.id("reverse_spider")
    );
    public static final ResourceKey<Item> REVERSE_SPIDER_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            HorrorMod.id("reverse_spider_spawn_egg")
    );

    public static final EntityType<ReverseSpider> REVERSE_SPIDER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            REVERSE_SPIDER_KEY,
            EntityType.Builder.of(ReverseSpider::new, MobCategory.MONSTER)
                    .sized(1.4F, 0.9F)
                    .eyeHeight(0.65F)
                    .clientTrackingRange(8)
                    .notInPeaceful()
                    .build(REVERSE_SPIDER_KEY)
    );

    public static final Item REVERSE_SPIDER_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            REVERSE_SPIDER_SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().setId(REVERSE_SPIDER_SPAWN_EGG_KEY).spawnEgg(REVERSE_SPIDER))
    );

    public static final ResourceKey<EntityType<?>> GLITCH_SQUARE_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            HorrorMod.id("glitch_square")
    );

    public static final EntityType<GlitchSquare> GLITCH_SQUARE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            GLITCH_SQUARE_KEY,
            EntityType.Builder.<GlitchSquare>of(GlitchSquare::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(GLITCH_SQUARE_KEY)
    );

    private HorrorEntities() {
    }

    /**
     * Reverse monsters skipped by the hostile-despawn event and eligible to appear behind the player.
     * Add future reverse monsters here.
     */
    public static List<EntityType<? extends Mob>> reverseMonsters() {
        return List.of(REVERSE_ZOMBIE, REVERSE_ENDERMAN, REVERSE_CREEPER, REVERSE_SKELETON, REVERSE_SPIDER);
    }

    public static boolean isReverse(Entity entity) {
        return reverseMonsters().contains(entity.getType());
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(REVERSE_ZOMBIE, Zombie.createAttributes());
        SpawnPlacementsInvoker.horror$register(
                REVERSE_ZOMBIE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ReverseZombie::checkNaturalSpawn
        );
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, REVERSE_ZOMBIE, ReverseMonsterSpawns.WEIGHT, 1, 1);
        SpawnPlacementsInvoker.horror$register(
                REVERSE_ENDERMAN,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ReverseEnderman::checkNaturalSpawn
        );
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, REVERSE_ENDERMAN, ReverseMonsterSpawns.WEIGHT, 1, 1);
        FabricDefaultAttributeRegistry.register(REVERSE_CREEPER, Creeper.createAttributes());
        SpawnPlacementsInvoker.horror$register(
                REVERSE_CREEPER,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ReverseCreeper::checkNaturalSpawn
        );
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, REVERSE_CREEPER, ReverseMonsterSpawns.WEIGHT, 1, 1);
        ReverseCreeperBlackout.initialize();
        ReverseCreeperBurst.initialize();
        ReverseZombieNights.initialize();
        ReverseMobLifetime.initialize();
        FabricDefaultAttributeRegistry.register(REVERSE_ENDERMAN, Enderman.createAttributes());
        FabricDefaultAttributeRegistry.register(NULL_CLONE, Player.createAttributes());
        FabricDefaultAttributeRegistry.register(WATDATMEAN, Watdatmean.createAttributes());
        FabricDefaultAttributeRegistry.register(REVERSE_SKELETON, Skeleton.createAttributes());
        SpawnPlacementsInvoker.horror$register(
                REVERSE_SKELETON,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ReverseSkeleton::checkNaturalSpawn
        );
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, REVERSE_SKELETON, ReverseMonsterSpawns.WEIGHT, 1, 1);
        FabricDefaultAttributeRegistry.register(REVERSE_SPIDER, Spider.createAttributes());
        SpawnPlacementsInvoker.horror$register(
                REVERSE_SPIDER,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ReverseSpider::checkNaturalSpawn
        );
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, REVERSE_SPIDER, ReverseMonsterSpawns.WEIGHT, 1, 1);
        HorrorMod.LOGGER.info("Registered reverse zombie {}", REVERSE_ZOMBIE_KEY);
        HorrorMod.LOGGER.info("Registered reverse enderman {}", REVERSE_ENDERMAN_KEY);
        HorrorMod.LOGGER.info("Registered reverse creeper {}", REVERSE_CREEPER_KEY);
        HorrorMod.LOGGER.info("Registered reverse skeleton {}", REVERSE_SKELETON_KEY);
        HorrorMod.LOGGER.info("Registered reverse spider {}", REVERSE_SPIDER_KEY);
    }
}
