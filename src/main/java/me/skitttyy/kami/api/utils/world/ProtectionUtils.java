package me.skitttyy.kami.api.utils.world;

import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProtectionUtils implements IMinecraft
{
    public static List<BlockPos> getSurroundPlacements(boolean selfTrap, boolean dynamic, boolean extend, boolean ignoreCrystals, boolean strictDirection)
    {
        BlockPos playerPos = PlayerUtils.getPlayerPos();
        List<BlockPos> positions = getSurroundPositions(playerPos, dynamic, extend, ignoreCrystals, strictDirection);


        if (selfTrap)
        {
            if (!mc.player.isCrawling())
            {
                positions.addAll(getSurroundPositions(playerPos.up(), dynamic, extend, ignoreCrystals, strictDirection));
            }
        }

        return positions;
    }


    public static List<BlockPos> getSurroundPositions(BlockPos pos, boolean dynamic, boolean extend, boolean ignoreCrystals, boolean strictDirection)
    {
        List<BlockPos> entities = getSurroundEntities(mc.player, pos, dynamic, extend, ignoreCrystals);
        List<BlockPos> blocks = new CopyOnWriteArrayList<>();
        for (BlockPos epos : entities)
        {
            for (Direction dir2 : Direction.values())
            {
                if (!dir2.getAxis().isHorizontal())
                {
                    continue;
                }
                BlockPos pos2 = epos.add(dir2.getVector());
                if (entities.contains(pos2) || blocks.contains(pos2))
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(pos2.toCenterPos());
                if (dist > MathUtil.square(6))
                {
                    continue;
                }
                blocks.add(pos2);
            }
        }
        for (BlockPos block : blocks)
        {
            Direction direction = BlockUtils.getPlaceableSide(block, strictDirection);
            if (direction == null)
            {
                blocks.add(block.down());
            }
        }


        if (AutoBreak.INSTANCE.mineDownTimer.isPassed())
            for (BlockPos entityPos : entities)
            {
                if (entityPos == pos)
                {
                    continue;
                }
                blocks.add(entityPos.down());
            }


        Collections.reverse(blocks);


//        blocks.removeIf(place -> AutoBreak.INSTANCE.isAnyMining(place));

        /*
         * Do non overplacing blocks first
         */
        blocks.sort(Comparator.comparingInt(surroundPos ->
                -mc.world.getOtherEntities(null, new Box(surroundPos))
                        .stream().filter(CrystalUtil::isEndCrystal).toList().size()
        ));


        return blocks;
    }

    public static List<BlockPos> getAutoCityBlocks(Entity entity, BlockPos pos)
    {

        List<BlockPos> entities = getSurroundEntities(entity, pos, true, false, true);
        List<BlockPos> blocks = new CopyOnWriteArrayList<>();
        for (BlockPos epos : entities)
        {
            for (Direction dir2 : Direction.values())
            {
                if (!dir2.getAxis().isHorizontal())
                {
                    continue;
                }
                BlockPos pos2 = epos.add(dir2.getVector());
                if (entities.contains(pos2) || blocks.contains(pos2))
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(pos2.toCenterPos());
                if (dist > MathUtil.square(6))
                {
                    continue;
                }

                blocks.add(pos2);
            }
        }
        blocks.sort(Comparator.comparingDouble(mod ->
                mc.player.getPos().distanceTo(mod.toCenterPos())
        ));


        return blocks;
    }

    public static List<BlockPos> getSurroundEntities(Entity entity, boolean dynamic)
    {
        List<BlockPos> entities = new LinkedList<>();
        entities.add(entity.getBlockPos());
        if (dynamic)
        {
            for (Direction dir : Direction.values())
            {
                if (!dir.getAxis().isHorizontal())
                {
                    continue;
                }
                entities.addAll(BlockUtils.getAllInBox(entity.getBoundingBox(), entity.getBlockPos()));
            }
        }
        return entities;
    }

    public static List<BlockPos> getSurroundEntities(Entity target, BlockPos pos, boolean dynamic, boolean extend, boolean overplace)
    {
        List<BlockPos> entities = new LinkedList<>();
        entities.add(pos);

        if (extend || dynamic)
            for (Direction dir : Direction.values())
            {
                if (!dir.getAxis().isHorizontal())
                {
                    continue;
                }
                BlockPos pos1 = pos.add(dir.getVector());

                if (extend)
                {
                    List<Entity> box = mc.world.getOtherEntities(!dynamic ? target : null, new Box(pos1))
                            .stream().filter(e -> !isEntityBlockingSurround(e, overplace)).toList();
                    if (box.isEmpty())
                    {
                        continue;
                    }
                    for (Entity entity : box)
                    {
                        entities.addAll(BlockUtils.getAllInBox(entity.getBoundingBox(), pos));
                    }
                } else
                {
                    entities.addAll(BlockUtils.getAllInBox(target.getBoundingBox(), pos));
                }
            }
        return entities;
    }


    public static boolean isEntityBlockingSurround(Entity entity, boolean ignoreCrystals)
    {
        return entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity
                || (entity instanceof EndCrystalEntity && !ignoreCrystals);
    }


}
