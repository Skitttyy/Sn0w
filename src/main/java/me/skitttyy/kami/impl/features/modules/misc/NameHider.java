package me.skitttyy.kami.impl.features.modules.misc;


import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class NameHider extends Module {
    public static NameHider INSTANCE;
    public Value<String> replacement = new ValueBuilder<String>()
            .withDescriptor("Replace")
            .withValue("nice iq")
            .register(this);

    public NameHider()
    {
        super("NameHider", Category.Misc);
        INSTANCE = this;
    }

    @Override
    public String getDescription()
    {
        return "NameHider: be anonymous";
    }


}
