package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.*;

import java.awt.*;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;


public class Blink extends Module {
    public static Blink INSTANCE;
    private final Queue<Packet<?>> packets = new LinkedBlockingQueue<>();


    public Blink()
    {
        super("Blink", Category.Player);
        INSTANCE = this;
    }


    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof PlayerActionC2SPacket || event.getPacket() instanceof PlayerMoveC2SPacket
                || event.getPacket() instanceof ClientCommandC2SPacket
                || event.getPacket() instanceof PlayerInteractEntityC2SPacket || event.getPacket() instanceof TeleportConfirmC2SPacket || event.getPacket() instanceof KeepAliveC2SPacket
        )
        {
            event.setCancelled(true);
            packets.add(event.getPacket());
        }

    }


    @Override
    public void onDisable()
    {
        super.onDisable();
        if (!packets.isEmpty())
        {
            for (Packet<?> p : packets)
            {
                PacketManager.INSTANCE.sendPacket(p);
            }
            packets.clear();
        }

    }


    @Override
    public String getHudInfo()
    {
        return packets.size() + "";
    }

    @Override
    public String getDescription()
    {
        return "Blink: \"Blinks\" packets to move in the blink of an eye!";
    }
}
