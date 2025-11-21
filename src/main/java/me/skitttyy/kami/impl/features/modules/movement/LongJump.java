package me.skitttyy.kami.impl.features.modules.movement;

import com.google.common.collect.Streams;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.event.events.move.TravelEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.BoostManager;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.player.MiddleClick;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class LongJump extends Module
{
    public static LongJump INSTANCE;

    public LongJump()
    {
        super("LongJump", Category.Movement);
        INSTANCE = this;
    }

    public Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Boost")
            .withModes("Boost", "Grim", "Strict", "StrictHigh")
            .register(this);
    Value<Number> boost = new ValueBuilder<Number>()
            .withDescriptor("Boost")
            .withValue(0.4)
            .withRange(0.2D, 10)
            .register(this);

    Value<Boolean> autoDisable = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Disable")
            .withValue(true)
            .register(this);
    Value<Boolean> onGround = new ValueBuilder<Boolean>()
            .withDescriptor("Friction")
            .withValue(true)
            .register(this);

    @SubscribeEvent()
    public void onPlayerTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        if (mode.getValue().equals("Grim"))
            RotationUtils.setRotation(new float[]{PlayerUtils.getMoveYaw(mc.player.getYaw()), 62.8f}, 99);
    }

    boolean hasBeenGrounded = false;

    private int stage = 0;
    private double moveSpeed, lastDist;


    @SubscribeEvent
    public void onPlayerMove(MoveEvent event)
    {
        switch (mode.getValue())
        {
            case "Grim":
                if (!mc.player.isFallFlying())
                    return;


                if (ElytraFly.INSTANCE.boostControl.getValue())
                    ElytraFly.INSTANCE.doBoost(event);
                break;
            default:


                if (!hasBeenGrounded && onGround.getValue()) return;


                if (mode.getValue().contains("Strict") && !BoostManager.INSTANCE.canDoLongjump()) return;


                if (mc.player.isFallFlying() || mc.player.isInLava() || mc.player.isSubmergedInWater())
                {
                    return;
                }
                switch (stage)
                {
                    case 0:
                        ++stage;
                        lastDist = 0.0D;
                        break;
                    case 2:
                        double motionY = 0.40123128;
                        if ((mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F) && mc.player.isOnGround())
                        {
                            if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST))
                                motionY += ((mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
                            event.motionY(motionY);
                            moveSpeed *= 2.149;
                        }
                        break;
                    case 3:
                        moveSpeed = lastDist - (0.76 * (lastDist - getBaseMoveSpeed()));
                        break;
                    default:
                        if ((!Streams.stream(mc.world.getCollisions(mc.player, mc.player.getBoundingBox().offset(0.0D, mc.player.getVelocity().y, 0.0D))).toList().isEmpty() || mc.player.verticalCollision) && stage > 0)
                        {
                            stage = mc.player.input.movementForward == 0.0F && mc.player.input.movementSideways == 0.0F ? 0 : 1;
                        }
                        moveSpeed = lastDist - lastDist / 159.0D;
                        break;
                }
                moveSpeed = Math.max(moveSpeed, getBaseMoveSpeed());
                double forward = mc.player.input.movementForward, strafe = mc.player.input.movementSideways, yaw = mc.player.getYaw();
                if (forward != 0 && strafe != 0)
                {
                    forward = forward * Math.sin(Math.PI / 4);
                    strafe = strafe * Math.cos(Math.PI / 4);
                } else
                {
                    event.setX(0);
                    event.setZ(0);
                }

                event.setX((forward * moveSpeed * -Math.sin(Math.toRadians(yaw)) + strafe * moveSpeed * Math.cos(Math.toRadians(yaw))) * 0.99D);
                event.setZ((forward * moveSpeed * Math.cos(Math.toRadians(yaw)) - strafe * moveSpeed * -Math.sin(Math.toRadians(yaw))) * 0.99D);
                ++stage;
                break;
        }
    }


    @SubscribeEvent
    public void onTravel(TravelEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        switch (mode.getValue())
        {
            case "Grim":
                int slot = InventoryUtils.getInventoryItemSlot(Items.ELYTRA);
                if (slot == -1) return;

                if (RotationManager.INSTANCE.isRotationBlocked(99))
                    return;
                if (!mc.player.isFallFlying())
                {
                    InventoryUtils.swapArmor(2, slot);

                    PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    mc.player.startFallFlying();
                    if (MiddleClick.INSTANCE.fireworkSchedule)
                    {
                        MiddleClick.INSTANCE.doFirework();
                        MiddleClick.INSTANCE.fireworkSchedule = false;
                    }
                    InventoryUtils.swapArmor(2, slot);
                }


                if (mc.player.isOnGround())
                    PlayerUtils.clientJump();


                break;
            default:
                break;
        }

    }


    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (!hasBeenGrounded)
        {
            hasBeenGrounded = mc.player.isOnGround();
        }

        if (!hasBeenGrounded && onGround.getValue()) return;

        lastDist = Math.sqrt(((mc.player.getX() - mc.player.prevX) * (mc.player.getX() - mc.player.prevX)) + ((mc.player.getZ() - mc.player.prevZ) * (mc.player.getZ() - mc.player.prevZ)));
        if (canSprint())
        {
            mc.player.setSprinting(true);
        }
    }


    @Override
    public void onEnable()
    {
        super.onEnable();
        hasBeenGrounded = false;


        if (NullUtils.nullCheck()) return;


        if (!NoAccel.paused)
            NoAccel.paused = true;
    }

    @Override
    public void onDisable()
    {
        super.onDisable();


        if (NullUtils.nullCheck()) return;


        if (NoAccel.paused)
            NoAccel.paused = false;

    }


    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && autoDisable.getValue())
        {
            this.toggle();
        }

    }


    private double getBaseMoveSpeed()
    {
        double baseSpeed = 0.272;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED))
        {
            final int amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            baseSpeed *= 1.0 + (0.2 * (amplifier + 1));
        }
        return baseSpeed * (boost.getValue().doubleValue());
    }

    @SubscribeEvent
    public void onTravel(TravelEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;


    }

    @SubscribeEvent
    public void onActionJump(LivingEvent.Jump event)
    {
        if (NullUtils.nullCheck()) return;


        int slot = InventoryUtils.getInventoryItemSlot(Items.ELYTRA);
        if (slot == -1) return;


        if (mode.getValue().equals("Grim"))
            event.setCancelled(true);
    }

    public static boolean isGrimJumping()
    {
        if (!INSTANCE.isEnabled()) return false;


        int slot = InventoryUtils.getInventoryItemSlot(Items.ELYTRA);
        if (slot == -1) return false;

        return INSTANCE.mode.getValue().equals("Grim");
    }

    private boolean canSprint()
    {
        return ((mc.player.input.movementSideways != 0.0F || mc.player.input.movementForward != 0.0F) && !mc.player.isBlocking() && !mc.player.isClimbing() && !mc.player.horizontalCollision && mc.player.getHungerManager().getFoodLevel() > 6);
    }

    @Override
    public String getHudInfo()
    {
        return mode.getValue();
    }

    @Override
    public String getDescription()
    {
        return "LongJump: jump but long and fast";
    }
}