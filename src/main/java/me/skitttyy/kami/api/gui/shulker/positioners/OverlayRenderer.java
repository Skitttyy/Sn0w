package me.skitttyy.kami.api.gui.shulker.positioners;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public abstract class OverlayRenderer {

    ItemStack stack;

    int stackX;
    int stackY;

    public OverlayRenderer(ItemStack stack, int x, int y) {
        this.stack = stack;
        this.stackX = x;
        this.stackY = y;
    }

    public void renderOptional(DrawContext context) {
        if(canDisplay()) {
            calculatePositions();
            render(context);
        }
    }

    protected abstract boolean canDisplay();
    protected abstract void calculatePositions();
    protected abstract void render(DrawContext context);
}
