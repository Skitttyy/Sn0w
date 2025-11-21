package me.skitttyy.kami.api.gui.shulker.positioners;

 
import me.skitttyy.kami.api.gui.shulker.GlobalValues;
import me.skitttyy.kami.api.gui.shulker.container.ContainerManager;
import me.skitttyy.kami.api.gui.shulker.data.IconScale;
import me.skitttyy.kami.api.utils.ducks.IDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class IconRenderer extends OverlayRenderer {

    IconScale iconPositionOptions;

    public float scale;
    public float xOffset;
    public float yOffset;
    public float zOffset;

    public IconRenderer(ContainerManager containerParser, ItemStack displayStack, int x, int y) {
        super(displayStack, x, y);
        setPositionOptions(containerParser);
    }

    private void setPositionOptions(ContainerManager containerParser) {
        switch(containerParser.getContainerType()) {
            case SHULKER_BOX:
                iconPositionOptions = (
                    containerParser.getStackSize() > 1 ?
                        GlobalValues.iconPositionOptionsStacked :
                        GlobalValues.iconPositionOptionsGeneral
                );
                break;
            case BUNDLE:
                iconPositionOptions = GlobalValues.iconPositionOptionsBundle;
                break;
            case OTHER:
                iconPositionOptions = GlobalValues.iconPositionOptionsGeneral;
                break;
            case NONE:
                break;
        }
    }

    protected void calculatePositions() {
        // Normal icon location
        xOffset = (float)iconPositionOptions.translateX - 8.0f;
        yOffset = (float)iconPositionOptions.translateY - 8.0f;
        zOffset = 100.0f + (float)(iconPositionOptions.translateZ * 10);

        scale = iconPositionOptions.scale;
    }

    protected void render(DrawContext context) {
        ((IDrawContext) context).adjustSize(true);
        context.drawItemWithoutEntity(stack, stackX, stackY);
        ((IDrawContext) context).adjustSize(false);
    }

    public void renderOptional(DrawContext context) {
        if(canDisplay()) {
            calculatePositions();
            render(context);
        }
    }

    @Override
    protected boolean canDisplay() {
        return stack != null && stack.getItem() != null;
    }
}
