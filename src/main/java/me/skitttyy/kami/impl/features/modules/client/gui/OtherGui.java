package me.skitttyy.kami.impl.features.modules.client.gui;

import me.skitttyy.kami.api.gui.GUI;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.gui.widget.impl.*;
import me.skitttyy.kami.api.utils.StringUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.render.IRenderer;
import me.skitttyy.kami.api.gui.theme.IColorScheme;
import me.skitttyy.kami.api.gui.theme.IMetrics;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.gui.ClickGui;
import me.skitttyy.kami.impl.gui.components.module.FeatureButton;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class OtherGui extends Module implements IColorScheme, IMetrics, IRenderer {


    Value<Boolean> textShadow = new ValueBuilder<Boolean>()
            .withDescriptor("Text Shadow")
            .withValue(true)
            .register(this);
    Value<Sn0wColor> textColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Text Color")
            .withValue(new Sn0wColor(255, 255, 255, 255))
            .register(this);
    Value<Sn0wColor> color = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Color")
            .withValue(new Sn0wColor(0, 179, 42, 255))
            .register(this);
    Value<Sn0wColor> backgroundColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Back Color")
            .withValue(new Sn0wColor(76, 76, 76, 150))
            .register(this);
    Value<Sn0wColor> backgroundSecondary = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Secondary")
            .withValue(new Sn0wColor(33, 33, 33, 150))
            .register(this);
    Value<Sn0wColor> backgroundTertiary = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Tertiary Color")
            .withValue(new Sn0wColor(3, 3, 3, 174))
            .register(this);
    Value<Sn0wColor> outlineColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Outline Color")
            .withValue(new Sn0wColor(0, 0, 0, 255))
            .register(this);
    Value<Number> width = new ValueBuilder<Number>()
            .withDescriptor("Width")
            .withValue(100)
            .withRange(60, 200)
            .register(this);
    Value<Number> gradientSize = new ValueBuilder<Number>()
            .withDescriptor("Gradient Size")
            .withValue(100)
            .withRange(0, 600)
            .register(this);
    Value<Sn0wColor> bgColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Background")
            .withValue(new Sn0wColor(0, 0, 0, 25))
            .register(this);
    public OtherGui() {
        super("OtherGui", Category.Client);
    }

    @Override
    public void onEnable() {
        if (NullUtils.nullCheck()) return;
        super.onEnable();
        ClickGui.INSTANCE.enterGui(this, this, this);
        setEnabled(false);
    }

    // component renderer stuffs

    @Override
    public void renderBackground(Context context) {
        renderRect(
                new Rect(
                        0,
                        context.getScaledResolution().getScaledHeight() - gradientSize.getValue().intValue(),
                        context.getScaledResolution().getScaledWidth(),
                        gradientSize.getValue().intValue()
                ),
                bgColor.getValue().getColor(),
                context.getColorScheme().getMainColor(0),
                RectMode.Fill,
                context
        );
        renderRect(
                new Rect(
                        0,
                        0,
                        context.getScaledResolution().getScaledWidth(),
                        context.getScaledResolution().getScaledHeight() - gradientSize.getValue().intValue()
                ),
                bgColor.getValue().getColor(),
                bgColor.getValue().getColor(),
                RectMode.Fill,
                context
        );
    }

    @Override
    public void renderLast(Context context)
    {

    }

    @Override
    public void renderFrameTitle(Context context, Rect rect, MouseHelper mouse, String title, boolean open) {
        context.getRenderer().renderRect(rect, context.getColorScheme().getMainColor(rect.getY()), context.getColorScheme().getMainColor(rect.getY()), RectMode.Fill, context);
        context.getRenderer().renderRect(rect, context.getColorScheme().getOutlineColor(), context.getColorScheme().getOutlineColor(), RectMode.Outline, context);
        int centerY = (rect.getHeight() - context.getRenderer().getTextHeight(title)) / 2;
        context.getRenderer().renderText(context.getDrawContext(), title, rect.getX() + 2, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public void renderFrameOutline(Context context, Rect rect, MouseHelper mouse) {
    }

    @Override
    public void renderFrame(Context context, Rect rect, MouseHelper mouse) {
        context.getRenderer().renderRect(rect, context.getColorScheme().getBackgroundColor(), context.getColorScheme().getBackgroundColor(), RectMode.Fill, context);
    }

    @Override
    public void renderBooleanWidget(BooleanWidget widget, Context context, Rect rect, MouseHelper mouse) {
        Color c = widget.getValue() ? context.getColorScheme().getMainColor(rect.getY()) : context.getColorScheme().getTertiaryBackgroundColor();
        context.getRenderer().renderRect(rect, c, c, RectMode.Fill, context);
        if (widget.getValue()){
            context.getRenderer().renderRect(rect, context.getColorScheme().getOutlineColor(), context.getColorScheme().getOutlineColor(), RectMode.Outline, context);
        }
        int centerY = (rect.getHeight() - context.getRenderer().getTextHeight(widget.getTitle())) / 2;
        context.getRenderer().renderText(context.getDrawContext(), widget.getTitle(), rect.getX() + 2, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public void renderBindWidget(BindWidget widget, Context context, Rect rect, MouseHelper mouse) {
        context.getRenderer().renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);
        float centerY = (rect.getHeight() - context.getRenderer().getTextHeight(widget.getTitle())) / 2f + 1f;
        String keyName = widget.getValue().getKey() != -1 ? StringUtils.getKeyName(widget.getValue().getKey(), widget.getValue()) : "NONE";
        String text = widget.isBinding() ? "Binding..." : "Bind: " + keyName;
        context.getRenderer().renderText(context.getDrawContext(), text, rect.getX() + 2, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public void renderFeatureButton(FeatureButton widget, Context context, Rect rect, MouseHelper mouse) {
        Color color = (widget.getTitle().toLowerCase().contains(GUI.searchBar.value.toLowerCase()) && !GUI.searchBar.value.equals("")) ? Color.YELLOW : (widget.getValue() ? context.getColorScheme().getMainColor(0) : context.getColorScheme().getSecondaryBackgroundColor());
        renderRect(rect, color, color, RectMode.Fill, context);
        if (widget.getValue()) {
            renderRect(rect, getOutlineColor(), getOutlineColor(), RectMode.Outline, context);
        }
        int centerY = (rect.getHeight() - getTextHeight(widget.getTitle())) / 2 + 1;
        renderText(context.getDrawContext(), widget.getTitle(), rect.getX() + 2, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());

    }

    @Override
    public void renderComboBox(ComboBoxWidget widget, Context context, Rect rect, MouseHelper mouseHelper) {
        renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);

        String text = widget.getTitle() + ": " + widget.getValue();
        int centerY = (rect.getHeight() - getTextHeight(text)) / 2 + 1;

        renderText(context.getDrawContext(), text, rect.getX() + 2, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public void renderSliderWidget(SliderWidget widget, Context context, Rect rect, Rect sliderRect, MouseHelper mouse) {
        renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);
        renderRect(sliderRect, context.getColorScheme().getMainColor(0), context.getColorScheme().getMainColor(0), RectMode.Fill, context);
        renderRect(sliderRect, context.getColorScheme().getOutlineColor(), context.getColorScheme().getOutlineColor(), RectMode.Outline, context);

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        String text = widget.getTitle() + ": " + df.format(widget.getValue().doubleValue());
        int centerY = (rect.getHeight() - getTextHeight(text)) / 2 + 1;

        renderText(context.getDrawContext(), text, rect.getX() + 2, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
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
        int centerY = (headerRect.getHeight() - getTextHeight(text)) / 2 + 1;

        String openString = open ? "-" : "+";
        int rightX = (headerRect.getWidth() - getTextWidth(openString)) - 2;

        renderText(context.getDrawContext(), text, headerRect.getX() + 2, headerRect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
        renderText(context.getDrawContext(), openString, headerRect.getX() + rightX, headerRect.getY() + centerY, widget.getValue(), context.getColorScheme().doesTextShadow());

        if (open)
        {
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
    public int getTextHeight(String text)
    {
        return mc.textRenderer.fontHeight;
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

    // color scheme

    @Override
    public Color getMainColor(int pos) {
        return color.getValue().getColor();
    }

    @Override
    public Color getOutlineColor() {
        return outlineColor.getValue().getColor();
    }

    @Override
    public Color getButtonColor() {
        return color.getValue().getColor();
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor.getValue().getColor();
    }

    @Override
    public Color getSecondaryBackgroundColor() {
        return backgroundSecondary.getValue().getColor();
    }

    @Override
    public Color getTertiaryBackgroundColor() {
        return backgroundTertiary.getValue().getColor();
    }

    @Override
    public Color getTextColor() {
        return textColor.getValue().getColor();
    }

    @Override
    public Color getTextColorHighlight() {
        return textColor.getValue().getColor();
    }

    @Override
    public Color getTextColorActive() {
        return textColor.getValue().getColor();
    }

    @Override
    public boolean doesTextShadow() {
        return textShadow.getValue();
    }

    // metrics

    @Override
    public int getSpacing() {
        return 1;
    }

    @Override
    public int getBetweenSpacing() {
        return 1;
    }

    @Override
    public int getSettingSpacing() {
        return 1;
    }



    @Override
    public int getFrameWidth() {
        return width.getValue().intValue();
    }

    @Override
    public int getButtonHeight() {
        return 14;
    }

    @Override
    public int getFrameHeight() {
        return 15;
    }

    @Override
    public String getDescription()
    {
        return "OtherGui: other gui";
    }
}
