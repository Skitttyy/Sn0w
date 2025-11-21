package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.world.EntityEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.FriendManager;
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
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Catcher extends Module
{
    Timer timer = new Timer();

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

    public Catcher()
    {
        super("Catcher", Category.Combat);
    }

    Map<Integer, String> oppPearls = new ConcurrentHashMap<>();

    Entity target;
    List<BlockPos> toPlace = new ArrayList<>();

    @SubscribeEvent
    public void onUpdate(final TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        target = TargetUtils.getTarget(targetRange.getValue().floatValue());

        if (target == null) return;


        toPlace.clear();

        if (AutoBreak.INSTANCE.didAction) return;

        if (mc.player.isUsingItem()) return;


        if (InventoryUtils.getHotbarItemSlot(Items.OBSIDIAN) == -1) return;


        if (timer.isPassed() && !oppPearls.isEmpty())
        {
            for (Map.Entry<Integer, String> tag : oppPearls.entrySet())
            {

                Entity entity = mc.world.getEntityById(tag.getKey());

                if (!(entity instanceof EnderPearlEntity))
                {
                    oppPearls.remove(tag.getKey());
                    continue;
                }
            }

            int id = oppPearls.entrySet().stream().min(Comparator.comparingDouble(ent -> mc.player.getPos().distanceTo(mc.world.getEntityById(ent.getKey()).getPos()))).get().getKey();
            EnderPearlEntity pearlEntity = (EnderPearlEntity) mc.world.getEntityById(id);

            if (pearlEntity == null) return;


            MathUtil.Result result = MathUtil.calcTrajectory(pearlEntity);

            if (result != null)
            {
                for(Vec3d point : result.getPoints())
                {
                    BlockPos pos = BlockPos.ofFloored(point.getX(), point.getY(), point.getZ());

                    if (BlockUtils.canPlaceBlock(pos, strictDirection.getValue()) && pos.getSquaredDistance(mc.player.getEyePos()) < MathUtil.square(range.getValue().floatValue()))
                    {
                        toPlace.add(pos);
                        timer.resetDelay();

                        if (!AntiCheat.INSTANCE.protocol.getValue())
                            doRotate(pos);

                        break;
                    }
                }
            }

        }

    }

    @SubscribeEvent
    public void onSpawnEntity(EntityEvent.Add event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getEntity() instanceof EnderPearlEntity pearl)
        {

            Entity player = mc.world.getClosestPlayer(event.getEntity(), 3.0);
            if (player != null && !FriendManager.INSTANCE.isFriend(event.getEntity()))
            {
                oppPearls.put(pearl.getId(), player.getName().getString());
            }
        }
    }


    @SubscribeEvent
    public void onRemoveEntity(EntityEvent.Remove event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getEntity() instanceof EnderPearlEntity pearl)
        {
            if (oppPearls.get(pearl.getId()) != null)
                oppPearls.remove(pearl.getId());
        }
    }


    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.AfterClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (AutoBreak.INSTANCE.didAction) return;

        if (mc.player.isUsingItem()) return;

        int blockSlot = InventoryUtils.getHotbarItemSlot(Items.OBSIDIAN);

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


            BlockUtils.placeBlock(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue()), true);
        }

        if (!toPlace.isEmpty())
            RotationUtils.silentSync();

        if (switched)
        {
            InventoryUtils.switchToSlot(oldSlot);
        }
        toPlace.clear();

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
        return "Catcher: Blocks thrown pearls with airplace";
    }
}
