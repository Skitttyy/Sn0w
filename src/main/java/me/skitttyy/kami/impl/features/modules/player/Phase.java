package me.skitttyy.kami.impl.features.modules.player;

import com.google.common.collect.Streams;
import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.PriorityManager;
import me.skitttyy.kami.api.management.notification.NotificationManager;
import me.skitttyy.kami.api.management.notification.types.TopNotification;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.Rotation;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.RaytraceUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;

public class Phase extends Module
{
    public static Phase INSTANCE;

    public Phase()
    {
        super("Phase", Category.Player);
        INSTANCE = this;
    }

    public Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Pearl")
            .withModes("Pearl", "Wall")
            .register(this);
    Value<Number> ticks = new ValueBuilder<Number>()
            .withDescriptor("Ticks")
            .withValue(5)
            .withRange(1, 10)
            .withPageParent(mode)
            .withPage("Wall")
            .withPlaces(0)
            .register(this);
    Value<Number> distance = new ValueBuilder<Number>()
            .withDescriptor("Distance")
            .withValue(10)
            .withRange(1, 20)
            .withPageParent(mode)
            .withPage("Wall")
            .withPlaces(0)
            .register(this);

    Value<Number> pitchAmt = new ValueBuilder<Number>()
            .withDescriptor("Pitch")
            .withValue(87)
            .withRange(30, 90)
            .withPageParent(mode)
            .withPage("Pearl")
            .withPlaces(0)
            .register(this);
    Value<Boolean> auto = new ValueBuilder<Boolean>()
            .withDescriptor("AutoPhase")
            .withValue(false)
            .withPageParent(mode)
            .withPage("Pearl")
            .register(this);
    Value<Boolean> attack = new ValueBuilder<Boolean>()
            .withDescriptor("Attack")
            .withValue(true)
            .withPageParent(mode)
            .withPage("Pearl")
            .register(this);
    Value<Boolean> sixb6t = new ValueBuilder<Boolean>()
            .withDescriptor("6b6t")
            .withValue(false)
            .withPageParent(mode)
            .withPage("Pearl")
            .register(this);
    Value<Boolean> selfFill = new ValueBuilder<Boolean>()
            .withDescriptor("SelfFill")
            .withValue(false)
            .withPageParent(mode)
            .withPage("Pearl")
            .register(this);
    Value<Boolean> inventory = new ValueBuilder<Boolean>()
            .withDescriptor("Inventory")
            .withValue(false)
            .withPageParent(mode)
            .withPage("Pearl")
            .register(this);
    boolean throwPearl = false;
    boolean readyToDisable = false;

    int disable = 0;

    float yaw = Float.NaN;
    float pitch = Float.NaN;

    @SubscribeEvent(Priority.MODULE_FIRST)
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (mode.getValue().equals("Pearl"))
        {


            int pearlSlot = InventoryUtils.getHotbarItemSlot(Items.ENDER_PEARL);
            int pearlInv = InventoryUtils.getInventoryItemSlot(Items.ENDER_PEARL);

            if (throwPearl || readyToDisable) return;

            if (mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL))
            {

                if (!auto.getValue())
                {
                    NotificationManager.INSTANCE.addNotification(new TopNotification(Formatting.LIGHT_PURPLE + "Pearl is on cooldown!", 400L, 200L, new Color(100, 100, 255)));
                    setEnabled(false);
                }
                return;
            }
            if (pearlSlot == -1)
            {
                if (!inventory.getValue())
                {
                    NotificationManager.INSTANCE.addNotification(new TopNotification(Formatting.LIGHT_PURPLE + "No pearls in hotbar!", 400L, 200L, new Color(100, 100, 255)));

                    setEnabled(false);
                    return;
                } else if (pearlInv == -1)
                {
                    NotificationManager.INSTANCE.addNotification(new TopNotification(Formatting.LIGHT_PURPLE + "No pearls in inv!", 400L, 200L, new Color(100, 100, 255)));

                    setEnabled(false);
                    return;
                }
            }


            if (auto.getValue())
                if (!mc.player.isCrawling() && PhaseWalk.INSTANCE.isPhasing())
                    return;


            if (sixb6t.getValue())
            {
                int flintSlot = InventoryUtils.getHotbarItemSlot(Items.FLINT_AND_STEEL);

                if (flintSlot != -1)
                {
                    if (BlockUtils.canIgnite(mc.player.getBlockPos(), false))
                    {
                        int oldSlot = mc.player.getInventory().selectedSlot;

                        if (oldSlot != flintSlot)
                            InventoryUtils.switchToSlot(flintSlot);
                        BlockUtils.placeBlock(mc.player.getBlockPos(), BlockUtils.getPlaceableSide(mc.player.getBlockPos(), false), false);

                        if (oldSlot != flintSlot)
                            InventoryUtils.switchToSlot(oldSlot);

                    }
                }
            }

            if (mc.player.isCrawling() && mc.player.getVelocity().y >= -0.15)
                return;


            PriorityManager.INSTANCE.lockUsageLock("PearlPhase");

            Rotation pearlRotation = getPearlRotation();


            yaw = pearlRotation.getYaw();
            pitch = pearlRotation.getPitch();
            if (!mc.player.isCrawling() && selfFill.getValue())
            {
                BlockPos blockPos = mc.player.getBlockPos();
                if (yaw >= 22.5 && yaw < 67.5)
                {
                    blockPos = blockPos.south().west();
                } else if (yaw >= 67.5 && yaw < 112.5)
                {
                    blockPos = blockPos.west();
                } else if (yaw >= 112.5 && yaw < 157.5)
                {
                    blockPos = blockPos.north().west();
                } else if (yaw >= 157.5 && yaw < 202.5)
                {
                    blockPos = blockPos.north();
                } else if (yaw >= 202.5 && yaw < 247.5)
                {
                    blockPos = blockPos.north().east();
                } else if (yaw >= 247.5 && yaw < 292.5)
                {
                    blockPos = blockPos.east();
                } else if (yaw >= 292.5 && yaw < 337.5)
                {
                    blockPos = blockPos.south().east();
                } else
                {
                    blockPos = blockPos.south();
                }

                int slot = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
                if (slot != -1 && blockPos != null && BlockUtils.canPlaceBlock(blockPos, true))
                {
                    int oldslot = mc.player.getInventory().selectedSlot;
                    InventoryUtils.switchToSlot(slot);
                    RotationUtils.doSilentRotate(blockPos, true);
                    BlockUtils.placeBlock(blockPos, BlockUtils.getPlaceableSide(blockPos, true), false);
                    RotationUtils.silentSync();

                    InventoryUtils.switchToSlot(oldslot);

                }
            }

            if (attack.getValue())
            {
                for (Entity entity : mc.world.getOtherEntities(null, new Box(mc.player.getBlockPos()).expand(0.2)))
                {
                    if (entity instanceof ItemFrameEntity)
                    {

                        if (!((ItemFrameEntity) entity).getHeldItemStack().isEmpty())
                        {
                            PacketManager.INSTANCE.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                            PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        }

                        PacketManager.INSTANCE.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                        PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }

                }

                BlockState state = mc.world.getBlockState(mc.player.getBlockPos());
                if (state.getBlock() instanceof ScaffoldingBlock)
                {
                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.UP));
                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.UP));
                }
            }

            RotationUtils.setRotation(pearlRotation);

            throwPearl = true;
        } else if (mode.getValue().

                equals("Wall"))

        {
            if (Streams.stream(mc.world.getCollisions(mc.player, mc.player.getBoundingBox().expand(0.01, 0, 0.01))).toList().size() < 2)
            {
                mc.player.setPosition(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.301, Math.floor(mc.player.getX()) + 0.699), mc.player.getY(), roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.301, Math.floor(mc.player.getZ()) + 0.699));

            } else if (mc.player.age % ticks.getValue().intValue() == 0)
            {
                mc.player.setPosition(mc.player.getX() + MathHelper.clamp(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.238, Math.floor(mc.player.getX()) + 0.762) - mc.player.getX(), -0.03, 0.03), mc.player.getY(), mc.player.getZ() + MathHelper.clamp(roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.238, Math.floor(mc.player.getZ()) + 0.762) - mc.player.getZ(), -0.03, 0.03));
                PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
                PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.23, Math.floor(mc.player.getX()) + 0.77), mc.player.getY(), roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.23, Math.floor(mc.player.getZ()) + 0.77), true));
            }
            disable++;
            if (disable >= distance.getValue().doubleValue())
            {
                toggle();
                return;
            }
        }
    }


    public Rotation getPearlRotation()
    {

        float yaw = RotationUtils.fixYaw(RotationUtils.getRotationsTo(mc.player.getEyePos(), mc.player.getBlockPos().toCenterPos())[0] + 180.0f);


        float pitch = mc.player.isCrawling() ? 90 : pitchAmt.getValue().floatValue();

        //ladders bypass
        if (mc.player.isClimbing())
        {
            Box bb = mc.player.getBoundingBox();

            for (BlockPos pos : BlockUtils.getAllInBox(bb.shrink(0, 1.3, 0)))
            {
                BlockState state = mc.world.getBlockState(pos);
                if (state.getBlock() instanceof LadderBlock)
                {

                    VoxelShape outlineShape = state.getOutlineShape(mc.world, pos);
                    if (outlineShape.isEmpty())
                        continue;


                    Box shape = outlineShape.getBoundingBox();

                    Box blockBB = new Box(pos.getX() + shape.minX, pos.getY() + shape.minY,
                            pos.getZ() + shape.minZ, pos.getX() + shape.maxX,
                            pos.getY() + shape.maxY, pos.getZ() + shape.maxZ);


                    if (bb.expand(0.01).intersects(blockBB))
                    {
                        yaw = RotationUtils.getRotationsTo(mc.player.getEyePos(), blockBB.getCenter())[0];
                        break;
                    }

                }
            }
        }
        return new Rotation(yaw, pitch);
    }


    @Override
    public void onEnable()
    {
        super.onEnable();

        if (NullUtils.nullCheck()) return;

        disable = 0;
    }


    private double roundToClosest(final double num, final double low, final double high)
    {
        final double d1 = num - low;
        final double d2 = high - num;
        if (d2 > d1)
        {
            return low;
        }
        return high;
    }


    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.InputTick event)
    {
        if (NullUtils.nullCheck()) return;


        if (mode.getValue().equals("Pearl") && mc.player.isCrawling())
            doPearl();
    }


    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.MovementTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;


        if (mode.getValue().equals("Pearl") && !mc.player.isCrawling())
            doPearl();
    }

    public void doPearl()
    {
        if (readyToDisable)
        {
            readyToDisable = false;
            this.setEnabled(false);
            return;

        }

        int pearlSlot = InventoryUtils.getHotbarItemSlot(Items.ENDER_PEARL);
        int pearlInv = InventoryUtils.getInventoryItemSlot(Items.ENDER_PEARL);
        if (pearlSlot == -1)
        {
            if (!inventory.getValue() || pearlInv == -1)
            {
                return;
            }
        }

        if (mc.player.isOnGround() && mc.player.isCrawling()) mc.player.jump();


        if (throwPearl && !Float.isNaN(yaw) && !Float.isNaN(pitch))
        {
            final int oldSlot = mc.player.getInventory().selectedSlot;

            if (inventory.getValue() && pearlSlot == -1)
            {
                InventoryUtils.swap(pearlInv, mc.player.getInventory().selectedSlot);
            } else
            {
                InventoryUtils.switchToSlot(pearlSlot);
            }

            if (!mc.player.isCrawling() && !isClimbing())
                PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround()));
            PacketManager.INSTANCE.sendPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, yaw, pitch));
            mc.player.swingHand(Hand.MAIN_HAND);
            if (inventory.getValue() && pearlSlot == -1)
            {
                InventoryUtils.swap(pearlInv, mc.player.getInventory().selectedSlot);
            } else
            {
                InventoryUtils.switchToSlot(oldSlot);
            }
            yaw = Float.NaN;
            pitch = Float.NaN;
            readyToDisable = true;
        }
    }

    public boolean isClimbing()
    {

        if (mc.player.isClimbing())
        {

            Box bb = mc.player.getBoundingBox();

            for (BlockPos pos : BlockUtils.getAllInBox(bb.shrink(0, 1.3, 0)))
            {

                BlockState state = mc.world.getBlockState(pos);
                if (state.isIn(BlockTags.CLIMBABLE) && (state.getBlock() instanceof LadderBlock || state.getBlock() instanceof VineBlock))
                {
                    VoxelShape outlineShape = state.getOutlineShape(mc.world, pos);
                    if (outlineShape.isEmpty())
                        continue;


                    Box shape = outlineShape.getBoundingBox();

                    Box blockBB = new Box(pos.getX() + shape.minX, pos.getY() + shape.minY,
                            pos.getZ() + shape.minZ, pos.getX() + shape.maxX,
                            pos.getY() + shape.maxY, pos.getZ() + shape.maxZ);


                    if (bb.expand(0.01).intersects(blockBB))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void onDisable()
    {
        super.onDisable();

        disable = 0;
        if (mode.getValue().equals("Pearl"))
        {
            if (PriorityManager.INSTANCE.isUsageLocked() && PriorityManager.INSTANCE.usageLockCause.equals("PearlPhase"))
                PriorityManager.INSTANCE.unlockUsageLock();

            throwPearl = false;
            readyToDisable = false;
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
        return "Phase: lets u move through blocks";
    }
}