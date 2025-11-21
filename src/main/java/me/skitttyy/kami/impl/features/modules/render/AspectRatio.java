package me.skitttyy.kami.impl.features.modules.render;


import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class AspectRatio extends Module {

    public static AspectRatio INSTANCE;
    public Value<Number> aspectAmount = new ValueBuilder<Number>()
            .withDescriptor("Aspect Amount")
            .withValue(1)
            .withRange(0, 3)
            .register(this);

    public AspectRatio()
    {
        super("AspectRatio", Category.Render);
        INSTANCE = this;
    }

    @Override
    public String getDescription()
    {
        return "AspectRatio: fan csgo hvh with this";
    }

}
