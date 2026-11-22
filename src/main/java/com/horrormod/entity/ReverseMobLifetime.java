package com.horrormod.entity;

import com.horrormod.event.HorrorEventActions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * A reverse mob that has never been seen and never aggroed a player is removed after two minutes.
 * Either event, once, keeps it and lets that kind spawn again.
 */
public final class ReverseMobLifetime {
    private static final long UNSEEN_TICKS = 20L * 60L * 2L;
    private static ReverseMobLifetime active;
    private final Map<UUID, Long> appeared = new HashMap<>();
    private final Set<UUID> met = new HashSet<>();

    private ReverseMobLifetime() {
    }

    public static void initialize() {
        active = new ReverseMobLifetime();
        ServerTickEvents.END_LEVEL_TICK.register(active::tick);
    }

    private void tick(ServerLevel level) {
        if (level.getGameTime() % 20L != 0L) {
            return;
        }
        long now = level.getGameTime();
        List<Entity> leaving = new ArrayList<>();
        Set<UUID> alive = new HashSet<>();
        for (Entity entity : level.getAllEntities()) {
            if (!entity.isAlive() || !HorrorEntities.isReverse(entity)) {
                continue;
            }
            UUID id = entity.getUUID();
            alive.add(id);
            if (engaged(level, entity)) {
                if (this.met.add(id)) {
                    ReverseRoster.noteMet(level);
                }
                continue;
            }
            if (this.met.contains(id)) {
                continue;
            }
            long start = this.appeared.computeIfAbsent(id, ignored -> now);
            if (now - start >= UNSEEN_TICKS) {
                leaving.add(entity);
            }
        }
        this.appeared.keySet().retainAll(alive);
        this.met.retainAll(alive);
        for (Entity entity : leaving) {
            this.appeared.remove(entity.getUUID());
            entity.discard();
        }
    }

    private static boolean engaged(ServerLevel level, Entity entity) {
        if (aggroedPlayer(entity)) {
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

    private static boolean aggroedPlayer(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return false;
        }
        return mob.getTarget() instanceof Player player && player.isAlive();
    }
}
