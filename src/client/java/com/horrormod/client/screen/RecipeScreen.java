package com.horrormod.client.screen;

import com.horrormod.entity.HorrorEntities;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The lectern recipe: a crafting grid and its result, drifting with a fading rainbow trail.
 */
public class RecipeScreen extends Screen {
    private static final Identifier CRAFTING_TABLE = Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");
    /** Crop of the crafting texture that contains the grid, arrow, and result slot. */
    private static final int CROP_U = 20;
    private static final int CROP_V = 10;
    private static final int IMAGE_WIDTH = 140;
    private static final int IMAGE_HEIGHT = 66;
    private static final int TRAIL = 8;

    private final ItemStack bone = new ItemStack(Items.BONE);
    private final ItemStack iron = new ItemStack(Items.IRON_INGOT);
    private final ItemStack savior = new ItemStack(HorrorEntities.SAVIOR);
    private final ItemStack result = new ItemStack(HorrorEntities.VEIL);

    public RecipeScreen() {
        super(Component.translatable("screen.horror_mod.stone_lectern"));
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
        long clock = this.minecraft.level == null ? 0L : this.minecraft.level.getOverworldClockTime();
        float time = (float) (clock % 100000L) + partialTick;
        int[] current = panelOrigin(time);
        int[][] ghosts = new int[TRAIL][];
        float maxDistance = 1.0F;
        for (int ghost = 0; ghost < TRAIL; ghost++) {
            ghosts[ghost] = panelOrigin(time - (ghost + 1) * 3.0F);
            maxDistance = Math.max(maxDistance, distance(current, ghosts[ghost]));
        }
        for (int ghost = TRAIL - 1; ghost >= 0; ghost--) {
            float away = distance(current, ghosts[ghost]) / maxDistance;
            int alpha = Math.round(24.0F + 196.0F * (1.0F - away));
            drawRecipe(graphics, ghosts[ghost][0], ghosts[ghost][1], rainbow(time - ghost * 6.0F, alpha));
        }
        drawRecipe(graphics, current[0], current[1], 0xFFFFFFFF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void drawRecipe(GuiGraphicsExtractor graphics, int x, int y, int color) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                CRAFTING_TABLE,
                x,
                y,
                CROP_U,
                CROP_V,
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                256,
                256,
                color
        );
        int alpha = (color >>> 24) & 0xFF;
        if (alpha < 90) {
            return;
        }
        drawGrid(graphics, x, y);
        graphics.item(this.result, x + 124 - CROP_U, y + 35 - CROP_V);
    }

    private void drawGrid(GuiGraphicsExtractor graphics, int x, int y) {
        ItemStack[] grid = {
                this.bone, this.iron, this.savior,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        };
        for (int index = 0; index < grid.length; index++) {
            if (grid[index].isEmpty()) {
                continue;
            }
            int column = index % 3;
            int row = index / 3;
            graphics.item(grid[index], x + 30 - CROP_U + column * 18, y + 17 - CROP_V + row * 18);
        }
    }

    private int[] panelOrigin(float time) {
        float seconds = time / 20.0F;
        float nx = (float) (Math.sin(seconds * 0.28) * 0.58 + Math.sin(seconds * 0.10 + 0.6) * 0.42);
        float ny = (float) (Math.cos(seconds * 0.22) * 0.55 + Math.sin(seconds * 0.08 + 2.0) * 0.45);
        int spanX = Math.max(1, this.width - IMAGE_WIDTH);
        int spanY = Math.max(1, this.height - IMAGE_HEIGHT);
        int x = Math.round(spanX * (0.5F + nx * 0.18F));
        int y = Math.round(spanY * (0.5F + ny * 0.18F));
        return new int[] {x, y};
    }

    private static float distance(int[] a, int[] b) {
        float dx = a[0] - b[0];
        float dy = a[1] - b[1];
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static int rainbow(float time, int alpha) {
        float hue = Math.floorMod((int) (time * 3.0F), 1000) / 1000.0F;
        int rgb = Color.HSBtoRGB(hue, 0.95F, 1.0F) & 0xFFFFFF;
        return (alpha << 24) | rgb;
    }
}
