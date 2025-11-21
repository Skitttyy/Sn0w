package me.skitttyy.kami.impl.features.modules.movement.timer;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.render.RenderTimer;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class Timer extends Module {
    public Timer()
    {
        super("Timer", Category.Movement);
    }

    Value<Number> timerAmount = new ValueBuilder<Number>()
            .withDescriptor("Timer Amount")
            .withValue(5)
            .withRange(0.1, 10)
            .register(this);

    float oldTickLength = 1.0F;
    int boostTime;
    int boostSpaceTime;

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (NullUtils.nullCheck()) return;

        oldTickLength = RenderTimer.getTickLength();
        boostTime = 0;
        doLoop = false;
        boostSpaceTime = 0;
    }

    boolean doLoop;

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (NullUtils.nullCheck()) return;

        if (mc.getRenderTickCounter()  == null) return;

        RenderTimer.setTickLength(oldTickLength);

    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (mc.getRenderTickCounter() == null) return;

        RenderTimer.setTickLength(timerAmount.getValue().floatValue());
    }

    @Override
    public String getHudInfo()
    {
        return MathUtil.round(timerAmount.getValue().floatValue(), 1) + "";
    }

    @Override
    public String getDescription()
    {
        return "Timer: Decreases/Increases the amount of ticks in a second";
    }
}
