package com.horrormod.event;

import com.horrormod.HorrorMod;
import com.horrormod.content.HorrorBlocks;
import com.horrormod.entity.HorrorEntities;
import com.horrormod.entity.Watdatmean;
import com.horrormod.event.HorrorMemory.PlacedBlock;
import com.horrormod.event.HorrorMemory.PlayerMemory;
import com.horrormod.network.HorrorEffectPayload;
import com.horrormod.sound.HorrorSounds;
import com.horrormod.world.VoidChunks;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;

/**
 * What each one-shot event does. An event returns false when its conditions are not met and is left unused.
 */
public final class HorrorEventActions {
    private static final double LOOK_DOT = Math.cos(Math.toRadians(70.0));
    private static final int HOTBAR = 9;
    private static final int FLICKER_TICKS = 40;
    private static final int BRIGHTNESS_TICKS = 100;
    private static final int STAR_TICKS = 400;
    private static final int CELESTIAL_TICKS = 20 * 60 * 5;
    private static final int WOLF_TICKS = 20 * 15;
    private static final int HOLE_COUNT = 5;
    private static final int NIGHT_START = 13000;
    private static final int NIGHT_END = 23000;
    private static final int GHOST_TICKS = 20;
    private static final int BREAK_WINDOW = 20 * 20;
    private static final int WRONG_STEPS = 4;
    private static final float WRONG_STEP_VOLUME = 3.0F;
    private static final int GRASS_RADIUS = 3;
    private static final int TORCH_VANISH_MAX = 15;
    private static final double CHEST_REACH = 15.0 * 15.0;
    private static final double NEAR_PLAYER = 24.0 * 24.0;
    private static final Map<UUID, RecentBreak> BROKEN = new ConcurrentHashMap<>();
    private static final Map<UUID, GhostBlock> GHOSTS = new ConcurrentHashMap<>();
    private static final Map<UUID, List<BlockPos>> CHESTS = new ConcurrentHashMap<>();
    private static final List<DelayedSound> SOUNDS = new ArrayList<>();
    private static final Map<UUID, StepPitch> STEPS = new ConcurrentHashMap<>();

    private HorrorEventActions() {
    }

    public static boolean perform(ServerLevel level, ServerPlayer player, PlayerMemory memory, HorrorEvent event) {
        return switch (event) {
            case STRETCHED_FLESH -> stretched(level, player, memory);
            case BARK -> bark(level, player);
            case TORCH_REDSTONE -> torchToRedstone(level, player, memory);
            case TORCH_VANISH -> torchVanish(level, player, memory);
            case SNAP_TIME -> snapTime(level);
            case HOLE -> hole(level, memory);
            case REVERSE_BEHIND -> reverseBehind(level, player);
            case SHUFFLE -> shuffle(player, memory);
            case DELETE_ITEMS -> deleteItems(player, memory);
            case GIVE_GLITCH -> giveGlitch(player, memory);
            case SITTING_WOLF -> sittingWolf(level, player, memory);
            case PLACED_VANISH -> placedVanish(level, player, memory);
            case FLICKER -> show(level, player, memory, event, FLICKER_TICKS, 0.0F, 0.0F, 1.0F, 1.0F);
            case CELESTIAL -> show(level, player, memory, event, CELESTIAL_TICKS, 0.0F, 0.0F, 1.0F, 1.0F);
            case STARS -> stars(level, player, memory);
            case DESPAWN_HOSTILES -> despawnHostiles(level, player);
            case BRIGHTNESS -> show(level, player, memory, event, BRIGHTNESS_TICKS, 0.0F, 0.0F, 1.0F, 1.0F);
            case CRASH_TOAST -> crashToast(player);
            case GL_ERROR -> glError(player);
            case GLITCH_SOUND -> glitchSound(level, player);
            case FOOTSTEPS -> footsteps(level, player);
            case HOTBAR_SKIP -> hotbarSkip(player, memory);
            case GRASS_DIRT -> grassToDirt(level, player, memory);
            case GHOST_MINE -> ghostMine(level, player);
            case CAVE_BREATH -> caveBreath(level, player);
            case CHEST_LID -> chestLid(level, player);
            case WRONG_STEP -> wrongStep(player);
        };
    }

    /**
     * Plays flicker for every player who can see {@code subject}. Counts as the one-shot flicker event.
     */
    public static void flickerIfSeen(ServerLevel level, Entity subject) {
        Vec3 point = subject.getEyePosition();
        for (ServerPlayer player : level.players()) {
            if (!onScreen(player, point)) {
                continue;
            }
            PlayerMemory memory = HorrorMemory.get(level).player(player.getUUID());
            if (HorrorEvent.FLICKER.id().equals(memory.pendingId())) {
                memory.setPending(null, 0L);
            }
            memory.markUsed(HorrorEvent.FLICKER);
            show(level, player, memory, HorrorEvent.FLICKER, FLICKER_TICKS, 0.0F, 0.0F, 1.0F, 1.0F);
        }
    }

    /** True when the point is inside a normal view and not behind a block. */
    public static boolean onScreen(ServerPlayer player, Vec3 target) {
        if (player.isSpectator()) {
            return false;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 delta = target.subtract(eye);
        double distance = delta.length();
        if (distance > 96.0) {
            return false;
        }
        if (distance < 1.0E-4) {
            return true;
        }
        if (player.getLookAngle().dot(delta.scale(1.0 / distance)) < Math.cos(Math.toRadians(50.0))) {
            return false;
        }
        BlockHitResult hit = player.level().clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(target) <= 2.25;
    }

    public static void syncEffect(ServerPlayer player, PlayerMemory memory) {
        if (!(player.level() instanceof ServerLevel level) || !memory.effectActive(level.getGameTime())) {
            return;
        }
        ServerPlayNetworking.send(player, payload(memory));
    }

    public static void tickWolf(ServerLevel level, PlayerMemory memory, long now) {
        UUID id = memory.wolf();
        if (id == null) {
            return;
        }
        Entity entity = level.getEntity(id);
        if (now >= memory.wolfUntil()) {
            if (memory.wolfPos() != null && !level.isLoaded(memory.wolfPos())) {
                return;
            }
            if (entity instanceof Wolf wolf) {
                wolf.discard();
            }
            memory.clearWolf();
            return;
        }
        if (!(entity instanceof Wolf wolf) || !wolf.isAlive()) {
            if (entity != null || (memory.wolfPos() != null && level.isLoaded(memory.wolfPos()))) {
                memory.clearWolf();
            }
            return;
        }
        wolf.setOrderedToSit(true);
        wolf.setInSittingPose(true);
        wolf.getNavigation().stop();
    }

    public static void tickVanish(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        List<PlacedBlock> pending = memory.vanish();
        if (pending.isEmpty()) {
            return;
        }
        boolean changed = pending.removeIf(placed -> level.isLoaded(placed.pos()) && !matches(level, placed));
        if (pending.isEmpty()) {
            if (changed) {
                memory.dirty();
            }
            return;
        }
        for (PlacedBlock placed : pending) {
            if (!level.isLoaded(placed.pos())) {
                if (changed) {
                    memory.dirty();
                }
                return;
            }
        }
        if (lookingAtAny(player, pending)) {
            if (changed) {
                memory.dirty();
            }
            return;
        }
        erase(level, memory);
    }

    private static boolean stretched(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        RandomSource random = level.getRandom();
        float narrow = 2.0F + random.nextFloat() * 2.0F;
        float wide = narrow * (2.5F + random.nextFloat() * 2.0F);
        boolean horizontal = random.nextBoolean();
        int ticks = 20 * (60 + random.nextInt(61));
        return show(
                level,
                player,
                memory,
                HorrorEvent.STRETCHED_FLESH,
                ticks,
                random.nextFloat(),
                random.nextFloat(),
                horizontal ? wide : narrow,
                horizontal ? narrow : wide
        );
    }

    private static boolean bark(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * (Math.PI * 2.0);
        double distance = 6.0 + random.nextDouble() * 10.0;
        SoundEvent sound = level.registryAccess()
                .lookupOrThrow(Registries.WOLF_SOUND_VARIANT)
                .getOrThrow(WolfSoundVariants.CLASSIC)
                .value()
                .adultSounds()
                .ambientSound()
                .value();
        level.playSound(
                null,
                player.getX() + Math.cos(angle) * distance,
                player.getY(),
                player.getZ() + Math.sin(angle) * distance,
                sound,
                SoundSource.NEUTRAL,
                1.0F,
                0.9F + random.nextFloat() * 0.2F
        );
        return true;
    }

    private static boolean torchToRedstone(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        BlockPos pos = pickTorch(level, player, memory);
        if (pos == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        BlockState redstone;
        if (state.is(Blocks.WALL_TORCH)) {
            redstone = Blocks.REDSTONE_WALL_TORCH.defaultBlockState()
                    .setValue(RedstoneWallTorchBlock.FACING, state.getValue(WallTorchBlock.FACING));
        } else {
            redstone = Blocks.REDSTONE_TORCH.defaultBlockState();
        }
        level.setBlock(pos, redstone, Block.UPDATE_ALL);
        Identifier key = BuiltInRegistries.BLOCK.getKey(redstone.getBlock());
        forgetTorch(memory, pos, key == null ? null : key.toString());
        return true;
    }

    private static boolean torchVanish(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        List<BlockPos> ready = unwatchedTorches(level, player, memory);
        if (ready.isEmpty()) {
            return false;
        }
        shuffle(ready, level.getRandom());
        int count = 1 + level.getRandom().nextInt(Math.min(TORCH_VANISH_MAX, ready.size()));
        for (int index = 0; index < count; index++) {
            BlockPos pos = ready.get(index);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            forgetTorch(memory, pos, null);
        }
        return true;
    }

    private static boolean snapTime(ServerLevel level) {
        if (!(level.clockManager() instanceof ServerClockManager clocks)) {
            return false;
        }
        Holder<WorldClock> clock = level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD);
        long current = clocks.getInstance(clock).totalTicks();
        long dayTime = Math.floorMod(current, 24000L);
        long target = dayTime < 13000L ? 18000L : 6000L;
        long next = current - dayTime + target;
        if (next < 0L) {
            next += 24000L;
        }
        clocks.setTotalTicks(clock, next);
        return true;
    }

    private static boolean hole(ServerLevel level, PlayerMemory memory) {
        List<BlockPos> order = new ArrayList<>(memory.walks());
        if (order.isEmpty()) {
            return false;
        }
        shuffle(order, level.getRandom());
        int carved = 0;
        for (BlockPos pos : order) {
            if (carve(level, pos)) {
                carved++;
                if (carved >= HOLE_COUNT) {
                    return true;
                }
            }
        }
        return carved > 0;
    }

    private static boolean reverseBehind(ServerLevel level, ServerPlayer player) {
        List<EntityType<? extends Mob>> types = HorrorEntities.reverseMonsters();
        EntityType<? extends Mob> type = types.get(level.getRandom().nextInt(types.size()));
        int air = type == HorrorEntities.REVERSE_ENDERMAN ? 3 : 2;
        BlockPos feet = findBehind(level, player, air);
        if (feet == null) {
            return false;
        }
        return type.spawn(level, feet, EntitySpawnReason.EVENT) != null;
    }

    private static boolean shuffle(ServerPlayer player, PlayerMemory memory) {
        if (memory.inventoryVisible()) {
            return false;
        }
        Inventory inventory = player.getInventory();
        int storage = inventory.getNonEquipmentItems().size() - HOTBAR;
        if (storage < 2) {
            return false;
        }
        ItemStack[] stacks = new ItemStack[storage];
        boolean any = false;
        for (int slot = 0; slot < storage; slot++) {
            stacks[slot] = inventory.getItem(HOTBAR + slot).copy();
            any |= !stacks[slot].isEmpty();
        }
        if (!any) {
            return false;
        }
        int shift = 1 + player.getRandom().nextInt(storage - 1);
        for (int slot = 0; slot < storage; slot++) {
            inventory.setItem(HOTBAR + slot, stacks[Math.floorMod(slot - shift, storage)]);
        }
        syncInventory(player);
        return true;
    }

    private static boolean deleteItems(ServerPlayer player, PlayerMemory memory) {
        if (memory.inventoryVisible()) {
            return false;
        }
        Inventory inventory = player.getInventory();
        int size = inventory.getNonEquipmentItems().size();
        List<Integer> filled = new ArrayList<>();
        for (int slot = 0; slot < size; slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
                filled.add(slot);
            }
        }
        if (filled.isEmpty()) {
            return false;
        }
        RandomSource random = player.getRandom();
        if (filled.size() == 1) {
            inventory.setItem(filled.getFirst(), ItemStack.EMPTY);
        } else {
            int remove = 1 + random.nextInt(Math.min(3, filled.size() - 1));
            for (int count = 0; count < remove; count++) {
                inventory.setItem(filled.remove(random.nextInt(filled.size())), ItemStack.EMPTY);
            }
        }
        syncInventory(player);
        return true;
    }

    private static boolean giveGlitch(ServerPlayer player, PlayerMemory memory) {
        if (memory.inventoryVisible()) {
            return false;
        }
        Inventory inventory = player.getInventory();
        int size = inventory.getNonEquipmentItems().size();
        List<Integer> empty = new ArrayList<>();
        for (int slot = HOTBAR; slot < size; slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                empty.add(slot);
            }
        }
        if (empty.isEmpty()) {
            return false;
        }
        inventory.setItem(empty.get(player.getRandom().nextInt(empty.size())), new ItemStack(HorrorEntities.GLITCH));
        syncInventory(player);
        return true;
    }

    private static boolean sittingWolf(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        if (memory.wolf() != null) {
            return false;
        }
        BlockPos spot = wolfSpot(level, player, memory);
        if (spot == null) {
            return false;
        }
        Wolf wolf = EntityTypes.WOLF.create(level, EntitySpawnReason.EVENT);
        if (wolf == null) {
            return false;
        }
        wolf.snapTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, 0.0F, 0.0F);
        wolf.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), EntitySpawnReason.EVENT, null);
        wolf.setTame(true, false);
        wolf.setOwnerReference(EntityReference.of(UUID.randomUUID()));
        wolf.setPersistenceRequired();
        wolf.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
        wolf.setYHeadRot(wolf.getYRot());
        wolf.setYBodyRot(wolf.getYRot());
        wolf.setOrderedToSit(true);
        wolf.setInSittingPose(true);
        if (!level.addFreshEntity(wolf)) {
            return false;
        }
        memory.setWolf(wolf.getUUID(), level.getGameTime() + WOLF_TICKS, spot);
        return true;
    }

    private static boolean placedVanish(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        if (!memory.vanish().isEmpty()) {
            return false;
        }
        List<PlacedBlock> group = chooseGroup(level, memory);
        if (group == null) {
            return false;
        }
        memory.setVanish(group);
        if (!lookingAtAny(player, memory.vanish())) {
            erase(level, memory);
        }
        return true;
    }

    private static boolean stars(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        int dayTime = Math.floorMod((int) level.getOverworldClockTime(), 24000);
        if (dayTime < NIGHT_START || dayTime >= NIGHT_END) {
            return false;
        }
        BlockPos eye = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        if (!level.canSeeSky(eye)) {
            return false;
        }
        return show(level, player, memory, HorrorEvent.STARS, STAR_TICKS, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    private static boolean despawnHostiles(ServerLevel level, ServerPlayer player) {
        if (!lookingAtHostile(level, player)) {
            return false;
        }
        List<Monster> doomed = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Monster monster
                    && monster.isAlive()
                    && !HorrorEntities.isReverse(monster)
                    && !(monster instanceof Watdatmean)) {
                doomed.add(monster);
            }
        }
        if (doomed.isEmpty()) {
            return false;
        }
        for (Monster monster : doomed) {
            monster.discard();
        }
        return true;
    }

    private static boolean crashToast(ServerPlayer player) {
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(HorrorMod.id("error"));
        if (holder == null) {
            return false;
        }
        player.getAdvancements().award(holder, "seen");
        return true;
    }

    private static boolean glError(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("########## GL ERROR ##########").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("@ Pre render").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("1282: Invalid operation").withStyle(ChatFormatting.GRAY));
        return true;
    }

    private static boolean glitchSound(ServerLevel level, ServerPlayer player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), HorrorSounds.GLITCH_TRANSITION, SoundSource.MASTER, 1.0F, 1.0F);
        return true;
    }

    private static boolean show(ServerLevel level, ServerPlayer player, PlayerMemory memory, HorrorEvent event, int ticks, float x, float y, float stretchX, float stretchY) {
        memory.setEffect(event.id(), level.getGameTime() + ticks, x, y, stretchX, stretchY);
        ServerPlayNetworking.send(player, payload(memory));
        return true;
    }

    private static HorrorEffectPayload payload(PlayerMemory memory) {
        return new HorrorEffectPayload(memory.effect(), memory.effectUntil(), memory.effectX(), memory.effectY(), memory.effectStretchX(), memory.effectStretchY());
    }

    private static BlockPos pickTorch(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        List<BlockPos> ready = unwatchedTorches(level, player, memory);
        if (ready.isEmpty()) {
            return null;
        }
        return ready.get(level.getRandom().nextInt(ready.size()));
    }

    /** Placed torches that are still torches and are outside the player's view. */
    private static List<BlockPos> unwatchedTorches(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        List<BlockPos> torches = memory.torches();
        boolean removed = torches.removeIf(pos -> level.isLoaded(pos) && !isTorch(level.getBlockState(pos)));
        if (removed) {
            memory.dirty();
        }
        List<BlockPos> ready = new ArrayList<>();
        for (BlockPos pos : torches) {
            if (level.isLoaded(pos) && isTorch(level.getBlockState(pos)) && !lookingAt(player, Vec3.atCenterOf(pos), 128.0)) {
                ready.add(pos);
            }
        }
        return ready;
    }

    private static boolean isTorch(BlockState state) {
        return state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH);
    }

    private static void forgetTorch(PlayerMemory memory, BlockPos pos, String replacement) {
        memory.torches().remove(pos);
        memory.placed().removeIf(placed -> placed.pos().equals(pos));
        if (replacement != null) {
            memory.placed().add(new PlacedBlock(pos, replacement));
        }
        memory.dirty();
    }

    private static boolean carve(ServerLevel level, BlockPos feet) {
        if (!level.isLoaded(feet) || VoidChunks.isRemoved(level, feet.getX() >> 4, feet.getZ() >> 4)) {
            return false;
        }
        boolean carved = false;
        for (int y = feet.getY(); y >= level.getMinY(); y--) {
            BlockPos pos = new BlockPos(feet.getX(), y, feet.getZ());
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.BEDROCK)) {
                break;
            }
            if (state.isAir() || state.is(HorrorBlocks.STONE_LECTERN)) {
                continue;
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            carved = true;
        }
        return carved;
    }

    private static BlockPos findBehind(ServerLevel level, ServerPlayer player, int air) {
        Vec3 look = player.getLookAngle();
        double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        double hx = horizontal < 1.0E-4 ? 0.0 : look.x / horizontal;
        double hz = horizontal < 1.0E-4 ? 1.0 : look.z / horizontal;
        int feetY = Mth.floor(player.getY());
        for (int distance = 2; distance <= 4; distance++) {
            int x = Mth.floor(player.getX() - hx * distance);
            int z = Mth.floor(player.getZ() - hz * distance);
            for (int y = feetY + 1; y >= feetY - 3; y--) {
                BlockPos feet = new BlockPos(x, y, z);
                if (hasRoom(level, feet, air) && player.position().distanceToSqr(x + 0.5, y, z + 0.5) > 2.25) {
                    return feet;
                }
            }
        }
        return null;
    }

    private static boolean hasRoom(ServerLevel level, BlockPos feet, int air) {
        if (!level.isLoaded(feet) || VoidChunks.isRemoved(level, feet.getX() >> 4, feet.getZ() >> 4)) {
            return false;
        }
        BlockPos ground = feet.below();
        if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
            return false;
        }
        if (!level.getFluidState(feet).isEmpty() || !level.getBlockState(ground).getFluidState().isEmpty()) {
            return false;
        }
        for (int offset = 0; offset < air; offset++) {
            BlockState state = level.getBlockState(feet.above(offset));
            if (!state.isAir() || !state.getFluidState().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static BlockPos wolfSpot(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        List<BlockPos> spots = new ArrayList<>();
        for (BlockPos pos : memory.walks()) {
            double dx = pos.getX() + 0.5 - player.getX();
            double dz = pos.getZ() + 0.5 - player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < 3.0 || distance > 24.0 || !hasRoom(level, pos, 2)) {
                continue;
            }
            if (!lookingAt(player, Vec3.atCenterOf(pos), 32.0)) {
                spots.add(pos);
            }
        }
        if (spots.isEmpty()) {
            return null;
        }
        return spots.get(level.getRandom().nextInt(spots.size()));
    }

    private static List<PlacedBlock> chooseGroup(ServerLevel level, PlayerMemory memory) {
        List<PlacedBlock> valid = new ArrayList<>();
        boolean dropped = false;
        Iterator<PlacedBlock> iterator = memory.placed().iterator();
        while (iterator.hasNext()) {
            PlacedBlock placed = iterator.next();
            if (!level.isLoaded(placed.pos())) {
                continue;
            }
            if (!matches(level, placed)) {
                iterator.remove();
                dropped = true;
                continue;
            }
            valid.add(placed);
        }
        if (dropped) {
            memory.dirty();
        }
        if (valid.size() < 2) {
            return null;
        }
        RandomSource random = level.getRandom();
        int want = valid.size() >= 3 && random.nextBoolean() ? 3 : 2;
        List<PlacedBlock> group = findGroup(valid, want, random);
        if (group == null && want == 3) {
            group = findGroup(valid, 2, random);
        }
        return group;
    }

    private static List<PlacedBlock> findGroup(List<PlacedBlock> valid, int want, RandomSource random) {
        for (int attempt = 0; attempt < 16; attempt++) {
            List<PlacedBlock> rest = new ArrayList<>(valid);
            shuffle(rest, random);
            List<PlacedBlock> group = new ArrayList<>();
            for (PlacedBlock placed : rest) {
                if (group.isEmpty() || fits(group, placed)) {
                    group.add(placed);
                }
                if (group.size() == want) {
                    return group;
                }
            }
        }
        return null;
    }

    private static boolean fits(List<PlacedBlock> group, PlacedBlock other) {
        for (PlacedBlock member : group) {
            if (faceAdjacent(member.pos(), other.pos()) || member.pos().distSqr(other.pos()) > 36) {
                return false;
            }
        }
        return true;
    }

    private static boolean faceAdjacent(BlockPos first, BlockPos second) {
        int dx = Math.abs(first.getX() - second.getX());
        int dy = Math.abs(first.getY() - second.getY());
        int dz = Math.abs(first.getZ() - second.getZ());
        return dx + dy + dz == 1;
    }

    private static boolean matches(ServerLevel level, PlacedBlock placed) {
        BlockState state = level.getBlockState(placed.pos());
        if (state.is(HorrorBlocks.STONE_LECTERN)) {
            return false;
        }
        Identifier key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return key != null && key.toString().equals(placed.block());
    }

    private static void erase(ServerLevel level, PlayerMemory memory) {
        for (PlacedBlock placed : List.copyOf(memory.vanish())) {
            if (level.isLoaded(placed.pos()) && matches(level, placed)) {
                level.setBlock(placed.pos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            memory.placed().removeIf(existing -> existing.pos().equals(placed.pos()));
            memory.torches().remove(placed.pos());
        }
        memory.clearVanish();
    }

    private static boolean lookingAtAny(ServerPlayer player, List<PlacedBlock> blocks) {
        for (PlacedBlock placed : blocks) {
            if (lookingAt(player, Vec3.atCenterOf(placed.pos()), 128.0)) {
                return true;
            }
        }
        return false;
    }

    private static boolean lookingAtHostile(ServerLevel level, ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(48.0);
        for (Monster monster : level.getEntities(EntityTypeTest.forClass(Monster.class), box, Entity::isAlive)) {
            if (lookingAt(player, monster.getEyePosition(), 48.0)) {
                return true;
            }
        }
        return false;
    }

    private static boolean lookingAt(ServerPlayer player, Vec3 target, double maxDistance) {
        Vec3 eye = player.getEyePosition();
        Vec3 delta = target.subtract(eye);
        double distance = delta.length();
        if (distance > maxDistance) {
            return false;
        }
        if (distance < 1.0E-4) {
            return true;
        }
        if (player.getLookAngle().dot(delta.scale(1.0 / distance)) < LOOK_DOT) {
            return false;
        }
        BlockHitResult hit = player.level().clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(target) <= 2.25;
    }

    private static void syncInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastFullState();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastFullState();
        }
    }

    public static void recordBreak(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state) {
        if (level.dimension() != Level.OVERWORLD || state.isAir() || state.is(Blocks.BEDROCK) || state.is(HorrorBlocks.STONE_LECTERN)) {
            return;
        }
        if (state.getBlock() instanceof EntityBlock) {
            return;
        }
        BROKEN.put(player.getUUID(), new RecentBreak(copy(pos), state, level.getGameTime()));
    }

    public static void rememberChest(ServerPlayer player, BlockPos pos) {
        if (!(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        List<BlockPos> chests = CHESTS.computeIfAbsent(player.getUUID(), id -> new ArrayList<>());
        BlockPos stored = copy(pos);
        synchronized (chests) {
            chests.remove(stored);
            chests.add(stored);
            while (chests.size() > 32) {
                chests.removeFirst();
            }
        }
    }

    public static void tickFollowups(ServerLevel level, long now) {
        synchronized (SOUNDS) {
            Iterator<DelayedSound> sounds = SOUNDS.iterator();
            while (sounds.hasNext()) {
                DelayedSound sound = sounds.next();
                if (now < sound.when()) {
                    continue;
                }
                sounds.remove();
                level.playSound(null, sound.x(), sound.y(), sound.z(), sound.sound(), SoundSource.PLAYERS, sound.volume(), sound.pitch());
            }
        }
        GHOSTS.entrySet().removeIf(entry -> finishGhost(level, entry.getValue(), now));
    }

    /**
     * Wrong steps are louder and the wrong pitch. Both sides share this in a singleplayer game.
     */
    public static float adjustStepPitch(Player player, float original) {
        StepPitch step = activeStep(player);
        return step == null ? original : step.pitch;
    }

    public static float adjustStepVolume(Player player, float original) {
        StepPitch step = activeStep(player);
        return step == null ? original : original * WRONG_STEP_VOLUME;
    }

    private static StepPitch activeStep(Player player) {
        StepPitch step = STEPS.get(player.getUUID());
        if (step == null) {
            return null;
        }
        synchronized (step) {
            long now = player.level().getGameTime();
            if (step.tick != now) {
                step.tick = now;
                step.left--;
            }
            if (step.left < 0) {
                STEPS.remove(player.getUUID(), step);
                return null;
            }
            return step;
        }
    }

    private static boolean footsteps(ServerLevel level, ServerPlayer player) {
        if (!walking(player)) {
            return false;
        }
        Vec3 look = player.getLookAngle();
        double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        double hx = horizontal < 1.0E-4 ? 0.0 : look.x / horizontal;
        double hz = horizontal < 1.0E-4 ? 1.0 : look.z / horizontal;
        DelayedSound[] steps = new DelayedSound[3];
        long now = level.getGameTime();
        RandomSource random = level.getRandom();
        for (int step = 0; step < steps.length; step++) {
            double distance = 2.5 + step * 1.5;
            double x = player.getX() - hx * distance;
            double y = player.getY();
            double z = player.getZ() - hz * distance;
            BlockPos ground = BlockPos.containing(x, y, z).below();
            if (!level.isLoaded(ground)) {
                return false;
            }
            BlockState state = level.getBlockState(ground);
            if (state.isAir()) {
                state = level.getBlockState(ground.above());
            }
            SoundType type = state.getSoundType();
            steps[step] = new DelayedSound(
                    now + step * 7L,
                    x,
                    y,
                    z,
                    type.getStepSound(),
                    Math.max(0.35F, type.getVolume() * 0.7F),
                    type.getPitch() * (0.85F + random.nextFloat() * 0.3F)
            );
        }
        synchronized (SOUNDS) {
            for (DelayedSound step : steps) {
                SOUNDS.add(step);
            }
        }
        return true;
    }

    private static boolean hotbarSkip(ServerPlayer player, PlayerMemory memory) {
        if (memory.inventoryVisible()) {
            return false;
        }
        int next = (player.getInventory().getSelectedSlot() + 1) % HOTBAR;
        player.getInventory().setSelectedSlot(next);
        player.connection.send(new ClientboundSetHeldSlotPacket(next));
        return true;
    }

    private static boolean grassToDirt(ServerLevel level, ServerPlayer player, PlayerMemory memory) {
        List<BlockPos> walks = new ArrayList<>(memory.walks());
        if (walks.isEmpty()) {
            return false;
        }
        shuffle(walks, level.getRandom());
        for (BlockPos pos : walks) {
            if (grassPatch(level, player, pos)) {
                return true;
            }
        }
        return false;
    }

    /** Turns a square of grass around one walked spot into dirt, leaving any block still in view. */
    private static boolean grassPatch(ServerLevel level, ServerPlayer player, BlockPos origin) {
        boolean changed = false;
        for (int dx = -GRASS_RADIUS; dx <= GRASS_RADIUS; dx++) {
            for (int dz = -GRASS_RADIUS; dz <= GRASS_RADIUS; dz++) {
                BlockPos ground = grassAt(level, new BlockPos(origin.getX() + dx, origin.getY(), origin.getZ() + dz));
                if (ground == null || lookingAt(player, Vec3.atCenterOf(ground), 64.0)) {
                    continue;
                }
                level.setBlock(ground, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                changed = true;
            }
        }
        return changed;
    }

    private static boolean ghostMine(ServerLevel level, ServerPlayer player) {
        RecentBreak broken = BROKEN.get(player.getUUID());
        if (broken == null || level.getGameTime() - broken.when() > BREAK_WINDOW) {
            return false;
        }
        if (!level.isLoaded(broken.pos()) || !level.getBlockState(broken.pos()).isAir()) {
            return false;
        }
        level.setBlock(broken.pos(), broken.state(), Block.UPDATE_ALL);
        GHOSTS.put(player.getUUID(), new GhostBlock(broken.pos(), broken.state(), level.getGameTime() + GHOST_TICKS));
        BROKEN.remove(player.getUUID(), broken);
        return true;
    }

    private static boolean caveBreath(ServerLevel level, ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        if (level.canSeeSkyFromBelowWater(pos)) {
            return false;
        }
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        if (pos.getY() >= surface - 1) {
            return false;
        }
        for (ServerPlayer other : level.players()) {
            if (other != player && !other.isSpectator() && other.distanceToSqr(player) < NEAR_PLAYER) {
                return false;
            }
        }
        level.playSound(null, player.getX(), player.getEyeY(), player.getZ(), HorrorSounds.WHISPER, SoundSource.AMBIENT, 0.55F, 1.0F);
        return true;
    }

    private static boolean chestLid(ServerLevel level, ServerPlayer player) {
        List<BlockPos> chests = CHESTS.get(player.getUUID());
        if (chests == null) {
            return false;
        }
        List<BlockPos> ready = new ArrayList<>();
        synchronized (chests) {
            chests.removeIf(pos -> level.isLoaded(pos) && !(level.getBlockState(pos).getBlock() instanceof ChestBlock));
            for (BlockPos pos : chests) {
                if (!level.isLoaded(pos)
                        || player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > CHEST_REACH
                        || ChestBlockEntity.getOpenCount(level, pos) > 0
                        || lookingAt(player, Vec3.atCenterOf(pos), 64.0)) {
                    continue;
                }
                ready.add(pos);
            }
        }
        if (ready.isEmpty()) {
            return false;
        }
        BlockPos pos = ready.get(level.getRandom().nextInt(ready.size()));
        if (!(level.getBlockState(pos).getBlock() instanceof ChestBlock chest)) {
            return false;
        }
        float pitch = 0.9F + level.getRandom().nextFloat() * 0.1F;
        level.playSound(null, pos, chest.getOpenChestSound(), SoundSource.BLOCKS, 0.5F, pitch);
        return true;
    }

    private static boolean wrongStep(ServerPlayer player) {
        if (!walking(player)) {
            return false;
        }
        float pitch = player.getRandom().nextBoolean() ? 0.5F : 1.75F;
        STEPS.put(player.getUUID(), new StepPitch(WRONG_STEPS, pitch));
        return true;
    }

    private static boolean walking(ServerPlayer player) {
        Input input = player.getLastClientInput();
        if (input.forward() || input.backward() || input.left() || input.right()) {
            return true;
        }
        Vec3 movement = player.getDeltaMovement();
        return movement.x * movement.x + movement.z * movement.z > 0.0025;
    }

    private static BlockPos grassAt(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
            return pos.below();
        }
        if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
            return pos;
        }
        return null;
    }

    private static boolean finishGhost(ServerLevel level, GhostBlock ghost, long now) {
        if (now < ghost.removeAt()) {
            return false;
        }
        if (!level.isLoaded(ghost.pos())) {
            return false;
        }
        BlockState state = level.getBlockState(ghost.pos());
        if (state.equals(ghost.state())) {
            level.playSound(null, ghost.pos(), state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
            level.setBlock(ghost.pos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        return true;
    }

    private static BlockPos copy(BlockPos pos) {
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    private record RecentBreak(BlockPos pos, BlockState state, long when) {
    }

    private record GhostBlock(BlockPos pos, BlockState state, long removeAt) {
    }

    private record DelayedSound(long when, double x, double y, double z, SoundEvent sound, float volume, float pitch) {
    }

    private static final class StepPitch {
        private int left;
        private final float pitch;
        private long tick = Long.MIN_VALUE;

        private StepPitch(int left, float pitch) {
            this.left = left;
            this.pitch = pitch;
        }
    }

    private static <T> void shuffle(List<T> list, RandomSource random) {
        for (int index = list.size() - 1; index > 0; index--) {
            int swap = random.nextInt(index + 1);
            T item = list.get(index);
            list.set(index, list.get(swap));
            list.set(swap, item);
        }
    }
}
