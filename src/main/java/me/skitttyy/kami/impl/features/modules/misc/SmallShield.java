package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.FrameEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.mixin.accessor.IHeldItemRenderer;

public class SmallShield extends Module {


    public static SmallShield INSTANCE;

    public SmallShield()
    {
        super("SmallShield", Category.Misc);
        INSTANCE = this;
    }

    public Value<Number> offset = new ValueBuilder<Number>()
            .withDescriptor("Offset")
            .withValue(1.0)
            .withRange(0.1, 3.0)
            .withPlaces(1)
            .register(this);


    @SubscribeEvent
    public void onUpdate(FrameEvent.FrameFlipEvent event)
    {
        if (NullUtils.nullCheck()) return;


        ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressOffHand(offset.getValue().floatValue());

    }


    @Override
    public String getDescription()
    {
        return "SmallShield: Moves your offhand downwards";
    }
}
