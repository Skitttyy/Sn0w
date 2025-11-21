package me.skitttyy.kami.impl.features.modules.misc.autobreak;

import me.skitttyy.kami.api.management.breaks.BreakManager;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.CrystalUtil;
import me.skitttyy.kami.api.utils.world.ProtectionUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.combat.CatAura;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoCity implements IMinecraft
{
    PlayerEntity target;


    public void calcAutoCity()
    {

        if (AutoBreak.INSTANCE.antiCrawl.getValue())
        {
            if (mc.player.isCrawling() || (!mc.player.isCrawling() && AutoBreak.INSTANCE.preMine.getValue() && BreakManager.INSTANCE.isBreaking(mc.player.getBlockPos())) && BlockUtils.getAllInBox(mc.player.getBoundingBox(), mc.player.getBlockPos()).size() == 1)
            {
                if (mineHead()) return;
            }
        }

        target = (PlayerEntity) TargetUtils.getTarget(AutoBreak.INSTANCE.targetRange.getValue().floatValue());

        if (target == null) return;


        if (!AutoBreak.INSTANCE.isMineAvailable()) return;


        if (mineBurrows())
        {
            if (mineSurround())
            {
                if (AutoBreak.INSTANCE.headCrystal.getValue())
                    mineOppositionHead();
            }
        }
    }

    public boolean mineOppositionHead()
    {
        BlockPos pos = target.getBlockPos().up(2);
        if (!AutoBreak.INSTANCE.isAnyMining(pos) && BlockUtils.isMineable(mc.world.getBlockState(pos)))
        {
            if (canMine(pos, true))
            {
                AutoBreak.INSTANCE.queue(pos, false);
            }
        }
        return AutoBreak.INSTANCE.isMineAvailable();
    }

    public boolean mineHead()
    {
        BlockPos pos = mc.player.getBlockPos().up();
        if (!AutoBreak.INSTANCE.isAnyMining(pos) && BlockUtils.isMineable(mc.world.getBlockState(pos)) && !AutoBreak.INSTANCE.isAnyMining(mc.player.getBlockPos().down()))
        {
            AutoBreak.INSTANCE.forceQueue(mc.player.getBlockPos().up(), true);
        }
        return AutoBreak.INSTANCE.isMineAvailable();
    }

    public boolean mineBurrows()
    {


        BlockPos pos = target.getBlockPos();
        List<BlockPos> burrows = BlockUtils.getAllInBox(target.getBoundingBox(), pos);
        burrows.sort(Comparator.comparingDouble(burrowPos -> target.squaredDistanceTo(burrowPos.toCenterPos())));

        if (burrows.size() <= 2)
        {
            boolean unbreakable = false;
            for (BlockPos burrowPos : burrows)
            {
                if (BlockUtils.isUnbreakable(mc.world.getBlockState(burrowPos)))
                {
                    unbreakable = true;
                    break;
                }
            }

            if (unbreakable)
            {
                unbreakable = false;
                for (BlockPos burrowPos : burrows)
                {
                    if (mc.world.isOutOfHeightLimit(burrowPos)) break;

                    if (BlockUtils.isUnbreakable(mc.world.getBlockState(burrowPos.down())))
                    {
                        unbreakable = true;
                        break;
                    }
                }


                BlockPos targetPos = target.getBlockPos();
                burrows = BlockUtils.getAllInBox(target.getBoundingBox(), unbreakable ? targetPos.up() : targetPos.down());
                burrows.sort(Comparator.comparingDouble(burrowPos -> target.squaredDistanceTo(burrowPos.toCenterPos())));
            }
        }
        for (BlockPos burrowPos : burrows)
        {

            if (!canMine(burrowPos, true)) continue;


            AutoBreak.INSTANCE.queue(burrowPos, true);
        }

        return AutoBreak.INSTANCE.isMineAvailable();
    }


    public boolean mineSurround()
    {

        BlockPos targetPos = target.getBlockPos();
        if (BlockUtils.isUnbreakable(mc.world.getBlockState(targetPos)))
        {
            List<BlockPos> burrows = BlockUtils.getAllInBox(target.getBoundingBox(), targetPos);
            burrows.sort(Comparator.comparingDouble(burrowPos -> target.squaredDistanceTo(burrowPos.toCenterPos())));

            boolean unbreakable = true;
            for (BlockPos burrowPos : burrows)
            {
                if (!BlockUtils.isUnbreakable(mc.world.getBlockState(burrowPos)))
                {
                    unbreakable = false;
                    break;
                }
            }
            if (unbreakable)
            {
                targetPos = target.getBlockPos().up();
            }

        }


        List<BlockPos> surround = ProtectionUtils.getAutoCityBlocks(target, targetPos);


        if (surround.isEmpty()) return AutoBreak.INSTANCE.isMineAvailable();

        surround.sort(Comparator.comparingDouble(pos -> target.squaredDistanceTo(pos.toCenterPos())));

        BlockPos second = null;
        BlockPos first = null;

        List<Pair<BlockPos, Double>> blocks = new ArrayList<>();
        List<BlockPos> badBlocks = new ArrayList<>();

        for (BlockPos blockPos : surround)
        {
            if (!canMine(blockPos, true)) continue;

            if (CrystalUtil.canPlaceCrystalIgnore(blockPos.down(), CatAura.INSTANCE.onePointTwelve.getValue()) && CatAura.INSTANCE.checkPlace(blockPos.down(), true))
            {
                double damage = CrystalUtil.calculateDamage(target, blockPos.toCenterPos().add(0.0, 0.5, 0.0), false, false, blockPos);
                blocks.add(new Pair<>(blockPos, damage));
            } else
            {
                badBlocks.add(blockPos);
            }
        }
        badBlocks.sort(Comparator.comparingDouble(pos -> mc.player.squaredDistanceTo(pos.toCenterPos())));
        blocks.sort(Comparator.comparingDouble(Pair::value));

        if (AutoBreak.INSTANCE.mode.getValue().equals("Double"))
        {
            if (!blocks.isEmpty())
            {
                second = blocks.get(0).key();
                blocks.remove(0);
            } else if (!badBlocks.isEmpty())
            {
                second = badBlocks.get(0);
                badBlocks.remove(0);
            }
        }

        if (!blocks.isEmpty())
        {
            first = blocks.get(0).key();
            blocks.remove(0);
        } else if (!badBlocks.isEmpty())
        {
            first = badBlocks.get(0);
            badBlocks.remove(0);
        }

        if (second != null && first != null)
        {
            AutoBreak.INSTANCE.queue(first, false);
            AutoBreak.INSTANCE.queue(second, false);
        } else if (second != null)
        {
            AutoBreak.INSTANCE.queue(second, false);
        } else if (first != null)
        {
            AutoBreak.INSTANCE.queue(first, false);
        }

        return AutoBreak.INSTANCE.isMineAvailable();
    }


    public boolean canMine(BlockPos pos, boolean check)
    {

        if (check)
            if (!AutoBreak.INSTANCE.canMine(pos)) return false;


        if (!BlockUtils.isMineable(mc.world.getBlockState(pos))) return false;

        if (mc.world.getBlockState(pos).getBlock().equals(Blocks.COBWEB)) return false;


        if (!AutoBreak.INSTANCE.selfMine.getValue() && pos.equals(mc.player.getBlockPos())) return false;


        //we reset on these
        if (check)
        {
            Direction side = BlockUtils.getMineableSide(pos, AutoBreak.INSTANCE.strictDirection.getValue());


            if (BlockUtils.distanceTo(pos, side) > AutoBreak.INSTANCE.breakRange.getValue().floatValue())
            {
                return false;
            }
            if (BlockUtils.placeTrace(pos))
            {
                return !(BlockUtils.distanceTo(pos, side) > AutoBreak.INSTANCE.walls.getValue().doubleValue());
            }
        }
        return true;
    }
}
