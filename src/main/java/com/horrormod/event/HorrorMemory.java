package com.horrormod.event;

import com.horrormod.HorrorMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Per-player event history for one world. An event id in {@code used} has already happened.
 */
public final class HorrorMemory extends SavedData {
    private static final int WALK_CAP = 64;
    private static final int TORCH_CAP = 256;
    private static final int PLACED_CAP = 128;

    public static final Codec<HorrorMemory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, PlayerMemory.CODEC).fieldOf("players").forGetter(HorrorMemory::players)
    ).apply(instance, HorrorMemory::new));

    public static final SavedDataType<HorrorMemory> TYPE = new SavedDataType<>(
            HorrorMod.id("events"),
            HorrorMemory::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, PlayerMemory> players = new HashMap<>();

    public HorrorMemory() {
    }

    private HorrorMemory(Map<UUID, PlayerMemory> players) {
        this.players.putAll(players);
    }

    public static HorrorMemory get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public PlayerMemory player(UUID id) {
        PlayerMemory memory = this.players.computeIfAbsent(id, key -> new PlayerMemory());
        memory.attach(this);
        return memory;
    }

    public Map<UUID, PlayerMemory> players() {
        for (PlayerMemory memory : this.players.values()) {
            memory.attach(this);
        }
        return this.players;
    }

    /**
     * A block the player placed, remembered by its block id so a later replacement is left alone.
     */
    public record PlacedBlock(BlockPos pos, String block) {
        public static final Codec<PlacedBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(PlacedBlock::pos),
                Codec.STRING.fieldOf("block").forGetter(PlacedBlock::block)
        ).apply(instance, PlacedBlock::new));
    }

    public static final class PlayerMemory {
        public static final Codec<PlayerMemory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().optionalFieldOf("used", List.of()).forGetter(memory -> List.copyOf(memory.used)),
                Codec.LONG.optionalFieldOf("next_roll", 0L).forGetter(memory -> memory.nextRoll),
                Codec.STRING.optionalFieldOf("pending", "").forGetter(memory -> memory.pending),
                BlockPos.CODEC.listOf().optionalFieldOf("walks", List.of()).forGetter(memory -> List.copyOf(memory.walks)),
                BlockPos.CODEC.listOf().optionalFieldOf("torches", List.of()).forGetter(memory -> List.copyOf(memory.torches)),
                PlacedBlock.CODEC.listOf().optionalFieldOf("placed", List.of()).forGetter(memory -> List.copyOf(memory.placed)),
                PlacedBlock.CODEC.listOf().optionalFieldOf("vanish", List.of()).forGetter(memory -> List.copyOf(memory.vanish)),
                UUIDUtil.CODEC.optionalFieldOf("wolf").forGetter(memory -> Optional.ofNullable(memory.wolf)),
                Codec.LONG.optionalFieldOf("wolf_until", 0L).forGetter(memory -> memory.wolfUntil),
                BlockPos.CODEC.optionalFieldOf("wolf_pos").forGetter(memory -> Optional.ofNullable(memory.wolfPos)),
                Codec.STRING.optionalFieldOf("effect", "").forGetter(memory -> memory.effect),
                Codec.LONG.optionalFieldOf("effect_until", 0L).forGetter(memory -> memory.effectUntil),
                Codec.FLOAT.optionalFieldOf("effect_x", 0.0F).forGetter(memory -> memory.effectX),
                Codec.FLOAT.optionalFieldOf("effect_y", 0.0F).forGetter(memory -> memory.effectY),
                Codec.FLOAT.optionalFieldOf("effect_stretch_x", 1.0F).forGetter(memory -> memory.effectStretchX),
                Codec.FLOAT.optionalFieldOf("effect_stretch_y", 1.0F).forGetter(memory -> memory.effectStretchY)
        ).apply(instance, PlayerMemory::create));

        private final List<String> used = new ArrayList<>();
        private long nextRoll;
        private String pending = "";
        private final List<BlockPos> walks = new ArrayList<>();
        private final List<BlockPos> torches = new ArrayList<>();
        private final List<PlacedBlock> placed = new ArrayList<>();
        private final List<PlacedBlock> vanish = new ArrayList<>();
        private UUID wolf;
        private long wolfUntil;
        private BlockPos wolfPos;
        private String effect = "";
        private long effectUntil;
        private float effectX;
        private float effectY;
        private float effectStretchX = 1.0F;
        private float effectStretchY = 1.0F;
        private boolean inventoryVisible;
        private HorrorMemory owner;

        private static PlayerMemory create(
                List<String> used,
                long nextRoll,
                String pending,
                List<BlockPos> walks,
                List<BlockPos> torches,
                List<PlacedBlock> placed,
                List<PlacedBlock> vanish,
                Optional<UUID> wolf,
                long wolfUntil,
                Optional<BlockPos> wolfPos,
                String effect,
                long effectUntil,
                float effectX,
                float effectY,
                float effectStretchX,
                float effectStretchY
        ) {
            PlayerMemory memory = new PlayerMemory();
            memory.used.addAll(used);
            memory.nextRoll = nextRoll;
            memory.pending = pending;
            memory.walks.addAll(walks);
            memory.torches.addAll(torches);
            memory.placed.addAll(placed);
            memory.vanish.addAll(vanish);
            memory.wolf = wolf.orElse(null);
            memory.wolfUntil = wolfUntil;
            memory.wolfPos = wolfPos.orElse(null);
            memory.effect = effect;
            memory.effectUntil = effectUntil;
            memory.effectX = effectX;
            memory.effectY = effectY;
            memory.effectStretchX = effectStretchX;
            memory.effectStretchY = effectStretchY;
            return memory;
        }

        void attach(HorrorMemory owner) {
            this.owner = owner;
        }

        public void dirty() {
            if (this.owner != null) {
                this.owner.setDirty();
            }
        }

        public boolean isUsed(HorrorEvent event) {
            return this.used.contains(event.id());
        }

        public int usedCount() {
            return this.used.size();
        }

        public void markUsed(HorrorEvent event) {
            if (this.used.contains(event.id())) {
                return;
            }
            this.used.add(event.id());
            dirty();
        }

        public long nextRoll() {
            return this.nextRoll;
        }

        public void setNextRoll(long nextRoll) {
            this.nextRoll = nextRoll;
            dirty();
        }

        public String pendingId() {
            int split = this.pending.lastIndexOf('@');
            return split < 0 ? this.pending : this.pending.substring(0, split);
        }

        public long pendingSince() {
            int split = this.pending.lastIndexOf('@');
            if (split < 0 || split == this.pending.length() - 1) {
                return 0L;
            }
            try {
                return Long.parseLong(this.pending.substring(split + 1));
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }

        /**
         * {@code now} is the game time the wait started. Clearing the event drops the timer.
         * The same event keeps its original start.
         */
        public void setPending(HorrorEvent event, long now) {
            String id = event == null ? "" : event.id();
            if (pendingId().equals(id)) {
                if (event != null && pendingSince() == 0L && now != 0L) {
                    this.pending = id + "@" + now;
                    dirty();
                }
                return;
            }
            this.pending = event == null ? "" : id + "@" + now;
            dirty();
        }

        public boolean inventoryVisible() {
            return this.inventoryVisible;
        }

        public void setInventoryVisible(boolean inventoryVisible) {
            this.inventoryVisible = inventoryVisible;
        }

        public void recordWalk(BlockPos pos) {
            BlockPos stored = copy(pos);
            if (!this.walks.isEmpty()) {
                BlockPos last = this.walks.get(this.walks.size() - 1);
                int dx = stored.getX() - last.getX();
                int dz = stored.getZ() - last.getZ();
                if (dx * dx + dz * dz < 64) {
                    return;
                }
            }
            this.walks.add(stored);
            trim(this.walks, WALK_CAP);
            dirty();
        }

        public void recordPlacement(BlockPos pos, String blockId, boolean torch) {
            BlockPos stored = copy(pos);
            this.placed.removeIf(existing -> existing.pos().equals(stored));
            this.placed.add(new PlacedBlock(stored, blockId));
            trim(this.placed, PLACED_CAP);
            this.torches.remove(stored);
            if (torch) {
                this.torches.add(stored);
                trim(this.torches, TORCH_CAP);
            }
            dirty();
        }

        public List<BlockPos> walks() {
            return this.walks;
        }

        public List<BlockPos> torches() {
            return this.torches;
        }

        public List<PlacedBlock> placed() {
            return this.placed;
        }

        public List<PlacedBlock> vanish() {
            return this.vanish;
        }

        public void setVanish(List<PlacedBlock> blocks) {
            this.vanish.clear();
            this.vanish.addAll(blocks);
            dirty();
        }

        public void clearVanish() {
            if (this.vanish.isEmpty()) {
                return;
            }
            this.vanish.clear();
            dirty();
        }

        public UUID wolf() {
            return this.wolf;
        }

        public long wolfUntil() {
            return this.wolfUntil;
        }

        public BlockPos wolfPos() {
            return this.wolfPos;
        }

        public void setWolf(UUID wolf, long wolfUntil, BlockPos wolfPos) {
            this.wolf = wolf;
            this.wolfUntil = wolfUntil;
            this.wolfPos = wolfPos;
            dirty();
        }

        public void clearWolf() {
            if (this.wolf == null && this.wolfPos == null) {
                return;
            }
            this.wolf = null;
            this.wolfUntil = 0L;
            this.wolfPos = null;
            dirty();
        }

        public boolean effectActive(long now) {
            return !this.effect.isEmpty() && now < this.effectUntil;
        }

        public void setEffect(String effect, long effectUntil, float x, float y, float stretchX, float stretchY) {
            this.effect = effect;
            this.effectUntil = effectUntil;
            this.effectX = x;
            this.effectY = y;
            this.effectStretchX = stretchX;
            this.effectStretchY = stretchY;
            dirty();
        }

        public String effect() {
            return this.effect;
        }

        public long effectUntil() {
            return this.effectUntil;
        }

        public float effectX() {
            return this.effectX;
        }

        public float effectY() {
            return this.effectY;
        }

        public float effectStretchX() {
            return this.effectStretchX;
        }

        public float effectStretchY() {
            return this.effectStretchY;
        }

        private static BlockPos copy(BlockPos pos) {
            return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        }

        private static void trim(List<?> list, int cap) {
            while (list.size() > cap) {
                list.remove(0);
            }
        }
    }
}
