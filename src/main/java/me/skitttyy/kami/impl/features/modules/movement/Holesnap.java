package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.network.ServerEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.render.RenderTimer;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.world.HoleUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

import java.text.DecimalFormat;

public class Holesnap extends Module {
    public boolean cancel = false;

    public static Holesnap INSTANCE;

    public Holesnap()
    {
        super("Holesnap", Category.Movement);
        INSTANCE = this;
    }

    Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(3)
            .withRange(1, 6)
            .register(this);
    Value<Boolean> postTimer = new ValueBuilder<Boolean>()
            .withDescriptor("Use Post Timer")
            .withValue(false)
            .register(this);
    Value<Boolean> boost = new ValueBuilder<Boolean>()
            .withDescriptor("Boost")
            .withValue(false)
            .register(this);
    Value<Boolean> useTimer = new ValueBuilder<Boolean>()
            .withDescriptor("Use TickSpeed")
            .withValue(false)
            .register(this);
    Value<Number> timerAmount = new ValueBuilder<Number>()
            .withDescriptor("Tick Amount")
            .withValue(5)
            .withRange(1, 10)
            .withParent(useTimer)
            .withParentEnabled(true)
            .register(this);
    Value<Number> BoostTime = new ValueBuilder<Number>()
            .withDescriptor("Boost Time")
            .withValue(25)
            .withRange(10, 100)
            .withParent(useTimer)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> doubles = new ValueBuilder<Boolean>()
            .withDescriptor("Doubles")
            .withValue(false)
            .register(this);
    Value<Boolean> selfHole = new ValueBuilder<Boolean>()
            .withDescriptor("Swapping")
            .withValue(false)
            .register(this);
    Value<Boolean> physics = new ValueBuilder<Boolean>()
            .withDescriptor("Physics")
            .withValue(false)
            .register(this);
    Value<Number> physicsTicks = new ValueBuilder<Number>()
            .withDescriptor("Physics Ticks")
            .withValue(3)
            .withRange(3, 20)
            .withParent(physics)
            .withParentEnabled(true)
            .register(this);

    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Normal")
            .withModes("Normal", "Pathfinding")
            .register(this);
    Value<String> stepMode = new ValueBuilder<String>()
            .withDescriptor("Step Mode")
            .withValue("Vanilla")
            .withModes("Vanilla", "NCP", "NONE")
            .register(this);
    Value<Sn0wColor> leftColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Left Color")
            .withValue(new Sn0wColor(255, 0, 255, 255))
            .register(this);
    Value<Sn0wColor> rightColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Right Color")
            .withValue(new Sn0wColor(0, 255, 0))
            .register(this);
    float oldTickLength = 50;
    int boostTime = 0;
    DecimalFormat df = new DecimalFormat("#.##");
    private int ticks = 0;

    BlockPos startPos = null;
    public Vec3d lastTarget;

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (NullUtils.nullCheck()) return;

        lastTarget = null;
        boostTime = 0;
        stuckTicks = 0;
        targetHole = null;
        startPos = PlayerUtils.getPlayerPos();
        targetHole = TargetUtils.getTargetHoleVec3D(range.getValue().doubleValue(), selfHole.getValue(), doubles.getValue());
        if (targetHole == null)
        {
            NoAccel.paused = false;
            cancel = false;
            if (useTimer.getValue())
            {
                if (mc.player.age > 10)
                    RenderTimer.setTickLength(1.0f);
            }

            ChatUtils.sendMessage(new ChatMessage("[Holesnap] Unable to find hole. Disabling", true, 777722));
            lastTarget = null;
            this.toggle();
        }


    }

    HoleUtils.Hole targetHole;

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (NullUtils.nullCheck()) return;

        ranPhysics = false;


        if (mc.player.age > 10)
            RenderTimer.setTickLength(1.0f);
        lastTarget = null;
        Step.setStepHeight(0.6f);
//        Step.paused = false;
        NoAccel.paused = false;
        posttimerticks = 0;

    }

    boolean disablenexttick = false;
    int stuckTicks;
    int posttimerticks;

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck())
            return;
        if (event.getPacket() instanceof PlayerMoveC2SPacket)
        {
            boostTime++;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck())
            return;

        if (boostTime >= BoostTime.getValue().intValue())
        {
            if (mc.player.age > 10)
                RenderTimer.setTickLength(1.0f);
        }
        if (stepMode.getValue().equals("NCP"))
        {
            if (targetHole == null || lastTarget == null) return;

            float yawRad = getRotationTo(lastTarget, mc.player.getPos()).x;
            double[] dir = PlayerUtils.forward(0.1, yawRad, true);
            Step.INSTANCE.doNCPStep(dir, true);
        }
    }

    boolean ranPhysics = false;

    @SubscribeEvent
    public void onMove(MoveEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (disablenexttick && postTimer.getValue())
        {
            NoAccel.paused = false;
//            PlayerUtils.centerPlayer();
            posttimerticks = 0;
            if (mc.player.age > 10)
                RenderTimer.setTickLength(1.0f);


            ChatUtils.sendMessage(new ChatMessage("[Holesnap] Snapped into hole, disabling.", true, 777722));
            disablenexttick = false;
            this.toggle();
            lastTarget = null;
            return;
        }

        HoleUtils.Hole doubleHole = HoleUtils.isDoubleHoleFr(PlayerUtils.getPlayerPos());
        if ((HoleUtils.isObbyHole(PlayerUtils.getPlayerPos()) || HoleUtils.isBedrockHoles(PlayerUtils.getPlayerPos()) || (doubles.getValue() && doubleHole != null && (lastTarget == null || (mc.player.getPos().distanceTo(lastTarget) < 0.1 || targetHole.toTarget != null)))) && (!selfHole.getValue() || !PlayerUtils.getPlayerPos().equals(startPos)))
        {
            if (postTimer.getValue() && !disablenexttick)
            {
                if (mc.player.age > 10)
                    RenderTimer.setTickLength(oldTickLength / 0.2f);
                posttimerticks++;
                if (posttimerticks >= 4)
                    disablenexttick = true;

                return;
            }
            NoAccel.paused = false;
//            PlayerUtils.centerPlayer();
            posttimerticks = 0;

            if (mc.player.age > 10)
                RenderTimer.setTickLength(1.0f);


            ChatUtils.sendMessage(new ChatMessage("[Holesnap] Snapped into hole, disabling.", true, 777722));
            posttimerticks = 0;
            disablenexttick = false;
            this.toggle();
            return;
        }
        if (targetHole != null && mc.world.getBlockState(targetHole.pos1).getBlock() == Blocks.AIR)
        {
            if (useTimer.getValue() && BoostTime.getValue().intValue() > boostTime)
            {
                if (mc.player.age > 10)
                    RenderTimer.setTickLength(timerAmount.getValue().floatValue());
            } else
            {
                if (mc.player.age > 10)
                    RenderTimer.setTickLength(1.0f);
            }
            if (physics.getValue() && !ranPhysics)
            {
                ranPhysics = true;
                for (int i = 0; i < physicsTicks.getValue().intValue(); i++)
                {
                    if (!this.isEnabled())
                    {
                        setEnabled(false);
                        return;
                    }
                    PlayerUtils.runPhysicsTick();
                }
            }
            NoAccel.paused = true;

            cancel = true;
            Vec3d playerPos = mc.player.getPos();
            Vec3d targetPos;
            if (targetHole.doubleHole)
            {
                if (targetHole.toTarget != null)
                {
                    targetPos = new Vec3d(targetHole.toTarget.getX() + 0.5, mc.player.getY(), targetHole.toTarget.getZ() + 0.5);
                } else
                {
                    BlockPos pos1 = targetHole.pos1;
                    BlockPos pos2 = targetHole.pos2;

                    double centerX = ((pos1.getX() + 0.5) + (pos2.getX() + 0.5)) / 2.0;
                    double centerZ = ((pos1.getZ() + 0.5) + (pos2.getZ() + 0.5)) / 2.0;
                    targetPos = new Vec3d(centerX, mc.player.getY(), centerZ);
                }
            } else
            {
                targetPos = new Vec3d(targetHole.pos1.getX() + 0.5, mc.player.getY(), targetHole.pos1.getZ() + 0.5);
            }
            lastTarget = targetPos;

            switch (mode.getValue())
            {
                case "Normal":
                    double yawRad = Math.toRadians(getRotationTo(playerPos, targetPos).x);
                    double dist = playerPos.distanceTo(targetPos);
                    double speed;
                    if (boost.getValue())
                    {
                        speed = mc.player.isOnGround() ? -Math.min(PlayerUtils.getDefaultBaseSpeed(true), dist / 2.0) : -PlayerUtils.getDefaultBaseSpeed(false);
                    } else
                    {
                        speed = mc.player.isOnGround() ? -Math.min(0.2805, dist / 2.0) : -PlayerUtils.getDefaultBaseSpeed(false);
                    }

                    event.setX(-Math.sin(yawRad) * speed);
                    event.setZ(Math.cos(yawRad) * speed);
                    if (mc.player.horizontalCollision && mc.player.isOnGround())
                    {
                        stuckTicks++;
                        if (stepMode.getValue().equals("Vanilla"))
                        {
                            Step.setStepHeight(Step.INSTANCE.stepHeight.getValue().floatValue());
                        }
                    } else
                    {
                        stuckTicks = 0;
                        Step.setStepHeight(0.6f);
                    }
                    break;

            }
        } else
        {
            if (useTimer.getValue())
            {
                if (mc.player.age > 10)
                    RenderTimer.setTickLength(1.0f);
            }
            NoAccel.paused = false;
            cancel = false;
            lastTarget = null;
            ChatUtils.sendMessage(new ChatMessage("[Holesnap] Hole no longer exists, disabling.", true, 777722));

            this.toggle();
        }
    }

    @SubscribeEvent
    public void onServerLeft(ServerEvent.ServerLeft event)
    {
        this.toggle();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (targetHole != null && lastTarget != null)
        {

            Vec3d targetPos = lastTarget;
            RenderUtil.renderLineFromPosToPos(mc.player.getPos(), targetPos, rightColor.getValue().getColor(), leftColor.getValue().getColor(), 1);

        }
    }

    public double normalizeAngle(Double angleIn)
    {
        double angle = angleIn;
        angle %= 360.0;
        if (angle >= 180.0)
        {
            angle -= 360.0;
        }
        if (angle < -180.0)
        {
            angle += 360.0;
        }
        return angle;
    }

    public Vec2f getRotationTo(Vec3d posTo, Vec3d posFrom)
    {
        return getRotationFromVec(posTo.subtract(posFrom));
    }

    public Vec2f getRotationFromVec(Vec3d vec)
    {
        double xz = Math.hypot(vec.x, vec.z);
        float yaw = (float) normalizeAngle(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
        float pitch = (float) normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
        return new Vec2f(yaw, pitch);
    }


    @Override
    public String getHudInfo()
    {
        if (targetHole != null && lastTarget != null)
        {
            Vec3d playerPos = mc.player.getPos();


            return df.format(playerPos.distanceTo(lastTarget));
        } else
        {
            return "None";
        }

    }

    @Override
    public String getDescription()
    {
        return "Holesnap: Moves and centers yourself into the nearest hole (or teleport with some settings)";
    }

}
