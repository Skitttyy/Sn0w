package me.skitttyy.kami.api.management;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.util.math.Box;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class HitboxManager implements IMinecraft
{

    public static HitboxManager INSTANCE;

    private final List<Entity> serverCrawling = new CopyOnWriteArrayList<>();

    public HitboxManager()
    {
        KamiMod.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPacketInbound(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;


        if (event.getPacket() instanceof EntityTrackerUpdateS2CPacket packet)
        {
            Entity entity = mc.world.getEntityById(packet.id());
            if (!(entity instanceof PlayerEntity))
            {
                return;
            }

            for (DataTracker.SerializedEntry<?> serializedEntry : packet.trackedValues())
            {
                DataTracker.Entry<?> entry = entity.getDataTracker().entries[serializedEntry.id()];
                if (!entry.getData().equals(Entity.POSE))
                {
                    continue;
                }


                if (serializedEntry.value().equals(EntityPose.SWIMMING))
                {
                    if (!serverCrawling.contains(entity))
                        serverCrawling.add(entity);
                } else
                {
                    serverCrawling.remove(entity);
                }
            }
        }
    }

    public boolean isServerCrawling(Entity entity)
    {
        return serverCrawling.contains(entity);
    }

    public Box getCrawlingBoundingBox(Entity entity)
    {
        return entity.getDimensions(EntityPose.SWIMMING).getBoxAt(entity.getPos());
    }
}