package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class ViewClip extends Module {
    public static ViewClip INSTANCE;
    public Value<Number> distance = new ValueBuilder<Number>()
            .withDescriptor("Distance")
            .withValue(4.0d)
            .withRange(1.0d, 10.0D)
            .register(this);

    public ViewClip()
    {
        super("ViewClip", Category.Render);
        INSTANCE = this;
    }

    @Override
    public String getDescription()
    {
        return "ViewClip: makes your third person go through blocks";
    }
}
