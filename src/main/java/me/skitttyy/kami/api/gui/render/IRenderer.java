package me.skitttyy.kami.api.gui.render;

import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.widget.impl.*;
import me.skitttyy.kami.impl.gui.components.module.FeatureButton;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public interface IRenderer {

    void renderBackground(Context context);
    void renderLast(Context context);

    // frame stuffs
    void renderFrameTitle(Context context, Rect rect, MouseHelper mouse, String title, boolean open);
    void renderFrameOutline(Context context, Rect rect, MouseHelper mouse);
    void renderFrame(Context context, Rect rect, MouseHelper mouse);

    // widgets
    void renderBooleanWidget(BooleanWidget widget, Context context, Rect rect, MouseHelper mouse);
    void renderBindWidget(BindWidget widget, Context context, Rect rect, MouseHelper mouse);
    void renderFeatureButton(FeatureButton button, Context context, Rect rect, MouseHelper mouse);
    void renderComboBox(ComboBoxWidget widget, Context context, Rect rect, MouseHelper mouseHelper);
    void renderSliderWidget(SliderWidget widget, Context context, Rect rect, Rect sliderRect, MouseHelper mouse);
    void renderColorWidget(ColorWidget widget, Context context, boolean open, Rect headerRect, Rect dims, Rect container, Rect alphaSlider, Rect hueSlider, Rect colorSquare);
    void renderStringWidget(TextEntryWidget widget, Context context, Rect rect, MouseHelper mouse);

    int getTextWidth(String text);
    float getTextWidthFloat(String text);

    int getTextHeight(String text);
    void renderText(DrawContext context, String text, float x, float y, Color color, boolean shadow);
    void renderRect(Rect rect, Color top, Color bottom, RectMode mode, Context context);

    void scissorRect(Rect dims);

    public enum RectMode {
        Fill,
        FillHorizontal,
        Outline,
        OutlineNoRasturize
    }
//    void render(Context context, Rect rect, MouseHelper mouse);


}
