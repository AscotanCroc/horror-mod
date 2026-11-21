package com.horrormod.entity;

import com.horrormod.network.ImpuritySignalPayload;
import com.horrormod.sound.HorrorSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

/**
 * A carried or dropped impurity detector listens for reverse mobs within {@link #RANGE} blocks.
 * The nearest one sets how loud and how often the static plays.
 */
public final class ImpurityDetector {
    public static final double RANGE = 300.0;
    private static final long SCAN_INTERVAL = 10L;
    /** Quiet cut, then a long wait, when the mob is at the edge of range. */
    private static final float FAR_GAP = 20.0F * 12.0F;
    /** Retriggers the short cut so it sounds continuous beside the mob. */
    private static final float NEAR_GAP = 8.0F;

    private static final Map<UUID, Long> nextSound = new HashMap<>();
    private static final Set<UUID> told = new HashSet<>();
    private static final Map<String, List<Heard>> byLevel = new HashMap<>();

    private ImpurityDetector() {
    }

    public static void initialize() {
        ServerTickEvents.END_LEVEL_TICK.register(ImpurityDetector::tick);
    }

    private static void tick(ServerLevel level) {
        long time = level.getGameTime();
        if (time % SCAN_INTERVAL == 0L) {
            scan(level);
        }
        playDue(level, time);
    }

    private static void scan(ServerLevel level) {
        List<Vec3> mobs = new ArrayList<>();
        List<ItemEntity> dropped = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (!entity.isAlive()) {
                continue;
            }
            if (HorrorEntities.isReverse(entity)) {
                mobs.add(entity.getBoundingBox().getCenter());
            } else if (entity instanceof ItemEntity item && item.getItem().is(HorrorEntities.IMPURITY_DETECTOR)) {
                dropped.add(item);
            }
        }

        List<Heard> heard = new ArrayList<>();
        Set<UUID> carrying = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            if (!carrying(player)) {
                continue;
            }
            carrying.add(player.getUUID());
            Vec3 origin = player.position();
            Vec3 mob = nearest(mobs, origin);
            if (mob == null) {
                if (told.remove(player.getUUID())) {
                    ServerPlayNetworking.send(player, ImpuritySignalPayload.NONE);
                }
                continue;
            }
            heard.add(new Heard(player.getUUID(), player.getX(), player.getEyeY(), player.getZ(), origin.distanceTo(mob)));
            ServerPlayNetworking.send(player, new ImpuritySignalPayload(true, mob.x, mob.y, mob.z));
            told.add(player.getUUID());
        }
        for (UUID id : new ArrayList<>(told)) {
            if (carrying.contains(id)) {
                continue;
            }
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null && player.level() == level) {
                ServerPlayNetworking.send(player, ImpuritySignalPayload.NONE);
            }
            told.remove(id);
        }
        for (ItemEntity item : dropped) {
            Vec3 origin = item.position();
            Vec3 mob = nearest(mobs, origin);
            if (mob == null) {
                continue;
            }
            heard.add(new Heard(item.getUUID(), origin.x, origin.y, origin.z, origin.distanceTo(mob)));
        }
        byLevel.put(level.dimension().identifier().toString(), heard);
    }

    private static void playDue(ServerLevel level, long time) {
        List<Heard> sources = byLevel.get(level.dimension().identifier().toString());
        if (sources == null) {
            return;
        }
        for (Heard source : sources) {
            long due = nextSound.getOrDefault(source.id, time);
            if (time < due) {
                continue;
            }
            float shaped = shaped(source.distance);
            float volume = Mth.lerp(shaped, 0.06F, 1.0F);
            level.playSound(
                    null,
                    source.x,
                    source.y,
                    source.z,
                    HorrorSounds.IMPURITY_STATIC,
                    SoundSource.AMBIENT,
                    volume,
                    1.0F
            );
            int gap = Math.round(Mth.lerp(shaped, FAR_GAP, NEAR_GAP));
            nextSound.put(source.id, time + Math.max(1, gap));
        }
    }

    private static boolean carrying(ServerPlayer player) {
        if (player.getOffhandItem().is(HorrorEntities.IMPURITY_DETECTOR)) {
            return true;
        }
        return player.getInventory().contains(stack -> stack.is(HorrorEntities.IMPURITY_DETECTOR));
    }

    private static Vec3 nearest(List<Vec3> mobs, Vec3 origin) {
        Vec3 best = null;
        double bestDistance = RANGE;
        for (Vec3 mob : mobs) {
            double distance = origin.distanceTo(mob);
            if (distance <= bestDistance) {
                best = mob;
                bestDistance = distance;
            }
        }
        return best;
    }

    /** Squared so the far end stays faint and the last stretch is the loud one. */
    private static float shaped(double distance) {
        float closeness = (float) (1.0 - Mth.clamp(distance / RANGE, 0.0, 1.0));
        return closeness * closeness;
    }

    private record Heard(UUID id, double x, double y, double z, double distance) {
    }
}
