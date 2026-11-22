package com.horrormod.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/**
 * Drop and size for each reverse monster.
 * Which kind may appear is {@link ReverseRoster}.
 */
public final class ReverseSpawnOrder {
    private ReverseSpawnOrder() {
    }

    public static boolean permits(ServerLevel level, EntityType<?> type) {
        Kind kind = Kind.of(type);
        if (kind == null) {
            return true;
        }
        return candidates(level).contains(kind);
    }

    public static Kind pick(ServerLevel level, RandomSource random) {
        List<Kind> candidates = candidates(level);
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private static List<Kind> candidates(ServerLevel level) {
        List<Kind> eligible = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            if (!ReverseRoster.canSpawn(level, kind.type)) {
                continue;
            }
            eligible.add(kind);
        }
        return eligible;
    }

    public enum Kind {
        ZOMBIE(HorrorEntities.REVERSE_ZOMBIE, ReverseZombie.class, 2, HorrorEntities.GLITCH),
        ENDERMAN(HorrorEntities.REVERSE_ENDERMAN, ReverseEnderman.class, 3, HorrorEntities.NULL_POINTER),
        CREEPER(HorrorEntities.REVERSE_CREEPER, ReverseCreeper.class, 2, HorrorEntities.SAVIOR),
        SKELETON(HorrorEntities.REVERSE_SKELETON, ReverseSkeleton.class, 2, HorrorEntities.DISAPPEARANCE),
        SPIDER(HorrorEntities.REVERSE_SPIDER, ReverseSpider.class, 1, HorrorEntities.EYE);

        private final EntityType<?> type;
        private final Class<? extends Entity> entityClass;
        private final int airBlocks;
        private final Item drop;

        Kind(EntityType<?> type, Class<? extends Entity> entityClass, int airBlocks, Item drop) {
            this.type = type;
            this.entityClass = entityClass;
            this.airBlocks = airBlocks;
            this.drop = drop;
        }

        public EntityType<?> type() {
            return this.type;
        }

        public Class<? extends Entity> entityClass() {
            return this.entityClass;
        }

        public int airBlocks() {
            return this.airBlocks;
        }

        public Item drop() {
            return this.drop;
        }

        public static Kind of(EntityType<?> type) {
            for (Kind kind : values()) {
                if (kind.type == type) {
                    return kind;
                }
            }
            return null;
        }
    }
}
