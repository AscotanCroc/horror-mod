package com.horrormod.entity;

import com.horrormod.HorrorMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Which reverse mobs are allowed to appear.
 * A killed kind stays out until all five have been killed. The same kind cannot
 * appear twice in a row until some reverse mob has been seen or has aggroed a player.
 */
public final class ReverseRoster extends SavedData {
    public static final Codec<ReverseRoster> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("killed", List.of()).forGetter(ReverseRoster::killed),
            Codec.STRING.optionalFieldOf("locked", "").forGetter(roster -> roster.locked)
    ).apply(instance, ReverseRoster::new));

    public static final SavedDataType<ReverseRoster> TYPE = new SavedDataType<>(
            HorrorMod.id("reverse_roster"),
            ReverseRoster::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Set<String> killed = new HashSet<>();
    private String locked = "";

    public ReverseRoster() {
    }

    private ReverseRoster(List<String> killed, String locked) {
        this.killed.addAll(killed);
        this.locked = locked == null ? "" : locked;
    }

    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (HorrorEntities.isReverse(entity) && entity.level() instanceof ServerLevel level) {
                get(level).killed(entity.getType());
            }
        });
    }

    public static ReverseRoster get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static boolean canSpawn(ServerLevelAccessor level, EntityType<?> type) {
        if (!(level.getLevel() instanceof ServerLevel server)) {
            return true;
        }
        return get(server).allows(type);
    }

    public static void noteSpawn(ServerLevelAccessor level, EntityType<?> type) {
        if (level.getLevel() instanceof ServerLevel server) {
            get(server).lock(type);
        }
    }

    /** A reverse mob was seen, or it aggroed a player, so the same kind may spawn again. */
    public static void noteMet(ServerLevel level) {
        get(level).unlock();
    }

    private List<String> killed() {
        return List.copyOf(this.killed);
    }

    private boolean allows(EntityType<?> type) {
        String id = key(type);
        if (id.isEmpty() || this.killed.contains(id)) {
            return false;
        }
        if (this.locked.isEmpty() || !this.locked.equals(id)) {
            return true;
        }
        for (EntityType<?> reverse : HorrorEntities.reverseMonsters()) {
            String other = key(reverse);
            if (!other.equals(id) && !this.killed.contains(other)) {
                return false;
            }
        }
        return true;
    }

    private void lock(EntityType<?> type) {
        String id = key(type);
        if (id.isEmpty() || id.equals(this.locked)) {
            return;
        }
        this.locked = id;
        setDirty();
    }

    private void unlock() {
        if (this.locked.isEmpty()) {
            return;
        }
        this.locked = "";
        setDirty();
    }

    private void killed(EntityType<?> type) {
        String id = key(type);
        if (id.isEmpty() || !this.killed.add(id)) {
            return;
        }
        if (this.killed.size() >= HorrorEntities.reverseMonsters().size()) {
            this.killed.clear();
        }
        this.locked = "";
        setDirty();
    }

    private static String key(EntityType<?> type) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return id == null ? "" : id.toString();
    }
}
