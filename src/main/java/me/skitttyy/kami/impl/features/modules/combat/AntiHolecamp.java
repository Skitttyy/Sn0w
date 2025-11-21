package me.skitttyy.kami.impl.features.modules.combat;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.CrystalManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AntiHolecamp extends Module {
    Timer timer = new Timer();

    Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(6)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    public Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Torch")
            .withModes("Torch", "Block")
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

    public AntiHolecamp()
    {
        super("AntiHolecamp", Category.Combat);
    }

    HoleCampBlock currentBlock;
    Entity target;
    BlockPos toPlace;
    int slot;
    boolean didPiston;

    public enum PlacePhase {
        PISTON, REDSTONE
    }


    @SubscribeEvent
    public void onUpdate(final TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (AutoBreak.INSTANCE.didAction) return;

        if ((target = TargetUtils.getTarget(targetRange.getValue().doubleValue())) == null) return;

        int redstoneSlot = switch (mode.getValue())
        {
            case "Torch" -> InventoryUtils.getHotbarItemSlot(Items.REDSTONE_TORCH);
            case "Block" -> InventoryUtils.getHotbarItemSlot(Items.REDSTONE_BLOCK);
            default -> -1;
        };

        int pistonSlot = InventoryUtils.getHotbarItemSlot(Items.PISTON);

        if (pistonSlot == -1)
            pistonSlot = InventoryUtils.getHotbarItemSlot(Items.STICKY_PISTON);

        if (redstoneSlot == -1 || pistonSlot == -1)
        {
            ChatUtils.sendMessage("[AntiHolecamp] You don't have the required items in your hotbar!");
            this.toggle();
            return;

        }


        if (currentBlock == null)
            currentBlock = getBestPos(target);


        if (currentBlock != null)
        {
            BlockPos pistonPos = currentBlock.getPos();
            BlockPos redstonePos = pistonPos.offset(currentBlock.getRedstoneDirection());


            if (!didPiston)
            {
                slot = pistonSlot;
                toPlace = pistonPos;
                if (rotate.getValue())
                    RotationUtils.doRotate(pistonPos, strictDirection.getValue());

            } else
            {
                slot = redstoneSlot;
                toPlace = redstonePos;

            }

        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.MovementTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;

        if (currentBlock == null) return;

        if (target == null) return;


        if (toPlace != null)
        {

            if (!didPiston)
            {
                if (currentBlock.getDirection() == Direction.NORTH)
                {
                    RotationUtils.packetRotate(180, 0);
                } else if (currentBlock.getDirection() == Direction.SOUTH)
                {
                    RotationUtils.packetRotate(0, 0);
                } else if (currentBlock.getDirection() == Direction.WEST)
                {
                    RotationUtils.packetRotate(90, 0);
                } else if (currentBlock.getDirection() == Direction.EAST)
                {
                    RotationUtils.packetRotate(-90, 0);
                }
            }

            int oldSlot = mc.player.getInventory().selectedSlot;


            InventoryUtils.switchToSlot(slot);

            BlockUtils.placeBlock(toPlace, BlockUtils.getPlaceableSide(toPlace, strictDirection.getValue()), false);


            if (oldSlot != mc.player.getInventory().selectedSlot)
                InventoryUtils.switchToSlot(oldSlot);

            if (!didPiston)
            {
                didPiston = true;
            } else
            {
                this.toggle();
            }
        }
        toPlace = null;
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (NullUtils.nullCheck()) return;

        currentBlock = null;
        didPiston = false;
        toPlace = null;
    }

    public HoleCampBlock getBestPos(Entity player)
    {
        List<HoleCampBlock> posList = getAvailablePos(player);
        posList.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        return posList.isEmpty() ? null : posList.get(0);
    }

    public List<HoleCampBlock> getAvailablePos(Entity player)
    {

        List<HoleCampBlock> positions = new ArrayList<>();


        BlockPos pos = player.getBlockPos().up();
        for (Direction direction : Direction.values())
        {
            if (direction.getAxis().isVertical()) continue;

            BlockPos pistonPos = pos.offset(direction);
            if (canPistonPos(pistonPos, direction))
            {
                for (Direction redstoneDir : Direction.values())
                {
                    if (redstoneDir.equals(direction.getOpposite())) continue;

                    BlockPos redstonePos = pistonPos.offset(redstoneDir);
                    if (BlockUtils.canPlaceBlock(redstonePos, strictDirection.getValue(), pistonPos) && BlockUtils.isInRange(redstonePos, range.getValue().floatValue()))
                    {
                        positions.add(new HoleCampBlock(pistonPos, direction, redstoneDir));
                    }

                }
            }
        }

        return positions;
    }


    public boolean canPistonPos(BlockPos pos, Direction offset)
    {
        if (!BlockUtils.canPlaceBlock(pos, strictDirection.getValue()))
            return false;

        if (!BlockUtils.isInRange(pos, range.getValue().floatValue()))
            return false;


        BlockPos opposite = pos.offset(offset.getOpposite(), 2);

        if (!mc.world.getBlockState(opposite).isAir())
            return false;


        boolean allow = false;
        for (Direction dir : Direction.values())
        {
            if (dir.equals(offset.getOpposite())) continue;

            BlockPos redstonePos = pos.offset(dir);
            if (BlockUtils.canPlaceBlock(redstonePos, strictDirection.getValue(), pos) && BlockUtils.isInRange(redstonePos, range.getValue().floatValue()))
            {
                allow = true;
                break;
            }

        }

        if (!allow) return false;


        return true;
    }


    @Getter
    @Setter
    public class HoleCampBlock {
        BlockPos pos;
        Direction direction;
        Direction redstoneDirection;

        public HoleCampBlock(BlockPos pos, Direction direction, Direction redstoneDirection)
        {
            this.pos = pos;
            this.direction = direction;
            this.redstoneDirection = redstoneDirection;

        }


    }

    @Override
    public String getDescription()
    {
        return "AntiHolecamp: Kicks people out of there safeholes using pistons";
    }
}
