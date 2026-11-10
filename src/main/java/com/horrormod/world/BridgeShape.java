package com.horrormod.world;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rows of bridges in the void. Each bridge runs along Z and repeats every {@link #SPAN} blocks.
 * Rows are spaced along X. Replace {@link #segment} when the bridge should be a different build.
 * {@code dx} is across the deck (0 at the center), {@code dy} is up from the walkway, and {@code along} is world Z.
 */
public final class BridgeShape {
    /** Walkway block, above the clouds. Feet stand one above this. */
    public static final int WALK_Y = 288;
    public static final double FEET_Y = WALK_Y + 1.0;
    /** Distance between bridge centerlines. */
    public static final int ROW_SPACING = 32;
    /** Length of one repeating span along the bridge. */
    public static final int SPAN = 16;

    private static final int HALF_WIDTH = 2;
    private static final int GIRDER_Y = -2;
    private static final int TIE_Y = -8;
    /** Stilt ends at y = -48, just above the bottom of the veil. */
    private static final int PIER_BOTTOM = -336;

    private static final BlockState PATH = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState STONE = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState BODY = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState LANTERN = Blocks.SOUL_LANTERN.defaultBlockState();

    private BridgeShape() {
    }

    public static int minBlockY() {
        return WALK_Y + PIER_BOTTOM;
    }

    public static int maxBlockY() {
        return WALK_Y + 2;
    }

    public static boolean onBridge(int x) {
        return Math.abs(x - rowCenter(x)) <= HALF_WIDTH;
    }

    /**
     * @return the block at this world position, or null where the void stays empty
     */
    public static BlockState block(int x, int y, int z) {
        int dx = x - rowCenter(x);
        if (dx < -HALF_WIDTH || dx > HALF_WIDTH) {
            return null;
        }
        return segment(dx, y - WALK_Y, z);
    }

    private static int rowCenter(int x) {
        return Math.floorDiv(x + ROW_SPACING / 2, ROW_SPACING) * ROW_SPACING;
    }

    private static BlockState segment(int dx, int dy, int along) {
        int ax = Math.abs(dx);
        int dz = Math.floorMod(along, SPAN);
        if (dy == 2 && (dz == 0 || dz == 8) && dx == lanternSide(along)) {
            return LANTERN;
        }
        if (dy == 1 && ax == HALF_WIDTH) {
            return STONE;
        }
        if (dy == 0 && ax <= HALF_WIDTH) {
            return ax == HALF_WIDTH ? STONE : PATH;
        }
        if (dy == -1 && ax <= HALF_WIDTH) {
            return BODY;
        }
        if (dy == GIRDER_Y && ax == HALF_WIDTH) {
            return STONE;
        }
        if (dz != 0) {
            return null;
        }
        if (dx == 0 && dy <= TIE_Y && dy >= PIER_BOTTOM) {
            return BODY;
        }
        if (dy == TIE_Y && ax <= HALF_WIDTH) {
            return STONE;
        }
        if (dy < GIRDER_Y && dy > TIE_Y && ax == HALF_WIDTH) {
            return STONE;
        }
        if (dy == PIER_BOTTOM && ax <= 1) {
            return BODY;
        }
        return null;
    }

    private static int lanternSide(int along) {
        return (Math.floorDiv(along, 8) & 1) == 0 ? -HALF_WIDTH : HALF_WIDTH;
    }
}
