package me.skitttyy.kami.api.management;

import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;

public class BoostManager implements IMinecraft {

    public static BoostManager INSTANCE;

    Timer explosionTimer = new Timer();
    Timer longjumpTimer = new Timer();

    double boostExplosionSpeed;
    boolean canLongjump = false;

    public BoostManager()
    {
        KamiMod.EVENT_BUS.register(this);
        boostExplosionSpeed = 0;
    }

    @SubscribeEvent(Priority.MANAGER_FIRST)
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;


//        if (event.getPacket() instanceof SPacketEntityVelocity)
//        {
//            SPacketEntityVelocity packet = (SPacketEntityVelocity) event.getPacket();
//            if (packet.getEntityID() == mc.player.getEntityId())
//            {
//                explosionTimer.resetDelay();
//                boostExplosionSpeed = Math.hypot(packet.getMotionX() / 8000D, packet.getMotionZ() / 8000D);
//            }
//        }

        if (event.getPacket() instanceof ExplosionS2CPacket packet)
        {

            if (mc.player.getPos().distanceTo(new Vec3d(packet.getX(), packet.getY(), packet.getZ())) <= 6.0 && (packet.getPlayerVelocityX() != 0 || packet.getPlayerVelocityZ()!= 0))
            {
                explosionTimer.resetDelay();

                boostExplosionSpeed = Math.hypot(packet.getPlayerVelocityX(), packet.getPlayerVelocityZ());

                canLongjump = true;
                longjumpTimer.resetDelay();

            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        explosionTimer.setDelay(400);
        longjumpTimer.setDelay(500);

        if (!PlayerUtils.isMoving())
        {
            boostExplosionSpeed = 0;
            canLongjump = false;
        }
        if (explosionTimer.isPassed())
        {
            boostExplosionSpeed = 0;
        }
        if (longjumpTimer.isPassed())
        {
            canLongjump = false;
        }

    }

    public double getBoostSpeed(boolean slow)
    {
        if (slow && boostExplosionSpeed != 0)
        {
            return AntiCheat.INSTANCE.boostAmount.getValue().floatValue();
        }

        return boostExplosionSpeed;
    }

    public boolean canDoLongjump()
    {
        return canLongjump;
    }
}
