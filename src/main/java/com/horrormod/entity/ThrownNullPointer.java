package com.horrormod.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownNullPointer extends ThrowableItemProjectile {
    public ThrownNullPointer(EntityType<? extends ThrownNullPointer> type, Level level) {
        super(type, level);
    }

    public ThrownNullPointer(Level level, LivingEntity shooter, ItemStack stack) {
        super(HorrorEntities.THROWN_NULL_POINTER, shooter, level, stack);
    }

    public static ThrownNullPointer create(ServerLevel level, LivingEntity shooter, ItemStack stack) {
        return new ThrownNullPointer(level, shooter, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return HorrorEntities.NULL_POINTER;
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level().isClientSide() || !(this.level() instanceof ServerLevel server)) {
            return;
        }
        NullClone.appear(server, this);
        this.discard();
    }
}
