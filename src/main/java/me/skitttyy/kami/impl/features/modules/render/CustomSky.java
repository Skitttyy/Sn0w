package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

import java.awt.*;

public class CustomSky extends Module {
    public static CustomSky INSTANCE;
    public Value<Sn0wColor> fogColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Color")
            .withValue(new Sn0wColor(0, 255, 255, 255))
            .register(this);

    public CustomSky()
    {
        super("CustomSky", Category.Render);
        INSTANCE = this;
    }

    @Override
    public String getDescription()
    {
        return "CustomSky: changes the color of the sky";
    }

}
