package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.impl.features.modules.misc.FakePlayer;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class NoAccel extends Module
{

    public NoAccel()
    {
        super("NoAccel", Category.Movement);
    }

    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Strict")
            .withModes("Strict", "Max", "Collide")
            .register(this);

    public static boolean paused;

    Timer timer = new Timer();

    @SubscribeEvent
    public void onMove(MoveEvent event)
    {


        if (NullUtils.nullCheck()) return;


        timer.setDelay(1000);


        if (!mode.getValue().equals("Collide"))
        {
            if (mc.player.isFallFlying() || mc.player.getAbilities().flying || paused) return;


            if (mc.player.isSneaking() || mc.player.isSubmergedInWater() || mc.player.isInLava()) return;


            if (mode.getValue().equals("Strict") && (!mc.player.isOnGround() || mc.options.jumpKey.isPressed())) return;


            if (PlayerUtils.isMoving())
            {
                PlayerUtils.doStrafe(event, PlayerUtils.getStrictBaseSpeed(0.2873f));
            } else
            {
                event.motionX(0);
                event.motionZ(0);
            }

        } else
        {

            if(!timer.isPassed()) return;

            if (PlayerUtils.isMoving())
            {
                int collisions = 0;
                for (Entity entity : mc.world.getEntities())
                {
                    if (checkIsCollidingEntity(entity) && MathHelper.sqrt((float) mc.player.squaredDistanceTo(entity)) <= 1.5)
                    {
                        collisions++;
                    }
                }
                if (collisions > 0)
                {
                    Vec3d velocity = mc.player.getVelocity();
                    double factor = 0.07 * collisions;
                    Vec2f strafe = PlayerUtils.getStrafeVec((float) factor);
                    mc.player.setVelocity(velocity.x + strafe.x, velocity.y, velocity.z + strafe.y);
                }
            }
        }
    }

    public boolean checkIsCollidingEntity(Entity entity)
    {
        return entity != null && entity != mc.player && entity instanceof LivingEntity
                && (FakePlayer.INSTANCE.fakePlayer == null || FakePlayer.INSTANCE.fakePlayer != entity) && !(entity instanceof ArmorStandEntity);
    }


    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event)
    {
        if (event.getPacket() instanceof EntityPositionS2CPacket packet)
        {
            if (packet.getEntityId() == mc.player.getId())
            {
                timer.resetDelay();
            }
        }
    }

    @Override
    public String getHudInfo()
    {
        return mode.getValue();
    }

    @Override
    public String getDescription()
    {
        return "NoAccel: Removes the acceleration when you run so you are always as fast as possible";
    }
}
