package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.world.PacketUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;


public class EntityControl extends Module {
    public static EntityControl INSTANCE;

    public EntityControl()
    {
        super("EntityControl", Category.Player);
        INSTANCE = this;
    }

    Value<Boolean> mountBypass = new ValueBuilder<Boolean>()
            .withDescriptor("Mount Bypass")
            .withValue(false)
            .register(this);


    @SubscribeEvent
    public void onPacket(PacketEvent event)
    {
        if (NullUtils.nullCheck()) return;
        if (mountBypass.getValue())
        {
            if (event.getPacket() instanceof PlayerInteractEntityC2SPacket packet)
            {
                if (PacketUtils.getInteractType(packet) == PacketUtils.InteractType.INTERACT_AT && PacketUtils.getEntity(packet) instanceof AbstractDonkeyEntity)
                    event.setCancelled(true);
            }
        }
    }


    @Override
    public String getHudInfo()
    {
        if (mountBypass.getValue())
            return "Mount";

        return "Ride";
    }


    @Override
    public String getDescription()
    {
        return "EntityControl: Control entities u ride";
    }
}