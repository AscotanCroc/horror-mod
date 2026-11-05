package com.horrormod.world;

import com.horrormod.HorrorMod;
import com.horrormod.content.HorrorBlocks;
import com.horrormod.content.StoneLecternBlock;
import com.horrormod.entity.HorrorEntities;
import com.horrormod.network.VeilRitualPayload;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Charges lecterns for players carrying [veil, then holds them through the trip into the veil.
 */
public final class VeilPassage {
    /** Ticks of black before the level change, so the loading screen opens under it. */
    private static final int TELEPORT_DELAY = 15;
    /** Matches the client's reveal plus its hold, with a little slack after the picture cuts. */
    private static final int FREEZE_TICKS = 220;
    private static final int SCAN_INTERVAL = 5;

    private static final Map<UUID, Integer> departures = new HashMap<>();
    private static final Map<UUID, Integer> frozen = new HashMap<>();
    private static final Map<UUID, Boolean> invulnerable = new HashMap<>();
    private static final Set<LecternRef> charged = new HashSet<>();
    private static int scanCooldown;

    private VeilPassage() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(VeilPassage::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> release(handler.getPlayer()));
    }

    public static void begin(ServerPlayer player) {
        UUID id = player.getUUID();
        if (departures.containsKey(id) || frozen.containsKey(id) || HorrorDimensions.VEIL.equals(player.level().dimension())) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        MinecraftServer server = level.getServer();
        if (server.getLevel(HorrorDimensions.VEIL) == null) {
            HorrorMod.LOGGER.warn("Veil dimension {} is not loaded", HorrorDimensions.VEIL);
            return;
        }
        invulnerable.putIfAbsent(id, player.isPermanentlyInvulnerable());
        player.setPermanentlyInvulnerable(true);
        departures.put(id, TELEPORT_DELAY);
        frozen.put(id, FREEZE_TICKS);
        ServerPlayNetworking.send(player, VeilRitualPayload.INSTANCE);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            hold(player);
            Integer wait = departures.get(player.getUUID());
            if (wait == null) {
                continue;
            }
            if (wait > 1) {
                departures.put(player.getUUID(), wait - 1);
                continue;
            }
            departures.remove(player.getUUID());
            depart(player);
        }
        if (--scanCooldown > 0) {
            return;
        }
        scanCooldown = SCAN_INTERVAL;
        chargeLecterns(server);
    }

    private static void depart(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        ServerLevel destination = level.getServer().getLevel(HorrorDimensions.VEIL);
        if (destination == null) {
            HorrorMod.LOGGER.warn("Veil dimension {} is not loaded", HorrorDimensions.VEIL);
            return;
        }
        player.teleport(new TeleportTransition(
                destination,
                new Vec3(0.5, 2.0, 0.5),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                TeleportTransition.DO_NOTHING
        ));
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static void hold(ServerPlayer player) {
        Integer ticks = frozen.get(player.getUUID());
        if (ticks == null) {
            return;
        }
        if (ticks <= 1) {
            frozen.remove(player.getUUID());
            Boolean previous = invulnerable.remove(player.getUUID());
            if (previous != null) {
                player.setPermanentlyInvulnerable(previous);
            }
            return;
        }
        frozen.put(player.getUUID(), ticks - 1);
        player.setLastClientInput(Input.EMPTY);
        player.setDeltaMovement(Vec3.ZERO);
        player.setSprinting(false);
        player.setJumping(false);
        player.setShiftKeyDown(false);
        player.resetFallDistance();
    }

    private static void chargeLecterns(MinecraftServer server) {
        Set<LecternRef> present = new HashSet<>();
        Set<LecternRef> desired = new HashSet<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                boolean carries = player.getInventory().contains(stack -> stack.is(HorrorEntities.VEIL));
                StoneLecternBlock.nearby(level, player.blockPosition(), pos -> {
                    LecternRef ref = new LecternRef(level.dimension(), pos);
                    present.add(ref);
                    if (carries) {
                        desired.add(ref);
                    }
                });
            }
        }
        Set<LecternRef> next = new HashSet<>();
        Set<LecternRef> seen = new HashSet<>(charged);
        seen.addAll(present);
        for (LecternRef ref : seen) {
            ServerLevel level = server.getLevel(ref.dimension());
            if (level == null || !level.isLoaded(ref.pos())) {
                if (charged.contains(ref)) {
                    next.add(ref);
                }
                continue;
            }
            BlockState state = level.getBlockState(ref.pos());
            if (!state.is(HorrorBlocks.STONE_LECTERN)) {
                continue;
            }
            boolean want = desired.contains(ref);
            if (state.getValue(StoneLecternBlock.CHARGED) != want) {
                level.setBlock(ref.pos(), state.setValue(StoneLecternBlock.CHARGED, want), Block.UPDATE_ALL);
            }
            if (want) {
                next.add(ref);
            }
        }
        charged.clear();
        charged.addAll(next);
    }

    private static void release(ServerPlayer player) {
        departures.remove(player.getUUID());
        frozen.remove(player.getUUID());
        Boolean previous = invulnerable.remove(player.getUUID());
        if (previous != null) {
            player.setPermanentlyInvulnerable(previous);
        }
    }

    private record LecternRef(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
