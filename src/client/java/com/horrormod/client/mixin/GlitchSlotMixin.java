package com.horrormod.client.mixin;

import com.horrormod.entity.HorrorEntities;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class GlitchSlotMixin {
    private static final float SLOT_STRETCH = 4.0F;

    @Inject(
            method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At("HEAD")
    )
    private void horror$stretchSlotIcon(
            LivingEntity entity,
            Level level,
            ItemStack stack,
            int x,
            int y,
            int seed,
            CallbackInfo callback
    ) {
        if (!stack.is(HorrorEntities.GLITCH)) {
            return;
        }
        Matrix3x2fStack pose = ((GuiGraphicsExtractor) (Object) this).pose();
        float anchor = y + 16.0F;
        pose.pushMatrix();
        pose.translate(0.0F, anchor);
        pose.scale(1.0F, SLOT_STRETCH);
        pose.translate(0.0F, -anchor);
    }

    @Inject(
            method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At("RETURN")
    )
    private void horror$restoreSlotIcon(
            LivingEntity entity,
            Level level,
            ItemStack stack,
            int x,
            int y,
            int seed,
            CallbackInfo callback
    ) {
        if (!stack.is(HorrorEntities.GLITCH)) {
            return;
        }
        ((GuiGraphicsExtractor) (Object) this).pose().popMatrix();
    }
}
