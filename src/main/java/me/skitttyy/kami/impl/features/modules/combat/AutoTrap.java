package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PriorityManager;
import me.skitttyy.kami.api.management.breaks.BreakManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.CrystalUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoTrap extends Module
{
    Timer timer = new Timer();

    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(0)
            .withRange(0, 1000)
            .withAction(set -> timer.setDelay(set.getValue().intValue()))
            .register(this);
    Value<String> trapMode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Top")
            .withModes("Full", "City", "Top", "None")
            .register(this);
    Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(6)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    Value<Number> blocksPerTick = new ValueBuilder<Number>()
            .withDescriptor("Blocks")
            .withValue(1)
            .withRange(1, 10)
            .withPlaces(0)
            .register(this);
    Value<Number> targetRange = new ValueBuilder<Number>()
            .withDescriptor("Target Range")
            .withValue(5d)
            .withRange(1d, 10d)
            .register(this);
    Value<Boolean> disable = new ValueBuilder<Boolean>()
            .withDescriptor("Disable")
            .withValue(false)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate", "rotate")
            .withValue(true)
            .register(this);
    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict")
            .withValue(false)
            .register(this);
    Value<Boolean> echest = new ValueBuilder<Boolean>()
            .withDescriptor("EChest")
            .withValue(false)
            .register(this);

    public AutoTrap()
    {
        super("AutoTrap", Category.Combat);
    }


    Entity target;
    List<BlockPos> toPlace = new ArrayList<>();

    private BlockPos startPos = null;

    @SubscribeEvent
    public void onUpdate(final TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (startPos == null)
        {
            toggle();
            return;
        }


        target = TargetUtils.getTarget(targetRange.getValue().doubleValue());
        if (target == null)
        {
            if (PriorityManager.INSTANCE.isUsageLocked() && PriorityManager.INSTANCE.usageLockCause.equals("AutoTrap"))
            {
                PriorityManager.INSTANCE.unlockUsageLock();
            }
            return;
        }

        if (!startPos.equals(PlayerUtils.getPlayerPos()))
        {
            if (disable.getValue())
            {
                setEnabled(false);
                return;
            }
        }

        toPlace.clear();
        int blocksInTick = 0;

        if (InventoryUtils.getHotbarItemSlot(echest.getValue() ? Items.ENDER_CHEST : Items.OBSIDIAN) == -1) return;

        if (!trapMode.getValue().equalsIgnoreCase("None"))
        {
            BlockPos[] offsets = getTrapOffsets();
            for (BlockPos pos : offsets)
            {
                if (canPlaceBlock(pos))
                {

                    if(AutoBreak.INSTANCE.isAnyMining(pos) && BlockUtils.isInterceptedByCrystalThatIsntHit(pos.up()))
                        continue;

                    if (rotate.getValue())
                        RotationUtils.doRotate(pos, strictDirection.getValue());

                    if (!timer.isPassed()) break;


                    toPlace.add(pos);
                    timer.resetDelay();
                    blocksInTick++;
                    PriorityManager.INSTANCE.lockUsageLock("AutoTrap");
                    if (blocksInTick >= blocksPerTick.getValue().intValue())
                    {
                        break;
                    }
                }
            }
        }
        if (blocksInTick == 0)
        {
            if (PriorityManager.INSTANCE.isUsageLocked() && PriorityManager.INSTANCE.usageLockCause.equals("AutoTrap"))
            {
                PriorityManager.INSTANCE.unlockUsageLock();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.MovementTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;


        int blockSlot = InventoryUtils.getHotbarItemSlot(echest.getValue() ? Items.ENDER_CHEST : Items.OBSIDIAN);

        int oldSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;

        for (BlockPos pos : toPlace)
        {
            if (blockSlot != mc.player.getInventory().selectedSlot)
            {
                InventoryUtils.switchToSlot(blockSlot);
                switched = true;
            }
            BlockUtils.placeBlock(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue()), false);
        }
        if (switched)
        {
            InventoryUtils.switchToSlot(oldSlot);
        }
        toPlace.clear();

    }


    boolean canPlaceBlock(BlockPos pos)
    {
        boolean allow = true;
        if (!BlockUtils.isReplaceable(pos))
        {
            return false;
        }
        if (BlockUtils.getPlaceableSide(pos, strictDirection.getValue()) == null)
        {
            return false;
        }

        double distance = mc.player.getEyePos().squaredDistanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        if (distance > MathUtil.square(range.getValue().doubleValue()))
        {
            return false;
        }




        return allow;
    }

    BlockPos[] offsetBlocks(BlockPos[] toOffset, BlockPos offsetPlace)
    {
        BlockPos[] offsets = new BlockPos[toOffset.length];
        int index = 0;
        for (BlockPos blockPos : toOffset)
        {
            offsets[index] = offsetPlace.add(blockPos);
            index++;
        }

        return offsets;
    }

    public BlockPos[] fullOffsets = new BlockPos[]{
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(-1, 1, 0),
            new BlockPos(1, 1, 0),
            new BlockPos(0, 1, 1),
            new BlockPos(0, 1, -1),
    };


    public BlockPos[] cityOffsets = new BlockPos[]{

            new BlockPos(1, 1, 0),
            new BlockPos(0, 1, 1),
            new BlockPos(-1, 1, 0),
            new BlockPos(0, 1, -1),
    };


    public BlockPos[] getTrapOffsets()
    {
        ArrayList<BlockPos> toPlace = new ArrayList<>();

        switch (trapMode.getValue())
        {
            case "Full":
                toPlace.addAll(Arrays.asList(offsetBlocks(fullOffsets, PlayerUtils.getPos(target))));

                break;
            case "City":
                toPlace.addAll(Arrays.asList(offsetBlocks(cityOffsets, PlayerUtils.getPos(target))));
                break;
            case "Top":
                return getObbyToHead(PlayerUtils.getPos(target));
        }
        toPlace.addAll(Arrays.asList(getObbyToHead(PlayerUtils.getPos(target))));

        return toPlace.toArray(new BlockPos[0]);
    }

    public BlockPos[] getObbyToHead(BlockPos feet)
    {
        ArrayList<BlockPos> obbyToHead = new ArrayList<>();
        BlockPos head = feet.add(new BlockPos(0, 1, 0));


        if (!BlockUtils.isReplaceable(head.up())) return new BlockPos[0];

        if (BlockUtils.canPlaceBlock(head.up(), strictDirection.getValue()))
        {
            obbyToHead.add(head.up());
            BlockPos[] blocks = new BlockPos[obbyToHead.size()];
            return obbyToHead.toArray(blocks);
        }
        for (Direction direction : Direction.values())
        {
            if (direction.getAxis().isVertical()) continue;

            if (verifyHeadTrapDirection(direction, head))
            {

                if (BlockUtils.canPlaceBlock(head.offset(direction), strictDirection.getValue()))
                    obbyToHead.add(head.offset(direction));

                if (BlockUtils.canPlaceBlock(head.offset(direction).up(), strictDirection.getValue()))
                    obbyToHead.add(head.offset(direction).up());

                if (BlockUtils.canPlaceBlock(head.up(), strictDirection.getValue()))
                    obbyToHead.add(head.up());
                break;
            }
        }
        BlockPos[] blocks = new BlockPos[obbyToHead.size()];

        return obbyToHead.toArray(blocks);
    }

    public boolean verifyHeadTrapDirection(Direction direction, BlockPos head)
    {
        BlockPos headOffset = head.offset(direction);
        if (!BlockUtils.canPlaceBlock(headOffset, strictDirection.getValue(), true)) return false;

        if (!BlockUtils.canPlaceBlock(headOffset.up(), strictDirection.getValue(), headOffset)) return false;

        if (!BlockUtils.canPlaceBlock(head.up(), strictDirection.getValue(), headOffset.up())) return false;

        return true;
    }

    public BlockPos getSurroundedBlock(BlockPos feet)
    {
        for (BlockPos offset : surroundOffsets)
        {
            BlockState blockState = mc.world.getBlockState(feet.add(offset));


            if (!blockState.isReplaceable())
            {
                return feet.add(offset);
            }
        }
        return null;
    }

    BlockPos[] surroundOffsets = new BlockPos[]{
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 0)

    };


    @Override
    public void onDisable()
    {
        super.onDisable();


        if (PriorityManager.INSTANCE.isUsageLocked() && PriorityManager.INSTANCE.usageLockCause.equals("AutoTrap"))
        {
            PriorityManager.INSTANCE.unlockUsageLock();
        }

        if (NullUtils.nullCheck()) return;


    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;

        startPos = PlayerUtils.getPlayerPos();
    }

    @Override
    public String getDescription()
    {
        return "AutoTrap: Boxes/Traps the nearest player to prevent them from escaping";
    }
}
