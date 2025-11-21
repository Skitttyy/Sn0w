package me.skitttyy.kami.api.gui.flow.impl;

import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.flow.Flow;

public class LinearFlow extends Flow {

    boolean setLength = true;

    public LinearFlow(Rect dims, int level){
        super(dims, level);
    }

    public boolean doesAutoLength() {
        return setLength;
    }

    public void setAutoLength(boolean setLength) {
        this.setLength = setLength;
    }

    @Override
    public void positionComponents(Context context) {
        super.positionComponents(context);
        int index = 0;
        int offset = 0;
        int betweenOffset = getLevel() > 1 ? context.getMetrics().getSettingSpacing() : context.getMetrics().getBetweenSpacing();
        int endOffset = getLevel() > 1 ? -context.getMetrics().getBetweenSpacing() : context.getMetrics().getBetweenSpacing();

        for (IComponent component : getComponents()){
            component.getDims().setX(dims.getX());
            component.getDims().setY(dims.getY() + offset);
            component.getDims().setWidth(dims.getWidth());

            if (component.isActive()) {
                offset += component.getDims().getHeight() + betweenOffset;
            }
            index++;
        }

        if (doesAutoLength()){
            getDims().setHeight(offset - endOffset);
        }
    }
}
