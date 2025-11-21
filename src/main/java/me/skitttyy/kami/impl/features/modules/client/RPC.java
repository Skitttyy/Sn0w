package me.skitttyy.kami.impl.features.modules.client;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.network.DiscordPresence;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class RPC extends Module {

    public static RPC INSTANCE;
    public Value<String> text = new ValueBuilder<String>()
            .withDescriptor("Text")
            .withValue("I love cats so flipping much")
            .register(this);
    public Value<String> image = new ValueBuilder<String>()
            .withDescriptor("Images")
            .withValue("Animals")
            .withModes("Animals", "Sn0wIcon", "Grails")
            .register(this);
    public RPC() {
        super("RPC", Category.Client);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        DiscordPresence.start();
    }

    @Override
    public void onDisable() {
        DiscordPresence.stop();
    }

    @Override
    public String getDescription() {
        return "RPC: Rep Sn0w on discord";
    }
}
