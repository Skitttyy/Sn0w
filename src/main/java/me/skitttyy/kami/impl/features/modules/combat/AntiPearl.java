package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import net.minecraft.entity.Entity;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class AntiPearl extends Module
{
    Timer timer = new Timer();

    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Scaffolding")
            .withModes("Scaffolding", "Frames")
            .register(this);
    Value<Boolean> doubleFill = new ValueBuilder<Boolean>()
            .withDescriptor("Fill")
            .withValue(true)
            .withPageParent(mode)
            .withPage("Frames")
            .register(this);

    Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(6)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(0)
            .withRange(0, 1000)
            .withAction(set -> timer.setDelay(set.getValue().intValue()))
            .register(this);
    Value<Number> targetRange = new ValueBuilder<Number>()
            .withDescriptor("Target Range")
            .withValue(5d)
            .withRange(1d, 10d)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(true)
            .register(this);
    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict")
            .withValue(false)
            .register(this);
    ItemFrameEntity placeNextTick = null;

    int oldSlotTick = -1;
    public AntiPearl()
    {
        super("AntiPearl", Category.Combat);
    }


    Entity target;
    List<BlockPos> toPlace = new ArrayList<>();

    @SubscribeEvent
    public void onUpdate(final TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        target = TargetUtils.getTarget(targetRange.getValue().floatValue());

        if (target == null) return;


        if(placeNextTick != null){
            if(mc.player.getInventory().getMainHandStack().getItem().equals(Items.ITEM_FRAME))
            {
                PacketManager.INSTANCE.sendPacket(PlayerInteractEntityC2SPacket.interact(placeNextTick, mc.player.isSneaking(), Hand.MAIN_HAND));
                PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                InventoryUtils.switchToSlot(oldSlotTick);
            }
            placeNextTick = null;
        }

        toPlace.clear();

        if (AutoBreak.INSTANCE.didAction) return;

        if (mc.player.isUsingItem()) return;


        if (InventoryUtils.getHotbarItemSlot(getItem()) == -1) return;


        if (timer.isPassed())
        {
            BlockPos pos = target.getBlockPos();

            if (mode.getValue().equals("Frames"))
                pos = pos.down();
            if (pos.getSquaredDistance(mc.player.getEyePos()) < MathUtil.square(range.getValue().floatValue()))
            {

                if (mode.getValue().equals("Scaffolding") && !BlockUtils.canPlaceBlockIgnore(pos, strictDirection.getValue()))
                    return;

                if (mode.getValue().equals("Frames") && !BlockUtils.canPlaceItemFrame(pos, Direction.UP, strictDirection.getValue()))
                {
                    ItemFrameEntity entity = BlockUtils.getItemFrame(pos, Direction.UP);


                    if (entity != null)
                    {

                        if(doubleFill.getValue())
                        if (entity.getHeldItemStack().isEmpty())
                        {
                            int oldSlot = mc.player.getInventory().selectedSlot;
                            InventoryUtils.switchToSlot(getItem());
                            placeNextTick = entity;
                            oldSlotTick = oldSlot;

                        }
                    }
                    return;
                }

                toPlace.add(pos);
                timer.resetDelay();

                if (!AntiCheat.INSTANCE.protocol.getValue())
                    doRotate(pos);
            }
        }

    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.AfterClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (AutoBreak.INSTANCE.didAction) return;

        if (mc.player.isUsingItem()) return;

        int blockSlot = InventoryUtils.getHotbarItemSlot(getItem());

        int oldSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;


        if (blockSlot == -1) return;

        for (BlockPos pos : toPlace)
        {
            if (blockSlot != mc.player.getInventory().selectedSlot)
            {
                InventoryUtils.switchToSlot(blockSlot);
                switched = true;
            }
            if (rotate.getValue() && AntiCheat.INSTANCE.protocol.getValue())
                RotationUtils.doSilentRotate(pos, strictDirection.getValue());

            if (mode.getValue().equals("Frames"))
            {
                BlockUtils.placeFrame(pos, Direction.UP, Hand.MAIN_HAND, true, true);
            } else
            {

                BlockUtils.placeBlock(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue()), true);
            }
        }

        if (!toPlace.isEmpty())
            RotationUtils.silentSync();

        if (switched)
        {
            InventoryUtils.switchToSlot(oldSlot);
        }
        toPlace.clear();

    }


    public Item getItem()
    {
        switch (mode.getValue())
        {
            case "Scaffolding":
                return Items.SCAFFOLDING;
            case "Frames":
                return Items.ITEM_FRAME;
        }
        return Items.SCAFFOLDING;
    }

    public void doRotate(BlockPos pos)
    {
        if (!rotate.getValue()) return;

        float[] rots = RotationUtils.getBlockRotations(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue()));
        if (rots != null)
        {
            RotationUtils.setRotation(rots);
        }
    }

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

    @Override
    public String getDescription()
    {
        return "AntiPearl: Places scaffolding inside of players to block them from pearling";
    }
}
