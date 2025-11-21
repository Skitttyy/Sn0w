package me.skitttyy.kami.impl.features.modules.player;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.event.events.move.SneakEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PriorityManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.RaytraceUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;


import java.awt.*;
import java.util.*;
import java.util.List;

public class Scaffold extends Module
{


    public static Scaffold INSTANCE;

    public Scaffold()
    {
        super("Scaffold", Category.Movement);
        INSTANCE = this;
    }

    Value<String> stage = new ValueBuilder<String>()
            .withDescriptor("Event")
            .withValue("Post")
            .withModes("Post", "Pre")
            .register(this);

    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Silent")
            .withModes("Silent", "Swap")
            .register(this);
    Value<String> tower = new ValueBuilder<String>()
            .withDescriptor("Tower", "towerMode")
            .withValue("None")
            .withModes("None", "Normal", "Move")
            .withAction(s -> handleTowerPage(s.getValue()))
            .register(this);
    Value<Boolean> limit = new ValueBuilder<Boolean>()
            .withDescriptor("Limit")
            .withValue(false)
            .register(this);
    public Value<Boolean> grim = new ValueBuilder<Boolean>()
            .withDescriptor("Grim")
            .withValue(false)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(false)
            .register(this);
    Value<Boolean> swing = new ValueBuilder<Boolean>()
            .withDescriptor("Swing")
            .withValue(false)
            .register(this);
    Value<Boolean> downwards = new ValueBuilder<Boolean>()
            .withDescriptor("Downwards")
            .withValue(false)
            .register(this);
    Value<String> safeWalk = new ValueBuilder<String>()
            .withDescriptor("SafeWalk")
            .withValue("None")
            .withModes("None", "Soft", "Strong")
            .register(this);
    Value<String> expand = new ValueBuilder<String>()
            .withDescriptor("Expand", "ExpandMode")
            .withValue("Off")
            .withModes("Off", "On", "Dynamic")
            .register(this);
    Value<Number> placeDelay = new ValueBuilder<Number>()
            .withDescriptor("Place Delay")
            .withValue(0)
            .withRange(0, 1000)
            .withPlaces(0)
            .register(this);
    Value<String> blockCounter = new ValueBuilder<String>()
            .withDescriptor("Block Counter")
            .withValue("None")
            .withModes("None", "Text", "Bar")
            .withAction(s -> handleBlockCounterPage(s.getValue()))
            .register(this);
    Value<Boolean> blockText = new ValueBuilder<Boolean>()
            .withDescriptor("Bar Text")
            .withValue(false)
            .register(this);
    Value<String> barColor = new ValueBuilder<String>()
            .withDescriptor("Bar Color")
            .withValue("Scissor")
            .withModes("Scissor", "Custom")
            .register(this);
    Value<Number> barThickness = new ValueBuilder<Number>()
            .withDescriptor("Thickness")
            .withValue(1)
            .withRange(0.5f, 15)
            .withPlaces(1)
            .register(this);
    Value<Sn0wColor> leftColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Left Color")
            .withValue(new Sn0wColor(255, 0, 255, 255))
            .register(this);
    Value<Sn0wColor> rightColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Right Color")
            .withValue(new Sn0wColor(0, 255, 0))
            .register(this);
    Value<Boolean> renderRays = new ValueBuilder<Boolean>()
            .withDescriptor("Visualize")
            .withValue(false)
            .register(this);
    public Value<Boolean> render = new ValueBuilder<Boolean>()
            .withDescriptor("Render")
            .withValue(true)
            .register(this);
    public Value<Sn0wColor> fill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill")
            .withValue(new Sn0wColor(255, 0, 0, 25))
            .withParentEnabled(true)
            .withParent(render)
            .register(this);
    public Value<Sn0wColor> line = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line")
            .withValue(new Sn0wColor(255, 0, 0, 255))
            .withParentEnabled(true)
            .withParent(render)
            .register(this);
    public Value<Number> fadeTime = new ValueBuilder<Number>()
            .withDescriptor("Fade Time")
            .withValue(200)
            .withRange(0, 1000)
            .withParentEnabled(true)
            .withParent(render)
            .register(this);
    public Map<BlockPos, Long> renderPositions = new HashMap<>();

    Timer placeTimer = new Timer();
    Timer towerTimer = new Timer();

    int blockCount = 0;
    protected float[] rotations;
    protected Pair.BlockPair pos;
    private static final List<Block> blacklistedBlocks = new ArrayList<>();

    public void handleBlockCounterPage(String page)
    {
        barColor.setActive(page.equals("Bar"));
        barThickness.setActive(page.equals("Bar"));
        blockText.setActive(page.equals("Bar"));

        leftColor.setActive(page.equals("Bar") && barColor.getValue().equals("Custom"));
        rightColor.setActive(page.equals("Bar") && barColor.getValue().equals("Custom"));

    }

    public void handleTowerPage(String page)
    {
        limit.setActive(!page.equals("None"));
    }

    Vec3d posAtTime;
    BlockHitResult lastResult;
    private boolean isSneaking;

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;

        blockCount = getBlockCount();
        pos = null;
        lastResult = null;
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (NullUtils.nullCheck()) return;


        if (PriorityManager.INSTANCE.isUsageLocked() && Objects.equals(PriorityManager.INSTANCE.usageLockCause, "Scaffold"))
        {
            PriorityManager.INSTANCE.unlockUsageLock();
        }
        if (isSneaking)
        {
            mc.options.sneakKey.setPressed(false);
        }

        isSneaking = false;
        pos = null;
        lastResult = null;
    }

    @SubscribeEvent
    public void onSneak(SneakEvent event)
    {
        if (!canSneak())
            event.setCancelled(true);
    }


    public boolean canSneak()
    {
        if (downwards.getValue() && mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed())
            return false;

        return true;
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        if (PriorityManager.INSTANCE.isUsageLocked() && !Objects.equals(PriorityManager.INSTANCE.usageLockCause, "Scaffold"))
            return;

        placeTimer.setDelay(placeDelay.getValue().longValue());
        blockCount = getBlockCount();

        if (getBlockSlot() == -1)
        {
            pos = null;
            return;
        }

        if (mode.getValue().equals("Swap"))
            mc.player.getInventory().selectedSlot = getBlockSlot();
        pos = null;

        pos = findNextPos();
        if (pos != null)
        {
            setRotations(pos.key(), pos.value());
            calcVecs();


            if (!tower.getValue().equals("None"))
            {

                BlockPos under = BlockUtils.getRoundedBlockPos(mc.player.getX(), mc.player.getY() - 1.6D, mc.player.getZ());

                if (mc.options.jumpKey.isPressed() && !mc.world.getBlockState(under).isReplaceable())
                {
                    if (!PlayerUtils.isMoving() || tower.getValue().equals("Move"))
                    {
                        if (mc.player.getVelocity().y != 0.42F)
                        {
                            PlayerUtils.setMotionY(0.42f);
                        }

                        //bypasses hypixel (old) tower check but also fixes the lagbacks on normal ncp lol
                        if (towerTimer.isPassed(1500) && limit.getValue())
                        {
                            PlayerUtils.setMotionY(-0.28f);
                            towerTimer.resetDelay();
                        }

                        PlayerUtils.setMotionXZ(mc.player.getVelocity().x * 0.9, mc.player.getVelocity().z * 0.9);
                    } else
                    {
                        towerTimer.resetDelay();
                    }
                } else if (!mc.options.jumpKey.isPressed())
                {
                    towerTimer.resetDelay();
                }
            }

        } else if (!mc.options.jumpKey.isPressed())
        {
            towerTimer.resetDelay();
        }


        if (pos == null && grim.getValue())
        {
            RotationUtils.setRotation(PlayerUtils.getMoveYaw(mc.player.getYaw()), 90.0f, 7);
        }

        if (stage.getValue().equals("Pre"))
            doPlace();


    }


    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;

        if (PriorityManager.INSTANCE.isUsageLocked() && !Objects.equals(PriorityManager.INSTANCE.usageLockCause, "Scaffold"))
            return;

        if (stage.getValue().equals("Post"))
            doPlace();


        if (pos == null)
        {
            PriorityManager.INSTANCE.unlockUsageLock();
        }

    }

    @SubscribeEvent
    public void draw(RenderGameOverlayEvent.Text event)
    {
        if (NullUtils.nullCheck()) return;

        if (blockCounter.getValue().equals("None")) return;


        final Color blockColor = getBlockColor();
        final ScaledResolution resolution = new ScaledResolution(mc);
        int slot = getBlockSlot();

        switch (blockCounter.getValue())
        {
            case "Bar":
                if (blockCount == 0 || MiddleClick.INSTANCE.startedTimer) return;

                float x = resolution.getScaledWidth() / 2.0F;
                float y = resolution.getScaledHeight() / 2.0F + 13;
                float thickness = barThickness.getValue().floatValue();
                float percentage = Math.min(1f, (blockCount) / (64.0f));

                float width = 80.0F;
                float half = width / 2;


                RenderUtil.renderRect(event.getContext().getMatrices(), (x - half - 0.5), (y - 0.5), (half * 2) + 1f, thickness + 1, 0x78000000);
                RenderUtil.renderGradient(event.getContext().getMatrices(), (x - half - 0.5), (y - 0.5), width * percentage + 1, thickness + 1,
                        barColor.getValue().equals("Scissor") ? Color.RED.darker().getRGB() : leftColor.getValue().getColor().darker().getRGB(), barColor.getValue().equals("Scissor") ? blockColor.darker().getRGB() : rightColor.getValue().getColor().darker().getRGB(), true);
                RenderUtil.renderGradient(event.getContext().getMatrices(), (x - half), y, width * percentage, thickness,
                        barColor.getValue().equals("Scissor") ? Color.RED.getRGB() : leftColor.getValue().getColor().getRGB(), barColor.getValue().equals("Scissor") ? blockColor.getRGB() : rightColor.getValue().getColor().getRGB(), true);
                if (slot != -1 && blockText.getValue())
                {
                    ItemStack blockStack = new ItemStack(mc.player.getInventory().getStack(slot).getItem());

                    RenderUtil.renderItemWithCount(event.getContext(), blockStack, new Point((int) (x - half + width * percentage) - 8, (int) (y + (thickness / 2)) - 8), blockCount, barColor.getValue().equals("Scissor") ? blockColor : rightColor.getValue().getColor(), true);
                }
                break;
            case "Text":
                if (slot != -1)
                {
                    ItemStack blockStack = new ItemStack(mc.player.getInventory().getStack(slot).getItem());
                    RenderUtil.renderItemWithCount(event.getContext(), blockStack, new Point((int) (resolution.getScaledWidth() / 2f + 1f - 16f / 2f), (int) (resolution.getScaledHeight() / 2f + 10)), blockCount, blockColor, true);
                } else
                {
                    ClickGui.CONTEXT.getRenderer().renderText(event.getContext(), Integer.toString(blockCount), resolution.getScaledWidth() / 2f + 1f - ClickGui.CONTEXT.getRenderer().getTextWidth(Integer.toString(blockCount)) / 2f, resolution.getScaledHeight() / 2f + 10f, blockColor, true);
                }
                break;
        }

    }


    @SubscribeEvent
    public void onMove(MoveEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (!safeWalk.getValue().equals("None") && PlayerUtils.isMoving() && mc.player.isOnGround())
        {
            Vec2f safeMove = PlayerUtils.safeWalk(event.getX(), event.getZ());

            if (safeWalk.getValue().equals("Strong"))
            {
                isSneaking = PlayerUtils.canSneak(safeMove);
                mc.options.sneakKey.setPressed(isSneaking);
                return;
            }

            event.setX(safeMove.x);
            event.setZ(safeMove.y);
        }
    }

    private int getBlockSlot()
    {
        for (int i = 0; i < 9; ++i)
        {
            if (mc.player.getInventory().getStack(i).getCount() != 0 && mc.player.getInventory().getStack(i).getItem() instanceof BlockItem && isItemValid(mc.player.getInventory().getStack(i).getItem()))
            {
                return i;
            }
        }
        return -1;
    }

    private int getBlockCount()
    {
        int blockCount = 0;
        for (int i = 0; i < 9; ++i)
        {
            if (mc.player.getInventory().getStack(i).getCount() != 0 && mc.player.getInventory().getStack(i).getItem() instanceof BlockItem && isItemValid(mc.player.getInventory().getStack(i).getItem()))
            {
                blockCount += mc.player.getInventory().getStack(i).getCount();
            }
        }
        return blockCount;
    }

    private void calcVecs()
    {
        lastResult = null;
        HitResult result = RaytraceUtils.getBlockHitResult(rotations[0], rotations[1]);
        if (result instanceof BlockHitResult hitResult)
        {
            if (HitResult.Type.MISS == hitResult.getType())
                return;
            posAtTime = mc.player.getEyePos();
            lastResult = hitResult;
        }
    }

    private Color getBlockColor()
    {
        float f2 = 64.0f;
        float f3 = Math.max(0.0f, Math.min(blockCount, f2) / f2);
        return new Color(Color.HSBtoRGB(f3 / 3.0f, 1.0f, 1.0f) | 0xFF000000);
    }

    private boolean isItemValid(Item item)
    {
        if (item instanceof BlockItem)
        {
            BlockItem itemBlock = (BlockItem) item;
            if (!blacklistedBlocks.contains(itemBlock.getBlock()))
            {
                BlockState baseState = itemBlock.getBlock().getDefaultState();
                if (!baseState.isReplaceable())
                {
                    BlockPos outOfBounds = new BlockPos(mc.player.getBlockX() + 4158, mc.player.getBlockY() + 4158, mc.player.getBlockZ() + 4158);
                    VoxelShape voxelShape = baseState.getCollisionShape(mc.world, outOfBounds);

                    return voxelShape.equals(VoxelShapes.fullCube());
                }
            }
        }
        return false;
    }


    public void doPlace()
    {
        if (pos == null)
            return;

        if (getBlockSlot() == -1) return;


        if (placeTimer.isPassed())
        {
            int oldSlot = mc.player.getInventory().selectedSlot;
            if (mode.getValue().equals("Silent"))
                InventoryUtils.switchToSlot(getBlockSlot());


            if (rotations != null && AntiCheat.INSTANCE.protocol.getValue() && rotate.getValue())
            {
                RotationUtils.doSilentRotate(rotations);
            }
            if (BlockUtils.placeBlock(pos.key(), pos.value(), Hand.MAIN_HAND, false, swing.getValue()) && render.getValue())
                renderPositions.put(pos.key(), System.currentTimeMillis());

            if (rotations != null && AntiCheat.INSTANCE.protocol.getValue() && rotate.getValue())
            {
                RotationUtils.silentSync();
            }
            PriorityManager.INSTANCE.lockUsageLock("Scaffold");

            placeTimer.resetDelay();
            if (mode.getValue().equals("Silent"))
                InventoryUtils.switchToSlot(oldSlot);
        }
    }


    public Pair.BlockPair getHelping(BlockPos pos)
    {
        for (Direction facing : Direction.values())
        {
            BlockPos p = pos.offset(facing);

            if (p.equals(mc.player.getBlockPos())) continue;

            Direction f = BlockUtils.getFacing(p);
            if (f != null)
            {
                return new Pair.BlockPair(p, f);
            }
        }
        for (Direction facing : Direction.values())
        {
            BlockPos p = pos.offset(facing);
            if (p.equals(mc.player.getBlockPos())) continue;

            if (mc.world.getBlockState(p).isReplaceable())
            {
                for (final Direction direction1 : Direction.values())
                {
                    final BlockPos neighbor1 = p.offset(direction1);

                    if (neighbor1.equals(mc.player.getBlockPos())) continue;

                    if (!mc.world.getBlockState(neighbor1).isReplaceable())
                    {
                        Direction f = BlockUtils.getFacing(neighbor1);
                        if (f != null)
                        {
                            return new Pair.BlockPair(neighbor1, f);
                        }
                    }
                }
            }
        }


        return null;
    }

    private void setRotations(BlockPos pos, Direction facing)
    {

        rotations = RotationUtils.getBlockRotations(pos.offset(facing), facing.getOpposite());
        if (rotate.getValue() && rotations != null)
        {
                RotationUtils.setRotation(rotations);
        }
    }


    //finds next pos for scaffold
    public Pair.BlockPair findNextPos()
    {
        BlockPos target = getTargetBlock();
        if (target == null) return null;

        Direction direction = BlockUtils.getFacing(target);
        if (direction != null)
            return new Pair.BlockPair(target, direction);

        Pair.BlockPair assist = getHelping(target);

        if (assist != null) return assist;


        //catch
        if (!mc.player.isOnGround())
        {
            BlockPos underPos = target.down();
            if (mc.world.getBlockState(underPos).isReplaceable())
            {
                Direction underDirection = BlockUtils.getFacing(underPos);

                if (underDirection != null) return new Pair.BlockPair(underPos, underDirection);

                Pair.BlockPair underAssist = getHelping(underPos);
                return underAssist;
            }
        }

        return null;
    }

    public BlockPos getTargetBlock()
    {
        BlockPos underPos = mc.player.getBlockPos().down();


        // downwards
        if (!canSneak())
        {
            underPos = underPos.down();
        }

        // place below if underpos is aviaalbl
        if (mc.world.getBlockState(underPos).isReplaceable())
            return underPos;

        if (expand.getValue().equals("Off") || (expand.getValue().equals("Dynamic") && !mc.options.jumpKey.isPressed()))
            return null;


        // expands by the direction ur moving
        if (mc.options.forwardKey.isPressed() && !mc.options.backKey.isPressed())
        {
            BlockPos forwardPos = underPos.offset(mc.player.getHorizontalFacing());

            if (mc.world.getBlockState(forwardPos).isReplaceable())
            {
                return forwardPos;
            }
        } else if (mc.options.backKey.isPressed() && !mc.options.forwardKey.isPressed())
        {
            BlockPos backPos = underPos.offset(mc.player.getHorizontalFacing().getOpposite());

            if (mc.world.getBlockState(backPos).isReplaceable())
            {
                return backPos;
            }
        }

        if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
        {
            BlockPos rightPos = underPos.offset(mc.player.getHorizontalFacing().rotateYClockwise());

            if (mc.world.getBlockState(rightPos).isReplaceable())
            {
                return rightPos;
            }
        } else if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
        {
            BlockPos leftPos = underPos.offset(mc.player.getHorizontalFacing().rotateYCounterclockwise());

            if (mc.world.getBlockState(leftPos).isReplaceable())
            {
                return leftPos;
            }
        }

        return null;

    }


    //initalize blacklisted blocks
    static
    {
        // shulker box
        blacklistedBlocks.add(Blocks.BLACK_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.BLUE_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.BROWN_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.CYAN_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.GRAY_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.GREEN_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.LIGHT_BLUE_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.LIME_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.MAGENTA_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.ORANGE_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.PINK_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.PURPLE_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.RED_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.LIGHT_GRAY_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.WHITE_SHULKER_BOX);
        blacklistedBlocks.add(Blocks.YELLOW_SHULKER_BOX);
        // falling blocks
        blacklistedBlocks.add(Blocks.ANVIL);
        blacklistedBlocks.add(Blocks.GRAVEL);
        blacklistedBlocks.add(Blocks.SAND);
        // stairs
        blacklistedBlocks.add(Blocks.ACACIA_STAIRS);
        blacklistedBlocks.add(Blocks.SANDSTONE_STAIRS);
        blacklistedBlocks.add(Blocks.SPRUCE_STAIRS);
        blacklistedBlocks.add(Blocks.STONE_STAIRS);
        blacklistedBlocks.add(Blocks.STONE_BRICK_STAIRS);
        blacklistedBlocks.add(Blocks.BIRCH_STAIRS);
        blacklistedBlocks.add(Blocks.BRICK_STAIRS);
        blacklistedBlocks.add(Blocks.DARK_OAK_STAIRS);
        blacklistedBlocks.add(Blocks.JUNGLE_STAIRS);
        blacklistedBlocks.add(Blocks.NETHER_BRICK_STAIRS);
        blacklistedBlocks.add(Blocks.OAK_STAIRS);
        blacklistedBlocks.add(Blocks.PURPUR_STAIRS);
        blacklistedBlocks.add(Blocks.QUARTZ_STAIRS);
        blacklistedBlocks.add(Blocks.RED_SANDSTONE_STAIRS);
        //misc blocks
        blacklistedBlocks.add(Blocks.CHORUS_FLOWER);
        blacklistedBlocks.add(Blocks.HOPPER);
        blacklistedBlocks.add(Blocks.COBWEB);

        //redstone stuff
        blacklistedBlocks.add(Blocks.PISTON);
        blacklistedBlocks.add(Blocks.STICKY_PISTON);
        blacklistedBlocks.add(Blocks.REDSTONE_BLOCK);


    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (renderRays.getValue() && lastResult != null)
        {
            RenderUtil.renderLineFromPosToPos(posAtTime, lastResult.getPos(), rightColor.getValue().getColor(), leftColor.getValue().getColor(), 1);
        }
    }

    @Override
    public String getHudInfo()
    {
        return "Cato";
    }

    @Override
    public String getDescription()
    {
        return "Scaffold: Places blocks under you or bridges fast";
    }
}
