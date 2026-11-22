package com.horrormod.client.mixin;

import com.horrormod.HorrorMod;
import com.horrormod.client.event.ImpurityFlicker;
import com.horrormod.client.render.ImpurityItemPipeline;
import com.horrormod.entity.HorrorEntities;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFeatureRenderer.class)
abstract class ImpurityHeldItemMixin {
    private static final Identifier SPRITE = HorrorMod.id("item/impurity_detector");

    @Inject(
            method = "getLayerColorSafe([ILnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void horror$impurityStrength(int[] tints, BakedQuad.MaterialInfo info, CallbackInfoReturnable<Integer> callback) {
        if (!SPRITE.equals(info.sprite().contents().name())) {
            return;
        }
        float strength = ImpurityFlicker.itemGlitch();
        if (strength <= 0.0F) {
            return;
        }
        int alpha = Math.round((1.0F - Mth.clamp(strength, 0.0F, 1.0F)) * 255.0F);
        callback.setReturnValue((alpha << 24) | 0xFFFFFF);
    }
}

@Mixin(GuiGraphicsExtractor.class)
abstract class ImpurityGuiItemMixin {
    private static final Identifier TEXTURE = HorrorMod.id("textures/item/impurity_detector.png");

    @Inject(
            method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void horror$impurityIcon(
            LivingEntity entity,
            Level level,
            ItemStack stack,
            int x,
            int y,
            int seed,
            CallbackInfo callback
    ) {
        float strength = ImpurityFlicker.itemGlitch();
        if (strength <= 0.0F || !stack.is(HorrorEntities.IMPURITY_DETECTOR)) {
            return;
        }
        callback.cancel();
        int alpha = Math.round((1.0F - Mth.clamp(strength, 0.0F, 1.0F)) * 255.0F);
        int color = (alpha << 24) | 0xFFFFFF;
        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
        graphics.blit(
                ImpurityItemPipeline.GUI,
                TEXTURE,
                x,
                y,
                0.0F,
                0.0F,
                16,
                16,
                16,
                16,
                16,
                16,
                color
        );
    }
}
