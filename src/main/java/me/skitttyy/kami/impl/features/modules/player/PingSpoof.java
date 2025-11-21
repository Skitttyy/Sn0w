package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PingSpoof extends Module {


    public static PingSpoof INSTANCE;

    public PingSpoof()
    {
        super("PingSpoof", Category.Player);
        INSTANCE = this;
    }

    private final ConcurrentMap<Packet<?>, Long> cachedPackets =
            new ConcurrentHashMap<>();
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(100)
            .withRange(30, 1000)
            .withPlaces(0)
            .register(this);

    Value<Boolean> transactions = new ValueBuilder<Boolean>()
            .withDescriptor("Transactions")
            .withValue(true)
            .register(this);
    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (mc.world == null)
            return;


        if (!mc.isInSingleplayer())
        {
            if (event.getPacket() instanceof KeepAliveC2SPacket
                    || ((event.getPacket() instanceof ResourcePackStatusC2SPacket
                    || event.getPacket() instanceof CommonPongC2SPacket) && transactions.getValue()))
            {
                if (cachedPackets.containsKey(event.getPacket()))
                {
                    cachedPackets.remove(event.getPacket());
                    return;
                }
                cachedPackets.put(event.getPacket(), System.currentTimeMillis());
                event.setCancelled(true);
            }
        }


    }


    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;


        cachedPackets.forEach((packet, time) ->
        {
            long elapsed = System.currentTimeMillis() - time;
            if (elapsed > delay.getValue().longValue())
            {
                mc.player.networkHandler.sendPacket(packet);
                cachedPackets.remove(packet);
            }
        });

    }

    @Override
    public String getHudInfo()
    {
        return "KeepAlive";
    }

    @Override
    public String getDescription()
    {
        return "PingSpoof: Delays keep alives to make your ping appear higher than it actually is";
    }
}
