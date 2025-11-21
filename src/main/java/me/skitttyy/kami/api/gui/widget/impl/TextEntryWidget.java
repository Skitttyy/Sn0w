package me.skitttyy.kami.api.gui.widget.impl;

import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.utils.StringUtils;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.util.InputUtil;
import me.skitttyy.kami.api.gui.widget.IWidget;
import net.minecraft.client.util.SelectionManager;
import org.lwjgl.glfw.GLFW;

import static me.skitttyy.kami.api.wrapper.IMinecraft.mc;

public class TextEntryWidget implements IWidget<String>, IComponent {

    Rect dims;
    String value;
    public boolean typing = false;

    public TextEntryWidget(Rect dims, String value)
    {
        this.dims = dims;
        this.value = value;
    }

    @Override
    public void draw(Context context, MouseHelper mouse)
    {
        if (!getDims().collideWithMouse(mouse))
        {
            typing = false;
        }

        getDims().setHeight(context.getMetrics().getButtonHeight());
        context.getRenderer().renderStringWidget(this, context, getDims(), mouse);
    }


    @Override
    public void click(Context context, MouseHelper mouse, int button)
    {
        if (getDims().collideWithMouse(mouse) && button == 0)
        {
            typing = !typing;
        }else{
            typing = false;
        }
    }

    @Override
    public void release(Context context, MouseHelper mouse, int state)
    {

    }

    @Override
    public void key(Context context, int key, char character)
    {
        if (!typing) return;

        switch (key)
        {
            case GLFW.GLFW_KEY_V ->
            {
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL))
                {
                    setValue(getValue() + SelectionManager.getClipboard(mc));
                }
            }
            case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER ->
            {
                typing = false;
            }
            case GLFW.GLFW_KEY_BACKSPACE ->
            {
                setValue(StringUtils.removeLastChar(getValue()));
            }
        }
    }

    @Override
    public void charTyped(Context context, char character)
    {
        if (typing)
        {
            setValue(getValue() + character);
        }
    }

    @Override
    public int getLevel()
    {
        return 3;
    }

    @Override
    public Rect getDims()
    {
        return dims;
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

    @Override
    public String getValue()
    {
        return value;
    }

    @Override
    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public String getTitle()
    {
        return "";
    }

    @Override
    public void setTitle(String title)
    {

    }

    @Override
    public Rect getDisplayDims()
    {
        return getDims();
    }
}
