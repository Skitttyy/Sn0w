package me.skitttyy.kami.impl.features.modules.client;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.client.font.FontManager;

public class FontModule extends Module
{

    public Value<Boolean> textShadow = new ValueBuilder<Boolean>()
            .withDescriptor("Shadow")
            .withValue(true)
            .register(this);
    public Value<Boolean> shortShadow = new ValueBuilder<Boolean>()
            .withDescriptor("Short")
            .withValue(false)
            .withParent(textShadow)
            .withParentEnabled(true)
            .register(this);
    public Value<Boolean> pop = new ValueBuilder<Boolean>()
            .withDescriptor("Pop")
            .withValue(false)
            .withParent(textShadow)
            .withParentEnabled(true)
            .register(this);
    public Value<Boolean> antiAlias = new ValueBuilder<Boolean>()
            .withDescriptor("AntiAlias")
            .withValue(true)
            .withAction(s -> doRefresh(s.getValue()))
            .register(this);
    public Value<Number> fontSize = new ValueBuilder<Number>()
            .withDescriptor("Font Size")
            .withValue(18)
            .withRange(10, 25)
            .withPlaces(0)
            .withAction(s -> doRefresh(s.getValue().intValue()))
            .register(this);

    public static FontModule INSTANCE;

    public FontModule()
    {
        super("Font", Category.Client);
        INSTANCE = this;
    }

    @Override
    public String getDescription()
    {
        return "Font: change the text rendering";
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        lastAlias = antiAlias.getValue();
        lastValue = fontSize.getValue().intValue();

    }
    int lastValue;
    boolean lastAlias = false;

    public void doRefresh(int value)
    {
        if (lastValue != value)
        {
            Fonts.refresh();
        }
        lastValue = value;


    }


    public void doRefresh(boolean value)
    {
        if (lastAlias != value)
        {
            Fonts.refresh();
        }
        lastAlias = value;


    }
}
