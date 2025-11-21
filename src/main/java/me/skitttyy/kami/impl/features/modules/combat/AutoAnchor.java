package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.PriorityManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.CrystalUtil;
import me.skitttyy.kami.api.utils.world.ca.PlaceResult;
import me.skitttyy.kami.api.utils.world.ca.Result;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import me.skitttyy.kami.impl.features.modules.player.Blink;
import me.skitttyy.kami.impl.features.modules.player.Phase;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AutoAnchor extends Module
{
    public static AutoAnchor INSTANCE;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    Value<String> page = new ValueBuilder<String>()
            .withDescriptor("Page")
            .withValue("Render")
            .withModes("Render", "Calc", "Action", "Misc", "Timing")
            .withAction(s -> handlePage(s.getValue()))
            .register(this);
    Value<String> targetSorting = new ValueBuilder<String>()
            .withDescriptor("Sort")
            .withValue("Damage")
            .withModes("Damage", "Range")
            .register(this);
    Value<Number> targetRange = new ValueBuilder<Number>()
            .withDescriptor("Target Range")
            .withValue(7d)
            .withRange(3d, 20d)
            .register(this);
    Value<Number> placeRange = new ValueBuilder<Number>()
            .withDescriptor("Place Range")
            .withValue(6)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    Value<Number> placeWallsRange = new ValueBuilder<Number>()
            .withDescriptor("Place Walls Range")
            .withValue(3d)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    Value<Number> placeDelay = new ValueBuilder<Number>()
            .withDescriptor("Place Delay")
            .withValue(1)
            .withRange(0, 1000)
            .register(this);
    public Value<Number> minDmg = new ValueBuilder<Number>()
            .withDescriptor("Min Damage")
            .withValue(2)
            .withRange(0, 36)
            .register(this);
    Value<Number> maxSelfDmg = new ValueBuilder<Number>()
            .withDescriptor("Max Self Damage")
            .withValue(4)
            .withRange(0, 36)
            .register(this);
    Value<Boolean> noSuicide = new ValueBuilder<Boolean>()
            .withDescriptor("Anti Suicide")
            .withValue(false)
            .register(this);
    public Value<Number> lethalCrystals = new ValueBuilder<Number>()
            .withDescriptor("Lethal Crystals")
            .withValue(0)
            .withRange(0, 5)
            .withPlaces(0)
            .register(this);

    Value<String> multiTask = new ValueBuilder<String>()
            .withDescriptor("MultiTask")
            .withValue("None")
            .withModes("None", "Soft", "Strong")
            .register(this);
    Value<String> miningIgnore = new ValueBuilder<String>()
            .withDescriptor("Mining")
            .withValue("None")
            .withModes("None", "Ignore", "StrictIgnore")
            .register(this);
    Value<Boolean> terrain = new ValueBuilder<Boolean>()
            .withDescriptor("Terrain")
            .withValue(false)
            .register(this);
    public Value<Boolean> armorAssume = new ValueBuilder<Boolean>()
            .withDescriptor("Assume")
            .withValue(false)
            .register(this);
    Value<String> autoSwitch = new ValueBuilder<String>()
            .withDescriptor("Auto Switch")
            .withValue("None")
            .withModes("None", "Normal", "Silent", "SilentBypass")
            .withAction(s -> handleSubsettingAutoSwitch(s.getValue()))
            .register(this);
    Value<Boolean> noGapSwitch = new ValueBuilder<Boolean>()
            .withDescriptor("Pause")
            .withValue(false)
            .register(this);
    Value<Number> explodeDelay = new ValueBuilder<Number>()
            .withDescriptor("Explode Delay")
            .withValue(1)
            .withRange(0, 1000)
            .register(this);
    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict Direction")
            .withValue(false)
            .register(this);
    Value<String> swapWait = new ValueBuilder<String>()
            .withDescriptor("Swap Delay")
            .withValue("None")
            .withModes("None", "Semi", "Full")
            .register(this);
    Value<Number> swapWaitDelay = new ValueBuilder<Number>()
            .withDescriptor("Swap Wait Delay")
            .withValue(300)
            .withRange(50, 500)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(false)
            .register(this);
    Value<String> rotationsType = new ValueBuilder<String>()
            .withDescriptor("Type", "rotationsType")
            .withValue("Simple")
            .withModes("Simple", "NCP", "GrimAbuse")
            .register(this);
    Value<String> timing = new ValueBuilder<String>()
            .withDescriptor("Timing")
            .withValue("Soft")
            .withModes("Soft", "Strict")
            .register(this);

    public Value<Sn0wColor> fillColorS = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill Color")
            .withValue(new Sn0wColor(0, 0, 0, 100))
            .register(this);
    public Value<Sn0wColor> lineColorS = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Outline Color")
            .withValue(new Sn0wColor(255, 255, 255, 255))
            .register(this);
    public Value<Boolean> renderDamage = new ValueBuilder<Boolean>()
            .withDescriptor("Damage Text")
            .withValue(false)
            .register(this);
    public Value<Number> textScale = new ValueBuilder<Number>()
            .withDescriptor("Text Scale")
            .withValue(1.4)
            .withRange(1, 2)
            .register(this);
    public Value<String> renderMode = new ValueBuilder<String>()
            .withDescriptor("Type")
            .withValue("Normal")
            .withModes("Normal", "Fade")
            .register(this);
    public Value<Number> fadeTime = new ValueBuilder<Number>()
            .withDescriptor("Fade Time")
            .withValue(1000)
            .withRange(100, 2000)
            .withPlaces(0)
            .register(this);
    public Value<Boolean> futureFade = new ValueBuilder<Boolean>()
            .withDescriptor("Future")
            .withValue(false)
            .register(this);

    public BlockPos renderPos;
    public BlockPos calcPos;
    float[] placeTargetRot = null;
    float[] explodeTargetRot = null;

    public PlayerEntity target;
    Timer placeTimer = new Timer();
    Timer explodeTimer = new Timer();

    public BlockPos explodePos;
    public double renderDMG;
    public static List<Pair.BlockPairTime> oldPlacements = new ArrayList<>();


    public AutoAnchor()
    {
        super("AutoAnchor", Category.Combat);
        INSTANCE = this;
    }

    @SubscribeEvent(Priority.MODULE_LAST)
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        if (Phase.INSTANCE.isEnabled()) return;


        if (PriorityManager.INSTANCE.isUsageLocked()) return;


        if (Blink.INSTANCE.isEnabled()) return;


        if (AutoBreak.INSTANCE.didAction) return;



        if(mc.world.getRegistryKey() == World.NETHER) return;


        prepare();


        if (timing.getValue().equals("Soft"))
            interact();
    }

    @SubscribeEvent(Priority.MODULE_LAST)
    public void onPlayerUpdate(TickEvent.MovementTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;

        if (Phase.INSTANCE.isEnabled()) return;


        if (PriorityManager.INSTANCE.isUsageLocked()) return;


        if (Blink.INSTANCE.isEnabled()) return;

        if (AutoBreak.INSTANCE.didAction) return;

        if(mc.world.getRegistryKey() == World.NETHER) return;

        if (timing.getValue().equals("Strict"))
            interact();
    }

    public void interact()
    {

        placeAnchor();
        explodeAnchor();

        if (explodePos == null && calcPos == null)
        {
            renderPos = null;
        } else if (explodePos != null && calcPos == null)
        {
            renderPos = explodePos;
        }
    }

    public boolean canRender()
    {
        if (calcPos == null) return false;


        int anchorSlot = InventoryUtils.getHotbarItemSlot(Items.RESPAWN_ANCHOR);
        return anchorSlot != -1;
    }

    public PlaceResult getBestPlaceResult(PlayerEntity targetPlayer)
    {

        BlockPos bestPos = null;
        final List<BlockPos> sphere = BlockUtils.sphere(placeRange.getValue().doubleValue() + 1.0f, mc.player.getBlockPos(), true, false);
        double bestDMG = 0.5D;

        for (final BlockPos pos : sphere)
        {

            if (!BlockUtils.canPlaceBlock(pos) && !(mc.world.getBlockState(pos).getBlock() instanceof RespawnAnchorBlock))
                continue;

            Direction side = BlockUtils.getPlaceableSide(pos, strictDirection.getValue());
            if (side == null)
                continue;

            Vec3d vec = pos.toCenterPos().add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);

            double distanceSq = vec.squaredDistanceTo(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            if (distanceSq > MathUtil.square(placeRange.getValue().doubleValue()))
                continue;

            boolean trace = BlockUtils.placeTrace(pos);
            if (trace && distanceSq > MathUtil.square(placeWallsRange.getValue().doubleValue()))
            {
                continue;
            }


            final float targetDamage = CrystalUtil.calculateDamage(targetPlayer, pos.toCenterPos(), terrain.getValue(), false, pos);
            if (bestDMG < targetDamage && targetDamage > minDmg.getValue().doubleValue())
            {
                final float selfDamage = CrystalUtil.calculateDamage(mc.player, pos.toCenterPos(), terrain.getValue(), false, pos);

                if (selfDamage > maxSelfDmg.getValue().doubleValue()) continue;

                if ((selfDamage + 2 > mc.player.getHealth() + mc.player.getAbsorptionAmount() && noSuicide.getValue()))
                    continue;

                bestDMG = targetDamage;
                bestPos = pos;
            }
        }
        if (bestDMG == 0.5D)
        {
            bestDMG = 0;
        }
        return new PlaceResult(targetPlayer, bestDMG, bestPos, getPlaceRot(bestPos));
    }

    public void placeAnchor()
    {


        if (calcPos != null)
        {

            if (rotate.getValue() && placeTargetRot == null) return;


            if (placeTimer.isPassed())
            {

                boolean doSilent = false;
                int anchorSlot = InventoryUtils.getHotbarItemSlot(Items.RESPAWN_ANCHOR);

                if (anchorSlot == -1)
                {
                    renderPos = null;
                    return;
                }

                if (BlockUtils.getBlockState(calcPos).getBlock() instanceof RespawnAnchorBlock) return;

                if (autoSwitch.getValue().equals("Normal"))
                {
                    if (noGapSwitch.getValue())
                    {
                        if (!PlayerUtils.isEatingGap())
                        {
                            InventoryUtils.switchToSlot(anchorSlot);
                        }
                    } else
                    {
                        InventoryUtils.switchToSlot(anchorSlot);
                    }
                }

                if ((autoSwitch.getValue().equals("Silent") || autoSwitch.getValue().equals("SilentBypass")))
                {
                    if (!(mc.player.getInventory().getMainHandStack().getItem().equals(Items.RESPAWN_ANCHOR)))
                        doSilent = true;
                }


                if (!autoSwitch.getValue().contains("Silent"))
                {
                    if (!(mc.player.getInventory().getMainHandStack().getItem().equals(Items.RESPAWN_ANCHOR)))
                    {
                        renderPos = null;
                        return;
                    }
                }

                int oldSlot = mc.player.getInventory().selectedSlot;


                if (doSilent)
                {
                    if (autoSwitch.getValue().equals("SilentBypass"))
                    {
                        InventoryUtils.switchToBypass(InventoryUtils.hotbarToInventory(anchorSlot), false);
                    } else
                    {
                        InventoryUtils.switchToSlot(anchorSlot);
                    }
                }

                Direction side = BlockUtils.getPlaceableSide(calcPos, strictDirection.getValue());

                if (side != null)
                {

                    BlockUtils.placeBlock(calcPos, side, false);

                    if (doSilent)
                    {
                        if (autoSwitch.getValue().equals("SilentBypass"))
                        {
                            InventoryUtils.switchToBypass(InventoryUtils.hotbarToInventory(anchorSlot), true);
                        } else
                        {
                            InventoryUtils.switchToSlot(oldSlot);
                        }
                    }

                    AntiCheat.INSTANCE.handleMultiTask();
                    placeTimer.resetDelay();
                }

            }
            renderPos = calcPos;
            if (renderPos != null && renderMode.getValue().equals("Fade"))
            {
                oldPlacements.removeIf(pair -> pair.key().equals(renderPos));
                oldPlacements.add(new Pair.BlockPairTime(renderPos));
            }
        } else
        {
        }
    }


    public void explodeAnchor()
    {


        if (explodePos != null)
        {

            if (rotate.getValue() && explodeTargetRot == null) return;


            if (explodeTimer.isPassed())
            {

                boolean doSilent = false;


                BlockState state = BlockUtils.getBlockState(explodePos);


                if (state == null || !(state.getBlock() instanceof RespawnAnchorBlock)) return;


                int slot = -1;
                boolean charge = state.get(RespawnAnchorBlock.CHARGES) == 0;
                if (charge)
                {
                    slot = InventoryUtils.getHotbarItemSlot(Items.GLOWSTONE);
                } else
                {
                    int totemSlot = InventoryUtils.getHotbarItemSlot(Items.TOTEM_OF_UNDYING);

                    if (totemSlot != -1)
                    {
                        slot = totemSlot;
                    } else
                    {
                        slot = InventoryUtils.getNonBlockInHotbar();
                    }
                }


                if (slot == -1)
                    return;

                if (autoSwitch.getValue().equals("Normal"))
                {
                    if (noGapSwitch.getValue())
                    {
                        if (!PlayerUtils.isEatingGap())
                        {
                            InventoryUtils.switchToSlot(slot);
                        }
                    } else
                    {
                        InventoryUtils.switchToSlot(slot);
                    }
                }


                if (charge)
                {
                    if ((autoSwitch.getValue().equals("Silent") || autoSwitch.getValue().equals("SilentBypass")))
                    {
                        if (!(mc.player.getInventory().getMainHandStack().getItem().equals(Items.GLOWSTONE)))
                            doSilent = true;
                    }


                    if (!autoSwitch.getValue().contains("Silent"))
                    {
                        if (!(mc.player.getInventory().getMainHandStack().getItem().equals(Items.GLOWSTONE)))
                        {
                            return;
                        }
                    }
                }

                int oldSlot = mc.player.getInventory().selectedSlot;


                if (doSilent)
                {
                    if (autoSwitch.getValue().equals("SilentBypass"))
                    {
                        InventoryUtils.switchToBypass(InventoryUtils.hotbarToInventory(slot), false);
                    } else
                    {
                        InventoryUtils.switchToSlot(slot);
                    }
                }


                Direction side = BlockUtils.getPlaceableSideCrystal(explodePos, strictDirection.getValue());

                if (side != null)
                {
                    Vec3d vec = explodePos.toCenterPos().add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);

                    BlockHitResult result = new BlockHitResult(vec, side, explodePos, false);

                    PacketManager.INSTANCE.sendPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, id));
                    mc.player.swingHand(Hand.MAIN_HAND);
                    if (doSilent)
                    {
                        if (autoSwitch.getValue().equals("SilentBypass"))
                        {
                            InventoryUtils.switchToBypass(InventoryUtils.hotbarToInventory(slot), true);
                        } else
                        {
                            InventoryUtils.switchToSlot(oldSlot);
                        }
                    }

                    AntiCheat.INSTANCE.handleMultiTask();

                    placeTimer.resetDelay();
                }
                renderPos = explodePos;
                if (explodePos != null && renderMode.getValue().equals("Fade"))
                {
                    oldPlacements.removeIf(pair -> pair.key().equals(renderPos));
                    oldPlacements.add(new Pair.BlockPairTime(renderPos));
                }
            }
        } else
        {
        }
    }

    public PlaceResult getBestExplodeResult(PlayerEntity targetPlayer)
    {

        BlockPos bestPos = null;
        final List<BlockPos> sphere = BlockUtils.sphere(placeRange.getValue().doubleValue() + 1.0f, mc.player.getBlockPos(), true, false);
        double bestDMG = 0.5D;

        for (final BlockPos pos : sphere)
        {


            BlockState state = BlockUtils.getBlockState(pos);

            if (state == null || !(state.getBlock() instanceof RespawnAnchorBlock)) continue;

            Direction side = BlockUtils.getPlaceableSideCrystal(pos, strictDirection.getValue());
            if (side == null)
                continue;

            Vec3d vec = pos.toCenterPos().add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);

            double distanceSq = vec.squaredDistanceTo(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            if (distanceSq > MathUtil.square(placeRange.getValue().doubleValue()))
                continue;


            boolean trace = BlockUtils.placeTrace(pos);
            if (trace && distanceSq > MathUtil.square(placeWallsRange.getValue().doubleValue()))
            {
                continue;
            }


            final float targetDamage = CrystalUtil.calculateDamage(targetPlayer, pos.toCenterPos(), terrain.getValue(), false, pos);
            if (bestDMG < targetDamage && targetDamage > minDmg.getValue().doubleValue())
            {
                final float selfDamage = CrystalUtil.calculateDamage(mc.player, pos.toCenterPos(), terrain.getValue(), false, pos);

                if (selfDamage > maxSelfDmg.getValue().doubleValue()) continue;

                if ((selfDamage + 2 > mc.player.getHealth() + mc.player.getAbsorptionAmount() && noSuicide.getValue()))
                    continue;

                bestDMG = targetDamage;
                bestPos = pos;
            }
        }
        if (bestDMG == 0.5D)
        {
            bestDMG = 0;
        }
        return new PlaceResult(targetPlayer, bestDMG, bestPos, getPlaceRot(bestPos));
    }

    public void prepare()
    {


        placeTimer.setDelay(placeDelay.getValue().longValue());
        explodeTimer.setDelay(explodeDelay.getValue().longValue());


        placeTargetRot = null;
        explodeTargetRot = null;


        Future<Result> resultFuture = executor.submit(new AutoAnchor.ResultTask());
        Result result = null;
        try
        {
            result = resultFuture.get();
        } catch (Exception e)
        {
            e.printStackTrace();
            ChatUtils.sendMessage(Formatting.RED + e.getCause().getMessage() + ": please send your log to me!!");
        }


        calcPos = null;
        explodePos = null;

        load(result);


        if (!rotate.getValue()) return;


        if (target != null)
        {
            if (PlayerUtils.isBoostedByFirework() && AntiCheat.INSTANCE.strafeFix.getValue() && !target.isFallFlying())
                return;
        }

        if (placeTargetRot != null && explodeTargetRot == null)
        {
            RotationUtils.setRotation(placeTargetRot);
        } else if (placeTargetRot == null && explodeTargetRot != null)
        {
            RotationUtils.setRotation(explodeTargetRot);
        } else if (placeTargetRot != null)
        {
            RotationUtils.setRotation(placeTargetRot);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (!renderMode.getValue().equals("Fade"))
        {
            if (target == null || renderPos == null)
            {
                return;
            }
        }

        if (!renderMode.getValue().equals("Fade"))
        {
            if (renderPos == null) return;

            if (!canRender()) return;

        }


        switch (renderMode.getValue())
        {
            case "Normal":
                Color fillColor = fillColorS.getValue().getColor();
                Color lineColor = lineColorS.getValue().getColor();
                RenderUtil.renderBox(
                        RenderType.FILL,
                        new Box(renderPos),
                        fillColor,
                        fillColor
                );
                RenderUtil.renderBox(
                        RenderType.LINES,
                        new Box(renderPos),
                        lineColor,
                        lineColor
                );
                if (renderDamage.getValue())
                    RenderBuffers.schedulePreRender(() ->
                    {
                        RenderUtil.drawText(String.format("%.1f", renderDMG), new Box(renderPos).getCenter(), textScale.getValue().floatValue());
                    });

                break;
            case "Fade":
                break;
        }
    }

    public Result getResult(PlayerEntity player)
    {
        return new Result(getBestExplodeResult(player), getBestPlaceResult(player));
    }

    public void load(Result result)
    {

        target = result.target;

        boolean offhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        if (InventoryUtils.getHotbarItemSlot(Items.END_CRYSTAL) != -1 || offhand)
        {
            if (result.calcPos != null)
            {
                calcPos = result.calcPos;
                placeTargetRot = result.placeRots;
                renderDMG = result.bestPlaceDMG;

            } else
            {
                if (result.bestPlaceDMG == 0)
                {
                    renderPos = null;
                }

            }
        } else
        {
            calcPos = null;
            renderPos = null;
            placeTargetRot = null;
            renderDMG = 0;
        }
        if (result.explodePos != null)
        {
            explodePos = result.explodePos;
            explodeTargetRot = result.breakRots;
        }

        if (result.calcPos == null && result.explodePos == null)
            renderPos = null;
    }

    public void handleSubsettingAutoSwitch(String value)
    {
        noGapSwitch.setActive(value.equals("Normal") && page.getValue().equals("Place"));
    }
    @Override
    public void onDisable()
    {
        super.onDisable();
        if (NullUtils.nullCheck()) return;


        renderPos = null;
        target = null;
        calcPos = null;
        explodePos = null;
        explodeTargetRot = null;
        placeTargetRot = null;

    }
    public float[] getPlaceRot(BlockPos pos)
    {
        if (pos == null) return null;

        if (!rotate.getValue()) return null;


        Direction side = BlockUtils.getPlaceableSide(pos, strictDirection.getValue());

        return RotationUtils.getBlockRotations(pos, side);
    }

    public Result getTargetResult()
    {
        switch (targetSorting.getValue())
        {
            case "Damage":
                List<Entity> targets = TargetUtils.getTargets(targetRange.getValue().doubleValue()).toList();
                Result bestResult = null;
                for (Entity target : targets)
                {
                    if (target == mc.player) continue;

                    Result currentResult = getResult((PlayerEntity) target);
                    if (bestResult == null)
                    {
                        bestResult = currentResult;
                        continue;
                    }
                    if (currentResult.getDamage() > bestResult.getDamage())
                    {
                        bestResult = currentResult;
                    }
                }
                if (bestResult == null)
                {
                    reset();
                    return null;
                }
                return bestResult;
            case "Range":
                target = (PlayerEntity) TargetUtils.getTarget(targetRange.getValue().doubleValue());
                if (target == null)
                {
                    reset();
                    return null;
                }
                return getResult(target);
        }
        return null;
    }

    public void reset()
    {
        explodePos = null;
        calcPos = null;
        explodeTargetRot = null;
        placeTargetRot = null;
    }

    private class ResultTask implements Callable<Result>
    {

        public ResultTask()
        {
        }

        @Override
        public Result call() throws Exception
        {
            return getTargetResult();
        }
    }

    public void handlePage(String page)
    {
        // "Render", "Calc", "Place", "Timing", "Dev"
        // Render
        fillColorS.setActive(page.equals("Render"));
        lineColorS.setActive(page.equals("Render"));
        renderDamage.setActive(page.equals("Render"));
        textScale.setActive(page.equals("Render") && renderDamage.getValue());

        renderMode.setActive(page.equals("Render"));
        fadeTime.setActive(page.equals("Render") && renderMode.getValue().equals("Fade"));
        futureFade.setActive(page.equals("Render") && renderMode.getValue().equals("Fade"));

        // Calc
        minDmg.setActive(page.equals("Calc"));
        maxSelfDmg.setActive(page.equals("Calc"));
        targetRange.setActive(page.equals("Calc"));
        targetSorting.setActive(page.equals("Calc"));

        noSuicide.setActive(page.equals("Calc"));
        lethalCrystals.setActive(page.equals("Calc"));
        multiTask.setActive(page.equals("Calc"));

        miningIgnore.setActive(page.equals("Calc"));
        terrain.setActive(page.equals("Calc"));
        armorAssume.setActive(page.equals("Calc"));


        // Place
        placeRange.setActive(page.equals("Action"));
        placeWallsRange.setActive(page.equals("Action"));

        autoSwitch.setActive(page.equals("Action"));
        noGapSwitch.setActive(page.equals("Action") && autoSwitch.getValue().equals("Normal"));
        strictDirection.setActive(page.equals("Action"));


        //misc
        rotate.setActive(page.equals("Misc"));
        rotationsType.setActive(page.equals("Misc") && rotate.getValue());


        swapWait.setActive(page.equals("Misc"));
        swapWaitDelay.setActive(page.equals("Misc") && !(swapWait.getValue().equals("None")));

        // delays
        explodeDelay.setActive(page.equals("Timing"));
        placeDelay.setActive(page.equals("Timing"));
        timing.setActive(page.equals("Timing"));
    }

    @Override
    public String getDescription()
    {
        return "AutoAnchor: Places and explodes respawn anchors to kill enemys";
    }
}
