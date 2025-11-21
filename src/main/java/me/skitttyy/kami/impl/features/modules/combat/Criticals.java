package me.skitttyy.kami.impl.features.modules.combat;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.network.ServerEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.Utils32k;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.PacketUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;


public class Criticals extends Module
{
    Timer timer = new Timer();


    public Criticals()
    {
        super("Criticals", Category.Combat);
    }

    public Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Packet")
            .withModes("Packet", "Strict", "Jump", "MiniJump", "Grim", "GrimV2", "GrimCC")
            .register(this);
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(250)
            .withRange(250, 1500)
            .withAction(set -> timer.setDelay(set.getValue().longValue()))
            .register(this);
    Value<Boolean> onlyKa = new ValueBuilder<Boolean>()
            .withDescriptor("Only Aura")
            .withValue(false)
            .register(this);

    boolean resetSprint = false;
    boolean groundMe = false;
    boolean canGrimCrit = false;

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;


        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket packet)
        {
            if (!timer.isPassed()) return;


            if (PacketUtils.getInteractType(packet) != PacketUtils.InteractType.ATTACK) return;

            Entity entity = PacketUtils.getEntity(packet);

            if (entity == null || entity instanceof EndCrystalEntity) return;

            if (Utils32k.isHolding32k(mc.player)) return;

            if (onlyKa.getValue() && !KillAura.INSTANCE.isEnabled()) return;


            if ((!mc.player.getAbilities().flying) && !mc.player.isInLava() && !mc.player.isSubmergedInWater())
            {
//                if (mode.getValue().equals("Grim"))
//                {
//
//                    if (!mc.player.isCrawling() && (!isClipped()) || !canGrimCrit)
//                        return;
//
//                }


                if (mode.getValue().equals("GrimCC"))
                {

                    if (!isHeadBlocked() && !mc.player.isCrawling())
                        return;

                }

                if (mode.getValue().equals("GrimV2") && mc.player.isOnGround())
                {
                    return;
                } else if (!mc.player.isOnGround())
                    return;

                // cant crit while sprinting
                if (mc.player.isSprinting())
                {
                    PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                    resetSprint = true;
                }

                switch (mode.getValue())
                {
                    case "Strict":
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.1E-7D, mc.player.getZ(), false));
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.0E-8D, mc.player.getZ(), false));
                        groundMe = true;
                        break;
                    case "Packet":
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0625D, mc.player.getZ(), false));
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
                        break;
                    case "Jump":
                        if (mc.player.isOnGround())
                            mc.player.jump();
                        break;
                    case "MiniJump":
                        if (mc.player.isOnGround())
                            PlayerUtils.setMotionY(0.3425);
                        break;
                    case "Grim":
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0625D, mc.player.getZ(), false));
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.04535, mc.player.getZ(), false));
                        break;
                    case "GrimV2":
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() - 0.0001f, mc.player.getZ(), RotationUtils.getActualYaw(), RotationUtils.getActualPitch(), false));
                        break;
                    case "GrimCC":
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0625, mc.player.getZ(), false));
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0625013579, mc.player.getZ(), false));
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.3579e-6, mc.player.getZ(), false));

                        break;

                }
                timer.resetDelay();
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof ExplosionS2CPacket packet)
        {
            Vec3d explosionVec = new Vec3d(packet.getX(), packet.getY(), packet.getZ());

            if (explosionVec.distanceTo(mc.player.getPos()) < 6.0)
            {
                canGrimCrit = true;
            }
        }
        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet)
        {
            if (packet.getEntityId() == mc.player.getId())
                canGrimCrit = true;
        }
    }

    @SubscribeEvent
    public void onServerLeft(ServerEvent.ServerLeft event)
    {
        canGrimCrit = false;
    }


    @SubscribeEvent
    public void onDeathEvent(LivingEvent.Death event)
    {
        if (mc.player == event.getEntity())
            canGrimCrit = false;

    }

    public boolean isClipped()
    {
        Box bb = mc.player.getBoundingBox();
        for (BlockPos pos : BlockUtils.getAllInBox(bb.shrink(0, 1.2, 0).offset(0, 1, 0).expand(0.01, 0, 0.01)))
        {
            if (mc.world.getBlockState(pos).blocksMovement()) return true;
        }

        return false;
    }

    public boolean isHeadBlocked()
    {
        Box bb = mc.player.getBoundingBox();
        for (BlockPos pos : BlockUtils.getAllInBox(bb.shrink(0, 1.2, 0).offset(0, 1, 0)))
        {
            if (mc.world.getBlockState(pos).blocksMovement()) return true;
        }

        return false;
    }

    public boolean isBlockAbove()
    {
        return !BlockUtils.isInBlock(mc.player.getBoundingBox().offset(0, 1, 0), true).isEmpty();
    }


    @SubscribeEvent
    public void postSendPacket(PacketEvent.SendPost event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket)
        {

            if (groundMe)
            {

                PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
                groundMe = false;
            }
            if (resetSprint)
            {

                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                resetSprint = false;
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
        return "Criticals: Makes all ur attacks critical hits!";
    }
}
