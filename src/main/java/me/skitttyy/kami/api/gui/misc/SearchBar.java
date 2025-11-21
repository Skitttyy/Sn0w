package me.skitttyy.kami.api.gui.misc;

import me.skitttyy.kami.api.gui.GUI;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.utils.StringUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.render.animation.Easing;
import me.skitttyy.kami.api.utils.render.animation.type.ColorAnimation;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static me.skitttyy.kami.api.wrapper.IMinecraft.mc;

public class SearchBar {
    boolean typing = false;
    public String value;
    Rect rect;
    ColorAnimation animation = new ColorAnimation(Color.WHITE, ColorUtil.newAlpha(Color.WHITE, 100), Easing.CUBIC_IN_OUT, 300L, false);

    public SearchBar(Rect rect)
    {
        this.rect = rect;
        value = "";
    }

    public void click(MouseHelper mouse, int button)
    {
        if (rect.collideWithMouse(mouse))
        {
            if (button == 0)
            {
                typing = !typing;
            }
        } else
        {
            typing = false;
        }
    }

    public void key(int key)
    {
        if (!typing) return;

        switch (key)
        {
            case GLFW.GLFW_KEY_V ->
            {
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL))
                {
                    value = value + SelectionManager.getClipboard(mc);
                }
            }
            case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER ->
            {
                typing = false;
            }
            case GLFW.GLFW_KEY_BACKSPACE ->
            {
                value = StringUtils.removeLastChar(value);
            }
        }
    }

    public void charTyped(char character)
    {
        if (typing)
        {
            value = value + character;
        }
    }


    public void onRender(Context context, MouseHelper mouse)
    {
        String renderText;

        animation.setState(rect.collideWithMouse(mouse) || typing || !value.isEmpty());

        if (value.isEmpty() && !typing)
        {
            renderText = "Search";
        } else
        {
            renderText = typing ? value + (GUI.typeCounter ? "": "_"): value;
        }
        String magnifyingGlass = "\uD83D\uDD0D";
        if (FontModule.INSTANCE.isEnabled()) magnifyingGlass = "";
        renderText = renderText + " " + magnifyingGlass;
        rect.setWidth((Math.max(context.getRenderer().getTextWidth(renderText) + 3, context.getRenderer().getTextWidth(magnifyingGlass + "Search") + 3)));
        int centerY = (rect.getHeight() - context.getRenderer().getTextHeight(renderText)) / 2;
        context.getRenderer().renderText(context.getDrawContext(), renderText, rect.getX() + 1, rect.getY() + centerY + 1, animation.getColor(), context.getColorScheme().doesTextShadow());
    }

    public boolean hasCustomName(ItemStack stack)
    {
        return stack.get(DataComponentTypes.CUSTOM_NAME) != null;
    }
}
