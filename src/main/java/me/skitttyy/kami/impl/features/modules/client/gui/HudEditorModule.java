package me.skitttyy.kami.impl.features.modules.client.gui;

import me.skitttyy.kami.api.gui.widget.impl.*;
import me.skitttyy.kami.api.utils.StringUtils;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.client.gui.DrawContext;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.hudeditor.HudEditorGUI;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.render.IRenderer;
import me.skitttyy.kami.api.gui.theme.IColorScheme;
import me.skitttyy.kami.api.gui.theme.IMetrics;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.impl.gui.components.module.FeatureButton;

import java.awt.*;

public class HudEditorModule extends Module implements IColorScheme, IMetrics, IRenderer {

    public static HudEditorModule INSTANCE;

    public HudEditorModule()
    {
        super("HudEditor", Category.Client);
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        if (NullUtils.nullCheck()) return;

        super.onEnable();
        HudEditorGUI.INSTANCE.enterGui(this, this, this);
        setEnabled(false);
    }

    @Override
    public void renderBackground(Context context)
    {
        renderRect(
                new Rect(
                        0,
                        0,
                        context.getScaledResolution().getScaledWidth(),
                        context.getScaledResolution().getScaledHeight()
                ),
                Sn0wGui.INSTANCE.gradientTop.getValue().getColor(),
                Sn0wGui.INSTANCE.gradentBottom.getValue().getColor(),
                RectMode.Fill,
                context
        );

    }

    @Override
    public void renderLast(Context context)
    {
        Sn0wGui.INSTANCE.renderLast(context);
    }

    @Override
    public void renderFrameTitle(Context context, Rect rect, MouseHelper mouse, String title, boolean open)
    {
        Sn0wGui.INSTANCE.renderFrameTitle(context, rect, mouse, title, open);
    }

    @Override
    public void renderFrameOutline(Context context, Rect rect, MouseHelper mouse)
    {
        Sn0wGui.INSTANCE.renderFrameOutline(context, rect, mouse);
    }

    @Override
    public void renderFrame(Context context, Rect rect, MouseHelper mouse)
    {
        Sn0wGui.INSTANCE.renderFrame(context, rect, mouse);
    }

    @Override
    public void renderBooleanWidget(BooleanWidget widget, Context context, Rect rect, MouseHelper mouse)
    {
        Sn0wGui.INSTANCE.renderBooleanWidget(widget, context, rect, mouse);
    }

    @Override
    public void renderBindWidget(BindWidget widget, Context context, Rect rect, MouseHelper mouse)
    {
        Sn0wGui.INSTANCE.renderBindWidget(widget, context, rect, mouse);
    }


    @Override
    public void renderFeatureButton(FeatureButton widget, Context context, Rect rect, MouseHelper mouse)
    {
        Sn0wGui.INSTANCE.renderFeatureButton(widget, context, rect, mouse);
    }

    @Override
    public void renderComboBox(ComboBoxWidget widget, Context context, Rect rect, MouseHelper mouseHelper)
    {
        Sn0wGui.INSTANCE.renderComboBox(widget, context, rect, mouseHelper);
    }

    @Override
    public void renderSliderWidget(SliderWidget widget, Context context, Rect rect, Rect sliderRect, MouseHelper mouse)
    {
        Sn0wGui.INSTANCE.renderSliderWidget(widget, context, rect, sliderRect, mouse);
    }

    @Override
    public float getTextWidthFloat(String text)
    {
        return Sn0wGui.INSTANCE.getTextWidthFloat(text);
    }

    @Override
    public void renderColorWidget(ColorWidget widget, Context context, boolean open, Rect headerRect, Rect dims, Rect container, Rect alphaSlider, Rect hueSlider, Rect colorSquare)
    {
        Sn0wGui.INSTANCE.renderColorWidget(widget, context, open, headerRect, dims, container, alphaSlider, hueSlider, colorSquare);
    }

    @Override
    public void renderStringWidget(TextEntryWidget widget, Context context, Rect rect, MouseHelper mouse)
    {
        Sn0wGui.INSTANCE.renderStringWidget(widget, context, rect, mouse);
    }

    @Override
    public int getTextWidth(String text)
    {
        return mc.textRenderer.getWidth(text);
    }

    @Override
    public int getTextHeight(String text)
    {
        return mc.textRenderer.fontHeight;
    }

    @Override
    public void renderText(DrawContext context, String text, float x, float y, Color color, boolean shadow)
    {
        Sn0wGui.INSTANCE.renderText(context, text, x, y, color, shadow);
    }

    @Override
    public void renderRect(Rect rect, Color inputTop, Color inputBottom, RectMode mode, Context context)
    {
        Sn0wGui.INSTANCE.renderRect(rect, inputTop, inputBottom, mode, context);
    }


    @Override
    public void scissorRect(Rect dims)
    {
        Sn0wGui.INSTANCE.scissorRect(dims);
    }

    @Override
    public Color getMainColor(int pos)
    {
        return Sn0wGui.INSTANCE.getMainColor(pos);
    }

    @Override
    public Color getOutlineColor()
    {
        return Sn0wGui.INSTANCE.getOutlineColor();
    }

    @Override
    public Color getButtonColor()
    {
        return Sn0wGui.INSTANCE.getButtonColor();
    }

    @Override
    public Color getBackgroundColor()
    {
        return Sn0wGui.INSTANCE.getBackgroundColor();
    }

    @Override
    public Color getSecondaryBackgroundColor()
    {
        return Sn0wGui.INSTANCE.getSecondaryBackgroundColor();
    }

    @Override
    public Color getTertiaryBackgroundColor()
    {
        return Sn0wGui.INSTANCE.getTertiaryBackgroundColor();
    }

    @Override
    public Color getTextColor()
    {
        return Sn0wGui.INSTANCE.getTextColor();
    }

    @Override
    public Color getTextColorHighlight()
    {
        return Sn0wGui.INSTANCE.getTextColorHighlight();
    }

    @Override
    public Color getTextColorActive()
    {
        return Sn0wGui.INSTANCE.getTextColorActive();
    }

    @Override
    public boolean doesTextShadow()
    {
        return Sn0wGui.INSTANCE.doesTextShadow();
    }

    @Override
    public int getSpacing()
    {
        return Sn0wGui.INSTANCE.getSpacing();
    }

    @Override
    public int getBetweenSpacing()
    {
        return Sn0wGui.INSTANCE.getBetweenSpacing();
    }

    @Override
    public int getSettingSpacing()
    {
        return Sn0wGui.INSTANCE.getSettingSpacing();
    }

    @Override
    public int getFrameWidth()
    {
        return Sn0wGui.INSTANCE.getFrameWidth();
    }

    @Override
    public int getButtonHeight()
    {
        return Sn0wGui.INSTANCE.getButtonHeight();
    }

    @Override
    public int getFrameHeight()
    {
        return Sn0wGui.INSTANCE.getFrameHeight();
    }


    // component renderer stuffs

    public void registerGUI()
    {
        HudEditorGUI.INSTANCE.updateGUI(this, this, this);
    }

    @Override
    public String getDescription()
    {
        return "HudEditor: edit the various hud elements";
    }
}