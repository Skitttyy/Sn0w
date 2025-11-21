package me.skitttyy.kami.api.management;


import lombok.Getter;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.network.ServerEvent;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.impl.KamiMod;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.util.Arrays;

@Getter
public class TPSManager {


    private float[] tpsCounts = new float[10];


    private float ticksPerSecond = 20.0F;
    private int incPackets = 0;
    private int sendPackets = 0;
    private int time = 0;
    @Getter
    private long day = 0;
    private final int NORMALTPS = 20;
    Timer packetTimer;

    public static TPSManager INSTANCE;

    public TPSManager()
    {
        KamiMod.EVENT_BUS.register(this);
        packetTimer = new Timer();
        packetTimer.setDelay(1000);

        Arrays.fill(tpsCounts, 20);

    }


    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event)
    {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
        {
            onTimeUpdate();
        }
        incPackets++;
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event)
    {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
        {
            onTimeUpdate();
        }
        sendPackets++;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (packetTimer.isPassed())
        {
            incPackets = 0;
            sendPackets = 0;
            packetTimer.resetDelay();
        }
    }

    @SubscribeEvent
    public void onDisconnect(ServerEvent.ServerLeft event)
    {
        reset();
    }


    private int getNextStep(int time)
    {
        if (time > 23000) return 24000;
        if (time > 13000) return 23000;
        if (time > 12000) return 13000;
        return 12000;
    }

    public void reset()
    {
        this.ticksPerSecond = 20.0F;
    }


    public float getTickRate()
    {
        return ticksPerSecond;
    }

    public Long getLastResponse()
    {
        return lastUpdate;
    }

    public float getAverage()
    {
        float total = 0L;

        for (float j : tpsCounts)
            total += j;

        return (total / tpsCounts.length);
    }

    private long lastUpdate = -1;

    public void onTimeUpdate()
    {
        long currentTime = System.currentTimeMillis();

        if (lastUpdate == -1)
        {
            lastUpdate = currentTime;
            return;
        }

        long timeDiff = currentTime - lastUpdate;

        float tickTime = timeDiff / 20.0F;
        if (tickTime == 0)
        {
            tickTime = 50;
        }

        float tps = 1000 / tickTime;

        System.arraycopy(tpsCounts, 0, tpsCounts, 1, tpsCounts.length - 1);
        tpsCounts[0] = tps;

        this.ticksPerSecond = tps;
        lastUpdate = currentTime;
    }

    public String getTimeOverTps()
    {
        if (time == 0) return "0/0";

        return (time / NORMALTPS) + "/" + (getNextStep(time) / NORMALTPS);
    }

    public String getTimePhase()
    {
        if (time > 23000) return "Dawn";
        if (time > 18500) return "Night";
        if (time > 17500) return "Midnight";
        if (time > 13000) return "Evening";
        if (time > 12000) return "Dusk";
        if (time > 6500) return "Afternoon";
        if (time > 5500) return "Noon";
        return "Morning";
    }

}
