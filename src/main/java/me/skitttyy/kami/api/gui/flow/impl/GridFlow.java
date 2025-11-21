package me.skitttyy.kami.api.gui.flow.impl;

import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.flow.Flow;

public class GridFlow extends Flow {

    int rows;
    int columns;
    boolean autoHeight = false;
    boolean autoColumns = true;

    public GridFlow(Rect dims, int rows, int columns, int level) {
        super(dims, level);
        this.rows = rows;
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public boolean doesAutoHeight() {
        return autoHeight;
    }

    public void setAutoHeight(boolean autoHeight) {
        this.autoHeight = autoHeight;
    }

    public boolean doesAutoColumns() {
        return autoColumns;
    }

    public void setAutoColumns(boolean autoColumns) {
        this.autoColumns = autoColumns;
    }

    @Override
    public void positionComponents(Context context) {
        super.positionComponents(context);
        int index = 0;
        int indexInColumn = 0;
        int componentWidth = (getDims().getWidth() - (columns - 1) * context.getMetrics().getBetweenSpacing()) / columns;
        int componentHeight = (getDims().getHeight() - (rows - 1) * context.getMetrics().getBetweenSpacing()) / rows;


        for (IComponent component : getComponents()){
            int componentX = componentWidth * indexInColumn + (indexInColumn - 1) * context.getMetrics().getBetweenSpacing();
            component.getDims().setX(getDims().getX() + componentX);
            component.getDims().setWidth(componentWidth);
            component.getDims().setY(getDims().getY() + index * componentHeight + context.getMetrics().getBetweenSpacing());
            if (autoHeight){
                component.getDims().setHeight(componentHeight);
            }
            indexInColumn++;
            if (indexInColumn == columns){
                indexInColumn = 0;
                index++;
            }

        }
    }
}
