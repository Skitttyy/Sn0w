package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import me.skitttyy.kami.impl.features.modules.movement.ElytraFly;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AutoMace extends Module
{

    Value<Number> targetRange = new ValueBuilder<Number>()
            .withDescriptor("Target Range")
            .withValue(10)
            .withRange(10, 30)
            .register(this);
    public Value<Number> attackRange = new ValueBuilder<Number>()
            .withDescriptor("AttackRange")
            .withValue(6)
            .withRange(3, 6)
            .withPlaces(1)
            .register(this);
    public Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(false)
            .register(this);

    public Value<Boolean> track = new ValueBuilder<Boolean>()
            .withDescriptor("Track")
            .withValue(true)
            .register(this);

    public Value<Boolean> rubberband = new ValueBuilder<Boolean>()
            .withDescriptor("Lagback")
            .withValue(true)
            .register(this);


    public AutoMace()
    {
        super("AutoMace", Category.Combat);
    }


    private PlayerEntity target = null;
    private Timer timer = new Timer();

    private boolean attacking = false;
    private boolean shouldAttack = false;
    private boolean reset = false;


    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event)
    {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && reset)
        {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(mc.player.isOnGround()));
            reset = false;
        }
    }

    @SubscribeEvent
    public void onPlayerMove(MoveEvent event)
    {
        if (!track.getValue() || mc.player.isOnGround() || target == null) return;
        if (mc.player.distanceTo(target) <= attackRange.getValue().floatValue()) return;
        if (!timer.isPassed(200)) return;


        if (mc.player.squaredDistanceTo(target.getX(), mc.player.getY(), target.getZ()) > MathUtil.square(6.0f))
        {
            if (!mc.player.isFallFlying())
            {
                PlayerUtils.equipElytra();
            }

        } else
        {
            if (mc.player.isFallFlying())
            {
                PlayerUtils.disEquipElytra();
            }
        }
        if (mc.player.isFallFlying())
        {
            PlayerUtils.moveTowards(event, target.getPos(), ElytraFly.INSTANCE.horizontalSpeed.getValue().floatValue(), true);
        } else
        {
            PlayerUtils.moveTowards(event, target.getPos(), PlayerUtils.getBaseSpeed(0.2873f), false);

        }
    }


    @SubscribeEvent
    public void onUpdatePre(final TickEvent.PlayerTickEvent.Post event)
    {
        if (NullUtils.nullCheck() || !shouldAttack || !attacking || target == null)
        {
            shouldAttack = false;
            return;
        }
        if (AutoBreak.INSTANCE.didAction) return;

        int slot = InventoryUtils.getHotbarItemSlot(Items.MACE);
        int previousSlot = mc.player.getInventory().selectedSlot;


        Vec3d previous = mc.player.getPos();
        if (slot != previousSlot)
            InventoryUtils.switchToSlot(slot);
        mc.interactionManager.attackEntity(mc.player, target);

        mc.player.swingHand(Hand.MAIN_HAND);

        if (slot != previousSlot)
            InventoryUtils.switchToSlot(previousSlot);

        if (rubberband.getValue()) doRubberband(previous);

        shouldAttack = false;
        timer.resetDelay();
    }


    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;


        if (PlayerUtils.isElytraEquipped())
            if (mc.player.isFallFlying()) mc.player.stopFallFlying();


    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        target = getTarget();

        attacking = false;
        shouldAttack = false;

        if (target == null || mc.player.distanceTo(target) > attackRange.getValue().floatValue()) return;


        int slot = InventoryUtils.getHotbarItemSlot(Items.MACE);
        if (slot == -1) return;

        if (rotate.getValue())
            RotationUtils.setRotation(RotationUtils.getRotationsTo(mc.player.getEyePos(), KillAura.INSTANCE.getAttackRotateVec(target)));

        attacking = true;

        if (!timer.isPassed(1000)) return;

        shouldAttack = true;
    }


    private void doRubberband(Vec3d previous)
    {
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1, mc.player.getZ(), false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(previous.x, previous.y, previous.z, false));
        reset = true;
    }

    private PlayerEntity getTarget()
    {
        PlayerEntity optimalTarget = null;
        for (PlayerEntity player : mc.world.getPlayers())
        {
            if (player == mc.player) continue;
            if (!player.isAlive() || player.getHealth() <= 0.0f) continue;
            if (mc.player.squaredDistanceTo(player) > MathHelper.square(targetRange.getValue().doubleValue())) continue;
            if (FriendManager.INSTANCE.isFriend(player)) continue;

            if (optimalTarget == null)
            {
                optimalTarget = player;
                continue;
            }

            if (mc.player.squaredDistanceTo(player) < mc.player.squaredDistanceTo(optimalTarget))
            {
                optimalTarget = player;
            }
        }

        return optimalTarget;
    }

    @Override
    public String getDescription()
    {
        return "AutoMace: maces people after falling";
    }
}