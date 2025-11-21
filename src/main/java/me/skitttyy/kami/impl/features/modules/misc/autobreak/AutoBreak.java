package me.skitttyy.kami.impl.features.modules.misc.autobreak;

import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.render.RenderTimer;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.ProtectionUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.movement.NoSlow;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.ArrayList;
import java.awt.Color;
import java.util.List;

public class AutoBreak extends Module
{
    public static AutoBreak INSTANCE;

    Value<String> page = new ValueBuilder<String>()
            .withDescriptor("Page")
            .withValue("Breaking")
            .withModes("Breaking", "AutoCity", "Render")
            .withAction(s -> handlePage(s.getValue()))
            .register(this);
    public Value<Number> breakAt = new ValueBuilder<Number>()
            .withDescriptor("Break At")
            .withValue(1)
            .withRange(0, 1)
            .withPlaces(1)
            .register(this);
    /**
     * Breaking
     */
    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Type")
            .withValue("Normal")
            .withModes("Normal", "Double")
            .register(this);
    Value<String> logic = new ValueBuilder<String>()
            .withDescriptor("Logic")
            .withValue("Always")
            .withModes("Always", "Only", "Basic")
            .register(this);
    Value<Number> breakRange = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(6)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    Value<Number> walls = new ValueBuilder<Number>()
            .withDescriptor("Walls")
            .withValue(3.0D)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    Value<String> swapMode = new ValueBuilder<String>()
            .withDescriptor("Switch")
            .withValue("None")
            .withModes("None", "Silent", "Alter", "6b6t")
            .register(this);
    public Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict")
            .withValue(true)
            .register(this);
    Value<Boolean> airCheck = new ValueBuilder<Boolean>()
            .withDescriptor("AirStrict")
            .withValue(false)
            .register(this);
    public Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(false)
            .register(this);
    Value<Boolean> builders = new ValueBuilder<Boolean>()
            .withDescriptor("2b2t")
            .withValue(false)
            .register(this);
    Value<Boolean> inhibit = new ValueBuilder<Boolean>()
            .withDescriptor("Inhibit")
            .withValue(false)
            .register(this);
    public Value<Boolean> swing = new ValueBuilder<Boolean>()
            .withDescriptor("Swing")
            .withValue(true)
            .register(this);
    Value<Boolean> unbreak = new ValueBuilder<Boolean>()
            .withDescriptor("Unbreak")
            .withValue(true)
            .register(this);
    Value<String> remineMode = new ValueBuilder<String>()
            .withDescriptor("Rebreak")
            .withValue("None")
            .withModes("None", "Normal", "Insta", "Fast")
            .register(this);
    Value<Boolean> alwaysInstant = new ValueBuilder<Boolean>()
            .withDescriptor("Predict")
            .withValue(true)
            .register(this);
    Timer instantTimer = new Timer();

    Value<Number> instantDelay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(0)
            .withRange(0, 1000)
            .withAction(set -> instantTimer.setDelay(set.getValue().longValue()))
            .register(this);
    Value<String> pause = new ValueBuilder<String>()
            .withDescriptor("Pause")
            .withValue("None")
            .withModes("None", "Eat")
            .register(this);
    /**
     * Targetting
     */
    public Value<Boolean> autoCity = new ValueBuilder<Boolean>()
            .withDescriptor("Auto City")
            .withValue(true)
            .register(this);

    Value<Number> targetRange = new ValueBuilder<Number>()
            .withDescriptor("Distance")
            .withValue(7d)
            .withRange(3d, 20d)
            .register(this);
    public Value<Boolean> antiCrawl = new ValueBuilder<Boolean>()
            .withDescriptor("Anti Crawl")
            .withValue(true)
            .register(this);
    public Value<Boolean> preMine = new ValueBuilder<Boolean>()
            .withDescriptor("Pre Mine")
            .withValue(true)
            .register(this);
    public Value<Boolean> selfMine = new ValueBuilder<Boolean>()
            .withDescriptor("Self Mine")
            .withValue(false)
            .register(this);
    public Value<Boolean> headCrystal = new ValueBuilder<Boolean>()
            .withDescriptor("HeadCrystal")
            .withValue(false)
            .register(this);

    /**
     * Renders
     */
    Value<Sn0wColor> fillColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill Color")
            .withValue(new Sn0wColor(255, 0, 0, 81))
            .register(this);
    Value<Sn0wColor> lineColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Outline Color")
            .withValue(new Sn0wColor(255, 0, 0, 255))
            .register(this);
    Value<Sn0wColor> fillColor2 = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill Color 2")
            .withValue(new Sn0wColor(0, 255, 0, 81))
            .register(this);
    Value<Sn0wColor> lineColor2 = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Outline Color 2")
            .withValue(new Sn0wColor(0, 255, 0, 255))
            .register(this);
    private long lastBreak;

    /**
     * TODO: "AutoBreak failed too many times, not doublemining for a little bit"
     */
    public AutoBreak()
    {
        super("AutoBreak", Category.Misc);
        INSTANCE = this;
    }

    long TIMEOUT = 500L;
    public BlockPos selectedPos;
    Timer unbreakCooldown = new Timer();
    Timer unbreakSecondCooldown = new Timer();
    Timer packetBreakCooldown = new Timer();

    public BreakData normalData = null;
    public BreakData packetData = null;
    public boolean didAction = false;
    boolean switchBack = false;
    int slotToSwitchBack;
    public AutoCity cityHelper = new AutoCity();
    Pair.BreakPair packetQueue = null;
    Pair.BreakPair mainQueue = null;
    Timer cooldown = new Timer();
    boolean longSwitchBack = false;
    public Timer mineDownTimer = new Timer();
    boolean doBreak = false;

    @SubscribeEvent(Priority.MODULE_FIRST)
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (PlayerUtils.runningPhysics) return;

        if (RenderTimer.getTickLength() > 1.1f) return;

        mineDownTimer.setDelay(250);

        cooldown.setDelay(150L);
        unbreakCooldown.setDelay(150);
        packetBreakCooldown.setDelay(150);

        unbreakSecondCooldown.setDelay(150);
        if (switchBack)
        {
            if (longSwitchBack)
            {
                longSwitchBack = false;
                return;
            }

            switchBack = false;
            switch (swapMode.getValue())
            {
                case "6b6t", "Silent":
                    InventoryUtils.switchToSlot(slotToSwitchBack);
                    break;
                case "Alter":
                    doNoSlow(true);
                    InventoryUtils.switchToBypass(slotToSwitchBack, true);
                    doNoSlow(false);
                    break;
            }
        }
        tickData();

        if (autoCity.getValue())
            cityHelper.calcAutoCity();

        if (packetQueue != null)
        {
            startPacketMining(packetQueue.key(), packetQueue.value(), true);
            packetQueue = null;
        } else if (mainQueue != null)
        {
            if (isMining()) abort(normalData);


            startNormalMining(mainQueue.key(), mainQueue.value(), true);
            if (mainQueue.reset)
                normalData.resetOverride();
            mainQueue = null;
        }

        if (selectedPos != null && !isMining() && (!remineMode.getValue().equals("None")) && !remineMode.getValue().equals("Insta") && !(BlockUtils.getBlockState(selectedPos).getBlock() instanceof AirBlock) && cooldown.isPassed())
        {
            if (mc.player.getPos().squaredDistanceTo(selectedPos.getX() + 0.5f, selectedPos.getY() + 0.5f, selectedPos.getZ() + 0.5f) <= MathUtil.square(breakRange.getValue().floatValue()))
            {
                Direction minableSide = BlockUtils.getMineableSide(selectedPos, strictDirection.getValue());

                if (minableSide != null)
                {
                    cooldown.resetDelay();
                    startNormalMining(selectedPos, minableSide, false);
                }
            } else
            {
                selectedPos = null;
            }
        }
    }


    @SubscribeEvent
    public void onBreakBlock(LivingEvent.AttackBlock event)
    {
        if (NullUtils.nullCheck()) return;


        /* unbreakable check */
        if (mc.player.isCreative()) return;

        event.setCancelled(true);


        /* unbreak */
        if (!canMine(event.getPos()))
        {
            if (isMining(event.getPos()) && unbreak.getValue() && unbreakSecondCooldown.isPassed())
            {

                abort(normalData);
                selectedPos = null;
                unbreakCooldown.resetDelay();
            }
            return;
        }
        if (!unbreakCooldown.isPassed() && unbreak.getValue()) return;

        if (swing.getValue())
            mc.player.swingHand(Hand.MAIN_HAND);
        doManualMine(event.getPos(), event.getDirection());
        unbreakSecondCooldown.resetDelay();
    }


    @SubscribeEvent
    public void afterClientTick(TickEvent.AfterClientTickEvent event)
    {


        if (NullUtils.nullCheck()) return;

        if (PlayerUtils.runningPhysics) return;

        if (RenderTimer.getTickLength() > 1.1f) return;


        if (doBreak)
        {
            tryBreak();
        }
        doBreak = false;

        didAction = false;


    }


    public void tickData()
    {
        if (isMining())
        {
            normalData.tick();

            if (normalData.timeout())
                abort(normalData);


            if (normalData.isReady(false) && canDoBreak(normalData))
                doNormalBreak();
        }

        if (isPacketMining())
        {
            packetData.tick();

            if (packetData.timeout())
                packetData.reset();


            if (packetData.isReady(true) && canDoBreak(packetData))
                doPacketBreak();
        }
    }


    public boolean canDoBreak(BreakData data)
    {
        if (data == packetData || (remineMode.getValue().equals("None") || remineMode.getValue().equals("Normal") || remineMode.getValue().equals("Fast")) || data.resetOverride)
        {

            if (data == packetData && data.attempts > 0)
            {
                if (!packetBreakCooldown.isPassed())
                    return false;
            }
            if (data.isAir())
            {
                data.reset();
                if (data == normalData)
                    abort(normalData);
                return false;
            }


        } else if (data == normalData)
        {
            if (normalData.attempts < 1 && isBlockDelayGrim())
                return false;
            if (data.isAir() && remineMode.getValue().equals("Insta") && alwaysInstant.getValue() && instantTimer.isPassed())
            {
                instantTimer.resetDelay();
                return true;
            }
            return !data.isAir();
        }
        return true;
    }

    public void doNormalBreak()
    {
        if (mc.player.isUsingItem() && pause.getValue().equals("Eat")) return;


        BlockPos pos = normalData.getPos();


        Direction direction = BlockUtils.getMineableSide(pos, strictDirection.getValue());

        if (direction == null)
        {
            abort(normalData);
            return;
        }

        if (BlockUtils.distanceTo(pos, direction) > breakRange.getValue().floatValue())
        {
            abort(normalData);
            return;
        }

        if (BlockUtils.placeTrace(pos))
        {
            if (BlockUtils.distanceTo(pos, direction) > walls.getValue().doubleValue())
            {
                abort(normalData);
                return;
            }
        }

        int slot = normalData.getBestSlot();


        boolean doSwap = slot != -1 && RotationManager.INSTANCE.serverSlot != slot;


        if (doSwap)
        {
            if (swapMode.getValue().equals("None"))
            {
                abort(normalData);
                return;
            }
        }

        if (rotate.getValue())
            RotationUtils.doRotate(pos, direction);


        if (!mode.getValue().equals("Double"))
        {
            didAction = true;
            doBreak = true;
        } else
        {
            tryBreak();

        }
    }


    public void tryBreak()
    {

        BlockPos pos = normalData.getPos();


        Direction direction = BlockUtils.getMineableSide(pos, strictDirection.getValue());

        int slot = normalData.getBestSlot();
        int oldSlot = mc.player.getInventory().selectedSlot;


        boolean doSwap = slot != -1 && RotationManager.INSTANCE.serverSlot != slot;


        if (doSwap)
        {
            switch (swapMode.getValue())
            {
                case "None":
                    abort(normalData);
                    return;
                case "Silent":

                    if (!mode.getValue().equals("Double"))
                        didAction = true;
                    InventoryUtils.switchToSlot(slot);
                    break;
                case "Alter":

                    boolean send = doNoSlow(true);
                    InventoryUtils.switchToBypass(InventoryUtils.hotbarToInventory(slot), false);

                    if (send)
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));

                    if (!mode.getValue().equals("Double"))
                        didAction = true;
                    break;
                case "6b6t":

                    if (!mode.getValue().equals("Double"))
                        didAction = true;

                    slotToSwitchBack = oldSlot;
                    InventoryUtils.switchToSlot(slot);
                    switchBack = true;
                    break;
            }
        }

        normalData.markAttempt();

        List<BlockPos> surroundPos = ProtectionUtils.getSurroundEntities(mc.player, true);

        for (BlockPos surround : surroundPos)
        {
            if (surround.down().equals(normalData.getPos()))
            {
                mineDownTimer.resetDelay();
            }

        }

//        if (!mode.getValue().equals("Double"))

//        if (!mode.getValue().equals("Double"))
        if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
        PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction, id));
        lastBreak = System.currentTimeMillis();

//        if (!mode.getValue().equals("Double"))
        if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);

        PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction, id));

        if (doSwap)
        {
            switch (swapMode.getValue())
            {
                case "Silent":
                    InventoryUtils.switchToSlot(oldSlot);
                    break;
                case "Alter":
                    InventoryUtils.switchToBypass(InventoryUtils.hotbarToInventory(slot), true);
                    doNoSlow(false);
                    break;
            }
        }
        if (remineMode.getValue().equals("Insta") || normalData.resetOverride)
        {
            return;
        }
        normalData.reset();

        /* ncp fast rebreak */
        if (remineMode.getValue().equals("Fast"))
        {
            startNormalMining(pos, direction, false);
        }
    }

    public void doPacketBreak()
    {

        if (mc.player.isUsingItem() && pause.getValue().equals("Eat")) return;

        BlockPos pos = packetData.getPos();


        if (BlockUtils.distanceTo(pos, packetData.getDirection()) > breakRange.getValue().floatValue())
        {
            packetData.reset();
            return;
        }


        int slot = packetData.getBestSlot();
        int oldSlot = mc.player.getInventory().selectedSlot;


        boolean doSwap = slot != -1 && RotationManager.INSTANCE.serverSlot != slot;


        if (doSwap)
        {
            switch (swapMode.getValue())
            {
                case "None":
                    packetData.reset();
                    return;
                case "Silent", "6b6t":
                    didAction = true;
                    InventoryUtils.switchToSlot(slot);
                    slotToSwitchBack = oldSlot;
                    if (swapMode.getValue().equals("6b6t")) longSwitchBack = true;


                    break;
                case "Alter":
                    doNoSlow(true);
                    InventoryUtils.switchToBypass(InventoryUtils.hotbarToInventory(slot), false);

                    doNoSlow(false);
                    didAction = true;
                    slotToSwitchBack = InventoryUtils.hotbarToInventory(slot);
                    break;
            }
            switchBack = true;
        }

        List<BlockPos> surroundPos = ProtectionUtils.getSurroundEntities(mc.player, true);

        for (BlockPos surround : surroundPos)
        {
            if (surround.down().equals(packetData.getPos()))
            {
                mineDownTimer.resetDelay();
            }

        }

        packetData.markAttempt();
        packetBreakCooldown.resetDelay();
        if (packetData.attempts > 2)
        {
            packetData.reset();
        }


    }

    boolean doNoSlow(boolean pre)
    {


        if (PlayerUtils.isMoving() && NoSlow.INSTANCE.isEnabled() && NoSlow.INSTANCE.guiMove.getValue() && NoSlow.INSTANCE.mode.getValue().equals("Strict"))
        {
            if (pre)
            {
                NoSlow.INSTANCE.cancelDisabler = true;
                return NoSlow.INSTANCE.doStrictPre();
            } else
            {
                NoSlow.INSTANCE.doStrictPost();
                NoSlow.INSTANCE.cancelDisabler = false;
            }
        }
        return false;
    }

    public void doManualMine(BlockPos pos, Direction direction)
    {

        switch (mode.getValue())
        {
            case "Normal":
                if (isMining())
                    abort(normalData);

                startNormalMining(pos, direction, false);
                break;
            case "Double":
                if (logic.getValue().equals("Always"))
                {
                    /* we are mining nothing so we can start the packetmine */
                    if (canPacketMine())
                    {
                        startPacketMining(pos, direction, false);
                        return;
                    }
                }
                if (isMining())
                    abort(normalData);


                if ((!logic.getValue().equals("Only") || mc.world.getBlockState(pos).getBlock() != Blocks.ENDER_CHEST) && canPacketMine())
                    startPacketMining(pos, direction, false);
                else
                    startNormalMining(pos, direction, false);
                break;

        }
        selectedPos = pos;
    }

    boolean sendNextTick = false;

    /**
     * i love cats! <3
     */
    public void sendPackets(BlockPos pos, Direction direction, boolean stop)
    {

        switch (mode.getValue())
        {
            case "Double":


//                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction, id));
//                    if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
//                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction, id));
//                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction, id));
//                    if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
//                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction, id));
//                    if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
//                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction, id));
//                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction, id));
//                    if (stop)
//                    {
//                        if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
//                        PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction, id));
//                        PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction, id));
//                    }


                if (builders.getValue())
                {
//                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
//                            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction));
//                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
//                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
//                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
//                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction));
//                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
//                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
//                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
//                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction));
//                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
//                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));

                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction, id));
                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction, id));
                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction, id));
                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction, id));
                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket
                            (PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction, id));

                    if (swing.getValue())
                    {
                        PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }
                } else
                {
                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction));
                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction));
                    PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
                    if (swing.getValue())
                    {
                        PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }
                }
                break;
            case "Normal":
                PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction, id));
                if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);

                if (builders.getValue())
                {
                    if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
                    PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction, id));

                }
                break;
        }
    }

    public boolean onSuccess(BreakData data)
    {
        if (data == normalData)
        {
            if (remineMode.getValue().equals("Fast"))
            {
                if (data.attempts < 1) return true;
            }
            if (!remineMode.getValue().equals("Insta") || data.resetOverride)
            {
                data.reset();
                return false;
            }
        } else if (data == packetData)
        {
            data.reset();
            return false;
        }
        return true;
    }


    public void startPacketMining(BlockPos pos, Direction direction, boolean queued)
    {
        if (packetData == null)
        {
            packetData = new BreakData(pos, direction);
            packetData.start();
        } else
        {
            packetData.start(pos, direction);
        }
        if (queued)
            packetData.queued = true;

        if (rotate.getValue())
            RotationUtils.doRotate(pos, direction);
        sendPackets(pos, direction, true);

    }

    public void startNormalMining(BlockPos pos, Direction direction, boolean queued)
    {

        didAction = true;
        if (normalData == null)
        {
            normalData = new BreakData(pos, direction);
            normalData.start();
        } else
        {
            normalData.start(pos, direction);
        }

        if (queued)
            normalData.queued = true;
        if (rotate.getValue())
            RotationUtils.doRotate(pos, direction);


        sendPackets(pos, direction, false);
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;

        normalData = null;
    }


    @Override
    public void onDisable()
    {
        super.onDisable();
        if (NullUtils.nullCheck()) return;


        if (normalData != null && normalData.isMining())
        {
            abort(normalData);
        }
    }


    public void abort(BreakData data)
    {
        PacketManager.INSTANCE.sendPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        data.reset();
    }

    public boolean isMining()
    {
        return normalData != null && normalData.isMining();
    }


    public boolean isPacketMining()
    {
        return packetData != null && packetData.isMining();
    }

    public boolean isPacketMining(BlockPos pos)
    {
        return packetData != null && packetData.isMining(pos);
    }

    public boolean isMining(BlockPos pos)
    {
        return normalData != null && normalData.isMining(pos);
    }


    @SubscribeEvent(Priority.SUPER_FIRST)
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet)
        {
            if (packet.getSelectedSlot() == RotationManager.INSTANCE.serverSlot) return;


            if (strictDirection.getValue() && swapMode.getValue().equals("Alter"))
            {

                if (isMining())
                    abort(normalData);
            }
        }
    }

    @SubscribeEvent(Priority.SUPER_FIRST)
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;


        if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
        {
            if (isAnyMining(packet.getPos()))
            {
                if (isPacketMining(packet.getPos()))
                {
                    packetData.updateBlock(packet.getState());
                } else
                {
                    normalData.updateBlock(packet.getState());
                }
            }
        }
    }


    public boolean canMine(BlockPos pos)
    {
        if (isPacketMining(pos) || isMining(pos))
            return false;

        if (mainQueue != null)
        {
            if (mainQueue.key().equals(pos)) return false;
        }


        if (packetQueue != null)
        {
            if (packetQueue.key().equals(pos)) return false;
        }

        return BlockUtils.isMineable(mc.world.getBlockState(pos));
    }


    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (normalData != null && normalData.isMining() && (!normalData.isAir()))
            renderData(normalData, false);

        if (mode.getValue().equals("Double") && packetData != null && packetData.isMining())
            renderData(packetData, true);
    }


    public void renderData(BreakData data, boolean packet)
    {
        float maxBreak = packet ? 1.0f : breakAt.getValue().floatValue();


        float color = maxBreak - 0.3f;
        BlockPos mining = data.getPos();
        VoxelShape outlineShape = VoxelShapes.fullCube();
        outlineShape = BlockUtils.getBlockState(data.getPos()).getOutlineShape(mc.world, mining);
        outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;

        Box render1 = outlineShape.getBoundingBox();
        Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
        Vec3d center = render.getCenter();
        float scale = MathHelper.clamp(MathHelper.lerp(mc.getRenderTickCounter().getTickDelta(false), data.getLastBestDamage() / maxBreak, data.getBestDamage() / maxBreak), 0, 1.0f);
        double dx = (render1.maxX - render1.minX) / 2.0;
        double dy = (render1.maxY - render1.minY) / 2.0;
        double dz = (render1.maxZ - render1.minZ) / 2.0;
        final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);

        Color fillColorInterp = fillColor.getValue().getColor();
        Color lineColorInterp = lineColor.getValue().getColor();
        if (data.getBestDamage() > color)
        {


            float progress = (float) MathHelper.clamp(MathUtil.normalize((MathHelper.lerp(mc.getRenderTickCounter().getTickDelta(false), data.getLastBestDamage(), data.getBestDamage())) - color, 0, 0.3), 0, 1);
            fillColorInterp = ColorUtil.interpolate(progress, fillColor2.getValue().getColor(), fillColorInterp);
            lineColorInterp = ColorUtil.interpolate(progress, lineColor2.getValue().getColor(), lineColorInterp);
        }
        RenderUtil.renderBox(
                RenderType.FILL,
                scaled,
                fillColorInterp,
                fillColorInterp
        );
        RenderUtil.renderBox(
                RenderType.LINES,
                scaled,
                lineColorInterp,
                lineColorInterp
        );
    }

    public boolean canPacketMine()
    {
        return mode.getValue().equals("Double") && !isPacketMining() && !isMining();
    }

    public boolean isMineAvailable()
    {
        if (mode.getValue().equals("Double"))
            return (!isMining() || !isPacketMining()) && (packetQueue == null || mainQueue == null);

        return (!isMining() && (mainQueue == null));
    }

    public boolean isInstantMining(BlockPos pos)
    {
        if (!isMining(pos)) return false;

        return normalData.getBestDamage() > breakAt.getValue().floatValue() - 0.1f && remineMode.getValue().equals("Insta");
    }

    public void queue(BlockPos pos, boolean reset)
    {
        if (!BlockUtils.isMineable(mc.world.getBlockState(pos))) return;

        Direction direction = BlockUtils.getMineableSide(pos, strictDirection.getValue());
        if (direction == null) return;


        if (canPacketMine() && packetQueue == null)
        {

            if (rotate.getValue())
                RotationUtils.doRotate(pos, direction);

            packetQueue = new Pair.BreakPair(pos, direction);
        } else if (!isMining() && mainQueue == null)
        {

            if (rotate.getValue())
                RotationUtils.doRotate(pos, direction);

            mainQueue = new Pair.BreakPair(pos, direction, reset);
        }
    }


    public boolean isAboutToBreak(BlockPos pos)
    {
        if (!this.isEnabled()) return false;

        if (!isMining(pos) && !isPacketMining(pos)) return false;


        return isMining(pos) ? normalData.getBestDamage() > breakAt.getValue().floatValue() - 0.1f : packetData.getBestDamage() > 0.9f;
    }

    public void wipe()
    {
        if (normalData != null)
            normalData.reset();

        if (packetData != null)
            packetData.reset();
    }

    public boolean isAnyMining(BlockPos pos)
    {
        return isMining(pos) || isPacketMining(pos);
    }


    public boolean isAnyMining(ArrayList<BlockPos> list)
    {

        for (BlockPos pos : list)
        {
            if (isAnyMining(pos)) return true;
        }
        return false;
    }


    public void forceQueue(BlockPos pos, boolean reset)
    {

        if (!BlockUtils.isMineable(mc.world.getBlockState(pos))) return;


        Direction direction = BlockUtils.getMineableSide(pos, strictDirection.getValue());
        if (direction == null) return;

        if (canPacketMine() && packetQueue == null)
        {
            if (rotate.getValue())
                RotationUtils.doRotate(pos, direction);
            packetQueue = new Pair.BreakPair(pos, direction, false);
        } else if (!isMining(pos) && mainQueue == null)
        {

            if (rotate.getValue())
                RotationUtils.doRotate(pos, direction);

            mainQueue = new Pair.BreakPair(pos, direction, false);
        }
    }


    @Override
    public String getHudInfo()
    {
        if (NullUtils.nullCheck()) return "";


        return MathUtil.round(getBestDamage(), 1) + "";
    }

    public double getBestDamage()
    {

        double damage = 0.0;
        if (mode.getValue().equals("Double") && isPacketMining())
            damage = packetData.getBestDamage();

        if (isMining() && normalData.getBestDamage() > damage)
            damage = normalData.getBestDamage();

        return Math.min(damage, 1);
    }

    public boolean isBlockDelayGrim()
    {
        return System.currentTimeMillis() - lastBreak <= 280 && inhibit.getValue();
    }

    public double getPacketMineDmg()
    {
        double damage = 0.0;

        if (mode.getValue().equals("Double") && isPacketMining())
        {
            damage = packetData.getBestDamage();
        } else if (isMining())
        {
            damage = normalData.getBestDamage();
        }
        return Math.min(damage, 1);
    }

    public boolean isBothAvailable()
    {
        return (!isMining() && !isPacketMining()) && (packetQueue == null && mainQueue == null);
    }

    @Override
    public String getDescription()
    {
        return "AutoBreak: Auto breaks blocks and stuff";
    }

    public void handlePage(String page)
    {
        // "Breaking", "AutoCity", "Render"

        // Breaking
        breakAt.setActive(page.equals("Breaking"));

        mode.setActive(page.equals("Breaking"));
        logic.setActive(page.equals("Breaking") && mode.getValue().equals("Double"));
        remineMode.setActive(page.equals("Breaking"));

        alwaysInstant.setActive(page.equals("Breaking") && remineMode.getValue().equals("Insta"));
        instantDelay.setActive(page.equals("Breaking") && remineMode.getValue().equals("Insta") && alwaysInstant.getValue());

        swing.setActive(page.equals("Breaking"));

        rotate.setActive(page.equals("Breaking"));
        builders.setActive(page.equals("Breaking"));

        strictDirection.setActive(page.equals("Breaking"));
        airCheck.setActive(page.equals("Breaking"));
        inhibit.setActive(page.equals("Breaking"));

        unbreak.setActive(page.equals("Breaking"));

        breakRange.setActive(page.equals("Breaking"));
        walls.setActive(page.equals("Breaking"));

        swapMode.setActive(page.equals("Breaking"));
        pause.setActive(page.equals("Breaking"));

        // AutoCity & Targetting
        autoCity.setActive(page.equals("AutoCity"));

        targetRange.setActive(autoCity.getValue() && page.equals("AutoCity"));
        antiCrawl.setActive(autoCity.getValue() && page.equals("AutoCity"));
        preMine.setActive(autoCity.getValue() && antiCrawl.getValue() && page.equals("AutoCity"));

        selfMine.setActive(autoCity.getValue() && page.equals("AutoCity"));
        headCrystal.setActive(page.equals("AutoCity"));

        // Renders
        fillColor.setActive(page.equals("Render"));
        lineColor.setActive(page.equals("Render"));
        fillColor2.setActive(page.equals("Render"));
        lineColor2.setActive(page.equals("Render"));

    }

}
