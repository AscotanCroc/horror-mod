package com.horrormod.entity;

import com.horrormod.network.BlackoutPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.phys.Vec3;

/**
 * Holds nearby players still for the reverse creeper's blackout and tells their clients to lock input.
 */
public final class ReverseCreeperBlackout {
    public static final int TICKS = 140;
    /** Seven real minutes, at the game's 20 ticks per second. */
    private static final int SKIP_TICKS = 7 * 60 * 20;

    private static final Map<UUID, Long> until = new HashMap<>();
    private static final Map<UUID, Boolean> wasInvulnerable = new HashMap<>();

    private ReverseCreeperBlackout() {
    }

    public static void initialize() {
        ServerTickEvents.START_LEVEL_TICK.register(ReverseCreeperBlackout::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> release(handler.getPlayer()));
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !(entity instanceof Player player && isFrozen(player)));
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> !frozen(player));
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> frozen(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        UseItemCallback.EVENT.register((player, level, hand) -> frozen(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> frozen(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> frozen(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> frozen(player) ? InteractionResult.FAIL : InteractionResult.PASS);
    }

    public static void strike(ServerLevel level, ReverseCreeper creeper) {
        skipTime(level);
        long end = level.getGameTime() + TICKS;
        double reach = ReverseCreeperBurst.RADIUS * ReverseCreeperBurst.RADIUS;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.distanceToSqr(creeper) > reach) {
                continue;
            }
            drainHunger(player);
            protect(player, end);
            ServerPlayNetworking.send(player, new BlackoutPayload(end));
        }
    }

    public static boolean isFrozen(Player player) {
        Long end = until.get(player.getUUID());
        if (end == null || player.level().getGameTime() >= end) {
            if (end != null) {
                release(player);
            }
            return false;
        }
        return true;
    }

    private static void protect(ServerPlayer player, long end) {
        wasInvulnerable.putIfAbsent(player.getUUID(), player.isPermanentlyInvulnerable());
        player.setPermanentlyInvulnerable(true);
        until.put(player.getUUID(), end);
    }

    private static void release(Player player) {
        until.remove(player.getUUID());
        Boolean previous = wasInvulnerable.remove(player.getUUID());
        if (previous != null) {
            player.setPermanentlyInvulnerable(previous);
        }
    }

    private static void skipTime(ServerLevel level) {
        if (!(level.clockManager() instanceof ServerClockManager clocks)) {
            return;
        }
        Holder<WorldClock> clock = level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD);
        clocks.addTicks(clock, SKIP_TICKS);
    }

    private static void drainHunger(ServerPlayer player) {
        FoodData food = player.getFoodData();
        int level = food.getFoodLevel();
        if (level <= 0) {
            return;
        }
        int next = Math.max(1, level - Math.round(level * 0.33F));
        food.setFoodLevel(next);
        if (food.getSaturationLevel() > next) {
            food.setSaturation(next);
        }
    }

    private static void tick(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            Long end = until.get(player.getUUID());
            if (end == null) {
                continue;
            }
            if (now >= end) {
                release(player);
                continue;
            }
            player.setLastClientInput(Input.EMPTY);
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(0.0, motion.y, 0.0);
            player.setSprinting(false);
            player.setJumping(false);
            player.setShiftKeyDown(false);
            if (player.isUsingItem()) {
                player.stopUsingItem();
            }
            Entity vehicle = player.getVehicle();
            if (vehicle != null) {
                Vec3 vehicleMotion = vehicle.getDeltaMovement();
                vehicle.setDeltaMovement(0.0, vehicleMotion.y, 0.0);
            }
        }
    }

    private static boolean frozen(Player player) {
        return isFrozen(player);
    }
}
