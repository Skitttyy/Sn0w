package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class EntitySpeed extends Module
{
    Value<Number> speed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(2.0d)
            .withRange(0.1d, 5.0d)
            .register(this);
    Value<Boolean> strict = new ValueBuilder<Boolean>()
            .withDescriptor("Strict")
            .withValue(false)
            .register(this);
    Value<Boolean> packet = new ValueBuilder<Boolean>()
            .withDescriptor("Packet")
            .withValue(false)
            .register(this);
    Value<String> bounds = new ValueBuilder<String>()
            .withDescriptor("Bounds")
            .withValue("Up")
            .withModes("Up", "Down", "Preserve", "Limit", "Soft", "Snap")
            .withParent(packet)
            .withParentEnabled(true)
            .register(this);
    public static EntitySpeed INSTANCE;

    public EntitySpeed()
    {
        super("EntitySpeed", Category.Movement);
        INSTANCE = this;
    }

    private static final Random rand = new Random();

    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        if (mc.player.isRiding() && mc.player.getControllingVehicle() != null)
        {
            double d = Math.cos(Math.toRadians(mc.player.getYaw() + 90.0f));
            double d2 = Math.sin(Math.toRadians(mc.player.getYaw() + 90.0f));
            if (strict.getValue())
            {
                PacketManager.INSTANCE.sendPacket(PlayerInteractEntityC2SPacket.interact(
                        mc.player.getControllingVehicle(), false, Hand.MAIN_HAND));
            }
            handleEntityMotion(speed.getValue().floatValue(), d, d2);
        }


    }

    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;


        if (packet.getValue())
            if (mc.player.isRiding() && mc.player.getControllingVehicle() != null)
            {

                Vec3d vec3d = mc.player.getControllingVehicle().getPos().add(0.0, getBounds(mc.player.getControllingVehicle().getPos()), 0.0);
                BoatEntity entityBoat = new BoatEntity(mc.world, vec3d.x, vec3d.y, vec3d.z);
                entityBoat.setYaw(mc.player.getControllingVehicle().getYaw());
                entityBoat.setPitch(mc.player.getControllingVehicle().getPitch());
                PacketManager.INSTANCE.sendPacket(new VehicleMoveC2SPacket(entityBoat));
            }
    }

    @SubscribeEvent
    public void onPacketInbound(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;

        if (!mc.player.isRiding() || mc.options.sneakKey.isPressed() || mc.player.getControllingVehicle() == null)
        {
            return;
        }
        if (strict.getValue())
        {
            if (event.getPacket() instanceof EntityPassengersSetS2CPacket)
            {
                event.setCancelled(true);
            } else if (event.getPacket() instanceof PlayerPositionLookS2CPacket)
            {
                event.setCancelled(true);
            }
        }
    }

    private void handleEntityMotion(float entitySpeed, double d, double d2)
    {
        Vec3d motion = mc.player.getControllingVehicle().getVelocity();
        //
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        if (forward == 0 && strafe == 0)
        {
            mc.player.getControllingVehicle().setVelocity(0.0, motion.y, 0.0);
            return;
        }
        mc.player.getControllingVehicle().setVelocity((forward * entitySpeed * d) + (strafe * entitySpeed * d2),
                motion.y, (forward * entitySpeed * d2) - (strafe * entitySpeed * d));
    }

    public double getBounds(Vec3d in)
    {
        switch (bounds.getValue())
        {
            case "Up":
                return in.y + (1337);
            case "Down":
                return in.y + (-1337);
            case "Preserve":
                int n = rand.nextInt(29000000);
                if (rand.nextBoolean())
                {
                    return n;
                }
                return in.y + (-n);
            case "Limit":
                int j = rand.nextInt(22) + 70;

                return in.y + j;
            case "Soft":
            case "Snap":
                return 0;
        }
        return in.y + (-1337);
    }

    @Override
    public String getHudInfo()
    {
        return MathUtil.round(speed.getValue().doubleValue(), 1) + "";
    }

    @Override
    public String getDescription()
    {
        return "EntitySpeed: Rideee (fast) till u cant no more";
    }
}
