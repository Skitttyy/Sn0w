package me.skitttyy.kami.impl.features.modules.movement;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.event.events.move.TravelEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.Baritone;
import net.minecraft.entity.MovementType;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module
{
    public static ElytraFly INSTANCE;

    public ElytraFly()
    {
        super("ElytraFly", Category.Movement);
        INSTANCE = this;
    }

    public Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Creative")
            .withModes("Creative", "Bounce", "Control", "Accel", "Factor")
            .withAction(val -> handlePage(val.getValue()))
            .register(this);
    public Value<Number> horizontalSpeed = new ValueBuilder<Number>()
            .withDescriptor("Horizontal")
            .withValue(3f)
            .withRange(1, 5)
            .register(this);
    Value<Number> verticalSpeed = new ValueBuilder<Number>()
            .withDescriptor("Vertical")
            .withValue(3f)
            .withRange(1, 5)
            .register(this);
    Value<Boolean> antiKick = new ValueBuilder<Boolean>()
            .withDescriptor("Anti Kick")
            .withValue(true)
            .withPageParent(mode)
            .withPage("Factor")
            .register(this);
    Value<Number> factor = new ValueBuilder<Number>()
            .withDescriptor("Factor")
            .withValue(1.5f)
            .withRange(0.1, 50.0f)
            .withPageParent(mode)
            .withPage("Factor")
            .register(this);
    Value<Number> upFactor = new ValueBuilder<Number>()
            .withDescriptor("Up Factor")
            .withValue(1.0f)
            .withRange(0, 10.0f)
            .withPageParent(mode)
            .withPage("Factor")
            .register(this);
    Value<Number> downFactor = new ValueBuilder<Number>()
            .withDescriptor("Down Factor")
            .withValue(1.0f)
            .withRange(0, 10.0f)
            .withPageParent(mode)
            .withPage("Factor")
            .register(this);
    Value<Boolean> infDura = new ValueBuilder<Boolean>()
            .withDescriptor("Inf Dura")
            .withValue(true)
            .withPageParent(mode)
            .withPage("Factor")
            .register(this);
    Value<Boolean> accelerate = new ValueBuilder<Boolean>()
            .withDescriptor("Accelerate")
            .withValue(true)
            .withPageParent(mode)
            .withPage("Factor")
            .register(this);
    Value<String> strictMode = new ValueBuilder<String>()
            .withDescriptor("Strict")
            .withValue("None")
            .withModes("None", "Normal", "NCP", "Glide")
            .withPageParent(mode)
            .withPage("Factor")
            .register(this);


    //control
    Value<Boolean> boostControl = new ValueBuilder<Boolean>()
            .withDescriptor("Accel", "boostControl")
            .withValue(true)
            .withPageParent(mode)
            .withPage("Control")
            .register(this);

    //Bounce
    Value<Boolean> pitch = new ValueBuilder<Boolean>()
            .withDescriptor("Pitch")
            .withValue(false)
            .register(this);
    Value<Boolean> boostBounce = new ValueBuilder<Boolean>()
            .withDescriptor("Accel", "boostBounce")
            .withValue(true)
            .withPageParent(mode)
            .withPage("Bounce")
            .register(this);
    Value<Boolean> groundBoost = new ValueBuilder<Boolean>()
            .withDescriptor("OnGround")
            .withValue(true)
            .withPageParent(mode)
            .withPage("Bounce")
            .register(this);
    Value<Boolean> highwayDodge = new ValueBuilder<Boolean>()
            .withDescriptor("Highway")
            .withValue(false)
            .register(this);

    private boolean rSpeed;

    private double curSpeed;
    double GRIM_AIR_FRICTION = 0.0264444413;
    Vec3d lastPos;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (LongJump.isGrimJumping()) return;


        if (KamiMod.BARITONE_AVAILABLE)
        {

            if (highwayDodge.getValue())
                doObstaclePass();

            if (KamiMod.isBaritonePaused()) return;
        }
        switch (mode.getValue())
        {
            case "Creative":
                break;
            case "Bounce":

                if (pitch.getValue())
                    RotationUtils.setRotation(new float[]{mc.player.getYaw(), 85});

                break;
            case "Control":
                if (!mc.player.isFallFlying()) return;


                RotationUtils.setRotation(new float[]{PlayerUtils.getMoveYaw(mc.player.getYaw()), getControlPitch()}, 99);
                break;

        }
    }


    @SubscribeEvent
    public void onPlayerMove(MoveEvent event)
    {

        if (KamiMod.isBaritonePaused()) return;

        if (LongJump.isGrimJumping()) return;


        if (!mc.player.getInventory().getArmorStack(2).getItem().equals(Items.ELYTRA))
            return;
        switch (mode.getValue())
        {
            case "Creative":
                if (!mc.player.isFallFlying())
                {
                    return;
                }
                event.motionY(0.0);
                if (mc.options.jumpKey.isPressed())
                {
                    event.motionY(verticalSpeed.getValue().floatValue());
                } else if (mc.options.sneakKey.isPressed())
                {
                    event.motionY(-verticalSpeed.getValue().floatValue());
                }
                float forward = mc.player.input.movementForward;
                float strafe = mc.player.input.movementSideways;
                if (forward == 0.0f && strafe == 0.0f)
                {
                    event.motionX(0.0);
                    event.motionZ(0.0);
                    return;
                }
                PlayerUtils.setSpeed(horizontalSpeed.getValue().floatValue(), event);
                break;
            case "Bounce":
                if (!mc.player.isFallFlying())
                    return;


                if (!event.getType().equals(MovementType.SELF)) return;

//                if (lastPos != null && groundBoost.getValue())
//                {
//
//                    double speedBps = mc.player.getPos().subtract(lastPos).multiply(20.0D, 0.0D, 20.0D).length();
//
//                    if (mc.player.isOnGround() && mc.player.isSprinting())
//                    {
//                        if (speedBps > 20)
//                            event.motionY(0);
//
//                        PlayerUtils.setMotionY(0);
//
//                    }
//                }
                if (boostBounce.getValue())
                    doBoost(event);

                lastPos = mc.player.getPos();
                break;
            case "Control":
                if (!mc.player.isFallFlying())
                    return;

                if (boostControl.getValue())
                    doBoost(event);
                break;
            case "Accel":
                if (!mc.player.isFallFlying())
                    return;

                doBoost(event);
                break;
            case "Factor":

                if (!mc.player.isFallFlying())
                {
                    PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }

                if (mc.player.horizontalCollision) rSpeed = true;

                if (accelerate.getValue())
                {
                    if (rSpeed)
                    {
                        curSpeed = 1.0;
                        rSpeed = false;
                    }
                    if (curSpeed < factor.getValue().floatValue())
                    {
                        curSpeed += 0.1;
                    }
                    if (curSpeed - 0.1 > factor.getValue().floatValue())
                    {
                        curSpeed -= 0.1;
                    }
                } else
                {
                    curSpeed = factor.getValue().floatValue();
                }

                if (mc.options.jumpKey.isPressed())
                {
                    event.motionY(upFactor.getValue().floatValue());
                } else if (mc.options.sneakKey.isPressed())
                {
                    event.motionY(-downFactor.getValue().floatValue());
                } else if (strictMode.getValue().equals("Normal"))
                {
                    if (mc.player.age % 32 == 0 && !rSpeed && (Math.abs(event.getX()) >= 0.05 || Math.abs(event.getZ()) >= 0.05))
                    {
                        PlayerUtils.setMotionY(-2.0E-4);
                        event.setY(0.006200000000000001);
                    } else
                    {
                        event.motionY(-2.0E-4);
                    }
                } else if (strictMode.getValue().equals("Glide"))
                {
                    event.motionY(-0.00001F);
                } else
                {
                    event.motionY(0.0);
                }

                event.setX(event.getX() * (rSpeed ? 0.0 : curSpeed));
                event.setZ(event.getZ() * (rSpeed ? 0.0 : curSpeed));

                if (antiKick.getValue() && event.getX() == 0.0 && event.getZ() == 0.0 && !rSpeed)
                {
                    event.setX(Math.sin(Math.toRadians(mc.player.age % 360)) * 0.03);
                    event.setZ(Math.cos(Math.toRadians(mc.player.age % 360)) * 0.03);
                }
                event.setCancelled(true);
                break;
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;


        if (KamiMod.isBaritonePaused()) return;

        if (LongJump.isGrimJumping()) return;


        if (mode.getValue().equals("Factor"))
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket)
            {
                rSpeed = true;
            }
    }


    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;

        if (KamiMod.isBaritonePaused()) return;

        if (LongJump.isGrimJumping()) return;

//
//        if (mode.getValue().equals("Control"))
//        {
//            if (event.getPacket() instanceof PlayerMoveC2SPacket)
//            {
//                if (lastVelocity != null)
//                {
//                    event.setCancelled(true);
//                }
//            }
//        }

    }

    public static boolean isPacketFlying()
    {
        return INSTANCE.isEnabled() && INSTANCE.mode.getValue().equals("Factor");
    }

    public float getControlPitch()
    {
        if (PlayerUtils.isBoostedByFirework())
        {
            if (mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed())
            {
                if (PlayerUtils.isMoving())
                {
                    return -50f;
                } else
                {
                    return -90.0f;
                }
            } else if (mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed())
            {

                if (PlayerUtils.isMoving())
                {
                    return 50f;
                } else
                {
                    return 90.0f;
                }
            } else
            {
                return 0.0f;
            }
        } else
        {
            if (mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed())
            {
                return -50;
            } else if (mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed())
            {
                return 50f;
            } else
            {
                return 0.1f;
            }
        }

    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;

        rSpeed = false;
        curSpeed = 1.0;

    }


    @SubscribeEvent
    public void onTravel(TravelEvent.Pre event)
    {

        if (NullUtils.nullCheck()) return;
        if (LongJump.isGrimJumping()) return;


        if (KamiMod.isBaritonePaused()) return;

        if (!mc.player.getInventory().getArmorStack(2).getItem().equals(Items.ELYTRA))
            return;


        switch (mode.getValue())
        {
            case "Bounce", "Control":


                if (mode.getValue().equals("Control") && !mc.player.isFallFlying()) return;

                if (mode.getValue().equals("Control"))
                    if (!PlayerUtils.isMoving() && !(mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed()) && !(mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed()))
                    {


                        event.setCancelled(true);
                        return;
                    }

                if(!mc.player.isFallFlying())
                {
                    PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    mc.player.startFallFlying();
                }

                if (mc.player.isOnGround())
                {
                    PlayerUtils.clientJump();
                    if (lastPos != null && groundBoost.getValue())
                    {

                        double speedBps = mc.player.getPos().subtract(lastPos).multiply(20.0D, 0.0D, 20.0D).length();

                        if (mc.player.isOnGround() && mc.player.isSprinting())
                        {
                            if (speedBps > 20)
                                PlayerUtils.setMotionY(0);

                        }
                    }
                }

                break;

            case "Creative":
                break;
        }

    }

    public void doBoost(MoveEvent event)
    {
        float yaw = RotationUtils.getActualYaw();
        final double x = GRIM_AIR_FRICTION * Math.cos(Math.toRadians(yaw + 90.0f));
        final double z = GRIM_AIR_FRICTION * Math.sin(Math.toRadians(yaw + 90.0f));
        event.motionX(event.getX() + x);
        event.motionZ(event.getZ() + z);
    }


    public Vec3d glide(float speed)
    {
        Vec3d glide = getGlideVec(speed / 50.0f);
        Vec3d motion = mc.player.getVelocity();
        return new Vec3d(motion.x + glide.x, 0, motion.z + glide.z);
    }

    public Vec3d getGlideVec(float speed)
    {

        float yaw = mc.player.getYaw();
        final double x = speed * Math.cos(Math.toRadians(yaw + 90.0f));
        final double z = speed * Math.sin(Math.toRadians(yaw + 90.0f));
        return new Vec3d(x, 0.0d, z);
    }


    @SubscribeEvent
    public void onActionJump(LivingEvent.Jump event)
    {
        if (NullUtils.nullCheck()) return;

        if (LongJump.isGrimJumping()) return;

        if (KamiMod.isBaritonePaused()) return;
        if (mode.getValue().equals("Bounce"))
        {
            event.setCancelled(true);
        }
    }

    public void doObstaclePass()
    {
        if (!KamiMod.isBaritonePaused())
        {
            if (mc.player.horizontalCollision)
                Baritone.INSTANCE.doObstaclePass();
        }
    }


    public void handlePage(String page)
    {
        //Creative
        horizontalSpeed.setActive(page.equals("Creative"));
        verticalSpeed.setActive(page.equals("Creative"));

        //Bounce
        pitch.setActive(page.equals("Bounce"));
    }

    @Override
    public String getHudInfo()
    {
        return mode.getValue();
    }

    @Override
    public String getDescription()
    {
        return "ElytraFly: I just flew 300 blocks (like a butterfly this time) thanks to sn0wgod.cc";
    }
}