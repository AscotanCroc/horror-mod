package com.horrormod.entity;

import com.horrormod.HorrorMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class HorrorDamageTypes {
    public static final ResourceKey<DamageType> PASSED_AWAY = ResourceKey.create(Registries.DAMAGE_TYPE, HorrorMod.id("passed_away"));

    private HorrorDamageTypes() {
    }
}
