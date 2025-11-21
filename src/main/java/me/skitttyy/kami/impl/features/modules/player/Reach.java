package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.player.ReachEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class Reach extends Module {
    public static Reach INSTANCE;

    public Value<Number> reach = new ValueBuilder<Number>()
            .withDescriptor("Reach")
            .withValue(0)
            .withRange(0, 3)
            .withPlaces(1)
            .register(this);

    public Reach()
    {
        super("Reach", Category.Player);
        INSTANCE = this;
    }


    @SubscribeEvent
    public void onReachEvent(ReachEvent event)
    {
        event.setReach(event.getReach() + reach.getValue().floatValue());
        event.setCancelled(true);
    }

    @Override
    public String getHudInfo()
    {
        return "+" + MathUtil.round((reach.getValue().floatValue()), 1);
    }

    @Override
    public String getDescription()
    {
        return "Reach: reach far away blocks";
    }

}
