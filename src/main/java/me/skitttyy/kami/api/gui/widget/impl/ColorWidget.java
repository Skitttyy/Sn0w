package me.skitttyy.kami.api.gui.widget.impl;

import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.render.animation.Easing;
import me.skitttyy.kami.api.utils.render.animation.type.DynamicColorAnimation;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import me.skitttyy.kami.api.gui.widget.IWidget;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static me.skitttyy.kami.api.wrapper.IMinecraft.mc;

public abstract class ColorWidget implements IWidget<Color>, IComponent {

    Sn0wColor value;
    String title;
    boolean open = false;
    Rect dims;
    Rect displayDims;
    Rect insideDims;
    Rect pickerRect;
    Rect alphaRect;
    Rect hueRect;
    Rect colorSquare;

    boolean draggingHue;
    boolean draggingColor;
    boolean draggingAlpha;
    boolean hovering;
    public DynamicColorAnimation animation;

    public ColorWidget(String title, Sn0wColor value, Rect dims)
    {
        this.title = title;
        this.value = value;
        this.dims = dims;
        displayDims = new Rect(0, 0, 0, 0);
        pickerRect = new Rect(0, 0, 0, 0);
        alphaRect = new Rect(0, 0, 0, 0);
        insideDims = new Rect(0, 0, 0, 0);
        hueRect = new Rect(0, 0, 0, 0);
        colorSquare = new Rect(0, 0, 0, 0);
        animation = new DynamicColorAnimation(ColorUtil.newAlpha(Color.WHITE, 0), Easing.CUBIC_IN_OUT, 300L);

    }

    @Override
    public Color getValue()
    {
        return value.getColor();
    }

    public void setSyncing(boolean sync)
    {
        this.value.setSync(!value.isSyncing());
    }

    public boolean getSyncing()
    {
        return this.value.isSyncing();
    }

    public boolean getUnsyncable()
    {
        return this.value.isUnsyncable();
    }

    @Override
    public void setValue(Color value)
    {
        this.value.setColor(value);
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public Rect getDisplayDims()
    {
        return open ? dims : displayDims;
    }

    @Override
    public void draw(Context context, MouseHelper mouse)
    {
        displayDims.setX(getDims().getX());
        displayDims.setY(getDims().getY());
        displayDims.setWidth(getDims().getWidth());
        displayDims.setHeight(context.getMetrics().getButtonHeight());

        float[] hsb = Color.RGBtoHSB(getValue().getRed(), getValue().getGreen(), getValue().getBlue(), null);

        int spacing = 1;

        int alphaSliderWidth = context.getMetrics().getButtonHeight();
        int squareSize = insideDims.getWidth() - (alphaSliderWidth + spacing * 3);
        hovering = false;

        int size = displayDims.getHeight() - 2;
        int rightX = displayDims.getX() + (displayDims.getWidth() - size - 1);
        Rect colorRect = new Rect(rightX, displayDims.getY() + 1, size, size);

        Color mainColor = getUnsyncable() ? Color.RED : Color.WHITE;
        Color goal = ColorUtil.newAlpha(mainColor, 0);
        if (getSyncing())
        {
            goal = mainColor;
        } else if (colorRect.collideWithMouse(context.getHelper()))
        {
            if (getUnsyncable())
            {
                goal = mainColor;
            } else
            {
                goal = ColorUtil.newAlpha(mainColor, 100);
            }
        }
        animation.gotoColor(goal);


        insideDims.setX(getDims().getX() + context.getMetrics().getSpacing());
        insideDims.setY(getDims().getY() + displayDims.getHeight() + context.getMetrics().getSpacing());
        insideDims.setWidth(getDims().getWidth() - context.getMetrics().getSpacing() * 2);
        insideDims.setHeight(getDims().getHeight() - displayDims.getHeight() - context.getMetrics().getSpacing() * 2);

        hueRect.setX(insideDims.getX() + spacing);
        hueRect.setWidth(insideDims.getWidth() - (spacing * 2));
        hueRect.setY(insideDims.getY() + spacing + squareSize + spacing);
        hueRect.setHeight(context.getMetrics().getButtonHeight());

        alphaRect.setX(colorSquare.getX() + colorSquare.getWidth() + spacing);
        alphaRect.setY(colorSquare.getY());
        alphaRect.setWidth(alphaSliderWidth);
        alphaRect.setHeight(colorSquare.getHeight());

        colorSquare.setX(insideDims.getX() + spacing);
        colorSquare.setY(insideDims.getY() + spacing);
        colorSquare.setWidth(squareSize);
        colorSquare.setHeight(squareSize);

        dims.setHeight(displayDims.getHeight() + squareSize + hueRect.getHeight() + context.getMetrics().getSpacing() * 4);

        if (draggingHue)
        {
            int clampMouseX = MathHelper.clamp(mouse.getX(), hueRect.getX(), hueRect.getX() + hueRect.getWidth());
            float normal = (float) MathUtil.normalize(clampMouseX, hueRect.getX(), hueRect.getX() + hueRect.getWidth());
            setValue(ColorUtil.newAlpha(Color.getHSBColor(MathHelper.clamp(normal, 0, 1), hsb[1], hsb[2]), getValue().getAlpha()));
        }

        if (draggingColor)
        {
            int clampMouseX = MathHelper.clamp(mouse.getX(), colorSquare.getX(), colorSquare.getX() + colorSquare.getWidth());
            float normalX = (float) MathUtil.normalize(clampMouseX, colorSquare.getX(), colorSquare.getX() + colorSquare.getWidth());
            int clampMouseY = MathHelper.clamp(mouse.getY(), colorSquare.getY(), colorSquare.getY() + colorSquare.getHeight());
            float normalY = (float) MathUtil.normalize(clampMouseY, colorSquare.getY(), colorSquare.getY() + colorSquare.getHeight());
            normalY = (-normalY) + 1;
            normalY = MathHelper.clamp(normalY, 0, 1);
            setValue(ColorUtil.newAlpha(Color.getHSBColor(hsb[0], normalX, normalY), getValue().getAlpha()));
        }

        if (draggingAlpha)
        {
            int clampMouseY = MathHelper.clamp(mouse.getY(), alphaRect.getY(), alphaRect.getY() + alphaRect.getHeight());
            float normal = (float) MathUtil.normalize(clampMouseY, alphaRect.getY(), alphaRect.getY() + alphaRect.getHeight());
            setValue(ColorUtil.newAlpha(getValue(), (int) MathHelper.clamp(normal * 255, 0, 255)));
        }
        context.getRenderer().renderColorWidget(
                this,
                context,
                open,
                displayDims,
                dims,
                insideDims,
                alphaRect,
                hueRect,
                colorSquare
        );
        if (colorSquare.collideWithMouse(mouse) && getDims().collideWithMouse(mouse))
        {
            hovering = true;
        }
    }

    @Override
    public void click(Context context, MouseHelper mouse, int button)
    {
        if (displayDims.collideWithMouse(mouse))
        {
            int size = displayDims.getHeight() - 2;
            int rightX = displayDims.getX() + (displayDims.getWidth() - size - 1);
            Rect colorRect = new Rect(rightX, displayDims.getY() + 1, size, size);
            if (colorRect.collideWithMouse(mouse) && button == 0 && !getUnsyncable())
            {

                setSyncing(!getSyncing());
            } else if (button == 1)
            {
                open = !open;
            }
        }

        if (open && button == 0)
        {
            if (hueRect.collideWithMouse(mouse))
            {
                draggingHue = true;
            }
            if (colorSquare.collideWithMouse(mouse))
            {
                draggingColor = true;
            }
            if (alphaRect.collideWithMouse(mouse))
            {
                draggingAlpha = true;
            }
        }
    }

    @Override
    public void release(Context context, MouseHelper mouse, int state)
    {
        draggingHue = false;
        draggingColor = false;
        draggingAlpha = false;
    }

    @Override
    public void key(Context context, int key, char character)
    {
        if (hovering && InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_CONTROL) && open)
        {
            if (key == GLFW.GLFW_KEY_C)
            {
                ChatUtils.sendMessage("Copied Color! " + getValue().getRed() + " " + getValue().getGreen() + " " + getValue().getBlue() + " ");
                Manager.INSTANCE.COPIED_COLOR = getValue();
            } else if (key == GLFW.GLFW_KEY_V)
            {
                if (Manager.INSTANCE.COPIED_COLOR == null)
                {
                    ChatUtils.sendMessage(Formatting.RED + "No color copied!");
                } else
                {
                    setValue(Manager.INSTANCE.COPIED_COLOR);
                    ChatUtils.sendMessage("Set Color! " + getValue().getRed() + " " + getValue().getGreen() + " " + getValue().getBlue() + " ");
                }
            }
        }
    }

    @Override
    public void charTyped(Context context, char character)
    {

    }

    @Override
    public int getLevel()
    {
        return 3;
    }

    @Override
    public Rect getDims()
    {
        return open ? dims : displayDims;
    }

    @Override
    public boolean isDraggable()
    {
        return false;
    }

    @Override
    public boolean isActive()
    {
        return true;
    }
}
