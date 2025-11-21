package me.skitttyy.kami.api.gui.shulker.positioners;


import me.skitttyy.kami.api.gui.shulker.GlobalValues;
import me.skitttyy.kami.api.gui.shulker.container.ContainerManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class CapacityBarRenderer extends OverlayRenderer {
    public static final int CAPACITY_BAR_COLOR_FILL = MathHelper.packRgb(0.4F, 0.4F, 1.0F);
    public static final int CAPACITY_BAR_COLOR_BACK = -16777216;

    float capacity;

    public int xBackgroundStart;
    public int yBackgroundStart;
    public int xBackgroundEnd;
    public int yBackgroundEnd;

    public int xCapacityStart;
    public int yCapacityStart;
    public int xCapacityEnd;
    public int yCapacityEnd;

    public CapacityBarRenderer(ContainerManager containerParser, ItemStack stack, int x, int y)
    {
        super(stack, x, y);
        this.capacity = containerParser.getCapacity();
    }

    protected boolean canDisplay()
    {
        return (
                (!GlobalValues.hideWhenEmpty || capacity > 0.0f) &&
                        (!GlobalValues.hideWhenFull || capacity < 1.0f)
        );
    }

    protected void calculatePositions()
    {
        int step = (int) (GlobalValues.length * capacity);
        int shadowHeight = GlobalValues.displayShadow ? 1 : 0;

        xBackgroundStart = stackX + GlobalValues.translateX;
        yBackgroundStart = stackY + GlobalValues.translateY;

        xBackgroundEnd = xBackgroundStart + GlobalValues.length;
        yBackgroundEnd = yBackgroundStart + GlobalValues.width + shadowHeight;
        xCapacityStart = xBackgroundStart;
        yCapacityStart = yBackgroundStart;
        xCapacityEnd = xBackgroundStart + step;
        yCapacityEnd = yCapacityStart + GlobalValues.width;
    }

    protected void render(DrawContext context)
    {
        context.fill(RenderLayer.getGuiOverlay(), xBackgroundStart, yBackgroundStart, xBackgroundEnd, yBackgroundEnd,
                CAPACITY_BAR_COLOR_BACK);
        context.fill(RenderLayer.getGuiOverlay(), xCapacityStart, yCapacityStart, xCapacityEnd, yCapacityEnd,
                CAPACITY_BAR_COLOR_FILL | CAPACITY_BAR_COLOR_BACK);
    }

    public void renderOptional(DrawContext context)
    {
        if (canDisplay())
        {
            calculatePositions();
            render(context);
        }
    }
}