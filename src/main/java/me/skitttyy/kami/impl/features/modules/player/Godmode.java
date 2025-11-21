package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.world.PacketUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class Godmode extends Module {

    public Godmode()
    {
        super("Godmode", Category.Player);
    }


    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket packet && PacketUtils.getInteractType((PlayerInteractEntityC2SPacket) event.getPacket()) == PacketUtils.InteractType.ATTACK && PacketUtils.getEntity((PlayerInteractEntityC2SPacket) event.getPacket()) instanceof EndCrystalEntity)
        {

            PacketUtils.getEntity(packet).remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public String getDescription()
    {
        return "Godmode: Removes crystals when u hit them when in godmode";
    }
}