package com.horrormod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

/**
 * Torches and other block lights near a reverse creeper stop emitting.
 */
public final class ReverseCreeperDark {
    public static final int RADIUS = 15;
    private static final int RADIUS_SQR = RADIUS * RADIUS;

    private ReverseCreeperDark() {
    }

    public static boolean snuffs(Level level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(RADIUS);
        return !level.getEntities(
                EntityTypeTest.forClass(ReverseCreeper.class),
                box,
                creeper -> creeper.isAlive() && creeper.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= RADIUS_SQR
        ).isEmpty();
    }

    public static void refresh(ServerLevel level, BlockPos center) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz > RADIUS_SQR) {
                        continue;
                    }
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (!level.isLoaded(cursor)) {
                        continue;
                    }
                    if (level.getBlockState(cursor).getLightEmission() > 0) {
                        level.getLightEngine().checkBlock(cursor);
                    }
                }
            }
        }
    }
}
