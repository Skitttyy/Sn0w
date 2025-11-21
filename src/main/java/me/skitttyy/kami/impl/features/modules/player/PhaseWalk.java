package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.event.events.move.SneakEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.render.FrameEvent;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.impl.features.modules.movement.Speed;
import me.skitttyy.kami.mixin.accessor.IPlayerMoveC2SPacket;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.util.math.MathHelper.floor;

public class PhaseWalk extends Module {

    Timer timer = new Timer();

    Timer packetTimer = new Timer();

    Value<String> phaseMode = new ValueBuilder<String>()
            .withDescriptor("Phase")
            .withValue("Clip")
            .withModes("Clip", "Smooth")
            .register(this);
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(200)
            .withRange(0, 1000)
            .withAction(s -> timer.setDelay(s.getValue().longValue()))
            .register(this);
    Value<String> handleTeleport = new ValueBuilder<String>()
            .withDescriptor("Handle Teleport")
            .withValue("All")
            .withModes("All", "Above", "Predict", "None")
            .register(this);
    Value<Boolean> onlyInBlock = new ValueBuilder<Boolean>()
            .withDescriptor("Only In Block")
            .withValue(false)
            .register(this);
    Value<String> inhibit = new ValueBuilder<String>()
            .withDescriptor("Inhibit", "inhibitMode")
            .withValue("None")
            .withModes("None", "Rotate", "Necessary", "Both")
            .register(this);
    Value<String> motion = new ValueBuilder<String>()
            .withDescriptor("Speed")
            .withValue("Slow")
            .withModes("Slow", "Soft", "Factor")
            .register(this);
    Value<Number> factor = new ValueBuilder<Number>()
            .withDescriptor("Factor")
            .withValue(1)
            .withPageParent(motion)
            .withPage("Factor")
            .withRange(0, 5)
            .withPlaces(2)
            .register(this);
    Value<Boolean> down = new ValueBuilder<Boolean>()
            .withDescriptor("Down")
            .withValue(true)
            .register(this);
    Value<Boolean> noVoid = new ValueBuilder<Boolean>()
            .withDescriptor("Anti Void")
            .withValue(false)
            .withParent(down)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> boost = new ValueBuilder<Boolean>()
            .withDescriptor("Boost")
            .withValue(false)
            .register(this);
    Value<String> limit = new ValueBuilder<String>()
            .withDescriptor("Limit", "limitMode")
            .withValue("None")
            .withModes("None", "Packets")
            .register(this);
    Value<Number> packetMax = new ValueBuilder<Number>()
            .withDescriptor("Max Packets")
            .withValue(1)
            .withPageParent(limit)
            .withPage("Packets")
            .withRange(1, 100)
            .withPlaces(0)
            .register(this);
    Value<Number> resetDelay = new ValueBuilder<Number>()
            .withDescriptor("Reset")
            .withValue(300)
            .withPageParent(limit)
            .withPage("Packets")
            .withRange(1, 1000)
            .withPlaces(0)
            .withAction(s -> packetTimer.setDelay(s.getValue().longValue()))
            .register(this);
    public static PhaseWalk INSTANCE;
    private final Map<Integer, Vec3d> predictions = new HashMap<>();

    public PhaseWalk()
    {
        super("PhaseWalk", Category.Player);
        INSTANCE = this;
    }

    boolean extra = false;

    boolean dontTouchRots = false;
    int tpId = 0;

    @SubscribeEvent
    public void onGameLoop(FrameEvent.FrameFlipEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (extra)
        {
            PlayerUtils.runPhysicsTick();
            extra = false;
        }
    }


    @SubscribeEvent
    public void onMove(MoveEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (phaseMode.getValue().equals("Clip"))
            doPhase(event);
        if (!motion.getValue().equals("Slow"))
        {
            if (!mc.player.isSubmergedInWater() && !mc.player.isInLava())
            {

                if (motion.getValue().equals("Factor") && mc.player.hasStatusEffect(StatusEffects.SPEED))
                    PlayerUtils.phaseSpeed(event, PlayerUtils.getStrictBaseSpeed(0.2873f), factor.getValue().floatValue());
                else
                    PlayerUtils.setSpeed(PlayerUtils.getStrictBaseSpeed(0.2873f), event);

            } else if (mc.player.input.movementForward == 0.0F && mc.player.input.movementSideways == 0.0F)
            {
                event.motionX(0);
                event.motionZ(0);
            }
        }


        if (phaseMode.getValue().equals("Smooth"))
            doPhase(event);

    }


    public void doPhase(MoveEvent event)
    {

        if (limit.getValue().equals("Packets") && packets > packetMax.getValue().longValue()) return;

        switch (phaseMode.getValue())
        {
            case "Clip":
                boolean flag = false;
                if (shouldPacket() || (flag = (down.getValue() && mc.options.sneakKey.isPressed() && isInBlock())))
                {

                    double[] forward = PlayerUtils.forward(0.001);

                    if (!flag || !noVoid.getValue() || !(mc.player.getY() <= 1))
                    {
                        if (flag && !mc.player.verticalCollision) return;

                        if (timer.isPassed())
                        {
                            sendPackets(mc.player.getX() + forward[0], mc.player.getY() + getUpMovement(), mc.player.getZ() + forward[1]);
                            timer.resetDelay();
                            extra = boost.getValue();
                        }
                    }
                }
                break;
        }
    }

    int packets;

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (packetTimer.isPassed())
        {
            packets = 0;
            packetTimer.resetDelay();
        }

        if (Speed.INSTANCE.isEnabled())
            this.setEnabled(false);
    }

    @SubscribeEvent(Priority.MANAGER_LAST)
    public void onUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        dontTouchRots = RotationManager.INSTANCE.isRotating();
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;


        if (inhibit.getValue().equals("Both") || inhibit.getValue().equals("Rotate"))
        {
            if (isInBlock())
            {
                if (event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround && !dontTouchRots)
                {
                    event.setCancelled(true);
                }
                if (event.getPacket() instanceof PlayerMoveC2SPacket.Full movePacket && !dontTouchRots)
                {
                    event.setCancelled(true);
                    IPlayerMoveC2SPacket packet = (IPlayerMoveC2SPacket) movePacket;

                    PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(packet.getX(), packet.getY(), packet.getZ(), movePacket.isOnGround()));
                }
            }
        }
        if (!event.isCancelled())
            packets++;
    }

    @SubscribeEvent
    public void onPacketRecieve(PacketEvent.Receive event)
    {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet)
        {
            if (handleTeleport.getValue().contains("Predict"))
            {
                Vec3d prediction = predictions.get(packet.getTeleportId());
                if (prediction != null)
                {

                    if (prediction.x == packet.getX() && prediction.y == packet.getY() && prediction.z == packet.getZ())
                    {
                        event.setCancelled(true);
                        predictions.remove(packet.getTeleportId());
                        PacketManager.INSTANCE.sendPacket(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                        return;
                    }
                }
                tpId = packet.getTeleportId();
                return;
            }

            if (handleTeleport.getValue().equals("All"))
            {
                PacketManager.INSTANCE.sendPacket(new TeleportConfirmC2SPacket(tpId - 1));
                PacketManager.INSTANCE.sendPacket(new TeleportConfirmC2SPacket(tpId));
                PacketManager.INSTANCE.sendPacket(new TeleportConfirmC2SPacket(tpId + 1));
            }
            if (handleTeleport.getValue().equals("Above"))
            {
                PacketManager.INSTANCE.sendPacket(new TeleportConfirmC2SPacket(tpId + 1));
            }
        }

        if (inhibit.getValue().equals("Both") || inhibit.getValue().equals("Necessary"))
        {
            if (event.getPacket() instanceof ClientCommandC2SPacket packet)
            {
                if (packet.getMode().equals(ClientCommandC2SPacket.Mode.START_SPRINTING))
                {
                    event.setCancelled(true);
                } else if (packet.getMode().equals(ClientCommandC2SPacket.Mode.STOP_SPRINTING))
                {
                    event.setCancelled(true);
                }
            }
        }
    }


    double getUpMovement()
    {
        if (noVoid.getValue() && mc.player.getY() <= 1)
        {
            return (mc.options.jumpKey.isPressed() ? 1 : 0) * getSpeed();
        }
        return (mc.options.jumpKey.isPressed() ? 1 : mc.options.sneakKey.isPressed() ? -1 : 0) * getSpeed();
    }

    public void sendPackets(double x, double y, double z)
    {
        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, mc.player.isOnGround()));
        sendMovePackets(new Vec3d(mc.player.getX(), mc.player.getY() - 85, mc.player.getZ()));
        if (handleTeleport.getValue().contains("Predict"))
        {

            mc.player.setPosition(x, y, z);
            predictions.put(++tpId, new Vec3d(x, y, z));
            PacketManager.INSTANCE.sendPacket(new TeleportConfirmC2SPacket(tpId));
        }



    }

    public void sendMovePackets(Vec3d vec)
    {
        PacketManager.INSTANCE.sendQuietPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, mc.player.isOnGround()));
    }

    public void onEnable()
    {
        super.onEnable();

        if (NullUtils.nullCheck()) return;

    }

    @SubscribeEvent
    public void onSneak(SneakEvent event)
    {
        if (PlayerUtils.isMoving() && mc.options.sneakKey.isPressed())
            event.setCancelled(true);
    }

    double getSpeed()
    {
        return PlayerUtils.getBaseSpeed(0.2873f) / 10d;
    }

    boolean shouldPacket()
    {
        return mc.player.horizontalCollision && (!onlyInBlock.getValue() || isPhasing());
    }

//    @SubscribeEvent
//    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
//    {
//        if (this.isEnabled())
//            setEnabled(false);
//    }
//
//    @SubscribeEvent
//    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event)
//    {
//        if (this.isEnabled())
//            setEnabled(false);
//    }

    boolean isInBlock()
    {
        return (!onlyInBlock.getValue() || isPhasing());
    }

    public boolean isPhasing()
    {
        Box bb = mc.player.getBoundingBox();
        for (int x = floor(bb.minX); x < floor(bb.maxX) + 1; x++)
        {
            for (int y = floor(bb.minY); y < floor(bb.maxY) + 1; y++)
            {
                for (int z = floor(bb.minZ); z < floor(bb.maxZ) + 1; z++)
                {
                    if (mc.world.getBlockState(new BlockPos(x, y, z)).blocksMovement())
                    {
                        if (bb.intersects(new Box(x, y, z, x + 1, y + 1, z + 1)))
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
    @Override
    public String getDescription()
    {
        return "PhaseWalk: phases through blocks with the packetfly exploit";
    }

    @Override
    public String getHudInfo()
    {
        return motion.getValue();
    }
}
