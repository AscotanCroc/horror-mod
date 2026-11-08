package com.horrormod.content;

import com.horrormod.HorrorMod;
import com.horrormod.entity.HorrorEntities;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class HorrorTabs {
    public static final ResourceKey<CreativeModeTab> SAMPLE_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            HorrorMod.id("sample")
    );

    public static final CreativeModeTab SAMPLE = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            SAMPLE_TAB,
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.horror_mod.sample"))
                    .icon(() -> new ItemStack(HorrorBlocks.SAMPLE_BLOCK))
                    .displayItems((parameters, output) -> {
                        output.accept(HorrorBlocks.SAMPLE_BLOCK);
                        output.accept(HorrorBlocks.STONE_LECTERN);
                        output.accept(HorrorEntities.REVERSE_ZOMBIE_SPAWN_EGG);
                        output.accept(HorrorEntities.GLITCH);
                        output.accept(HorrorEntities.REVERSE_ENDERMAN_SPAWN_EGG);
                        output.accept(HorrorEntities.REVERSE_CREEPER_SPAWN_EGG);
                        output.accept(HorrorEntities.REVERSE_SKELETON_SPAWN_EGG);
                        output.accept(HorrorEntities.REVERSE_SPIDER_SPAWN_EGG);
                        output.accept(HorrorBlocks.BLACK_COBWEB);
                        output.accept(HorrorEntities.SAVIOR);
                        output.accept(HorrorEntities.VEIL);
                        output.accept(HorrorEntities.NULL_POINTER);
                        output.accept(HorrorEntities.WATDATMEAN_SPAWN_EGG);
                    })
                    .build()
    );

    private HorrorTabs() {
    }

    public static void initialize() {
        HorrorMod.LOGGER.info("Registered sample creative tab {}", SAMPLE_TAB);
    }
}
