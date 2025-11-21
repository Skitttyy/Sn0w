package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.movement.NoSlow;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;

public class FastLatency extends Module {

    public static FastLatency INSTANCE;
    Timer timer = new Timer();


    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(1000)
            .withRange(0, 5000)
            .withAction(set -> timer.setDelay(set.getValue().longValue()))
            .register(this);


    private long ping;
    public int resolvedPing;

    public FastLatency()
    {
        super("FastLatency", Category.Misc);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        if (timer.isPassed())
        {
            PacketManager.INSTANCE.sendPacket(new RequestCommandCompletionsC2SPacket(27845, "w "));
            ping = System.currentTimeMillis();
            timer.resetDelay();
        }
    }


    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e)
    {
        if (e.getPacket() instanceof CommandSuggestionsS2CPacket c && c.id() == 27845)
        {
            resolvedPing = (int) MathUtil.clamp(System.currentTimeMillis() - ping, 0, 1000) / 2;
            timer.resetDelay();
        }
    }

    @Override
    public String getDescription()
    {
        return "FastLatency: Gets ping updates faster";
    }

}
