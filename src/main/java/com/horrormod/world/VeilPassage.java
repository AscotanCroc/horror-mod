package com.horrormod.world;

import com.horrormod.HorrorMod;
import com.horrormod.content.HorrorBlocks;
import com.horrormod.content.StoneLecternBlock;
import com.horrormod.entity.HorrorEntities;
import com.horrormod.network.VeilCutsceneDonePayload;
import com.horrormod.network.VeilRitualPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Charges lecterns for players carrying [veil, then holds them through the trip into the veil.
 */
public final class VeilPassage {
    /** First item sits this many blocks down the bridge from the arrival spot. */
    private static final int ITEM_LEAD = 8;
    /** Starts the dimension change while the video is still covering the screen. */
    private static final int TELEPORT_DELAY = 15;
    /** Releases the player if the client never reports that the video ended. */
    private static final int FREEZE_TICKS = 400;
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
        ServerPlayNetworking.registerGlobalReceiver(VeilCutsceneDonePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> finish(player));
        });
    }

    public static void finish(ServerPlayer player) {
        UUID id = player.getUUID();
        if (departures.containsKey(id)) {
            departures.remove(id);
            depart(player);
        }
        if (!frozen.containsKey(id)) {
            return;
        }
        frozen.remove(id);
        restore(player);
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
        float[] look = moonLook(destination);
        player.teleport(new TeleportTransition(
                destination,
                new Vec3(0.5, BridgeShape.FEET_Y, 0.5),
                Vec3.ZERO,
                look[0],
                look[1],
                TeleportTransition.DO_NOTHING
        ));
        player.setYRot(look[0]);
        player.setXRot(look[1]);
        player.setYHeadRot(look[0]);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        layItems(destination, player);
    }

    /**
     * Yaw and pitch that look straight at the moon. The moon travels east to west through the zenith,
     * so its angle is a turn around the east-west axis after the sky's yaw offset.
     */
    private static float[] moonLook(ServerLevel level) {
        float degrees = level.environmentAttributes().getDimensionValue(EnvironmentAttributes.MOON_ANGLE);
        double theta = Math.toRadians(degrees);
        double x = -Math.sin(theta);
        double y = Math.cos(theta);
        float pitch = (float) -Math.toDegrees(Math.asin(Math.clamp(y, -1.0, 1.0)));
        float yaw = (float) Math.toDegrees(Math.atan2(-x, 0.0));
        return new float[] { yaw, pitch };
    }

    private static void layItems(ServerLevel level, ServerPlayer player) {
        List<ItemStack> stacks = new ArrayList<>();
        Inventory inventory = player.getInventory();
        NonNullList<ItemStack> carried = inventory.getNonEquipmentItems();
        for (int slot = 0; slot < carried.size(); slot++) {
            ItemStack stack = carried.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            stacks.add(stack.copy());
            carried.set(slot, ItemStack.EMPTY);
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            stacks.add(stack.copy());
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        ItemStack cursor = player.containerMenu.getCarried();
        if (!cursor.isEmpty()) {
            stacks.add(cursor.copy());
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
        double y = BridgeShape.FEET_Y;
        for (int index = 0; index < stacks.size(); index++) {
            double z = 0.5 + ITEM_LEAD + index;
            ItemEntity entity = new ItemEntity(level, 0.5, y, z, stacks.get(index));
            entity.setUnlimitedLifetime();
            entity.setNoPickUpDelay();
            entity.setDeltaMovement(Vec3.ZERO);
            level.addFreshEntity(entity);
        }
        inventory.setChanged();
        player.containerMenu.broadcastChanges();
    }

    private static void hold(ServerPlayer player) {
        Integer ticks = frozen.get(player.getUUID());
        if (ticks == null) {
            return;
        }
        if (ticks <= 1) {
            frozen.remove(player.getUUID());
            restore(player);
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
        restore(player);
    }

    private static void restore(ServerPlayer player) {
        Boolean previous = invulnerable.remove(player.getUUID());
        if (previous != null) {
            player.setPermanentlyInvulnerable(previous);
        }
    }

    private record LecternRef(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
