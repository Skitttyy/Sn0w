package me.skitttyy.kami.impl.features.modules.movement;

import com.google.common.collect.Streams;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.network.ServerEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.mixin.accessor.IPlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class PacketFly extends Module
{

    public static PacketFly INSTANCE;

    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Factorize")
            .withModes("Factorize", "Fast", "Lagback")
            .register(this);
    Value<Number> factor = new ValueBuilder<Number>()
            .withDescriptor("Factor")
            .withValue(1.5)
            .withRange(0.1, 4.0f)
            .withPlaces(1)
            .register(this);
    Value<String> phase = new ValueBuilder<String>()
            .withDescriptor("Phase")
            .withValue("Full")
            .withModes("Full", "Semi", "None")
            .register(this);
    Value<String> bounds = new ValueBuilder<String>()
            .withDescriptor("Bounds")
            .withValue("Up")
            .withModes("Up", "Down", "Preserve", "Limit", "Soft", "Snap")
            .register(this);
    Value<Boolean> antiKick = new ValueBuilder<Boolean>()
            .withDescriptor("AntiKick")
            .withValue(true)
            .register(this);
    Value<Boolean> phased = new ValueBuilder<Boolean>()
            .withDescriptor("Phased")
            .withValue(true)
            .register(this);
    Value<String> limit = new ValueBuilder<String>()
            .withDescriptor("Limit", "LimitMode")
            .withValue("None")
            .withModes("None", "Axis", "Ticks", "Strict", "All")
            .register(this);
    Value<Boolean> dupe = new ValueBuilder<Boolean>()
            .withDescriptor("Confirm")
            .withValue(false)
            .register(this);

    // a map of predictions
    private final Map<Integer, Vec3d> predictions = new HashMap<>();

    // the current teleport id to predict off of
    private int tpId = 0;

    // the time to slow down to prevent NCP kicks
    private int lagTime = 0;

    public PacketFly()
    {
        super("PacketFly", Category.Movement);
        INSTANCE = this;
    }


    //rewrite soon

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (NullUtils.nullCheck()) return;
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;


    }

    private static final double CONCEAL = 0.0624;
    private static final double MOVE_FACTOR = 1.0 / StrictMath.sqrt(2.0);


    float factorBuffer = 0;

    @SubscribeEvent
    public void onMove(MoveEvent event)
    {
        if (NullUtils.nullCheck()) return;


        int loops;
        if (mode.getValue().equals("Factorize") && (mc.player.age % 3 == 0 || !(limit.getValue().equals("Ticks") || limit.getValue().equals("All"))))
        {

            float rawFactor = factor.getValue().floatValue();
            loops = (int) Math.floor(rawFactor);
            float extraFactor = rawFactor - loops;
            factorBuffer -= 0.1f;
            if (factorBuffer <= extraFactor)
            {

                factorBuffer = 1;
                loops++;
            }
            if (bounds.getValue().equals("Soft") && canJitter())
            {
                loops = 1;
                if (limit.getValue().equals("Strict") && mc.player.age % 3 == 0)
                {
                    loops = 0;
                }
            }
        } else

        {
            loops = 1;
            if ((mode.getValue().equals("Fast") || (limit.getValue().equals("Strict") && canJitter())) && (limit.getValue().equals("Ticks") || limit.getValue().equals("All") || (limit.getValue().equals("Strict") && canJitter())) && mc.player.age % 3 == 0)
            {
                loops = 0;
            }
        }


        double moveSpeed = (phased.getValue() || --lagTime > 0 || isPhased()) ? CONCEAL : 0.2873;
        double motionY = 0.0;

        boolean doAntiKick = false;

        if (mc.options.jumpKey.isPressed())
        {
            if ((limit.getValue().equals("Axis") || limit.getValue().equals("All")))
            {

                if (PlayerUtils.isMoving())
                {
                    if (mc.player.age % 2 == 0)
                    {
                        motionY = CONCEAL;
                        moveSpeed = 0;
                    } else
                    {
                        moveSpeed *= MOVE_FACTOR;
                    }
                } else
                {
                    motionY = CONCEAL;
                }
            } else
            {
                motionY = CONCEAL;
                if (PlayerUtils.isMoving())
                {
                    //loops = 1;

                    moveSpeed *= MOVE_FACTOR;
                    motionY *= MOVE_FACTOR;
                }
            }
            doAntiKick = antiKick.getValue()
                    && mc.player.age % 20 == 0
                    && !isPhased()
                    && !mc.world.isSpaceEmpty(mc.player.getBoundingBox());

            if (doAntiKick)
            {
                loops = 1;
                motionY = -0.04;
            }
        } else if (mc.options.sneakKey.isPressed())
        {
            if (limit.getValue().equals("All") || limit.getValue().equals("Axis"))
            {
                if (PlayerUtils.isMoving())
                {
                    if (mc.player.age % 2 == 0)
                    {
                        motionY = -CONCEAL;
                        moveSpeed = 0;
                    } else
                    {
                        moveSpeed *= MOVE_FACTOR;
                    }
                } else
                {
                    motionY = -CONCEAL;
                }
            } else
            {
                motionY = -CONCEAL;

                if (PlayerUtils.isMoving())
                {


                    moveSpeed *= MOVE_FACTOR;
                    motionY *= MOVE_FACTOR;
                }
            }
        } else

        {
            doAntiKick = antiKick.getValue()
                    && mc.player.age % 20 == 0
                    && !isPhased()
                    && !mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox());

            if (doAntiKick)
            {
                loops = 1;

                motionY = -0.04;
            }
        }

        // send our packets
        sendMovePackets(loops, moveSpeed, motionY, doAntiKick);

        event.setX(mc.player.getVelocity().x);
        event.setY(mc.player.getVelocity().y);
        event.setZ(mc.player.getVelocity().z);

        doPhase();
    }

    boolean canJitter()
    {
        return isPhased() || mc.player.horizontalCollision || mc.player.verticalCollision;
    }

    public void doPhase()
    {
        switch (phase.getValue())
        {
            case "Full":
                if (!mc.player.noClip)
                    mc.player.noClip = true;
                break;
            case "Semi":
                if (isPhased() || mc.player.verticalCollision)
                {
                    mc.player.noClip = true;
                } else
                {
                    if (mc.player.noClip)
                        mc.player.noClip = false;
                }
                break;
            case "None":
                if (mc.player.noClip)
                    mc.player.noClip = false;
                break;

        }
    }

    private void sendMovePackets(int loops, double moveSpeed, double motionY, boolean antiKick)
    {

        if (loops == 0)
        {
            mc.player.setVelocity(0, 0, 0);
            return;
        }

        double[] strafe = PlayerUtils.getMoveSpeed(moveSpeed);

        // loop for factorizing
        for (int i = 1; i < loops + 1; ++i)
        {
            double motionX = strafe[0] * i;
            double motionZ = strafe[1] * i;

            double velY = motionY;

            if (!antiKick)
            {
                velY *= i;
            }

            // set our client-sided velocity
            PlayerUtils.setMotionX(motionX);
            PlayerUtils.setMotionY(velY);
            PlayerUtils.setMotionZ(motionZ);


            Vec3d posVec = mc.player.getPos();

            Vec3d moveVec = posVec.add(motionX, velY, motionZ);

            sendMovePackets(moveVec);
            if (bounds.getValue().equals("Soft") && canJitter())
                sendMovePackets(new Vec3d(posVec.x + motionX, posVec.y + randomBounds(), posVec.z + motionZ));
            else
                sendMovePackets(modify(posVec));


            doPrediction(moveVec);
        }

    }

    public void doPrediction(Vec3d moveVec)
    {
        if (tpId != 0)
        {
            if (!mode.getValue().equals("Lagback"))
            {

                predictions.put(++tpId, moveVec);

                PacketManager.INSTANCE.sendQuietPacket(new TeleportConfirmC2SPacket(tpId));
            }
        }
    }

    private boolean isPhased()
    {
        return !Streams.stream(mc.world.getCollisions(mc.player, mc.player.getBoundingBox().expand(-0.0625, -0.0625, -0.0625))).toList().isEmpty();
    }



    @SubscribeEvent
    public void onDisconnect(ServerEvent.ServerLeft event)
    {
        if (this.isEnabled())
            setEnabled(false);
    }

    @SubscribeEvent
    public void onJoin(ServerEvent.ServerJoined event)
    {
        if (this.isEnabled())
            setEnabled(false);
    }

    public Vec3d modify(Vec3d in)
    {

        return new Vec3d(in.x, getBounds(in), in.z);
    }

    public static double randomBounds()
    {
        int randomValue = rand.nextInt(22) + 70;
        if (rand.nextBoolean())
        {
            return randomValue;
        }
        return -randomValue;
    }

    private static final Random rand = new Random();

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

    public void sendMovePackets(Vec3d vec)
    {
        PacketManager.INSTANCE.sendQuietPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, true));
    }


    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet)
        {

            Vec3d prediction = predictions.get(packet.getTeleportId());
            if (prediction != null)
            {

                if (prediction.x == packet.getX() && prediction.y == packet.getY() && prediction.z == packet.getZ())
                {

                    if (!mode.getValue().equals("Lagback"))
                    {
                        event.setCancelled(true);
                    }


                    if (dupe.getValue() || mc.player.age % 2 == 0)
                    {
                        PacketManager.INSTANCE.sendQuietPacket(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                    }
                    predictions.remove(packet.getTeleportId());

                    return;
                }
            }

            ((IPlayerPositionLookS2CPacket) packet).setYaw(mc.player.getYaw());
            ((IPlayerPositionLookS2CPacket) packet).setPitch(mc.player.getPitch());

            PacketManager.INSTANCE.sendQuietPacket(new TeleportConfirmC2SPacket(packet.getTeleportId()));

            lagTime = 10;
            tpId = packet.getTeleportId();
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {

        if (NullUtils.nullCheck()) return;


        if (!mc.isInSingleplayer()) return;


        if (event.getPacket() instanceof TeleportConfirmC2SPacket || event.getPacket() instanceof PlayerMoveC2SPacket)
            event.setCancelled(true);


    }

    @Override
    public String getDescription()
    {
        return "PacketFly: manipulates confirm teleport packets to fly and phase";
    }

    public String getHudInfo()
    {
        return mode.getValue();
    }


}
