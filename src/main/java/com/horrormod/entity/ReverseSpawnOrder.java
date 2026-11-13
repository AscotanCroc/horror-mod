package com.horrormod.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/**
 * Which reverse monster may appear.
 * Carrying that monster's drop keeps it out. The lock lifts when every drop is being
 * carried, and when a player is carrying the veil. Each allowed kind is an equal pick.
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
        boolean open = itemLockLifted(level);
        List<Kind> eligible = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            if (!open && carrying(level, kind.drop)) {
                continue;
            }
            eligible.add(kind);
        }
        return eligible;
    }

    /**
     * True when the carried drops should no longer keep their monsters out.
     */
    private static boolean itemLockLifted(ServerLevel level) {
        if (carrying(level, HorrorEntities.VEIL)) {
            return true;
        }
        for (Kind kind : Kind.values()) {
            if (!carrying(level, kind.drop)) {
                return false;
            }
        }
        return true;
    }

    private static boolean carrying(ServerLevel level, Item item) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.getInventory().contains(stack -> stack.is(item))) {
                return true;
            }
        }
        return false;
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
