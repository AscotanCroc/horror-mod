package com.horrormod.world;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

/**
 * Rows of bridges in the void. Each bridge runs along Z and repeats every {@link #SPAN} blocks.
 * Rows are spaced along X. One span in {@link #TOWER_ODDS} is a tower instead: a round shaft
 * from the floor to the walkway, open at the base, with a circular stair that meets the
 * neighboring spans at the deck.
 */
public final class BridgeShape {
    /** Walkway block, above the clouds. Feet stand one above this. */
    public static final int WALK_Y = 288;
    /** Distance between bridge centerlines. */
    public static final int ROW_SPACING = 32;
    /** Length of one repeating span along the bridge. */
    public static final int SPAN = 16;
    /** A span becomes a tower when its hash falls on this modulus. */
    public static final int TOWER_ODDS = 200;

    private static final int HALF_WIDTH = 2;
    private static final int GIRDER_Y = -2;
    private static final int TIE_Y = -8;
    /** Bottom of the veil. The stilts stand on this layer. Matches {@code dimension_type/veil.json}. */
    public static final int FLOOR_Y = -64;
    private static final int PIER_BOTTOM = FLOOR_Y - WALK_Y;

    /** Outer radius of a tower. The circle sits in the span, with one block of deck left at one end. */
    private static final int TOWER_RADIUS = 7;
    private static final int TOWER_RADIUS_SQ = TOWER_RADIUS * TOWER_RADIUS;
    /** Lanterns sit one block outside the shell, in front of each gate. */
    private static final int ENTRANCE = TOWER_RADIUS + 1;
    /** Open shaft inside this distance. The shell is everything farther out, through {@link #TOWER_RADIUS}. */
    private static final int ROOM_SQ = 25;
    private static final int BRICK_SQ = 36;
    /** Deck cuts in where the bridge axis leaves the open room. */
    private static final int CAP = 5;
    /** Tower center measured from the first block of the span. */
    private static final int CENTER = 7;
    private static final int GATE_HALF = 2;
    private static final int GATE_HEIGHT = 7;
    private static final int LAMP_GAP = 8;

    private static final BlockState PATH = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState STONE = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState BODY = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState LANTERN = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState HANGING_LAMP = Blocks.SOUL_LANTERN.defaultBlockState()
            .setValue(BlockStateProperties.HANGING, true);

    /**
     * Orthogonal loop just inside the shell, one block of rise per step.
     * Index 0 is east; the walk turns toward +Z.
     */
    private static final int[] RING_X = {
            4, 4, 4, 3, 3, 2, 2, 1,
            0, -1, -2, -2, -3, -3, -4, -4,
            -4, -4, -4, -3, -3, -2, -2, -1,
            0, 1, 2, 2, 3, 3, 4, 4
    };
    private static final int[] RING_Z = {
            0, 1, 2, 2, 3, 3, 4, 4,
            4, 4, 4, 3, 3, 2, 2, 1,
            0, -1, -2, -2, -3, -3, -4, -4,
            -4, -4, -4, -3, -3, -2, -2, -1
    };
    private static final Direction[] RING_FACING = new Direction[RING_X.length];
    private static final BlockState[] STAIRS = new BlockState[6];
    /** Step index for a tower-local column, or -1 when that column is not a tread. */
    private static final int[][] TREAD = new int[TOWER_RADIUS * 2 + 1][TOWER_RADIUS * 2 + 1];

    static {
        for (int x = 0; x < TREAD.length; x++) {
            for (int z = 0; z < TREAD[x].length; z++) {
                TREAD[x][z] = -1;
            }
        }
        if (RING_X.length != RING_Z.length) {
            throw new IllegalStateException("stair ring");
        }
        for (int step = 0; step < RING_X.length; step++) {
            int x = RING_X[step];
            int z = RING_Z[step];
            int next = (step + 1) % RING_X.length;
            int manhattan = Math.abs(x - RING_X[next]) + Math.abs(z - RING_Z[next]);
            if (manhattan != 1 || x * x + z * z >= ROOM_SQ) {
                throw new IllegalStateException("stair ring breaks at " + step);
            }
            int sx = x + TOWER_RADIUS;
            int sz = z + TOWER_RADIUS;
            if (TREAD[sx][sz] != -1) {
                throw new IllegalStateException("stair ring repeats " + x + "," + z);
            }
            TREAD[sx][sz] = step;
            int previous = Math.floorMod(step - 1, RING_X.length);
            int towardX = RING_X[previous] - x;
            int towardZ = RING_Z[previous] - z;
            RING_FACING[step] = towardX != 0
                    ? (towardX > 0 ? Direction.EAST : Direction.WEST)
                    : (towardZ > 0 ? Direction.SOUTH : Direction.NORTH);
            int ix = x == 0 ? 0 : x - Integer.signum(x);
            int iz = z == 0 ? 0 : z - Integer.signum(z);
            if ((ix != 0 || iz != 0) && ix * ix + iz * iz < ROOM_SQ && TREAD[ix + TOWER_RADIUS][iz + TOWER_RADIUS] == -1) {
                TREAD[ix + TOWER_RADIUS][iz + TOWER_RADIUS] = step;
            }
        }
        BlockState stair = Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.HALF, Half.BOTTOM)
                .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT)
                .setValue(StairBlock.WATERLOGGED, false);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            STAIRS[facing.ordinal()] = stair.setValue(StairBlock.FACING, facing);
        }
    }

    private BridgeShape() {
    }

    public static int minBlockY() {
        return FLOOR_Y;
    }

    public static int maxBlockY() {
        return WALK_Y + 2;
    }

    public static boolean onBridge(int x) {
        return Math.abs(x - rowCenter(x)) <= HALF_WIDTH;
    }

    /** True when a bridge or a tower can place blocks at this x. */
    public static boolean nearRow(int x) {
        return Math.abs(x - rowCenter(x)) <= ENTRANCE;
    }

    /** True when this column holds a bridge, or a tower that replaced its span. */
    public static boolean generates(int x, int z) {
        int dx = Math.abs(x - rowCenter(x));
        if (dx <= HALF_WIDTH) {
            return true;
        }
        return dx <= ENTRANCE && towerRoll(rowCenter(x), Math.floorDiv(z, SPAN));
    }

    /**
     * @return the block at this world position, or null where the void stays empty
     */
    public static BlockState block(int x, int y, int z) {
        int row = rowCenter(x);
        int dx = x - row;
        if (towerRoll(row, Math.floorDiv(z, SPAN))) {
            return tower(dx, y, z);
        }
        if (dx < -HALF_WIDTH || dx > HALF_WIDTH) {
            return null;
        }
        return segment(dx, y - WALK_Y, z);
    }

    /**
     * The bridge that stood here before towers. Old chunks still use it to finish stilts
     * that stop short of the floor.
     */
    static BlockState bridgeOnly(int x, int y, int z) {
        int dx = x - rowCenter(x);
        if (dx < -HALF_WIDTH || dx > HALF_WIDTH) {
            return null;
        }
        return segment(dx, y - WALK_Y, z);
    }

    private static int rowCenter(int x) {
        return Math.floorDiv(x + ROW_SPACING / 2, ROW_SPACING) * ROW_SPACING;
    }

    private static boolean towerRoll(int row, int segment) {
        long mixed = (row * 341873128712L) ^ (segment * 132897987541L) ^ 0x70E11L;
        mixed = (mixed ^ (mixed >>> 33)) * 0xff51afd7ed558ccdL;
        mixed = (mixed ^ (mixed >>> 33)) * 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        return Math.floorMod(mixed, TOWER_ODDS) == 0;
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

    private static BlockState tower(int dx, int y, int z) {
        int origin = Math.floorDiv(z, SPAN) * SPAN;
        int dz = z - (origin + CENTER);
        int dist2 = dx * dx + dz * dz;
        int ax = Math.abs(dx);
        int dy = y - WALK_Y;

        if (ax == ENTRANCE && dz == 0 && y == FLOOR_Y + 1) {
            return LANTERN;
        }

        if (ax <= HALF_WIDTH && Math.abs(dz) >= CAP) {
            if (dy == 0) {
                return ax == HALF_WIDTH ? STONE : PATH;
            }
            if (dy == 1 && ax == HALF_WIDTH) {
                return STONE;
            }
            if (dy == 2 && ax == HALF_WIDTH && (dz == -TOWER_RADIUS || dz == TOWER_RADIUS + 1)) {
                return LANTERN;
            }
            if (dist2 > TOWER_RADIUS_SQ) {
                if (dy == -1) {
                    return BODY;
                }
                if (dy == GIRDER_Y && ax == HALF_WIDTH) {
                    return STONE;
                }
                return null;
            }
            if (dy == 1 || dy == 2) {
                return null;
            }
        }

        if (isGate(dx, dz, y, dist2)) {
            return null;
        }
        if (dist2 > TOWER_RADIUS_SQ || y <= FLOOR_Y || y > WALK_Y + 2) {
            return null;
        }
        if (dist2 >= ROOM_SQ) {
            return dist2 >= BRICK_SQ ? STONE : BODY;
        }
        return interior(dx, dz, y);
    }

    /** East and west arches at the floor. The top of the arch steps in. */
    private static boolean isGate(int dx, int dz, int y, int dist2) {
        if (Math.abs(dx) < 5 || dist2 > TOWER_RADIUS_SQ) {
            return false;
        }
        int above = y - FLOOR_Y;
        if (above < 1 || above > GATE_HEIGHT) {
            return false;
        }
        int half = GATE_HALF;
        int fromTop = GATE_HEIGHT - above;
        if (fromTop <= 0) {
            half = 0;
        } else if (fromTop == 1) {
            half = 1;
        }
        return Math.abs(dz) <= half;
    }

    private static BlockState interior(int dx, int dz, int y) {
        if (y == WALK_Y) {
            int step = tread(dx, dz);
            if (step >= 0 && step == stepIndex(WALK_Y - 1)) {
                return null;
            }
            return PATH;
        }
        if (y >= WALK_Y || y <= FLOOR_Y) {
            return null;
        }
        if (dx == 0 && dz == 0) {
            return shaft(y);
        }
        int step = tread(dx, dz);
        if (step < 0 || step != stepIndex(y)) {
            return null;
        }
        return STAIRS[RING_FACING[step].ordinal()];
    }

    /** A blackstone peg every few levels, with a soul lantern hanging under it. */
    private static BlockState shaft(int y) {
        int above = y - FLOOR_Y;
        if (above % LAMP_GAP == 0) {
            return BODY;
        }
        if ((above + 1) % LAMP_GAP == 0) {
            return HANGING_LAMP;
        }
        return null;
    }

    private static int stepIndex(int y) {
        return Math.floorMod(y - (FLOOR_Y + 1), RING_X.length);
    }

    private static int tread(int dx, int dz) {
        if (dx < -TOWER_RADIUS || dx > TOWER_RADIUS || dz < -TOWER_RADIUS || dz > TOWER_RADIUS) {
            return -1;
        }
        return TREAD[dx + TOWER_RADIUS][dz + TOWER_RADIUS];
    }
}
