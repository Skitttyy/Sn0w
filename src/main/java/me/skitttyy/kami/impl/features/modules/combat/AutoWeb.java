package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PriorityManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoWeb extends Module {
    public static AutoWeb INSTANCE;

    public AutoWeb()
    {
        super("AutoWeb", Category.Combat);
        INSTANCE = this;
    }

    Timer timer = new Timer();

    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Target")
            .withModes("Target", "Self")
            .register(this);
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(0)
            .withRange(0, 1000)
            .withPageParent(mode)
            .withPage("Target")
            .withAction(set -> timer.setDelay(set.getValue().intValue()))
            .register(this);
    Value<Number> targetRange = new ValueBuilder<Number>()
            .withDescriptor("Target Range")
            .withValue(5d)
            .withRange(1d, 10d)
            .withPageParent(mode)
            .withPage("Target")
            .register(this);
    Value<Boolean> ghost = new ValueBuilder<Boolean>()
            .withDescriptor("Ghost Switch")
            .withValue(false)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(false)
            .register(this);
    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict Direction")
            .withValue(false)
            .register(this);
    Value<Boolean> toggleWhenDone = new ValueBuilder<Boolean>()
            .withDescriptor("Disable")
            .withValue(false)
            .withPageParent(mode)
            .withPage("Target")
            .register(this);
    Entity target;
    List<BlockPos> toPlace = new ArrayList<>();

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (NullUtils.nullCheck()) return;

        target = null;

    }


    @Override
    public void onDisable()
    {
        super.onDisable();

        if (NullUtils.nullCheck()) return;

        target = null;
        unlock();

    }


    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Pre event)
    {


        if (NullUtils.nullCheck()) return;


        boolean offhand = mc.player.getInventory().offHand.get(0).getItem() == Items.COBWEB;


        if (mode.getValue().equals("Target"))
        {
            if (timer.isPassed())
            {
                target = TargetUtils.getTarget(targetRange.getValue().doubleValue());
                if (target == null) return;

                BlockPos feet = target.getBlockPos();
                if (feet != null && canPlaceBlock(feet) && mc.world.getBlockState(feet).getBlock() != Blocks.COBWEB)
                {

                    if (!offhand)
                        if (InventoryUtils.getHotbarItemSlot(Items.COBWEB) == -1) return;


                    if (rotate.getValue())
                        RotationUtils.doRotate(feet, strictDirection.getValue());
                    toPlace.add(feet);
                    if (rotate.getValue())
                    {
                        PriorityManager.INSTANCE.lockUsageLock("AutoWeb");
                    }
                } else
                {
                    unlock();
                }
            } else
            {
                unlock();
            }
        } else
        {

            if (canPlaceBlock(mc.player.getBlockPos()))
            {
                RotationUtils.doRotate(mc.player.getBlockPos(), strictDirection.getValue());
                toPlace.add(mc.player.getBlockPos());
            } else
            {
                this.setEnabled(false);
            }

        }
    }


    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.MovementTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;

        int webSlot = InventoryUtils.getHotbarItemSlot(Items.COBWEB);


        if (webSlot == -1)
        {
            toPlace.clear();
            if (mode.getValue().equals("Self"))
                this.setEnabled(false);
            return;
        }
        int oldSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;
        for (BlockPos pos : toPlace)
        {


            if (webSlot != mc.player.getInventory().selectedSlot)
            {
                InventoryUtils.switchToSlot(webSlot);
                switched = true;
            }

            BlockUtils.placeBlock(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue()), true);

            if (mode.getValue().equals("Self"))
            {
                this.setEnabled(false);
            }
        }
        if (switched)
            InventoryUtils.switchToSlot(oldSlot);
        toPlace.clear();

    }


    public void unlock()
    {
        if (PriorityManager.INSTANCE.isUsageLocked() && PriorityManager.INSTANCE.usageLockCause.equals("AutoWeb"))
        {
            PriorityManager.INSTANCE.unlockUsageLock();
        }
    }

    boolean canPlaceBlock(BlockPos pos)
    {
        boolean allow = true;
        if (!mc.world.getBlockState(pos).isReplaceable()) allow = false;

        if (!BlockUtils.canPlaceWeb(pos, strictDirection.getValue())) allow = false;

        return allow;
    }


    @Override
    public String getHudInfo()
    {
        if (mode.getValue().equals("Target"))
        {
            if (target != null)
            {
                return target.getName().getString();
            } else
            {
                return "";
            }
        } else
        {
            return "Self";
        }
    }

    @Override
    public String getDescription()
    {
        return "AutoWeb: Places webs on players to prevent them from moving";
    }
}
