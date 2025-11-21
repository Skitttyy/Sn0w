package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class TimeChanger extends Module {
    public static TimeChanger INSTANCE;

    public Value<Number> time = new ValueBuilder<Number>()
            .withDescriptor("Time")
            .withValue(1000)
            .withRange(0, 23000)
            .register(this);
    long oldTime;

    public TimeChanger()
    {
        super("TimeChanger", Category.Render);
        INSTANCE = this;
    }


    @Override
    public void onEnable()
    {
        super.onEnable();


        if (NullUtils.nullCheck()) return;

        oldTime = mc.world.getTime();
    }


    @Override
    public void onDisable()
    {
        super.onDisable();

        if (NullUtils.nullCheck()) return;

        mc.world.setTimeOfDay(oldTime);
    }

    @SubscribeEvent
    private void onPacketReceive(PacketEvent.Receive event)
    {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
        {
            oldTime = ((WorldTimeUpdateS2CPacket) event.getPacket()).getTime();
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        mc.world.setTimeOfDay(time.getValue().longValue() * 1000);
    }

    @Override
    public String getDescription()
    {
        return "TimeChanger: changes the time";
    }

}
