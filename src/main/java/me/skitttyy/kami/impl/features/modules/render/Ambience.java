package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class Ambience extends Module
{
    public static Ambience INSTANCE;
    public Value<Sn0wColor> color = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Color")
            .withValue(new Sn0wColor(0, 255, 255, 255))
            .register(this);

    public Ambience()
    {
        super("Ambience", Category.Render);
        INSTANCE = this;
    }



    @Override
    public String getDescription()
    {
        return "Ambience: colors the world a different color";
    }

}
