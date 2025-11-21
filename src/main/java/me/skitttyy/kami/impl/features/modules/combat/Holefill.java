package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PriorityManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.HoleUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import me.skitttyy.kami.impl.features.modules.player.Blink;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Holefill extends Module
{

    Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(4d)
            .withRange(1d, 6)
            .withPlaces(1)

            .register(this);
    Value<Number> wallsRange = new ValueBuilder<Number>()
            .withDescriptor("WallsRange")
            .withValue(3)
            .withRange(1d, 6)
            .withPlaces(1)
            .register(this);

    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(1)
            .withRange(0, 1000)
            .register(this);
    Value<Number> blocksPerTick = new ValueBuilder<Number>()
            .withDescriptor("Blocks")
            .withValue(1)
            .withRange(1, 10)
            .register(this);
    Value<Boolean> disableAfter = new ValueBuilder<Boolean>()
            .withDescriptor("Disable")
            .withValue(true)
            .register(this);
    Value<String> rotateMode = new ValueBuilder<String>()
            .withDescriptor("Rotate")
            .withValue("None")
            .withModes("None", "Normal")
            .register(this);

    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict")
            .withValue(true)
            .register(this);
    Value<Boolean> doubles = new ValueBuilder<Boolean>()
            .withDescriptor("Doubles")
            .withValue(true)
            .register(this);
    Value<Boolean> smart = new ValueBuilder<Boolean>()
            .withDescriptor("Smart", "smart")
            .withValue(false)
            .withAction(s -> handleSmartPage(s.getValue()))
            .register(this);
    Value<Boolean> webs = new ValueBuilder<Boolean>()
            .withDescriptor("Webs")
            .withValue(false)
            .register(this);
    Value<Number> smartTargetRange = new ValueBuilder<Number>()
            .withDescriptor("Target Range", "targetRange")
            .withValue(5d)
            .withRange(1d, 10d)
            .register(this);
    // the distance of the hole from the target
    Value<Number> smartBlockRange = new ValueBuilder<Number>()
            .withDescriptor("Activate Range", "smartBlockRange")
            .withValue(1d)
            .withRange(0.3d, 4d)
            .register(this);

    Value<Boolean> renderHoles = new ValueBuilder<Boolean>()
            .withDescriptor("Render")
            .withValue(true)
            .withAction(s -> handleRender(s.getValue()))
            .register(this);
    Value<Sn0wColor> fill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill")
            .withValue(new Sn0wColor(255, 0, 0, 25))
            .register(this);
    Value<Sn0wColor> line = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line")
            .withValue(new Sn0wColor(255, 0, 0, 255))
            .register(this);
    Value<Number> fadeTime = new ValueBuilder<Number>()
            .withDescriptor("Fade Time")
            .withValue(200)
            .withRange(0, 1000)
            .register(this);

    public Holefill()
    {
        super("HoleFill", Category.Combat);
    }

    Timer timeSystem = new Timer();

    List<HoleUtils.Hole> holes = new ArrayList<>();

    Map<HoleUtils.Hole, Long> renderPositions = new HashMap<>();

    Entity target;


    List<HoleUtils.Hole> toPlace = new ArrayList<>();

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (NullUtils.nullCheck()) return;


    }

    void handleSmartPage(boolean show)
    {
        smartBlockRange.setActive(show);
        smartTargetRange.setActive(show);
    }

    void handleRender(boolean show)
    {
        fill.setActive(show);
        line.setActive(show);
        fadeTime.setActive(show);

    }


    @SubscribeEvent
    public void onUpdatePre(final TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (PriorityManager.INSTANCE.usageLock && PriorityManager.INSTANCE.usageLockCause != "HoleFill") return;


        if (AutoBreak.INSTANCE.didAction) return;


        if (Blink.INSTANCE.isEnabled()) return;

        target = TargetUtils.getTargetHolefill(smartTargetRange.getValue().doubleValue());
        timeSystem.setDelay(delay.getValue().longValue());


        int blocksPlaced = 0;

        HoleUtils.Hole targetHole = null;
        if (timeSystem.isPassed())
        {
            getHoles();
            if (holes == null || holes.size() == 0)
            {
                if (disableAfter.getValue())
                {
                    setEnabled(false);
                }
                if (Objects.equals(PriorityManager.INSTANCE.usageLockCause, "HoleFill"))
                {
                    PriorityManager.INSTANCE.unlockUsageLock();
                }
                return;
            }

            for (HoleUtils.Hole hole : holes)
            {
                if (!canPlaceBlock(hole.pos1)) return;


                if (!AntiCheat.INSTANCE.protocol.getValue())
                    doRotate(hole.pos1);

                targetHole = hole;
                int blockSlot = InventoryUtils.getHotbarItemSlot(getBlockType());
                if (blockSlot == -1)
                {
                    return;
                }

                toPlace.add(targetHole);
                timeSystem.resetDelay();
                PriorityManager.INSTANCE.lockUsageLock("HoleFill");
                blocksPlaced++;
                if (blocksPlaced >= blocksPerTick.getValue().intValue())
                {
                    break;
                }
            }
        }

    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.MovementTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;


        if (toPlace.isEmpty()) return;

        int blockSlot = InventoryUtils.getHotbarItemSlot(getBlockType());

        int oldSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;
        for (HoleUtils.Hole targetHole : toPlace)
        {
            InventoryUtils.switchToSlot(blockSlot);

            switched = true;


            Direction direction = BlockUtils.getPlaceableSide(targetHole.pos1, strictDirection.getValue());


            if (rotateMode.getValue().equals("Normal") && AntiCheat.INSTANCE.protocol.getValue())
                RotationUtils.doSilentRotate(targetHole.pos1, direction);


            BlockUtils.placeBlock(targetHole.pos1, direction, true);

            renderPositions.put(targetHole, System.currentTimeMillis());
        }


        if (rotateMode.getValue().equals("Normal") && AntiCheat.INSTANCE.protocol.getValue() && !toPlace.isEmpty())
            RotationUtils.silentSync();


        if (switched)
            InventoryUtils.switchToSlot(oldSlot);
        toPlace.clear();

    }

    boolean canPlaceBlock(BlockPos pos)
    {
        boolean allow = true;
        if (!mc.world.getBlockState(pos).isReplaceable())
        {
            return false;
        }
        if (BlockUtils.getPlaceableSide(pos, strictDirection.getValue()) == null)
        {
            return false;
        }

        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof PlayerEntity || entity instanceof EndCrystalEntity)
            {
                return false;
            }
        }

        if (BlockUtils.placeTrace(pos))
        {
            if (BlockUtils.getDistanceSq(mc.player, pos) > MathUtil.square(wallsRange.getValue().doubleValue()))
            {
                return false;
            }
        }
        double distance = mc.player.getEyePos().squaredDistanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        if (distance > MathUtil.square(range.getValue().doubleValue()))
        {
            return false;
        }


        return true;
    }

    @SubscribeEvent
    public void onRender(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;
        if (!renderHoles.getValue()) return;


        for (Map.Entry<HoleUtils.Hole, Long> entry : renderPositions.entrySet())
        {
            int fillAlpha = fill.getValue().getAlpha();
            int lineAlpha = line.getValue().getAlpha();

            long time = System.currentTimeMillis() - entry.getValue();
            double normal = MathUtil.normalize(time, 0, fadeTime.getValue().doubleValue());
            normal = MathHelper.clamp(normal, 0, 1);
            normal = -normal;
            normal++;

            fillAlpha *= normal;
            lineAlpha *= normal;

            Color fillColor = ColorUtil.newAlpha(fill.getValue().getColor(), fillAlpha);
            Color lineColor = ColorUtil.newAlpha(line.getValue().getColor(), lineAlpha);

            HoleUtils.Hole hole = entry.getKey();
            Box bb;
            bb = new Box(hole.pos1);

            RenderUtil.renderBox(RenderType.FILL, bb, fillColor, fillColor);
            RenderUtil.renderBox(RenderType.LINES, bb, lineColor, lineColor);

            if (fillAlpha == 0 && lineAlpha == 0)
            {
                renderPositions.remove(entry.getKey());
            }

        }
    }

    public void getHoles()
    {
        loadHoles();
    }

    public void loadHoles()
    {
        holes = HoleUtils.getHoles(range.getValue().doubleValue(), mc.player.getBlockPos(), doubles.getValue()).stream()
                .filter(hole ->
                {
                    boolean isAllowedHole = true;
                    Box bb = hole.doubleHole ? new Box(
                            hole.pos1.getX(),
                            hole.pos1.getY(),
                            hole.pos1.getZ(),
                            hole.pos2.getX() + 1,
                            hole.pos2.getY() + 1,
                            hole.pos2.getZ() + 1
                    ) : new Box(hole.pos1);
                    for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, bb))
                    {
                        if (entity instanceof PlayerEntity || entity instanceof EndCrystalEntity)
                        {
                            isAllowedHole = false;
                            break;
                        }
                    }

                    return isAllowedHole;
                })
                .filter(hole ->
                {
                    boolean isAllowedSmart = false;
                    if (smart.getValue())
                    {
                        if (target != null)
                        {
                            if (target.squaredDistanceTo(hole.pos1.getX() + 0.5, hole.pos1.getY() + 1, hole.pos1.getZ() + 0.5) < MathUtil.square(smartBlockRange.getValue().doubleValue()))
                                isAllowedSmart = true;
                        }
                    } else
                    {
                        isAllowedSmart = true;
                    }
                    return isAllowedSmart;
                })
                .collect(Collectors.toList());
    }


    @Override
    public String getHudInfo()
    {
        return smart.getValue() ? "Smart" : "Normal";
    }


    public void doRotate(BlockPos pos)
    {
        if (rotateMode.getValue().equals("None")) return;

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

        if (PriorityManager.INSTANCE.isUsageLocked() && Objects.equals(PriorityManager.INSTANCE.usageLockCause, "HoleFill"))
        {
            PriorityManager.INSTANCE.unlockUsageLock();
        }
    }

    public Item getBlockType()
    {
        if (webs.getValue()) return Items.COBWEB;

        return Items.OBSIDIAN;
    }

    @Override
    public String getDescription()
    {
        return "HoleFill: Fills nearby safe holes to prevent other players from entering them";
    }
}