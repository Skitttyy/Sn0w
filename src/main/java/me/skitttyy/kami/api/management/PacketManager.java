package me.skitttyy.kami.api.management;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.ServerEvent;
import me.skitttyy.kami.api.utils.ducks.IClientPlayNetworkHandler;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.misc.FastLatency;
import me.skitttyy.kami.mixin.accessor.IClientWorld;
import net.minecraft.client.network.*;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;


import java.util.HashSet;
import java.util.Set;


public class PacketManager implements IMinecraft {

    public static PacketManager INSTANCE;


    private static final Set<Packet<?>> PACKET_CACHE = new HashSet<>();
    private ServerAddress address;
    public static final Set<Packet<?>> CANCELATHANDLE = new HashSet<>();

    private ServerInfo info;

    public PacketManager()
    {
        KamiMod.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onDisconnect(ServerEvent.ServerLeft event)
    {
        PACKET_CACHE.clear();
    }

    public void sendPacket(final Packet<?> p)
    {
        if (mc.getNetworkHandler() != null)
        {
            PACKET_CACHE.add(p);
            mc.getNetworkHandler().sendPacket(p);
        }
    }

    public void sendQuietPacket(final Packet<?> p)
    {
        if (mc.getNetworkHandler() != null)
        {
            PACKET_CACHE.add(p);
            ((IClientPlayNetworkHandler) mc.getNetworkHandler()).sendQuietPacket(p);
        }
    }

    public void specialCaseCancel(Packet<?> packet)
    {
        CANCELATHANDLE.add(packet);
    }

    public void sendPacket(final SequencedPacketCreator p)
    {
        if (mc.world != null)
        {
            PendingUpdateManager updater =
                    ((IClientWorld) mc.world).accquirePendingUpdateManager().incrementSequence();
            try
            {
                int i = updater.getSequence();
                Packet<ServerPlayPacketListener> packet = p.predict(i);
                sendPacket(packet);
            } catch (Throwable e)
            {
                e.printStackTrace();
                if (updater != null)
                {
                    try
                    {
                        updater.close();
                    } catch (Throwable e1)
                    {
                        e1.printStackTrace();
                        e.addSuppressed(e1);
                    }
                }
                throw e;
            }
            if (updater != null)
            {
                updater.close();
            }
        }
    }

    public int getClientLatency()
    {

        if (FastLatency.INSTANCE.isEnabled())
            return FastLatency.INSTANCE.resolvedPing;


        if (mc.getNetworkHandler() != null)
        {
            final PlayerListEntry playerEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (playerEntry != null)
            {
                return playerEntry.getLatency();
            }
        }
        return 0;
    }

    public ServerAddress getAddress()
    {
        return address;
    }

    public void setAddress(ServerAddress address)
    {
        this.address = address;
    }

    public ServerInfo getInfo()
    {
        return info;
    }

    public void setInfo(ServerInfo info)
    {
        this.info = info;
    }

    public boolean isCrystalPvpCC()
    {
        if (info != null)
        {
            return info.address.equalsIgnoreCase("us.crystalpvp.cc") || info.address.equalsIgnoreCase("crystalpvp.cc");
        }
        return false;
    }

    public boolean isGrimCC()
    {
        return info != null && info.address.equalsIgnoreCase("grim.crystalpvp.cc");
    }


    public boolean isCached(Packet<?> p)
    {
        return PACKET_CACHE.contains(p);
    }

    public boolean uncache(Packet<?> p)
    {
        return PACKET_CACHE.remove(p);
    }

}