package me.skitttyy.kami.api.gui.component.impl;

import me.skitttyy.kami.api.gui.helpers.IDraggable;
import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.flow.Flow;
import me.skitttyy.kami.api.gui.flow.impl.LinearFlow;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;

public class FrameComponent implements IComponent, IDraggable {

    String titleText;
    Rect dims;
    Rect title;
    boolean open;
    boolean dragging;
    int dragX;
    int dragY;
    Flow flow;

    public FrameComponent(String title, Rect dims)
    {
        this.titleText = title;
        this.dims = dims;
        this.title = dims;
        open = true;
        dragging = false;
        flow = new LinearFlow(new Rect(0, 0, 0, 0), 1);
    }

    public String getTitleText()
    {
        return titleText;
    }

    public void setTitleText(String titleText)
    {
        this.titleText = titleText;
    }

    @Override
    public void draw(Context context, MouseHelper mouse)
    {
        if (dragging)
        {
            drag(getDims(), dragX, dragY, mouse);
        }
        flow.getDims().setX(getDims().getX() + context.getMetrics().getSpacing());
        flow.getDims().setY(getDims().getY() + context.getMetrics().getFrameHeight() + context.getMetrics().getSpacing());
        flow.getDims().setWidth(getDims().getWidth() - context.getMetrics().getSpacing() * 2);
        // flow.getDims().setHeight(getDims().getHeight() + context.getMetrics().getSpacing() * 2);
        flow.positionComponents(context);
        getDims().setHeight(title.getHeight() + flow.getDims().getHeight() + context.getMetrics().getSpacing() * 2);

        title = new Rect(getDims().getX(), getDims().getY(), getDims().getWidth(), context.getMetrics().getFrameHeight());
        getDims().setWidth(context.getMetrics().getFrameWidth());

        if (open)
        {
            context.getRenderer().renderFrame(context, getDims(), mouse);
        } else
        {
            context.getRenderer().renderFrameOutline(context, title, mouse);
        }

        context.getRenderer().renderFrameTitle(context, title, mouse, getTitleText(), this.open);

        if (open)
        {
            flow.draw(context, mouse);
            context.getRenderer().renderFrameOutline(context, getDims(), mouse);
        }


    }

    @Override
    public void click(Context context, MouseHelper mouse, int button)
    {
        if (title.collideWithMouse(mouse))
        {
            if (button == 1)
            {
                open = !open;
            } else
            {
                if (isDraggable())
                {
                    dragging = true;
                    dragX = mouse.getX() - getDims().getX();
                    dragY = mouse.getY() - getDims().getY();
                }
            }
        }

        if (open)
        {

            flow.click(context, mouse, button);
        }
    }

    @Override
    public void release(Context context, MouseHelper mouse, int state)
    {
        dragging = false;
        if (open)
        {

            flow.release(context, mouse, state);
        }
    }

    @Override
    public void key(Context context, int key, char keycode)
    {
        if (open)
        {
            flow.key(context, key, keycode);
        }
    }

    @Override
    public void charTyped(Context context, char character)
    {
        if (open)
        {
            flow.charTyped(context, character);
        }
    }

    @Override
    public int getLevel()
    {
        return 0;
    }

    @Override
    public Rect getDims()
    {
        return dims;
    }

    public Flow getFlow()
    {
        return flow;
    }

    public void setFlow(Flow flow)
    {
        this.flow = flow;
    }

    @Override
    public boolean isDraggable()
    {
        return true;
    }

    @Override
    public boolean isActive()
    {
        return true;
    }
}
