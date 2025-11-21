package me.skitttyy.kami.impl.gui.renderer;

import me.skitttyy.kami.api.gui.GUI;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.gui.widget.impl.*;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.StringUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.impl.features.modules.client.gui.KamiGui;
import me.skitttyy.kami.impl.gui.components.module.FeatureButton;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.render.IRenderer;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;


public class Renderer implements IRenderer, IMinecraft {



    @Override
    public void renderBackground(Context context)
    {

    }

    @Override
    public void renderLast(Context context)
    {

    }

    @Override
    public void renderFrameTitle(Context context, Rect rect, MouseHelper mouse, String title, boolean open) {
        renderRect(rect, context.getColorScheme().getMainColor(rect.getX() + rect.getY()), context.getColorScheme().getMainColor(rect.getX() + rect.getWidth() + rect.getY() + rect.getHeight()), RectMode.Fill, context);
        int centerX = (rect.getWidth() - getTextWidth(title)) / 2;
        int centerY = (rect.getHeight() - getTextHeight(title)) / 2 + 1;

        String openString = open ? "-" : "+";
        int rightX = (rect.getWidth() - getTextWidth(openString)) - 2;

        renderText(context.getDrawContext(),title, rect.getX() + centerX, rect.getY() + centerY, context.getColorScheme().getTextColorHighlight(), context.getColorScheme().doesTextShadow());
        renderText(context.getDrawContext(),openString, rect.getX() + rightX, rect.getY() + centerY, context.getColorScheme().getTextColorHighlight(), context.getColorScheme().doesTextShadow());

    }



    @Override
    public void renderFrameOutline(Context context, Rect rect, MouseHelper mouse) {

        renderRect(
                rect,
                KamiGui.INSTANCE.getOutlineColor(),
                KamiGui.INSTANCE.getOutlineColor(),
                RectMode.Outline,
                context
        );
    }

    @Override
    public void renderFrame(Context context, Rect rect, MouseHelper mouse) {
        renderRect(rect, context.getColorScheme().getBackgroundColor(), context.getColorScheme().getBackgroundColor(), RectMode.Fill, context);
    }

    @Override
    public void renderBooleanWidget(BooleanWidget widget, Context context, Rect rect, MouseHelper mouse) {
        Color color = widget.getValue() ? context.getColorScheme().getMainColor(rect.getX() + rect.getY()) : context.getColorScheme().getTertiaryBackgroundColor();
        Color color2 = widget.getValue() ? context.getColorScheme().getMainColor(rect.getX() + rect.getWidth() + rect.getY() + rect.getHeight()) : context.getColorScheme().getTertiaryBackgroundColor();

        renderRect(rect, color, color2, RectMode.Fill, context);
        int centerX = (rect.getWidth() - getTextWidth(widget.getTitle())) / 2;
        int centerY = (rect.getHeight() - getTextHeight(widget.getTitle())) / 2 + 1;

        renderText(context.getDrawContext(),widget.getTitle(), rect.getX() + centerX, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public void renderBindWidget(BindWidget widget, Context context, Rect rect, MouseHelper mouse) {
        renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);
        float centerY = (rect.getHeight() - getTextHeight(widget.getTitle())) / 2f + 1f;
        String keyName = widget.getValue().getKey() != -1 ? StringUtils.getKeyName(widget.getValue().getKey(), widget.getValue()) : "NONE";
        String text = widget.isBinding() ? "Binding..." : "Bind: " + keyName;
        renderText(context.getDrawContext(),text, rect.getX() + 2, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public void renderFeatureButton(FeatureButton widget, Context context, Rect rect, MouseHelper mouse) {
        Color color = (widget.getTitle().toLowerCase().contains(GUI.searchBar.value.toLowerCase()) && !GUI.searchBar.value.equals("")) ? Color.YELLOW : (widget.getValue() ? context.getColorScheme().getMainColor(0) : context.getColorScheme().getSecondaryBackgroundColor());
        renderRect(rect, color, color, RectMode.Fill, context);
        int centerX = (rect.getWidth() - getTextWidth(widget.getTitle())) / 2;
        int centerY = (rect.getHeight() - getTextHeight(widget.getTitle())) / 2 + 1;
        renderText(context.getDrawContext(),widget.getTitle(), rect.getX() + centerX, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public void renderComboBox(ComboBoxWidget widget, Context context, Rect rect, MouseHelper mouseHelper) {

        renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);

        String text = widget.getTitle() + ": " + widget.getValue();
        int centerX = (rect.getWidth() - getTextWidth(text)) / 2;
        int centerY = (rect.getHeight() - getTextHeight(text)) / 2 + 1;

        renderText(context.getDrawContext(),text, rect.getX() + centerX, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public void renderSliderWidget(SliderWidget widget, Context context, Rect rect, Rect sliderRect, MouseHelper mouse) {
        renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);
        renderRect(sliderRect, context.getColorScheme().getMainColor(0), context.getColorScheme().getMainColor(0), RectMode.Fill, context);

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        String text = widget.getTitle() + ": " + df.format(widget.getValue().doubleValue());
        int centerX = (rect.getWidth() - getTextWidth(text)) / 2;
        int centerY = (rect.getHeight() - getTextHeight(text)) / 2 + 1;

        renderText(context.getDrawContext(),text, rect.getX() + centerX, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());

    }

    @Override
    public void renderColorWidget(ColorWidget widget, Context context, boolean open, Rect headerRect, Rect dims, Rect container, Rect alphaSlider, Rect hueSlider, Rect colorSquare) {
        int sliderWidth = 2;

        renderRect(
                open ? dims : headerRect,
                context.getColorScheme().getTertiaryBackgroundColor(),
                context.getColorScheme().getTertiaryBackgroundColor(),
                RectMode.Fill,
                context
        );

        String text = widget.getTitle();
        int centerX = (headerRect.getWidth() - getTextWidth(text)) / 2;
        int centerY = (headerRect.getHeight() - getTextHeight(text)) / 2 + 1;

        String openString = open ? "-" : "+";
        int rightX = (headerRect.getWidth() - getTextWidth(openString)) - 2;

        renderText(context.getDrawContext(),text, headerRect.getX() + centerX, headerRect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
        renderText(context.getDrawContext(),openString, headerRect.getX() + rightX, headerRect.getY() + centerY, widget.getValue(), context.getColorScheme().doesTextShadow());

        if (open) {
            int hueSegments = 30;

            for (int i = 0; i <= hueSegments; i++)
            {
                double normal = MathUtil.normalize(i, 0, hueSegments);
                Color color = Color.getHSBColor((float) normal, 1, 1);

                double normal2 = MathUtil.normalize(i + 1, 0, hueSegments);
                Color color2 = Color.getHSBColor((float) normal2, 1, 1);

                double curX = (double) hueSlider.getWidth() / ((double) hueSegments / i) ;
                double width = (double) hueSlider.getWidth() / ((double) hueSegments) ;


                Rect hueSegment = new Rect((int) (hueSlider.getX() + curX), (int) (hueSlider.getY() + ((hueSlider.getHeight()) / 2f) - 3), (int) Math.min(width + 1, hueSlider.getWidth() - curX), 6);
                renderRect(
                        hueSegment,
                        color,
                        color2,
                        RectMode.FillHorizontal,
                        context
                );
            }

            float[] hsb = Color.RGBtoHSB(widget.getValue().getRed(), widget.getValue().getGreen(), widget.getValue().getBlue(), null);
            int hueOffset = (int) (hsb[0] * hueSlider.getWidth());
            hueOffset = MathHelper.clamp(hueOffset, 0, hueSlider.getWidth());
            Rect huePicker = new Rect(hueSlider.getX() + hueOffset - sliderWidth / 2, hueSlider.getY(), sliderWidth, hueSlider.getHeight());
            renderRect(
                    huePicker,
                    new Color(255, 255, 255),
                    new Color(255, 255, 255),
                    RectMode.Fill,
                    context
            );


//            RenderSystem.enableBlend();
//            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
//
//            BufferBuilder buffer = TESSELLATOR.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
//
//            ColorUtil.color(buffer.vertex(matrices, colorSquare.getX(), colorSquare.getY(), 0), new Color(255, 255, 255));
//            ColorUtil.color(buffer.vertex(matrices, colorSquare.getX(), colorSquare.getY() + colorSquare.getHeight(), 0), new Color(255, 255, 255));
//            ColorUtil.color(buffer.vertex(matrices, colorSquare.getX() + colorSquare.getWidth(), colorSquare.getY() + colorSquare.getHeight(), 0), Color.getHSBColor(hsb[0], 1, 1));
//            ColorUtil.color(buffer.vertex(matrices, colorSquare.getX() + colorSquare.getWidth(), colorSquare.getY(), 0), Color.getHSBColor(hsb[0], 1, 1));
//
//            BufferRenderer.drawWithGlobalProgram(buffer.end());
//            RenderSystem.disableBlend();
            renderRect(
                    colorSquare,
                    new Color(255, 255, 255),
                    Color.getHSBColor(hsb[0], 1, 1),
                    RectMode.FillHorizontal,
                    context
            );
            renderRect(
                    colorSquare,
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 255),
                    RectMode.Fill,
                    context
            );


            renderRect(
                    colorSquare,
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 255),
                    RectMode.Fill,
                    context
            );


            int pickerSize = 2;
            int pickerOffsetX = MathHelper.clamp((int) (hsb[1] * colorSquare.getWidth()), 0, colorSquare.getWidth());
            int pickerOffsetY = MathHelper.clamp((int) (hsb[2] * colorSquare.getHeight()), 0, colorSquare.getHeight());
            Rect pickerRect = new Rect(colorSquare.getX() + pickerOffsetX - (pickerSize / 2), colorSquare.getY() + colorSquare.getHeight() - pickerOffsetY - (pickerSize / 2), pickerSize, pickerSize);
            renderRect(
                    pickerRect,
                    new Color(255, 255, 255),
                    new Color(255, 255, 255),
                    RectMode.Fill,
                    context
            );
            renderRect(
                    pickerRect,
                    new Color(0, 0, 0),
                    new Color(0, 0, 0),
                    RectMode.Outline,
                    context
            );


            //alpha slider

            renderRect(
                    alphaSlider,
                    Color.WHITE,
                    Color.WHITE,
                    RectMode.Fill,
                    context
            );
            renderRect(
                    alphaSlider,
                    ColorUtil.newAlpha(Color.getHSBColor(hsb[0], 1, 1), 24),
                    ColorUtil.newAlpha(Color.getHSBColor(hsb[0], 1, 1), 255),
                    RectMode.Fill,
                    context
            );
            double alphaNormal = MathHelper.clamp(MathUtil.normalize(widget.getValue().getAlpha(), 0, 255), 0, 1);
            int alphaOffset = (int) (alphaNormal * alphaSlider.getHeight());
            Rect alphaPicker = new Rect(alphaSlider.getX(), alphaSlider.getY() + alphaOffset - sliderWidth / 2, alphaSlider.getWidth(), sliderWidth);
            renderRect(
                    alphaPicker,
                    new Color(255, 255, 255),
                    new Color(255, 255, 255),
                    RectMode.Fill,
                    context
            );
        }
    }

    @Override
    public void renderStringWidget(TextEntryWidget widget, Context context, Rect rect, MouseHelper mouse) {
        renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);
        String renderText = widget.typing ? widget.getValue() + "|" : widget.getValue();
        int centerY = (rect.getHeight() - getTextHeight(renderText)) / 2;
        renderText(context.getDrawContext(), renderText, rect.getX() + 2, rect.getY() + centerY + 1, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }


    @Override
    public int getTextWidth(String text)
    {
        return mc.textRenderer.getWidth(text);
    }

    @Override
    public float getTextWidthFloat(String text)
    {
        return mc.textRenderer.getWidth(text);
    }


    @Override
    public int getTextHeight(String text) {
        return 9;
    }


    @Override
    public void renderText(DrawContext context, String text, float x, float y, Color color, boolean shadow)
    {
        Fonts.VANILLA.drawText(context, context.getMatrices(), text, x, y, color.getRGB(), shadow);
    }

    @Override
    public void renderRect(Rect rect, Color color, Color bottom, RectMode mode, Context context) {
        if (mode == RectMode.Fill)
        {
            RenderUtil.renderGradient(context.getDrawContext().getMatrices(), rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), color.getRGB(), bottom.getRGB(), false);
        }
        if (mode == RectMode.FillHorizontal)
        {
            RenderUtil.renderGradient(context.getDrawContext().getMatrices(), rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), color.getRGB(), bottom.getRGB(), true);
        }
        if (mode == RectMode.Outline)
        {
            RenderUtil.renderOutline(context.getDrawContext().getMatrices(), rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), color.getRGB(), true);
        }

        if (mode == RectMode.OutlineNoRasturize)
        {
            RenderUtil.renderOutline(context.getDrawContext().getMatrices(), rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), color.getRGB(), false);
        }
    }

    @Override
    public void scissorRect(Rect dims) {

    }
}