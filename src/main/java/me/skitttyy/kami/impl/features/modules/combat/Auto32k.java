package me.skitttyy.kami.impl.features.modules.combat;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.rotation.Rotation;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.RaytraceUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Auto32k extends Module {


    public Auto32k()
    {
        super("Auto32k", Category.Combat);
    }

    public Value<String> placeMode = new ValueBuilder<String>()
            .withDescriptor("PlaceMode")
            .withValue("Auto")
            .withModes("Auto", "Aim")
            .register(this);
    public Value<String> tickPlace = new ValueBuilder<String>()
            .withDescriptor("Tick")
            .withValue("PerTick")
            .withModes("PerTick", "MoreTick")
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(true)
            .register(this);
    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict Direction")
            .withValue(false)
            .register(this);
    Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Target Range")
            .withValue(6.0)
            .withRange(1.0, 6.0d)
            .withPlaces(0)
            .register(this);
    Value<Number> placeRange = new ValueBuilder<Number>()
            .withDescriptor("Place Range")
            .withValue(6.0)
            .withRange(1.0, 6.0d)
            .withPlaces(1)
            .register(this);

    public PlacePhase phase;


    String reason;
    Pos32k placeTarget;
    public static int block;
    public static int redstone;
    public static int hopper;
    public static int dispenser;
    public static boolean movedSword;

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck())
        {
            toggle();
            return;
        }

        block = InventoryUtils.getHotbarItemSlot(Items.OBSIDIAN);
        redstone = InventoryUtils.getHotbarItemSlot(Items.REDSTONE_BLOCK);
        dispenser = InventoryUtils.getHotbarItemSlot(Items.DISPENSER);
        hopper = InventoryUtils.getHotbarItemSlot(Items.HOPPER);
        phase = PlacePhase.DISPENSER;
        pastShulkerSlot = InventoryUtils.findShulker();
        placeTarget = null;
        placePos = null;
        Direction direction = mc.player.getHorizontalFacing().getOpposite();
        movedSword = false;
        if (placeMode.getValue().equals("Aim"))
        {

            BlockHitResult result = RaytraceUtils.getBlockHitResult(mc.player.getYaw(), mc.player.getPitch(), 6.0f);
            if (result != null && result.getType() != HitResult.Type.MISS)
            {
                BlockPos pos = result.getBlockPos();
                placeTarget = new Pos32k(pos.up(), BlockUtils.getPlaceableSide(pos.up(), strictDirection.getValue()) == null, direction);
                if (!canPlace32k(placeTarget.pos, placeTarget.direction))
                {
                    placeTarget = new Pos32k(pos.up().up(),BlockUtils.getPlaceableSide(pos.up().up(), strictDirection.getValue()) == null, direction);

                }
            }

            if(placeTarget != null && placeTarget.placeObby){
                phase = PlacePhase.OBBY;

            }
        } else
        {
            beginAutoPlacement();
        }


        if (placeTarget != null && placeTarget.pos != null && placeTarget.direction != null)
        {
            if (!canPlace32k(placeTarget.pos, placeTarget.direction))
            {
                disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "Could not place your 32k due to " + reason);

            } else
            {
                if (phase == PlacePhase.OBBY)
                {
                    if (block == -1)
                    {
                        disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "You have no obsidian, disabling");
                        return;
                    }
                }
                if (redstone == -1)
                {
                    disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "You have no redstone blocks, disabling");
                    return;

                }
                if (dispenser == -1)
                {
                    disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "You have no dispensers, disabling");
                    return;

                }
                if (hopper == -1)
                {
                    disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "You have no hoppers, disabling");
                    return;
                }

                if (InventoryUtils.findShulker() == -1)
                    disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "You have no shulkers, disabling");


            }
        } else
        {
            this.toggle();
            return;
        }

    }

    public void beginAutoPlacement()
    {
        final int range = this.range.getValue().intValue();
        BlockPos playerPos = mc.player.getBlockPos();
        Direction direction = mc.player.getHorizontalFacing().getOpposite();
        for (int y = -1; y <= 1; y++)
        {
            for (int z = -range; z <= range; z++)
            {
                for (int x = -range; x <= range; x++)
                {
                    BlockPos targetPos = playerPos.add(x, y, z);
                    if (canPlace32k(targetPos, direction))
                    {
                        boolean placeObby = mc.world.getBlockState(targetPos.down()).getBlock().equals(Blocks.AIR);
                        placeTarget = new Pos32k(targetPos, placeObby, direction);
                        if (tickPlace.getValue().equals("PerTick") && !placeObby)
                            phase = PlacePhase.DISPENSER;
                        else
                            phase = PlacePhase.OBBY;
                        return;
                    }
                }
            }
        }
        for (int y = -1; y <= 1; y++)
        {
            for (int z = -range; z <= range; z++)
            {
                for (int x = -range; x <= range; x++)
                {
                    BlockPos targetPos = playerPos.add(x, y, z);
                    for (Direction facing : Direction.values())
                    {
                        if (direction.getAxis().isVertical()) return;

                        if (canPlace32k(targetPos, facing))
                        {
                            boolean placeObby = mc.world.getBlockState(targetPos.down()).getBlock().equals(Blocks.AIR);
                            placeTarget = new Pos32k(targetPos, placeObby, facing);
                            if (tickPlace.getValue().equals("PerTick") && !placeObby)
                                phase = PlacePhase.DISPENSER;
                            else
                                phase = PlacePhase.OBBY;
                            return;
                        }
                    }
                }
            }
        }
    }


    public void disableSaying(final String s)
    {
        ChatUtils.sendMessage(new ChatMessage(s, false, 32455));
        toggle();
    }

    BlockPos placePos;

    @SubscribeEvent
    public void onUpdatePre(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        placePos = null;
        if (tickPlace.getValue().equals("PerTick"))
        {
            if (phase == PlacePhase.OBBY)
            {
                placePos = placeTarget.pos.down();
                doRotate(placePos);
                InventoryUtils.switchToSlot(block);
            } else if (phase == PlacePhase.DISPENSER)
            {
                doRotate(placeTarget.pos);
                placePos = placeTarget.pos;
                InventoryUtils.switchToSlot(dispenser);
            } else if (phase == PlacePhase.DISPENSERGUI)
            {
                doRotate(placeTarget.pos);
            } else if (phase == PlacePhase.REDSTONE)
            {
                placePos = redstoneTarget(placeTarget.pos, placeTarget.direction);
                if (placePos != null)
                {
                    InventoryUtils.switchToSlot(redstone);
                    doRotate(placeTarget.pos);
                }
            } else if (phase == PlacePhase.HOPPER)
            {
                placePos = placeTarget.pos.down().offset(placeTarget.direction);
                InventoryUtils.switchToSlot(hopper);
                doRotate(placeTarget.pos);
            }
        } else
        {
            if (phase == PlacePhase.OBBY)
            {
                placePos = placeTarget.pos.down();
                if (rotate.getValue())
                {
                    doRotate(placePos);
                } else
                {
                    if (placeTarget.direction.getOpposite() == Direction.NORTH)
                    {
                        doRotate(180f, 0);
                    } else if (placeTarget.direction.getOpposite() == Direction.SOUTH)
                    {
                        doRotate(0f, 0);
                    } else if (placeTarget.direction.getOpposite() == Direction.WEST)
                    {
                        doRotate(90f, 0);
                    } else if (placeTarget.direction.getOpposite() == Direction.EAST)
                    {
                        doRotate(-90f, 0);
                    }
                }
                if (placeTarget.placeObby)
                {
                    InventoryUtils.switchToSlot(block);
                }
            }
        }
    }

    public void doRotate(BlockPos pos)
    {
        if (!rotate.getValue()) return;

        float[] rots = RotationUtils.getBlockRotations(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue()));
        RotationUtils.setRotation(rots);
    }

    public void doRotate(Float yaw, float pitch)
    {
        RotationUtils.setRotation(yaw, pitch);
    }

    int pastShulkerSlot = -1;

    @SubscribeEvent
    public void onUpdatePost(TickEvent.InputTick event)
    {
        if (NullUtils.nullCheck()) return;


        if (tickPlace.getValue().equals("PerTick"))
        {
            if (placePos != null)
            {
                if (phase == PlacePhase.OBBY)
                {
                    if (!BlockUtils.placeBlock(placePos, BlockUtils.getPlaceableSide(placePos, strictDirection.getValue()), false))
                    {
                        disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "Failed to place obsidian, disabling");
                        return;
                    }
                    placePos = null;
                    phase = PlacePhase.DISPENSER;
                } else if (phase == PlacePhase.DISPENSER)
                {
                    //make sure we r rotated cuz ncp checks the rot from the tick before aswell
                    packetRot(placePos);
                    //rotate to current facing
                    if (placeTarget.direction.getOpposite() == Direction.NORTH)
                    {
                        RotationUtils.packetRotate(180, 0);
                    } else if (placeTarget.direction.getOpposite() == Direction.SOUTH)
                    {
                        RotationUtils.packetRotate(0, 0);
                    } else if (placeTarget.direction.getOpposite() == Direction.WEST)
                    {
                        RotationUtils.packetRotate(90, 0);
                    } else if (placeTarget.direction.getOpposite() == Direction.EAST)
                    {
                        RotationUtils.packetRotate(-90, 0);
                    }
                    if (!BlockUtils.placeBlock(placePos, BlockUtils.getPlaceableSide(placePos, strictDirection.getValue()), false))
                    {
                        disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "Failed to place dispenser, disabling");
                        return;
                    }
                    placePos = null;
                    phase = PlacePhase.DISPENSERGUI;
                    BlockUtils.openBlock(placeTarget.pos);
                    return;
                } else if (phase == PlacePhase.REDSTONE)
                {
                    if (!BlockUtils.placeBlock(placePos, BlockUtils.getPlaceableSide(placePos, strictDirection.getValue()), Hand.MAIN_HAND, false, true))
                    {
                        disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "Failed to place redstone block, disabling");
                        return;
                    }
                    placePos = null;
                    phase = PlacePhase.HOPPER;
                } else if (phase == PlacePhase.HOPPER)
                {
                    if (!BlockUtils.placeBlock(placePos, BlockUtils.getPlaceableSide(placePos, strictDirection.getValue()), Hand.MAIN_HAND, false, true))
                    {
                        disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "Failed to place hopper, disabling");
                        return;
                    }
                    BlockUtils.openBlock(placePos);

                    placePos = null;
                    phase = PlacePhase.HOPPERGUI;
                    return;
                }
            }
            if (phase == PlacePhase.DISPENSERGUI)
            {
                if (mc.currentScreen instanceof Generic3x3ContainerScreen)
                {
                    int slot = InventoryUtils.findShulker();
                    if (slot == -1)
                    {
                        disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "You have no shulker! Disabling");
                        return;
                    }
                    mc.interactionManager.clickSlot(((Generic3x3ContainerScreen) mc.currentScreen).getScreenHandler().syncId, 4, slot, SlotActionType.SWAP, mc.player);
                    mc.player.closeScreen();
                    phase = PlacePhase.REDSTONE;
                }
            } else if (phase == PlacePhase.HOPPERGUI)
            {
                if (!movedSword && mc.currentScreen instanceof HopperScreen)
                {
                    if (((HopperScreen) mc.currentScreen).getScreenHandler().getSlot(0).getStack().isEmpty())
                    {
                        return;
                    }
                    mc.interactionManager.clickSlot(((HopperScreen) mc.currentScreen).getScreenHandler().syncId, 0, pastShulkerSlot, SlotActionType.SWAP, mc.player);
                    movedSword = true;
                } else if (movedSword)
                {
                    this.phase = PlacePhase.FINISHED;
                    toggle();
                }
            }
        } else
        {
            if (phase == PlacePhase.OBBY)
            {
                //place obsidian
                if (placeTarget.placeObby)
                {
                    if (!BlockUtils.placeBlock(placePos, BlockUtils.getPlaceableSide(placePos, strictDirection.getValue()), false))
                    {
                        disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "Failed to place obsidian, disabling");
                        return;
                    }
                }
                InventoryUtils.switchToSlot(dispenser);

                packetRot(placeTarget.pos);
                placePos = placeTarget.pos;
                if (rotate.getValue())
                {
                    if (placeTarget.direction.getOpposite() == Direction.NORTH)
                    {
                        RotationUtils.packetRotate(180, 0);
                    } else if (placeTarget.direction.getOpposite() == Direction.SOUTH)
                    {
                        RotationUtils.packetRotate(0, 0);
                    } else if (placeTarget.direction.getOpposite() == Direction.WEST)
                    {
                        RotationUtils.packetRotate(90, 0);
                    } else if (placeTarget.direction.getOpposite() == Direction.EAST)
                    {
                        RotationUtils.packetRotate(-90, 0);
                    }
                }
                //place dispenser
                if (!BlockUtils.placeBlock(placePos, BlockUtils.getPlaceableSide(placePos, strictDirection.getValue()), false))
                {
                    disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "Failed to place dispenser, disabling");
                    return;
                }
                //TODO: this
//                Anti32k.INSTANCE.visitedPositions.add(placePos);

                //place hopper in advance
                placePos = placeTarget.pos.down().offset(placeTarget.direction);
                InventoryUtils.switchToSlot(hopper);
                packetRot(placePos);

                if (!BlockUtils.placeBlock(placePos, BlockUtils.getPlaceableSide(placePos, strictDirection.getValue()), false))
                {
                    disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "Failed to place hopper, disabling");
                    return;
                }
                phase = PlacePhase.DISPENSERGUI;
                packetRot(placeTarget.pos);
                BlockUtils.openBlock(placeTarget.pos);
            } else if (phase == PlacePhase.DISPENSERGUI)
            {
                if (mc.currentScreen instanceof Generic3x3ContainerScreen)
                {
                    int slot = InventoryUtils.findShulker();
                    if (slot == -1)
                    {
                        disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "You have no shulker! Disabling");
                        return;
                    }
                    mc.interactionManager.clickSlot(((HopperScreen) mc.currentScreen).getScreenHandler().syncId, 4, slot, SlotActionType.SWAP, mc.player);
                    mc.player.closeScreen();
                    placePos = redstoneTarget(placeTarget.pos, placeTarget.direction);
                    if (placePos != null)
                    {
                        InventoryUtils.switchToSlot(redstone);
                        packetRot(placeTarget.pos);
                        if (!BlockUtils.placeBlock(placePos, BlockUtils.getPlaceableSide(placePos, strictDirection.getValue()), false))
                        {
                            disableSaying(Formatting.RED + "[!] " + Formatting.WHITE + "Failed to place redstone block, disabling");
                            return;
                        }
                        BlockUtils.openBlock(placeTarget.pos.down().offset(placeTarget.direction));
                        phase = PlacePhase.HOPPERGUI;
                    }
                }
            } else if (phase == PlacePhase.HOPPERGUI)
            {
                if (!movedSword && mc.currentScreen instanceof HopperScreen)
                {
                    if (((GenericContainerScreen) mc.currentScreen).getScreenHandler().getSlot(0).getStack().isEmpty())
                    {
                        return;
                    }

                    mc.interactionManager.clickSlot(((HopperScreen) mc.currentScreen).getScreenHandler().syncId, 0, pastShulkerSlot, SlotActionType.SWAP, mc.player);
                    movedSword = true;
                } else if (movedSword)
                {
                    this.phase = PlacePhase.FINISHED;
                    toggle();
                }
            }
        }
    }


    public void packetRot(BlockPos pos)
    {
        if (!rotate.getValue()) return;

        float[] rots = RotationUtils.getBlockRotations(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue()));
        if (rots != null)
        {
            RotationUtils.packetRotate(rots);
        }
    }

    public boolean canPlace32k(BlockPos dispenserPos, Direction facing)
    {
        BlockPos obbyPos = dispenserPos.down();
        boolean placeObby = mc.world.getBlockState(obbyPos).getBlock().equals(Blocks.AIR);
        BlockPos redstonePos = redstoneTarget(dispenserPos, facing);
        if (redstonePos == null)
        {
            reason = "Redstone";
            return false;
        }

        if (placeObby)
        {
            if (!BlockUtils.canPlaceBlock(obbyPos, strictDirection.getValue()) && BlockUtils.isInRange(obbyPos, placeRange.getValue().doubleValue()))
            {
                reason = "Obsidian";
                return false;
            }
        }

        if (!BlockUtils.canPlaceBlockPretendDistance(dispenserPos, strictDirection.getValue(), obbyPos, placeRange.getValue().doubleValue()) || BlockUtils.willPlaceVertical(dispenserPos))
        {
            reason = "Dispenser";
            return false;
        }
        if (!BlockUtils.canPlaceBlockPretendDistance(obbyPos.offset(facing), strictDirection.getValue(), obbyPos, placeRange.getValue().doubleValue()))
        {
            reason = "Hopper";
            return false;
        }
        if (!mc.world.getBlockState(dispenserPos.offset(facing)).isReplaceable() && !(mc.world.getBlockState(dispenserPos.offset(facing)).getBlock() instanceof FireBlock))
        {
            reason = "dispense";
            return false;
        }

        return true;
    }

    public BlockPos redstoneTarget(final BlockPos blockPos, Direction facing)
    {
        final Direction[] sideFacings = {Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST, Direction.UP};
        for (final Direction f : sideFacings)
        {
            if (!f.equals(facing))
            {
                if (BlockUtils.canPlaceBlockPretendDistance(blockPos.offset(f), strictDirection.getValue(), blockPos, placeRange.getValue().doubleValue()))
                {
                    return blockPos.offset(f);
                }
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onRender3d(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;



        if (placeTarget != null)
        {
            RenderUtil.renderBox(RenderType.FILL, new Box(placeTarget.pos), Color.RED, Color.RED);
            RenderUtil.renderBox(RenderType.LINES, new Box(placeTarget.pos), Color.RED, Color.RED);
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        phase = PlacePhase.OBBY;
        placePos = null;
        movedSword = false;

    }

    class Pos32k {
        boolean placeObby;
        BlockPos pos;
        Direction direction;

        public Pos32k(BlockPos pos, boolean placeObby, Direction direction)
        {
            this.pos = pos;
            this.placeObby = placeObby;
            this.direction = direction;
        }
    }

    public enum PlacePhase {
        OBBY, DISPENSER, DISPENSERGUI, HOPPER, REDSTONE, HOPPERGUI, FINISHED
    }

    @Override
    public String getDescription()
    {
        return "Auto32k: Automatically places a 32k";
    }
}
