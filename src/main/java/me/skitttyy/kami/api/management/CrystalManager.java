package me.skitttyy.kami.api.management;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.world.EntityEvent;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrystalManager implements IMinecraft {

    public static CrystalManager INSTANCE;
    public static Map<BlockPos, Pair.BoxPair> crystalBoxes = new ConcurrentHashMap<>();


    public CrystalManager()
    {
        KamiMod.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck())
        {
            crystalBoxes.clear();
            return;
        }


        for (Entity entity : mc.world.getEntities())
        {
            if (entity instanceof EndCrystalEntity crystal)
            {
                if (!entity.isAlive()) continue;

                if (mc.player.distanceTo(entity) > 16) continue;

                if (!crystalBoxes.containsKey(crystal.getBlockPos()))
                {
                    crystalBoxes.put(entity.getBlockPos(), new Pair.BoxPair(entity.getBoundingBox()));
                }
            }
        }


        long currentTime = System.currentTimeMillis();
        for (Map.Entry<BlockPos, Pair.BoxPair> entry : crystalBoxes.entrySet())
        {
            BlockPos pos = entry.getKey();
            Pair.BoxPair pair = entry.getValue();

            if (mc.player.getPos().squaredDistanceTo(pos.toCenterPos()) > MathUtil.square(16.0f))
            {
                crystalBoxes.remove(pos);
                return;
            }

            EndCrystalEntity crystal = null;
            for (EndCrystalEntity entity : mc.world.getNonSpectatingEntities(EndCrystalEntity.class, pair.key()))
            {
                if (entity.getBlockPos().equals(pos))
                {
                    crystal = entity;
                    break;
                }
            }
            if (crystal == null && currentTime - pair.value() > 600L)
            {
                crystalBoxes.remove(pos);
            } else if (crystal != null)
            {
                crystalBoxes.put(pos, new Pair.BoxPair(crystal.getBoundingBox()));
            }
        }
    }


    @SubscribeEvent
    public void onEntityAdd(EntityEvent.Add event)
    {
        if (event.getEntity() instanceof EndCrystalEntity entity)
        {
            if (mc.player.distanceTo(entity) > 16) return;

            crystalBoxes.put(entity.getBlockPos(), new Pair.BoxPair(entity.getBoundingBox()));
        }
    }

    public boolean isRecentlyBlocked(BlockPos pos)
    {
        Box blockBox = new Box(pos);


        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, blockBox))
        {
            if ((entity instanceof EndCrystalEntity))
            {
                return true;
            }
        }
        for (Map.Entry<BlockPos, Pair.BoxPair> entry : crystalBoxes.entrySet())
        {
            Pair.BoxPair pair = entry.getValue();
            Box box = pair.key();
            if (box.intersects(blockBox))
            {
                return true;
            }
        }
        return false;
    }
}
