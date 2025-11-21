package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import me.skitttyy.kami.impl.features.modules.render.Freecam;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class NoSlow extends Module
{

    public boolean cancelDisabler = false;
    public Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Vanilla")
            .withModes("Vanilla", "Grim", "Meow", "Strict")
            .register(this);
    public Value<Boolean> invFix = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate Fix")
            .withValue(false)
            .withPage("Meow")
            .withPageParent(mode)
            .register(this);
    public Value<Boolean> items = new ValueBuilder<Boolean>()
            .withDescriptor("Items")
            .withValue(true)
            .register(this);
    public Value<Boolean> guiMove = new ValueBuilder<Boolean>()
            .withDescriptor("Inv Move")
            .withValue(true)
            .register(this);
    public Value<Boolean> webs = new ValueBuilder<Boolean>()
            .withDescriptor("Webs")
            .withValue(true)
            .register(this);
    public Value<Boolean> crawling = new ValueBuilder<Boolean>()
            .withDescriptor("Crawling")
            .withValue(false)
            .register(this);
    public Value<Boolean> sneak = new ValueBuilder<Boolean>()
            .withDescriptor("Sneaking")
            .withValue(false)
            .register(this);


    public static NoSlow INSTANCE;

    public NoSlow()
    {
        super("NoSlow", Category.Movement);
        INSTANCE = this;
    }

    private final Queue<Packet<?>> packets = new LinkedBlockingQueue<>();


    @SubscribeEvent(Priority.MODULE_LAST)
    private void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (!guiMove.getValue()) return;


        if (mc.currentScreen != null)
        {
            if (canInvMove())
            {
                for (KeyBinding k : new KeyBinding[]{mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey, mc.options.sprintKey})
                {
                    k.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
                }
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 264))
                {
                    mc.player.setPitch(mc.player.getPitch() + 5);
                }
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 265))
                {
                    mc.player.setPitch(mc.player.getPitch() - 5);
                }
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 262))
                {
                    mc.player.setYaw(mc.player.getYaw() + 5);
                }
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 263))
                {
                    mc.player.setYaw(mc.player.getYaw() - 5);
                }
                if (mc.player.getPitch() > 90)
                {
                    mc.player.setYaw(90);
                }
                if (mc.player.getPitch() < -90)
                {
                    mc.player.setYaw(-90);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        switch (mode.getValue())
        {
            case "Grim":
                if (mc.player.isUsingItem() && !mc.player.isRiding() && !mc.player.isFallFlying() && !mc.player.isSneaking())
                {
                    if (mc.player.getActiveHand() == Hand.MAIN_HAND)
                    {

//                        PacketManager.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 8 + 1));
//                        PacketManager.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                        PacketManager.INSTANCE.sendPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, RotationManager.INSTANCE.getServerYaw(), RotationManager.INSTANCE.getServerPitch()));
                    }
                }
                break;
            case "Meow":
            case "Strict":
                break;
        }
    }


    public void release()
    {
        for (Packet<?> p : packets)
        {
            PacketManager.INSTANCE.sendQuietPacket(p);
        }
        packets.clear();
    }

    public boolean canNoSlow()
    {
        if (!isEnabled()) return false;

        if (!items.getValue())
            return false;


        if (mode.getValue().equals("Grim"))
            if (mc.player.getActiveHand() == Hand.OFF_HAND)
                return false;

        if (mode.getValue().equals("Meow"))
            return mc.player.getItemUseTimeLeft() < 5 || ((mc.player.getItemUseTime() > 1) && mc.player.getItemUseTime() % 2 != 0);

        return true;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;


        if (cancelDisabler) return;


        switch (mode.getValue())
        {
            case "Strict":
                if (items.getValue() && event.getPacket() instanceof PlayerMoveC2SPacket packet)
                {
                    //if u just swapped items ncp doesnt check for noslow (items) this disables the check onground and in air lol
                    if (mc.player.isUsingItem() && packet.changesPosition())
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                } else if (guiMove.getValue() && event.getPacket() instanceof ClickSlotC2SPacket && PlayerUtils.isMoving())
                {
                    doStrictPre();
                }
                break;
            case "Meow":
                break;
        }
    }

    public boolean doStrictPre()
    {
        if (mc.player.isSneaking())
            PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

        if (mc.player.isSprinting())
            PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));


        if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed() && !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, 0.0656, 0.0)).iterator().hasNext())
        {
            PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0656, mc.player.getZ(), false));
            return true;
        }
        return false;

    }

    public void doStrictPost()
    {
        if (mc.player.isSneaking())
            PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

        if (mc.player.isSprinting())
            PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
    }

    @SubscribeEvent
    public void onPacketPost(PacketEvent.SendPost event)
    {
        if (NullUtils.nullCheck()) return;


        if (cancelDisabler) return;


        switch (mode.getValue())
        {
            case "Strict":
                if (guiMove.getValue() && event.getPacket() instanceof ClickSlotC2SPacket && PlayerUtils.isMoving())
                {
                    doStrictPost();
                }
                break;
            case "Meow":
                if (guiMove.getValue() && invFix.getValue())
                {
                    if (event.getPacket() instanceof ClickSlotC2SPacket packet)
                    {

                        if (packet.getSyncId() != 0) return;

                        if (packet.getActionType() != SlotActionType.PICKUP && packet.getActionType() != SlotActionType.PICKUP_ALL && packet.getActionType() != SlotActionType.QUICK_CRAFT)
                            PacketManager.INSTANCE.sendPacket(new CloseHandledScreenC2SPacket(0));
                    }
                }
                break;
        }


    }



    public boolean canInvMove()
    {
        if (Freecam.INSTANCE.isEnabled())
            return false;

        if (mc.currentScreen instanceof AbstractInventoryScreen<?>)
            return true;

        if (mc.currentScreen instanceof HandledScreen<?>)
            return true;

        if (mc.currentScreen instanceof ClickGui)
            return true;

        return false;
    }

    @Override
    public String getHudInfo()
    {
        return mode.getValue();
    }

    @Override
    public String getDescription()
    {
        return "NoSlow: Prevents you from getting slowed from various actions";
    }
}
