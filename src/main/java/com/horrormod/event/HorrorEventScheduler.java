package com.horrormod.event;

import com.horrormod.HorrorMod;
import com.horrormod.content.HorrorBlocks;
import com.horrormod.event.HorrorMemory.PlayerMemory;
import com.horrormod.world.PlayerPlacedBlocks;
import com.horrormod.network.InventoryVisiblePayload;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rolls one unused event per player. The delay between rolls is chosen here.
 * An event that cannot happen when it is chosen stays chosen and runs on the first later tick it can.
 * One that is still waiting after five minutes is dropped, and another event is chosen immediately.
 */
public final class HorrorEventScheduler {
    /** Wait before choosing the next unused event. */
    private static final int ROLL_DELAY_MIN = 20 * 15;
    private static final int ROLL_DELAY_MAX = 20 * 60 * 5;
    /** How long a chosen event may wait for its conditions. */
    private static final int PENDING_LIMIT = 20 * 60 * 5;

    private HorrorEventScheduler() {
    }

    public static void initialize() {
        ServerTickEvents.END_LEVEL_TICK.register(HorrorEventScheduler::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            server.execute(() -> syncJoined(player));
        });
        ServerPlayNetworking.registerGlobalReceiver(InventoryVisiblePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> setInventoryVisible(player, payload.visible()));
        });
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
                HorrorEventActions.recordBreak(serverLevel, serverPlayer, pos, state);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("horrormod")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("event")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(HorrorEvent.ids(), builder))
                                        .executes(HorrorEventScheduler::runCommand)))
        ));
    }

    public static void recordPlacement(BlockPlaceContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(HorrorBlocks.STONE_LECTERN) || state.is(Blocks.BEDROCK)) {
            return;
        }
        Identifier block = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (block == null) {
            return;
        }
        PlayerPlacedBlocks.get(level).record(pos, block.toString());
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        boolean torch = state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH);
        HorrorMemory.get(level).player(player.getUUID()).recordPlacement(pos, block.toString(), torch);
    }

    private static void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        HorrorMemory saved = HorrorMemory.get(level);
        long now = level.getGameTime();
        HorrorEventActions.tickFollowups(level, now);
        Set<UUID> present = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            present.add(player.getUUID());
            PlayerMemory memory = saved.player(player.getUUID());
            memory.recordWalk(player.blockPosition());
            HorrorEventActions.tickWolf(level, memory, now);
            HorrorEventActions.tickVanish(level, player, memory);
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            if (holdPending(level, player, memory, now)) {
                continue;
            }
            if (memory.nextRoll() == 0L) {
                memory.setNextRoll(now + rollDelay(level.getRandom()));
                continue;
            }
            if (now < memory.nextRoll() || memory.usedCount() >= HorrorEvent.values().length) {
                continue;
            }
            roll(level, player, memory, now);
        }
        for (Map.Entry<UUID, PlayerMemory> entry : saved.players().entrySet()) {
            if (!present.contains(entry.getKey())) {
                HorrorEventActions.tickWolf(level, entry.getValue(), now);
            }
        }
    }

    /** Keeps a chosen event until it can happen. Returns false when nothing is waiting. */
    private static boolean holdPending(ServerLevel level, ServerPlayer player, PlayerMemory memory, long now) {
        String id = memory.pendingId();
        if (id.isEmpty()) {
            return false;
        }
        HorrorEvent event = HorrorEvent.byId(id);
        if (event == null || memory.isUsed(event)) {
            memory.setPending(null, 0L);
            return false;
        }
        if (memory.pendingSince() == 0L) {
            memory.setPending(event, now);
        } else if (now - memory.pendingSince() >= PENDING_LIMIT) {
            memory.setPending(null, 0L);
            HorrorMod.LOGGER.info("Skipped horror event {} for {}", event.id(), player.getName().getString());
            roll(level, player, memory, now, event);
            return true;
        }
        if (HorrorEventActions.perform(level, player, memory, event)) {
            finish(level, player, memory, event, now);
        }
        return true;
    }

    private static void roll(ServerLevel level, ServerPlayer player, PlayerMemory memory, long now) {
        roll(level, player, memory, now, null);
    }

    private static void roll(ServerLevel level, ServerPlayer player, PlayerMemory memory, long now, HorrorEvent exclude) {
        List<HorrorEvent> candidates = new ArrayList<>();
        for (HorrorEvent event : HorrorEvent.values()) {
            if (!memory.isUsed(event) && event != exclude) {
                candidates.add(event);
            }
        }
        if (candidates.isEmpty()) {
            if (exclude != null && !memory.isUsed(exclude)) {
                waitFor(player, memory, exclude, now);
            }
            return;
        }
        HorrorEvent event = candidates.get(level.getRandom().nextInt(candidates.size()));
        if (!HorrorEventActions.perform(level, player, memory, event)) {
            waitFor(player, memory, event, now);
            return;
        }
        finish(level, player, memory, event, now);
    }

    private static void waitFor(ServerPlayer player, PlayerMemory memory, HorrorEvent event, long now) {
        memory.setPending(event, now);
        HorrorMod.LOGGER.info("Started waiting for horror event {} for {}", event.id(), player.getName().getString());
    }

    private static void finish(ServerLevel level, ServerPlayer player, PlayerMemory memory, HorrorEvent event, long now) {
        memory.markUsed(event);
        memory.setPending(null, 0L);
        memory.setNextRoll(now + rollDelay(level.getRandom()));
        HorrorMod.LOGGER.info("Horror event {} for {}", event.id(), player.getName().getString());
    }

    private static void setInventoryVisible(ServerPlayer player, boolean visible) {
        ServerLevel overworld = player.level().getServer().overworld();
        PlayerMemory memory = HorrorMemory.get(overworld).player(player.getUUID());
        memory.setInventoryVisible(visible);
        if (player.level() == overworld) {
            HorrorEventActions.syncEffect(player, memory);
        }
    }

    private static void syncJoined(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        HorrorEventActions.syncEffect(player, HorrorMemory.get(level).player(player.getUUID()));
    }

    private static int runCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("A player is needed."));
            return 0;
        }
        if (!(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.literal("These events only happen in the overworld."));
            return 0;
        }
        HorrorEvent event = HorrorEvent.byId(StringArgumentType.getString(context, "name"));
        if (event == null) {
            source.sendFailure(Component.literal("Unknown event."));
            return 0;
        }
        PlayerMemory memory = HorrorMemory.get(level).player(player.getUUID());
        if (!HorrorEventActions.perform(level, player, memory, event)) {
            source.sendFailure(Component.literal("That event cannot happen right now."));
            return 0;
        }
        boolean wasPending = event.id().equals(memory.pendingId());
        memory.markUsed(event);
        if (wasPending) {
            memory.setPending(null, 0L);
            memory.setNextRoll(level.getGameTime() + rollDelay(level.getRandom()));
        }
        HorrorMod.LOGGER.info("Horror event {} for {}", event.id(), player.getName().getString());
        source.sendSuccess(() -> Component.literal("Triggered " + event.id()), false);
        return 1;
    }

    private static int rollDelay(RandomSource random) {
        return ROLL_DELAY_MIN + random.nextInt(ROLL_DELAY_MAX - ROLL_DELAY_MIN + 1);
    }
}
