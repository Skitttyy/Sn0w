package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.player.MiddleClick;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class BoatFly extends Module
{
    Value<Number> speed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(0.9D)
            .withPlaces(1)
            .withRange(0.1d, 12.0D)
            .register(this);
    Value<Boolean> glide = new ValueBuilder<Boolean>()
            .withDescriptor("Glide")
            .withValue(false)
            .register(this);
    Value<Number> fallSpeed = new ValueBuilder<Number>()
            .withDescriptor("Fall Speed")
            .withValue(0.0d)
            .withPlaces(1)
            .withRange(0.0d, 2.0d)
            .withParent(glide)
            .withParentEnabled(true)
            .register(this);

    private static final Random rand = new Random();


    public static BoatFly INSTANCE;

    public BoatFly()
    {
        super("BoatFly", Category.Movement);
        INSTANCE = this;
    }

    @SubscribeEvent
    private void onTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (mc.player.getControllingVehicle() == null)
            return;


        Entity entity = mc.player.getControllingVehicle();

        if (mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed())
        {
            entity.setVelocity(entity.getVelocity().x, (speed.getValue().floatValue()), entity.getVelocity().z);
        } else if (mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed())
        {

            entity.setVelocity(entity.getVelocity().x, -(speed.getValue().floatValue()), entity.getVelocity().z);
        } else
        {
            if (glide.getValue())
            {
                entity.setVelocity(entity.getVelocity().x, -fallSpeed.getValue().floatValue(), entity.getVelocity().z);
            } else
            {
                entity.setVelocity(entity.getVelocity().x, 0, entity.getVelocity().z);
            }
        }
        entity.setYaw(mc.player.getYaw());

    }

    @Override
    public String getHudInfo()
    {
        return "Strong";
    }

    @Override
    public String getDescription()
    {
        return "BoatFly: Fly On Entitys";
    }


}
