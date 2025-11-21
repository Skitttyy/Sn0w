package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class Capes extends Module {
    public static Capes INSTANCE;
    public Value<String> capeMode = new ValueBuilder<String>()
            .withDescriptor("Cape Mode", "capeMode")
            .withValue("Dark")
            .withModes("Dark", "White", "Pk", "Emp", "None")
            .register(this);

    public Capes()
    {
        super("Capes", Category.Render);
        INSTANCE = this;
    }
    @Override
    public String getDescription() {
        return "Capes: Gives you cool capes that other sn0w users can see";
    }
}
