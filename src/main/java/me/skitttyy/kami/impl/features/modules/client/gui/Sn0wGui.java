package me.skitttyy.kami.impl.features.modules.client.gui;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.gui.hudeditor.HudEditorGUI;
import me.skitttyy.kami.api.gui.widget.impl.*;
import me.skitttyy.kami.api.management.FeatureManager;
import me.skitttyy.kami.api.management.SoundManager;
import me.skitttyy.kami.api.utils.StringUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.utils.render.animation.Animation;
import me.skitttyy.kami.api.utils.render.animation.Easing;
import me.skitttyy.kami.api.utils.render.animation.type.DynamicColorAnimation;
import me.skitttyy.kami.api.utils.render.particle.SnowParticle;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.glfw.GLFW;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.GUI;
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
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;


public class Sn0wGui extends Module implements IColorScheme, IMetrics, IRenderer
{

    public static Sn0wGui INSTANCE;
    Value<Sn0wColor> mainColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Color")
            .withValue(new Sn0wColor(255, 100, 100))
            .register(this);
    Value<Sn0wColor> buttonColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Button")
            .withValue(new Sn0wColor(255, 100, 100, 0))
            .register(this);
    Value<Sn0wColor> textColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Text Color")
            .withValue(new Sn0wColor(255, 255, 255))
            .register(this);
    Value<Sn0wColor> disabledText = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Disabled Text")
            .withValue(new Sn0wColor(192, 192, 192))
            .register(this);
    Value<Sn0wColor> background = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Background")
            .withValue(new Sn0wColor(28, 28, 28, 255))
            .register(this);
    Value<Sn0wColor> outlineColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Outline")
            .withValue(new Sn0wColor(0, 0, 0, 255))
            .register(this);
    Value<Sn0wColor> frameOutline = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Frame Outline")
            .withValue(new Sn0wColor(0, 0, 0, 0))
            .register(this);
    public Value<String> frameEnding = new ValueBuilder<String>()
            .withDescriptor("Icon")
            .withValue("Icon")
            .withModes("None", "Icon", "Count")
            .register(this);

    Value<Number> height = new ValueBuilder<Number>()
            .withDescriptor("Height")
            .withValue(12)
            .withRange(8, 20)
            .register(this);
    Value<Number> width = new ValueBuilder<Number>()
            .withDescriptor("Width")
            .withValue(100)
            .withRange(60, 200)
            .register(this);
    Value<Sn0wColor> gradientTop = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Gradient Top")
            .withValue(new Sn0wColor(255, 100, 100, 30))
            .register(this);
    Value<Sn0wColor> gradentBottom = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Gradient Bottom")
            .withValue(new Sn0wColor(0, 155, 155, 100))
            .register(this);

    Value<Boolean> drawParticles = new ValueBuilder<Boolean>()
            .withDescriptor("Particles")
            .withValue(true)
            .register(this);
    public Value<Number> factor = new ValueBuilder<Number>()
            .withDescriptor("Factor")
            .withValue(0.1f)
            .withRange(0.1, 1.0f)
            .withPlaces(2)
            .withParent(drawParticles)
            .withParentEnabled(true)
            .register(this);


    String lastDescriptionText = "";
    String lastHoveredModule = "";

    CopyOnWriteArrayList<SnowParticle> particles = new CopyOnWriteArrayList<>();

    DynamicColorAnimation colorAnimation = new DynamicColorAnimation(Color.WHITE, Easing.CUBIC_IN_OUT, 300L);
    boolean hasDescriptionToRender = false;
    boolean hasHovered = false;


    Animation fadeAnimation = new Animation(Easing.CUBIC_IN_OUT, 300L);

    public Sn0wGui()
    {
        super("Sn0wGui", Category.Client);
        getBind().setKey(GLFW.GLFW_KEY_BACKSLASH);
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        if (NullUtils.nullCheck()) return;
        super.onEnable();

        fadeAnimation = new Animation(Easing.CUBIC_IN_OUT, 300L);
        fadeAnimation.setState(true);

        ClickGui.INSTANCE.enterGui(this, this, this);
        if (drawParticles.getValue())
            initSnow();
        else
        {
            particles.clear();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (!(mc.currentScreen instanceof ClickGui) && !(mc.currentScreen instanceof HudEditorGUI))
        {
            setEnabled(false);
        }
    }

    public void initSnow()
    {
        particles.clear();
        int origin = getSnowOrigin();
        int bound = getSnowBound();
        for (int i = 0; i < MathUtil.randomInt(origin, bound); i++)
        {
            int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
            int height = MinecraftClient.getInstance().getWindow().getScaledHeight();
            particles.add(SnowParticle.random(width, height));

        }
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
                gradientTop.getValue().getColor(),
                gradentBottom.getValue().getColor(),
                RectMode.Fill,
                context
        );

        if (drawParticles.getValue())
        {
            for (SnowParticle particle : particles)
            {
                particle.onRender(context.getDrawContext(), context.getHelper().getX(), context.getHelper().getY());
            }

        }
    }

    public int getSnowOrigin()
    {
        return (int) (factor.getValue().intValue() * 100);
    }

    public int getSnowBound()
    {
        int origin = getSnowOrigin();
        int randomExtra = (int) (Math.random() * origin * 0.75f);
        return origin + 100 + randomExtra;
    }

    @Override
    public void renderLast(Context context)
    {

        if (hasDescriptionToRender)
        {
            colorAnimation.gotoColor(Color.WHITE);
        } else
        {
            colorAnimation.gotoColor(ColorUtil.newAlpha(Color.WHITE, 0));
        }
        renderText(context.getDrawContext(), lastDescriptionText, (float) (context.getScaledResolution().getScaledWidth() - ClickGui.CONTEXT.getRenderer().getTextWidth(lastDescriptionText)) / 2, 3, colorAnimation.getColor(), ClickGui.CONTEXT.getColorScheme().doesTextShadow());

        if (!hasHovered)
            lastHoveredModule = "";
        hasHovered = false;
        hasDescriptionToRender = false;
    }

    @Override
    public void renderFrameTitle(Context context, Rect rect, MouseHelper mouse, String title, boolean open)
    {

        context.getRenderer().renderRect(rect, context.getColorScheme().getMainColor(rect.getY()), context.getColorScheme().getMainColor(rect.getY()), RectMode.Fill, context);
        int centerY = (rect.getHeight() - context.getRenderer().getTextHeight(title)) / 2;
        context.getRenderer().renderText(context.getDrawContext(), title, rect.getX() + 2, rect.getY() + centerY + 1, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());


        switch (frameEnding.getValue())
        {
            case "Icon":
            {
                String icon = StringUtils.getCategoryIcon(title);

                if (!FontModule.INSTANCE.isEnabled() && FontModule.INSTANCE.pop.getValue())
                {
                    Fonts.VANILLA.renderCsgoLayer(context.getDrawContext(), context.getDrawContext().getMatrices(), icon, rect.getX() + rect.getWidth() - mc.textRenderer.getWidth(icon) - 2, rect.getY() + centerY + 1, ColorUtil.interpolate(fadeAnimation.getScaledTime(), context.getColorScheme().getTextColor(), ColorUtil.newAlpha(context.getColorScheme().getTextColor(), 0)).getRGB());
                } else
                {
                    Fonts.VANILLA.renderTextNoLayer(context.getDrawContext(), context.getDrawContext().getMatrices(), icon, rect.getX() + rect.getWidth() - mc.textRenderer.getWidth(icon) - 2, rect.getY() + centerY + 1, ColorUtil.interpolate(fadeAnimation.getScaledTime(), context.getColorScheme().getTextColor(), ColorUtil.newAlpha(context.getColorScheme().getTextColor(), 0)).getRGB(), context.getColorScheme().doesTextShadow());
                }
                break;
            }
            case "Count":
                String count = Formatting.GRAY + "[" + Formatting.RESET + FeatureManager.INSTANCE.MODULE_COUNTS.get(title.toLowerCase()) + Formatting.GRAY + "]";
                context.getRenderer().renderText(context.getDrawContext(), count, rect.getX() + rect.getWidth() - context.getRenderer().getTextWidth(count) - 2, rect.getY() + centerY + 1, ColorUtil.interpolate(fadeAnimation.getScaledTime(), context.getColorScheme().getTextColor(), ColorUtil.newAlpha(context.getColorScheme().getTextColor(), 0)), context.getColorScheme().doesTextShadow());
                break;
        }

    }

    @Override
    public void renderFrameOutline(Context context, Rect rect, MouseHelper mouse)
    {

        context.getRenderer().renderRect(rect, frameOutline.getValue().getColor(), frameOutline.getValue().getColor(), RectMode.Outline, context);
    }

    @Override
    public void renderFrame(Context context, Rect rect, MouseHelper mouse)
    {
        context.getRenderer().renderRect(rect, context.getColorScheme().getBackgroundColor(), context.getColorScheme().getBackgroundColor(), RectMode.Fill, context);
    }

    @Override
    public void renderBooleanWidget(BooleanWidget widget, Context context, Rect rect, MouseHelper mouse)
    {
        Color text = widget.getValue() ? getTextColor() : disabledText.getValue().getColor();
        int size = rect.getHeight() - 2;
        int rx = rect.getX() + rect.getWidth() - size - 1;
        Rect buttonRect = new Rect(rx, rect.getY() + 1, size, size);


        context.getRenderer().renderRect(buttonRect, widget.animation.getColor(), widget.animation.getColor(), RectMode.Fill, context);

        context.getRenderer().renderRect(buttonRect, getOutlineColor(), getOutlineColor(), RectMode.Outline, context);

        int centerY = (rect.getHeight() - context.getRenderer().getTextHeight(widget.getTitle())) / 2;
        context.getRenderer().renderText(context.getDrawContext(), widget.getTitle(), rect.getX() + 2, rect.getY() + centerY, text, context.getColorScheme().doesTextShadow());
    }

    @Override
    public void renderBindWidget(BindWidget widget, Context context, Rect rect, MouseHelper mouse)
    {
        context.getRenderer().renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);
        context.getRenderer().renderRect(rect, context.getColorScheme().getOutlineColor(), context.getColorScheme().getOutlineColor(), RectMode.Outline, context);

        float centerY = (rect.getHeight() - context.getRenderer().getTextHeight(widget.getTitle())) / 2f + 1f;
        String keyName = StringUtils.getKeyName(widget.getValue().getKey(), widget.getValue());
        StringBuilder dots = new StringBuilder();
        dots.append(".".repeat(Math.max(0, GUI.bindCounter)));
        String text = widget.isBinding() ? "Binding" + dots : "Bind: " + Formatting.GRAY + keyName;
        context.getRenderer().renderText(context.getDrawContext(), text, rect.getX() + 2, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }


    @Override
    public void renderFeatureButton(FeatureButton widget, Context context, Rect rect, MouseHelper mouse)
    {

        Color text = widget.getValue() ? getTextColor() : disabledText.getValue().getColor();
        renderRect(rect, buttonColor.getValue().getColor(), buttonColor.getValue().getColor(), RectMode.Fill, context);
        renderRect(rect, widget.animation.getColor(), widget.animation.getColor(), RectMode.Fill, context);
        renderRect(rect, getOutlineColor(), getOutlineColor(), RectMode.Outline, context);
        renderRect(widget.getDims(), getOutlineColor(), getOutlineColor(), RectMode.Outline, context);

        if (widget.open)
        {
            Rect settingBounds = new Rect(
                    widget.getDims().getX(),
                    widget.getDims().getY() + widget.getDisplayDims().getHeight(),
                    widget.getDims().getWidth(),
                    widget.getDims().getHeight() - widget.getDisplayDims().getHeight()
            );
            renderRect(settingBounds, getOutlineColor(), getOutlineColor(), RectMode.Outline, context);
        }
        int xOffset = 0;

        if (rect.collideWithMouse(mouse))
        {
            if (!widget.getFeature().getDescription().isEmpty() && !hasDescriptionToRender)
            {
                lastDescriptionText = widget.getFeature().getDescription();
                hasDescriptionToRender = true;
            }
            xOffset = 1;

            if (!Objects.equals(lastHoveredModule, widget.getFeature().getName()) && !hasHovered)
            {
                SoundManager.INSTANCE.play(SoundManager.INSTANCE.CLICK_SOUND);
                lastHoveredModule = widget.getFeature().getName();
            }
            hasHovered = true;
        }
        int centerY = (rect.getHeight() - getTextHeight(widget.getTitle())) / 2 + 1;
        renderText(context.getDrawContext(), widget.getTitle(), rect.getX() + 2 + xOffset, rect.getY() + centerY, text, context.getColorScheme().doesTextShadow());

        String openStr = widget.open ? "-" : "+";
        int rightX = rect.getX() + (rect.getWidth() - getTextWidth(openStr) - 1);
        renderText(context.getDrawContext(), openStr, rightX, rect.getY() + centerY, text, context.getColorScheme().doesTextShadow());


    }

    @Override
    public void renderComboBox(ComboBoxWidget widget, Context context, Rect rect, MouseHelper mouseHelper)
    {
        renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);

        String text = widget.getTitle() + ": " + Formatting.GRAY + widget.getValue();
        int centerY = (rect.getHeight() - getTextHeight(text)) / 2 + 1;

        renderText(context.getDrawContext(), text, rect.getX() + 2, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public void renderSliderWidget(SliderWidget widget, Context context, Rect rect, Rect sliderRect, MouseHelper mouse)
    {
        renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);

        sliderRect.setHeight(1);
        sliderRect.setY(rect.getY() + rect.getHeight() - sliderRect.getHeight());
        renderRect(sliderRect, context.getColorScheme().getMainColor(0), context.getColorScheme().getMainColor(0), RectMode.Fill, context);
        renderRect(sliderRect, context.getColorScheme().getOutlineColor(), context.getColorScheme().getOutlineColor(), RectMode.OutlineNoRasturize, context);


        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);

        String text = widget.getTitle() + ": " + Formatting.GRAY + df.format(widget.getValue().doubleValue());
        int centerY = (rect.getHeight() - getTextHeight(text)) / 2 + 1;

        renderText(context.getDrawContext(), text, rect.getX() + 2, rect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public float getTextWidthFloat(String text)
    {
        return Fonts.getTextWidth(text);
    }

    @Override
    public void renderColorWidget(ColorWidget widget, Context context, boolean open, Rect headerRect, Rect dims, Rect container, Rect alphaSlider, Rect hueSlider, Rect colorSquare)
    {
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

        int size = headerRect.getHeight() - 2;
        int rightX = headerRect.getX() + (headerRect.getWidth() - size - 1);
        Rect colorRect = new Rect(rightX, headerRect.getY() + 1, size, size);

        renderText(context.getDrawContext(), text, headerRect.getX() + 2, headerRect.getY() + centerY, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
        renderRect(colorRect, widget.getValue(), widget.getValue(), RectMode.Fill, context);
        renderRect(colorRect, getOutlineColor(), getOutlineColor(), RectMode.Outline, context);


        renderText(context.getDrawContext(), "S", colorRect.getX() + (((float) colorRect.getWidth() / 2) - ((float) getTextWidth("S") / 2)), headerRect.getY() + 2.2f, widget.animation.getColor(), true);

        if (open)
        {
            int hueSegments = 30;

            for (int i = 0; i <= hueSegments; i++)
            {
                double normal = MathUtil.normalize(i, 0, hueSegments);
                Color color = Color.getHSBColor((float) normal, 1, 1);

                double normal2 = MathUtil.normalize(i + 1, 0, hueSegments);
                Color color2 = Color.getHSBColor((float) normal2, 1, 1);

                double curX = (double) hueSlider.getWidth() / ((double) hueSegments / i);
                double width = (double) hueSlider.getWidth() / ((double) hueSegments);

                double x = (hueSlider.getX() + curX);
                double y = (hueSlider.getY() + ((hueSlider.getHeight()) / 2f) - 3);
                double actualWidth = Math.min(width, hueSlider.getWidth() - curX);


                Color left = ColorUtil.interpolate(fadeAnimation.getScaledTime(), color, ColorUtil.newAlpha(color, 0));
                Color right = ColorUtil.interpolate(fadeAnimation.getScaledTime(), color2, ColorUtil.newAlpha(color2, 0));
                RenderUtil.renderGradient(context.getDrawContext().getMatrices(), x, y, actualWidth, 6, left.getRGB(), right.getRGB(), true);

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
    public void renderStringWidget(TextEntryWidget widget, Context context, Rect rect, MouseHelper mouse)
    {
        renderRect(rect, context.getColorScheme().getTertiaryBackgroundColor(), context.getColorScheme().getTertiaryBackgroundColor(), RectMode.Fill, context);
        renderRect(rect, context.getColorScheme().getOutlineColor(), context.getColorScheme().getOutlineColor(), RectMode.Outline, context);
        String renderText = widget.typing ? widget.getValue() + (GUI.typeCounter ? "" : "_") : widget.getValue();
        int centerY = (rect.getHeight() - getTextHeight(renderText)) / 2;
        renderText(context.getDrawContext(), renderText, rect.getX() + 2, rect.getY() + centerY + 1, context.getColorScheme().getTextColor(), context.getColorScheme().doesTextShadow());
    }

    @Override
    public int getTextWidth(String text)
    {
        return (int) Fonts.getTextWidth(text);
    }

    @Override
    public int getTextHeight(String text)
    {
        return (int) Fonts.getTextHeight(text);
    }

    @Override
    public void renderText(DrawContext context, String text, float x, float y, Color color, boolean shadow)
    {
        Fonts.doOneText(context, text, x, y, ColorUtil.interpolate(fadeAnimation.getScaledTime(), color, ColorUtil.newAlpha(color, 0)), shadow);
    }

    @Override
    public void renderRect(Rect rect, Color inputTop, Color inputBottom, RectMode mode, Context context)
    {
        Color color = ColorUtil.interpolate(fadeAnimation.getScaledTime(), inputTop, ColorUtil.newAlpha(inputTop, 0));
        Color bottom = ColorUtil.interpolate(fadeAnimation.getScaledTime(), inputBottom, ColorUtil.newAlpha(inputBottom, 0));
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
    public void scissorRect(Rect dims)
    {
        ScaledResolution sr = new ScaledResolution(mc);
        double factor = sr.getScaleFactor();
//        GL11.glScissor(
//                ((int) (dims.getX() * factor)),
//                sr.getScaledHeight() - (dims.getY() + dims.getHeight()),
//                ((int) (dims.getWidth() * factor)),
//                ((int) (dims.getHeight() * factor))
//        );
    }

    @Override
    public Color getMainColor(int pos)
    {
        return mainColor.getValue().getColor();
    }

    @Override
    public Color getOutlineColor()
    {
        return outlineColor.getValue().getColor();
    }

    @Override
    public Color getButtonColor()
    {
        return new Color(0, 0, 0, 0);
    }

    @Override
    public Color getBackgroundColor()
    {
        return background.getValue().getColor();
    }

    @Override
    public Color getSecondaryBackgroundColor()
    {
        return new Color(0, 0, 0, 0);
    }

    @Override
    public Color getTertiaryBackgroundColor()
    {
        return new Color(0, 0, 0, 0);
    }

    @Override
    public Color getTextColor()
    {
        return textColor.getValue().getColor();
    }

    @Override
    public Color getTextColorHighlight()
    {
        return getTextColor();
    }

    @Override
    public Color getTextColorActive()
    {
        return getTextColor();
    }

    @Override
    public boolean doesTextShadow()
    {
        return FontModule.INSTANCE.textShadow.getValue();
    }

    @Override
    public int getSpacing()
    {
        return 1;
    }

    @Override
    public int getBetweenSpacing()
    {
        return 1;
    }

    @Override
    public int getSettingSpacing()
    {
        return getBetweenSpacing();
    }

    @Override
    public int getFrameWidth()
    {
        return width.getValue().intValue();
    }

    @Override
    public int getButtonHeight()
    {
        return height.getValue().intValue();
    }

    @Override
    public int getFrameHeight()
    {
        return getButtonHeight() + 2;
    }

    public void registerGUI()
    {
        ClickGui.INSTANCE.updateGUI(this, this, this);
    }

    @Override
    public String getDescription()
    {
        return "Sn0wGui: Main sn0w gui";
    }
}