package me.skitttyy.kami.api.management;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.player.PopEvent;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class PopManager implements IMinecraft {

    public static PopManager INSTANCE;

    public final Object2IntOpenHashMap<String> registry = new Object2IntOpenHashMap<>();


    public PopManager()
    {
        KamiMod.EVENT_BUS.register(this);
    }

    @SubscribeEvent(Priority.MANAGER_FIRST)
    public void onPacket(PacketEvent.Receive event)
    {

        if (event.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING)
        {

            Entity entity = packet.getEntity(mc.world);
            if (entity != null)
            {
                onPop(entity);
            }
        }
    }

    @SubscribeEvent
    private void onDeath(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;


        if (event.getPacket() instanceof EntityStatusS2CPacket pac && pac.getStatus() == 3)
        {
            Entity entity = pac.getEntity(mc.world);

            if (entity != null)
                new LivingEvent.Death(entity).post();
        }
    }


    public void onPop(Entity entity)
    {
        if (NullUtils.nullCheck()) return;

        final String name = entity.getName().getString();
        registry.put(name, (registry.getInt(name) + 1));
        new PopEvent.TotemPopEvent(entity, registry.getInt(name)).post();
    }

    public void onDeath(PlayerEntity player)
    {
        if (NullUtils.nullCheck()) return;

        final String name = player.getName().getString();
        if (registry.containsKey(name))
        {
            int pops = registry.getInt(name);
            registry.removeInt(name);
            new PopEvent.DeathPopEvent(player, pops).post();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.AfterClientTickEvent e)
    {
        if (NullUtils.nullCheck()) return;

        for (Entity player : mc.world.getEntities())
        {
            if (!(player instanceof PlayerEntity)) continue;

            if (((PlayerEntity) player).getHealth() > 0.0F)
                continue;

            this.onDeath((PlayerEntity) player);
        }
    }

//    @SubscribeEvent
//    public void onConnection(ConnectionEvent event)
//    {
//        if (NullUtils.nullCheck()) return;
//
//        //prevent spam when u first join
//        if (!(mc.player.ticksExisted > 20)) return;
//
//
//        if (Manager.INSTANCE.clearPopsOnLog.getValue())
//        {
//            if (event.getConnectionType() == ConnectionEvent.ConnectionType.LOGOUT)
//            {
//                if (!event.getName().equalsIgnoreCase(mc.session.getUsername()))
//                {
//                    if (event.getName() != null && !event.getName().equalsIgnoreCase(""))
//                    {
//                        clearPops(event.getName());
//                    }
//                }
//            }
//        }
//    }

    public int getPops(Entity entity)
    {
        return registry.getInt(entity.getName().getString());
    }

    public int getPops(String name)
    {
        return registry.getInt(name);
    }

    public void clearPops(String name)
    {
        if (registry.containsKey(name))
        {
            registry.put(name, 0);
        }
    }

}
