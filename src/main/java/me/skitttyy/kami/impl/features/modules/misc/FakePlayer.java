package me.skitttyy.kami.impl.features.modules.misc;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PopManager;
import me.skitttyy.kami.api.management.notification.NotificationManager;
import me.skitttyy.kami.api.management.notification.types.TopNotification;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.world.CrystalUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class FakePlayer extends Module
{

    private final Queue<Location> recordedPositions = new LinkedList<>();
    private final Queue<Location> recordedList = new LinkedList<>();
    public static FakePlayer INSTANCE;
    Value<String> fakeName = new ValueBuilder<String>()
            .withDescriptor("Ghost Name")
            .withValue("Catgirl")
            .register(this);

    Value<String> task = new ValueBuilder<String>()
            .withDescriptor("Task")
            .withValue("Record")
            .withModes("Record", "Play", "Normal")
            .register(this);

    public PlayerEntity fakePlayer;

    public FakePlayer()
    {
        super("FakePlayer", Category.Misc);
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;


        switch (task.getValue())
        {
            case "Clear" ->
            {
                recordedList.clear();
                recordedPositions.clear();
                ChatUtils.sendMessage("Reset!");
                this.toggle();
                return;
            }
            case "Record" ->
            {
                ChatUtils.sendMessage("Recording! Disable the module to stop recording.");
                recordedList.clear();
                recordedPositions.clear();
                return;
            }
            case "Playing" -> ChatUtils.sendMessage("Playing back ur recorded movements!.");
        }

        NotificationManager.INSTANCE.addNotification(new TopNotification("Spawned ghost " + fakeName.getValue() + "!", 1000L, 200L, Color.GREEN));

        fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("5300a928-b781-440a-8581-19343b39d29d"), fakeName.getValue()));


        fakePlayer.copyPositionAndRotation(mc.player);
        fakePlayer.bodyYaw = mc.player.getBodyYaw();
        fakePlayer.headYaw = mc.player.getHeadYaw();
        fakePlayer.setHealth(36);
        fakePlayer.setStackInHand(Hand.MAIN_HAND, mc.player.getMainHandStack().copy());
        fakePlayer.getInventory().setStack(36, mc.player.getInventory().getStack(36).copy());
        fakePlayer.getInventory().setStack(37, mc.player.getInventory().getStack(37).copy());
        fakePlayer.getInventory().setStack(38, mc.player.getInventory().getStack(38).copy());
        fakePlayer.getInventory().setStack(39, mc.player.getInventory().getStack(39).copy());

        mc.world.addEntity(fakePlayer);
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (NullUtils.nullCheck()) return;

        if (fakePlayer != null)
        {
            fakePlayer.remove(Entity.RemovalReason.DISCARDED);
            NotificationManager.INSTANCE.addNotification(new TopNotification("Removed ghost " + fakeName.getValue() + "!", 1000L, 200L, new Color(255, 85, 85)));
        }
        if (task.getValue().equals("Record"))
        {
            ChatUtils.sendMessage("Saved Recording!");
        }

    }


    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (task.getValue().equals("Record"))
        {
            Location location = new Location(mc.player);
            recordedList.add(location);
        }

        if (fakePlayer == null)
            return;

        if (fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING)
            fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        if (task.getValue().equals("Play") && fakePlayer != null)
        {
            Location loc = recordedPositions.poll();
            if (loc != null)
            {
                travel(loc);
            } else
            {
                recordedPositions.addAll(recordedList);
            }
        }

        if (fakePlayer.isDead())
        {
            new PacketEvent.Receive((new EntityStatusS2CPacket(fakePlayer, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES))).post();
            NotificationManager.INSTANCE.addNotification(new TopNotification("Ghost " + fakeName.getName() + " died!", 1000L, 200L, new Color(255, 85, 85)));
            toggle();
        }

    }

    public void travel(Location p)
    {
        if (mc.player.age % 2 == 0)
        {
            fakePlayer.setYaw(p.getYaw());
            fakePlayer.setPitch(p.getPitch());
            fakePlayer.setHeadYaw(p.getHead());
            fakePlayer.updateTrackedPositionAndAngles(
                    p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch(),
                    3);
            fakePlayer.setVelocity(p.getVelocity());
        }
    }


    boolean crystalAvailable = false;

    @SubscribeEvent
    public void onAttack(LivingEvent.Attack event)
    {
        if (NullUtils.nullCheck()) return;

        if (fakePlayer == null) return;

        if (event.getEntity() == fakePlayer && (fakePlayer.hurtTime == 0))
        {
            mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1f, 1f);



            if (mc.player.fallDistance > 0)
                mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1f, 1f);
            fakePlayer.onDamaged(mc.world.getDamageSources().generic());
            crystalAvailable = true;
            if (mc.player.getAttackCooldownProgress(20) >= 0.85)
                fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - InventoryUtils.getHitDamage(mc.player.getMainHandStack(), fakePlayer));
            else fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - 1f);
            if (fakePlayer.isDead())
            {
                if (fakePlayer.tryUseTotem(mc.world.getDamageSources().generic()))
                {
                    fakePlayer.setHealth(10f);
                    PopManager.INSTANCE.onPop(fakePlayer);
                    new EntityStatusS2CPacket(fakePlayer, EntityStatuses.USE_TOTEM_OF_UNDYING).apply(mc.player.networkHandler);
                }

            }
        }
    }


    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (fakePlayer == null) return;

        if (event.getPacket() instanceof ExplosionS2CPacket explosion)
        {
            onExplosion(explosion);
        }
    }

    public void onExplosion(ExplosionS2CPacket explosion)
    {
        if ((fakePlayer.hurtTime == 0 || crystalAvailable))
        {

            if (!crystalAvailable)
                fakePlayer.onDamaged(mc.world.getDamageSources().generic());
            fakePlayer.setHealth((fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount()) - CrystalUtil.calculateDamage(fakePlayer, new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ()), false, false));
            crystalAvailable = false;
            if (fakePlayer.isDead())
            {
                if (fakePlayer.tryUseTotem(mc.world.getDamageSources().generic()))
                {
                    PopManager.INSTANCE.onPop(fakePlayer);
                    fakePlayer.setHealth(10f);
                    new EntityStatusS2CPacket(fakePlayer, EntityStatuses.USE_TOTEM_OF_UNDYING).apply(mc.player.networkHandler);
                }
            }
        }
    }

    @Getter
    private static class Location
    {
        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final float pitch;
        private final float head;
        private final Vec3d velocity;

        public Location(PlayerEntity player)
        {
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            this.yaw = player.getYaw();
            this.pitch = player.getPitch();
            this.head = player.getHeadYaw();
            this.velocity = player.getVelocity();
        }

    }

    @Override
    public String getDescription()
    {
        return "FakePlayer: Spawns a fake player or ghost";
    }

}
