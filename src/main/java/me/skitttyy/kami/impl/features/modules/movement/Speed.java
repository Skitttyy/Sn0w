package me.skitttyy.kami.impl.features.modules.movement;

import com.google.common.collect.Streams;
import me.skitttyy.kami.api.event.Event;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.BoostManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.render.RenderTimer;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.misc.FakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.List;

public class Speed extends Module
{
    Value<String> speedMode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Strafe")
            .withModes("Strafe", "StrafeFast", "StrafeStrict", "StrictFast", "StrafeInsano", "Vanilla")
            .withAction(s -> handlePage(s.getValue()))
            .register(this);
    Value<String> timerMode = new ValueBuilder<String>()
            .withDescriptor("Timer", "TimerMode")
            .withValue("None")
            .withModes("None", "0888", "Fast")
            .register(this);
    Value<Boolean> buffer = new ValueBuilder<Boolean>()
            .withDescriptor("Buffer")
            .withValue(false)
            .register(this);
    Value<Boolean> boost = new ValueBuilder<Boolean>()
            .withDescriptor("Boost")
            .withValue(false)
            .register(this);
    Value<Number> speed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(1)
            .withRange(0.01, 2)
            .withPlaces(2)
            .withPageParent(speedMode)
            .withPage("Vanilla")
            .register(this);
    public static Speed INSTANCE;

    public Speed()
    {
        super("Speed", Category.Movement);
        INSTANCE = this;
    }

    private int strafeStage = 1;


    private double ncpPrevMotion = 0.0D;

    private double horizontal;
    double BUNNY_DIV_FRICTION = 160.0 - 0.923;

    // strafe normal
    private double currentSpeed = 0.0D;
    private double prevMotion = 0.0D;
    private boolean accelerate = false;
    private int state = 4;

    // aac
    private int aacCounter;
    float oldTickLength = 1.0F;

    void handlePage(String page)
    {
        // "Players", "Crystals"
        boolean isStrafe = isStrafeSpeed(page);
        timerMode.setActive(isStrafe);
        boost.setActive(isStrafe);
        buffer.setActive(speedMode.getValue().equals("Strafe"));


    }


    @Override
    public void onEnable()
    {
        super.onEnable();
        oldTickLength = 1.0f;
        reset();
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (!timerMode.getValue().equals("None"))
        {
            RenderTimer.setTickLength(oldTickLength);
        }
        reset();
    }

    public void reset()
    {
        ncpPrevMotion = 0.0D;
        currentSpeed = 0.0D;
        horizontal = 0;
        state = 4;
        prevMotion = 0;
        aacCounter = 0;
        accelerate = false;
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (mc.player.isFallFlying()) return;


        if (LongJump.INSTANCE.isEnabled() && LongJump.INSTANCE.mode.getValue().contains("Strict") && BoostManager.INSTANCE.canDoLongjump())
        {
            reset();
            return;
        }
        if (isStrafeSpeed())
        {
            if (mc.getRenderTickCounter() != null)
            {
                if (!timerMode.getValue().equals("None"))
                {
                    if (!TickBase.INSTANCE.isEnabled() || TickBase.INSTANCE.boostTime == 0)
                    {
                        RenderTimer.setTickLength((timerMode.getValue().equals("Fast") ? 1.0985f : 1.0888f));
                    }
                } else
                {
                    if (!TickBase.INSTANCE.isEnabled() || TickBase.INSTANCE.boostTime == 0)
                    {
                        RenderTimer.setTickLength(oldTickLength);
                    }
                }
            }

            if (PlayerUtils.isMoving())
            {
                mc.player.setSprinting(true);
            }
            ncpPrevMotion = Math.sqrt((mc.player.getX() - mc.player.prevX) * (mc.player.getX() - mc.player.prevX) + (mc.player.getZ() - mc.player.prevZ) * (mc.player.getZ() - mc.player.prevZ));
        }
    }

    public boolean isStrafeSpeed()
    {
        return speedMode.getValue().contains("Strafe") || speedMode.getValue().equals("StrictFast");
    }

    public boolean isStrafeSpeed(String page)
    {
        return page.contains("Strafe") || page.equals("StrictFast");
    }


    @SubscribeEvent
    public void onUpdateWalkingPlayerPre(TickEvent.MovementTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        if (mc.player.isFallFlying()) return;

        if (LongJump.INSTANCE.isEnabled() && LongJump.INSTANCE.mode.getValue().contains("Strict") && BoostManager.INSTANCE.canDoLongjump())
        {
            reset();
            return;
        }

        if (isStrafeSpeed())
        {
            double dX = mc.player.getX() - mc.player.prevX;
            double dZ = mc.player.getZ() - mc.player.prevZ;
            prevMotion = Math.sqrt(dX * dX + dZ * dZ);
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (mc.player.isFallFlying()) return;
        if (LongJump.INSTANCE.isEnabled() && LongJump.INSTANCE.mode.getValue().contains("Strict") && BoostManager.INSTANCE.canDoLongjump())
        {
            reset();
            return;
        }
        switch (speedMode.getValue())
        {
            case "Strafe":
            {
                doStrafe(event);
            }
            break;
            case "StrafeFast":
            {
                doStrafe(event);
            }
            break;
            case "StrafeStrict":
                doRestrictedSpeed(event, 0.465, 0.44, false);
                break;
            case "StrictFast":
                if (mc.player.hasStatusEffect(StatusEffects.SPEED))
                {
                    doStrafe(event);
                } else
                {
                    doRestrictedSpeed(event, 0.465, 0.44, true);
                }
                break;
            case "Vanilla":
                PlayerUtils.setSpeed(speed.getValue().doubleValue(), event);
                break;
        }
    }

    public void doStrafe(MoveEvent event)
    {
        if (state != 1 || (mc.player.input.movementForward == 0.0f || mc.player.input.movementSideways == 0.0f))
        {
            if (state == 2 && (mc.player.input.movementForward != 0.0f || mc.player.input.movementSideways != 0.0f) && mc.player.isOnGround())
            {
                double jumpSpeed = 0.0D;

                if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST))
                {
                    jumpSpeed += (mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F;
                }


                event.motionY(0.3999999463558197D + jumpSpeed);

                if (buffer.getValue() && !speedMode.getValue().equals("StrictFast"))
                    currentSpeed *= accelerate ? 1.6835D : 1.395D;
                else
                    currentSpeed *= 1.535;

                if (boost.getValue())
                    currentSpeed = Math.max(BoostManager.INSTANCE.getBoostSpeed(true), currentSpeed);

            } else if (state == 3)
            {
                double adjustedMotion = 0.66D * (prevMotion - getBaseMotionSpeed());
                currentSpeed = prevMotion - adjustedMotion;
                accelerate = !accelerate;
                if (boost.getValue())
                    currentSpeed = Math.max(BoostManager.INSTANCE.getBoostSpeed(true), currentSpeed);
            } else
            {
                List<VoxelShape> collisionBoxes = Streams.stream(mc.world.getCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().y, 0.0))).toList();
                if ((collisionBoxes.size() > 0 || mc.player.verticalCollision) && state > 0)
                {
                    state = mc.player.input.movementForward == 0.0f && mc.player.input.movementSideways == 0.0f ? 0 : 1;
                }
                currentSpeed = prevMotion - prevMotion / 159.0;
            }
        } else
        {
            currentSpeed = 1.35D * getBaseMotionSpeed() - 0.01D;
        }

        currentSpeed = Math.max(currentSpeed, getBaseMotionSpeed());


        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();

        if (forward == 0.0D && strafe == 0.0D)
        {
            event.setX(0.0D);
            event.setZ(0.0D);
        } else
        {
            if (forward != 0.0D)
            {
                if (strafe > 0.0D)
                {
                    yaw += (float) (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D)
                {
                    yaw += (float) (forward > 0.0D ? 45 : -45);
                }

                strafe = 0.0D;

                if (forward > 0.0D)
                {
                    forward = 1.0D;
                } else if (forward < 0.0D)
                {
                    forward = -1.0D;
                }
            }

            double cos = Math.cos(Math.toRadians(yaw + 90.0F));
            double sin = Math.sin(Math.toRadians(yaw + 90.0F));
            event.setX(forward * currentSpeed * cos + strafe * currentSpeed * sin);
            event.setZ(forward * currentSpeed * sin - strafe * currentSpeed * cos);
        }


        if (mc.player.input.movementForward == 0.0f && mc.player.input.movementSideways == 0.0f)
        {
            return;
        }

        state++;
    }

    public void doRestrictedSpeed(MoveEvent event, double baseRestriction, double actualRestriction, boolean fast)
    {
        if (PlayerUtils.isMoving())
        {

            //fall down faster
            if (fast && MathUtil.round(mc.player.getY() - (int) mc.player.getY(), 3) == MathUtil.round(0.138D, 3))
            {
                PlayerUtils.setMotionY(mc.player.getVelocity().y - 0.08D);
                event.setY(event.getY() - 0.09316090325960147D);
                mc.player.setPos(mc.player.getPos().x, mc.player.getPos().y - 0.09316090325960147D, mc.player.getPos().z);
            }

            // start motion
            if (strafeStage == 1)
            {
                currentSpeed = fast ? 1.38 : 1.35 * getBaseMotionSpeedNoPot() - 0.01;
            }
            // start jumping
            else if (strafeStage == 2 && mc.player.isOnGround())
            {
                double jumpSpeed = 0.41999998688697815;

                if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST))
                {
                    double amplifier = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
                    jumpSpeed += (amplifier + 1) * 0.1;
                }

                // jump
                event.motionY(jumpSpeed);

                double acceleration = 2.149;

                if (boost.getValue())
                    currentSpeed += BoostManager.INSTANCE.getBoostSpeed(false);

                currentSpeed *= acceleration;
            }

            //add speed
            else if (strafeStage == 3)
            {

                double scaledcurrentSpeed = 0.66 * (prevMotion - getBaseMotionSpeedNoPot());

                currentSpeed = prevMotion - scaledcurrentSpeed;

                if (boost.getValue())
                    currentSpeed += BoostManager.INSTANCE.getBoostSpeed(false);

            } else
            {
                if ((!Streams.stream(mc.world.getCollisions(mc.player, mc.player.getBoundingBox().offset(0, mc.player.getVelocity().y, 0))).toList().isEmpty() || mc.player.verticalCollision) && strafeStage > 0)
                {
                    strafeStage = PlayerUtils.isMoving() ? 1 : 0;
                }

                currentSpeed = prevMotion - (prevMotion / BUNNY_DIV_FRICTION);
            }

            // slow limit
            currentSpeed = Math.max(currentSpeed, getBaseMotionSpeedNoPot());

            double baseStrictSpeed = baseRestriction;

            double baseRestrictedSpeed = actualRestriction;
            if (mc.player.hasStatusEffect(StatusEffects.SPEED))
            {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                baseStrictSpeed *= 1 + (0.2 * (amplifier + 1));
                baseRestrictedSpeed *= 1 + (0.2 * (amplifier + 1));
            }

            if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS))
            {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                baseStrictSpeed /= 1 + (0.2 * (amplifier + 1));
                baseRestrictedSpeed /= 1 + (0.2 * (amplifier + 1));
            }

            // clamp speed to strict
            currentSpeed = Math.min(currentSpeed, aacCounter > 25 ? baseStrictSpeed : baseRestrictedSpeed);

            aacCounter++;

            if (aacCounter > 50)
            {
                aacCounter = 0;
            }

            float forward = mc.player.input.movementForward;
            float strafe = mc.player.input.movementSideways;
            if (mc.player.isSneaking()) strafe = 0;
            float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getRenderTickCounter().getTickDelta(false);

            if (!PlayerUtils.isMoving())
            {
                event.setX(0);
                event.setZ(0);
            } else if (forward != 0)
            {
                if (strafe >= 1)
                {
                    yaw += (forward > 0 ? -45 : 45);
                    strafe = 0;
                } else if (strafe <= -1)
                {
                    yaw += (forward > 0 ? 45 : -45);
                    strafe = 0;
                }

                if (forward > 0)
                {
                    forward = 1;
                } else if (forward < 0)
                {
                    forward = -1;
                }
            }

            double cos = Math.cos(Math.toRadians(yaw));
            double sin = -Math.sin(Math.toRadians(yaw));

            event.setX((forward * currentSpeed * sin) + (strafe * currentSpeed * cos));
            event.setZ((forward * currentSpeed * cos) - (strafe * currentSpeed * sin));

            strafeStage++;
        } else
        {
            event.setX(0);
            event.setZ(0);
        }
    }

    private double getBaseMotionSpeed()
    {
        double baseSpeed = 0.2873D;
        if (speedMode.getValue().equalsIgnoreCase("StrafeFast"))
            baseSpeed = 0.31;

        if (mc.player.hasStatusEffect(StatusEffects.SPEED))
        {
            if (speedMode.getValue().equalsIgnoreCase("StrafeFast"))
                baseSpeed = 0.32;

            int amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            baseSpeed *= 1.0D + (0.1D * ((double) amplifier + 1));
        }
        return baseSpeed;
    }

    private double getBaseMotionSpeedNoPot()
    {
        return 0.2873D;
    }


    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event)
    {
        if (event.getPacket() instanceof EntityPositionS2CPacket packet)
        {
            if (packet.getEntityId() == mc.player.getId())
            {
                ncpPrevMotion = 0.0D;
                currentSpeed = 0.0D;
                horizontal = 0;
                state = 4;
                prevMotion = 0;
                aacCounter = 0;
            }
        }
    }


    @Override
    public String getHudInfo()
    {
        return speedMode.getValue();
    }

    @Override
    public String getDescription()
    {
        return "Speed: Move around at rapid speeds";
    }
}
