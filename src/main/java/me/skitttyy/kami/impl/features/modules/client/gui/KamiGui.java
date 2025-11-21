package me.skitttyy.kami.impl.features.modules.client.gui;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.theme.IColorScheme;
import me.skitttyy.kami.api.gui.theme.IMetrics;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import me.skitttyy.kami.impl.gui.ClickGui;
import me.skitttyy.kami.impl.gui.renderer.Renderer;

import java.awt.*;

public class KamiGui extends Module implements IColorScheme, IMetrics {

    public static KamiGui INSTANCE;

    Value<Sn0wColor> color = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Color")
            .withValue(new Sn0wColor(32, 159, 220))
            .register(this);
    Value<Sn0wColor> outlineColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Outline Color")
            .withValue(new Sn0wColor(32, 159, 220))
            .register(this);
    Value<Number> frameText = new ValueBuilder<Number>()
            .withDescriptor("Frame Text")
            .withValue(255)
            .withRange(0, 255)
            .register(this);
    Value<Number> text = new ValueBuilder<Number>()
            .withDescriptor("Text")
            .withValue(0)
            .withRange(0, 255)
            .register(this);
    Value<Sn0wColor> frameColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Frame Color")
            .withValue(new Sn0wColor(255, 255, 255))
            .register(this);
    Value<Sn0wColor> secondaryColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Secondary Color")
            .withValue(new Sn0wColor(255, 255, 255))
            .register(this);
    Value<Sn0wColor> teritary = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Teritary Color")
            .withValue(new Sn0wColor(255, 255, 255))
            .register(this);
    Value<Number> width = new ValueBuilder<Number>()
            .withDescriptor("Width")
            .withValue(120)
            .withRange(80, 200)
            .register(this);
    Value<Number> spacing = new ValueBuilder<Number>()
            .withDescriptor("Spacing")
            .withValue(2)
            .withRange(0, 4)
            .register(this);
    Value<Number> inBSpacing = new ValueBuilder<Number>()
            .withDescriptor("In Spacing")
            .withValue(1)
            .withRange(0, 4)
            .register(this);


    public KamiGui()
    {
        super("MemeGui", Category.Client);
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        ClickGui.INSTANCE.enterGui(this, this, new Renderer());
        setEnabled(false);
    }

    @Override
    public Color getMainColor(int pos)
    {
        return color.getValue().getColor();
    }

    @Override
    public Color getOutlineColor()
    {
        return outlineColor.getValue().getColor();
    }

    @Override
    public Color getButtonColor()
    {
        return getMainColor(0);
    }

    @Override
    public Color getBackgroundColor()
    {
        return frameColor.getValue().getColor();
    }

    @Override
    public Color getSecondaryBackgroundColor()
    {
        return secondaryColor.getValue().getColor();
    }

    @Override
    public Color getTertiaryBackgroundColor()
    {
        return teritary.getValue().getColor();
    }

    @Override
    public Color getTextColor()
    {
        return new Color(text.getValue().intValue(), text.getValue().intValue(), text.getValue().intValue(), 255);
    }

    @Override
    public Color getTextColorHighlight()
    {
        return new Color(frameText.getValue().intValue(), frameText.getValue().intValue(), frameText.getValue().intValue());
    }

    @Override
    public Color getTextColorActive()
    {
        return getMainColor(0);
    }

    @Override
    public boolean doesTextShadow()
    {
        return FontModule.INSTANCE.textShadow.getValue();
    }

    @Override
    public int getSpacing()
    {
        return spacing.getValue().intValue();
    }

    @Override
    public int getBetweenSpacing()
    {
        return inBSpacing.getValue().intValue();
    }

    @Override
    public int getSettingSpacing()
    {
        return 0;
    }


    @Override
    public int getFrameWidth()
    {
        return width.getValue().intValue();
    }

    @Override
    public int getButtonHeight()
    {
        return 12;
    }

    @Override
    public int getFrameHeight()
    {
        return getButtonHeight() + 2;
    }


    @Override
    public String getDescription()
    {
        return "MemeGui: gui that fans kami";
    }
}
