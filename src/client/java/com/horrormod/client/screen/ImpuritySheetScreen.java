package com.horrormod.client.screen;

import com.horrormod.HorrorMod;
import com.horrormod.entity.HorrorEntities;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The impurity sheet: a still page with the detector's crafting grid.
 */
public class ImpuritySheetScreen extends Screen {
    private static final Identifier PAPER = HorrorMod.id("textures/gui/impurity_sheet.png");
    private static final int PAPER_WIDTH = 256;
    private static final int PAPER_HEIGHT = 192;

    private final ItemStack iron = new ItemStack(Items.IRON_INGOT);
    private final ItemStack diamond = new ItemStack(Items.DIAMOND);
    private final ItemStack result = new ItemStack(HorrorEntities.IMPURITY_DETECTOR);

    public ImpuritySheetScreen() {
        super(Component.translatable("screen.horror_mod.impurity_sheet"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = (this.width - PAPER_WIDTH) / 2;
        int y = (this.height - PAPER_HEIGHT) / 2;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                PAPER,
                x,
                y,
                0.0F,
                0.0F,
                PAPER_WIDTH,
                PAPER_HEIGHT,
                PAPER_WIDTH,
                PAPER_HEIGHT
        );
        int gridX = x + 70;
        int gridY = y + 58;
        for (int index = 0; index < 9; index++) {
            ItemStack stack = switch (index) {
                case 1, 3, 5, 7 -> this.iron;
                case 4 -> this.diamond;
                default -> ItemStack.EMPTY;
            };
            if (stack.isEmpty()) {
                continue;
            }
            graphics.item(stack, gridX + (index % 3) * 18, gridY + (index / 3) * 18);
        }
        graphics.item(this.result, x + 168, gridY + 18);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
}
