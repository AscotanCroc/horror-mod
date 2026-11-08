package com.horrormod.entity;

import com.horrormod.event.HorrorEventActions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * A reverse monster gives its slot back after six minutes off screen and without a target.
 * Being seen or aggroed starts that wait over.
 */
public final class ReverseMobLifetime {
    private static final long QUIET_TICKS = 20L * 60L * 6L;
    private final Map<UUID, Long> lastEngaged = new HashMap<>();

    private ReverseMobLifetime() {
    }

    public static void initialize() {
        ReverseMobLifetime lifetime = new ReverseMobLifetime();
        ServerTickEvents.END_LEVEL_TICK.register(lifetime::tick);
    }

    private void tick(ServerLevel level) {
        if (level.getGameTime() % 20L != 0L) {
            return;
        }
        long now = level.getGameTime();
        List<Entity> leaving = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (!entity.isAlive() || !HorrorEntities.isReverse(entity)) {
                continue;
            }
            if (engaged(level, entity)) {
                lastEngaged.put(entity.getUUID(), now);
                continue;
            }
            long since = lastEngaged.computeIfAbsent(entity.getUUID(), id -> now);
            if (now - since >= QUIET_TICKS) {
                leaving.add(entity);
            }
        }
        for (Entity entity : leaving) {
            lastEngaged.remove(entity.getUUID());
            entity.discard();
        }
    }

    private static boolean engaged(ServerLevel level, Entity entity) {
        if (aggroed(entity)) {
            return true;
        }
        Vec3 eyes = entity instanceof LivingEntity living ? living.getEyePosition() : entity.position();
        Vec3 feet = entity.position();
        for (ServerPlayer player : level.players()) {
            if (HorrorEventActions.onScreen(player, eyes) || HorrorEventActions.onScreen(player, feet)) {
                return true;
            }
        }
        return false;
    }

    private static boolean aggroed(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return false;
        }
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive();
    }
}
