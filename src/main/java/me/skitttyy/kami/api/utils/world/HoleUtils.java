package me.skitttyy.kami.api.utils.world;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class HoleUtils implements IMinecraft {

    public static boolean isHole(BlockPos pos)
    {
        boolean isHole = false;

        int amount = 0;

        for (BlockPos p : holeOffsets)
        {
            if (!mc.world.getBlockState(pos.add(p)).isReplaceable())
            {
                amount++;
            }
        }

        if (amount == 5)
        {
            isHole = true;
        }

        return isHole;

    }
    public static boolean isSurrounded(BlockPos pos)
    {
        boolean isHole = false;

        int amount = 0;

        for (BlockPos p : surroundOffsets)
        {
            if (!mc.world.getBlockState(pos.add(p)).isReplaceable())
            {
                amount++;
            }
        }

        if (amount == 4)
        {
            isHole = true;
        }

        return isHole;

    }


    public static boolean isBurrowed(final PlayerEntity entityPlayer)
    {

        final BlockPos blockPos = new BlockPos((int) Math.floor(entityPlayer.getX()), (int) Math.floor(entityPlayer.getY() + 0.2), (int) Math.floor(entityPlayer.getZ()));
        return mc.world.getBlockState(blockPos).getBlock() == Blocks.ENDER_CHEST || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(blockPos).getBlock() == Blocks.CHEST;
    }
    public static boolean isInBlock(final PlayerEntity entityPlayer)
    {

        final BlockPos blockPos = new BlockPos((int) Math.floor(entityPlayer.getX()), (int) Math.floor(entityPlayer.getY() + 0.2), (int) Math.floor(entityPlayer.getZ()));
        return mc.world.getBlockState(blockPos).getBlock() != Blocks.AIR;
    }

    public static boolean isObbyHole(BlockPos pos)
    {

        boolean isHole = true;
        int bedrock = 0;

        for (BlockPos off : holeOffsets)
        {
            Block b = mc.world.getBlockState(pos.add(off)).getBlock();

            if (!isSafeBlock(pos.add(off)))
            {
                isHole = false;
            } else
            {
                if (b == Blocks.OBSIDIAN || b == Blocks.ENDER_CHEST || b == Blocks.ANVIL)
                {
                    bedrock++;
                }
            }
        }

        if (mc.world.getBlockState(pos.add(0, 2, 0)).getBlock() != Blocks.AIR || mc.world.getBlockState(pos.add(0, 1, 0)).getBlock() != Blocks.AIR)
        {
            isHole = false;
        }

        if (bedrock < 1)
        {
            isHole = false;
        }
        return isHole;
    }

    public static boolean isBedrockHoles(BlockPos pos)
    {

        boolean isHole = true;

        for (BlockPos off : holeOffsets)
        {
            Block b = mc.world.getBlockState(pos.add(off)).getBlock();

            if (b != Blocks.BEDROCK)
            {
                isHole = false;
            }
        }

        if (mc.world.getBlockState(pos.add(0, 2, 0)).getBlock() != Blocks.AIR || mc.world.getBlockState(pos.add(0, 1, 0)).getBlock() != Blocks.AIR)
        {
            isHole = false;
        }

        return isHole;
    }

    public static Hole isDoubleHole(BlockPos pos)
    {

        if (checkOffset(pos, 1, 0))
        {
            return new Hole(false, true, pos, pos.add(1, 0, 0));
        }
        if (checkOffset(pos, 0, 1))
        {
            return new Hole(false, true, pos, pos.add(0, 0, 1));
        }
        /*

        for (EnumFacing f : EnumFacing.HORIZONTALS){
            int offX = f.getXOffset();
            int offZ = f.getZOffset();

            if (mc.world.getBlockState(pos.add(offX, 0, offZ)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(offX, 0, offZ)).getBlock() == Blocks.BEDROCK){
                if (mc.world.getBlockState(pos.add(offX * -2, 0, offZ * -2)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(offX * -2, 0, offZ * -2)).getBlock() == Blocks.BEDROCK){

                    if (mc.world.getBlockState(pos.add(offX * -1, 0, offZ * -1)).getBlock() == Blocks.AIR) {
                        if (isSafeBlock(pos.add(0, -1, 0)) && isSafeBlock(pos.add(offX * -1, -1, offZ * -1))) {

                            if (offZ == 0) {
                                if (isSafeBlock(pos.add(0, 0, 1)) && isSafeBlock(pos.add(0, 0, -1))) {
                                    if (isSafeBlock(pos.add(offX * -1, 0, 1)) && isSafeBlock(pos.add(offX * -1, 0, -1))) {
                                        Hole hole = new Hole(false, true, pos, pos.add(offX * -1, 0, 0));
                                        return hole;
                                    }
                                }
                            }

                            if (offX == 0) {
                                if (isSafeBlock(pos.add(1, 0, 0)) && isSafeBlock(pos.add(-1, 0, 0))) {
                                    if (isSafeBlock(pos.add(1, 0, offZ * -1)) && isSafeBlock(pos.add(-1, 0, offZ * -1))) {
                                        Hole hole = new Hole(false, true, pos, pos.add(0, 0, offZ * -1));
                                        return hole;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

         */
        return null;
    }

    public static Hole isDoubleHoleFr(BlockPos pos)
    {

        if (checkOffset(pos, 1, 0))
        {
            return new Hole(false, true, pos, pos.add(1, 0, 0));
        }
        if (checkOffset(pos, 0, 1))
        {
            return new Hole(false, true, pos, pos.add(0, 0, 1));
        }
        if (checkOffset(pos, -1, 0))
        {
            return new Hole(false, true, pos, pos.add(-1, 0, 0));
        }
        if (checkOffset(pos, 0, -1))
        {
            return new Hole(false, true, pos, pos.add(0, 0, -1));
        }
        /*

        for (EnumFacing f : EnumFacing.HORIZONTALS){
            int offX = f.getXOffset();
            int offZ = f.getZOffset();

            if (mc.world.getBlockState(pos.add(offX, 0, offZ)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(offX, 0, offZ)).getBlock() == Blocks.BEDROCK){
                if (mc.world.getBlockState(pos.add(offX * -2, 0, offZ * -2)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(offX * -2, 0, offZ * -2)).getBlock() == Blocks.BEDROCK){

                    if (mc.world.getBlockState(pos.add(offX * -1, 0, offZ * -1)).getBlock() == Blocks.AIR) {
                        if (isSafeBlock(pos.add(0, -1, 0)) && isSafeBlock(pos.add(offX * -1, -1, offZ * -1))) {

                            if (offZ == 0) {
                                if (isSafeBlock(pos.add(0, 0, 1)) && isSafeBlock(pos.add(0, 0, -1))) {
                                    if (isSafeBlock(pos.add(offX * -1, 0, 1)) && isSafeBlock(pos.add(offX * -1, 0, -1))) {
                                        Hole hole = new Hole(false, true, pos, pos.add(offX * -1, 0, 0));
                                        return hole;
                                    }
                                }
                            }

                            if (offX == 0) {
                                if (isSafeBlock(pos.add(1, 0, 0)) && isSafeBlock(pos.add(-1, 0, 0))) {
                                    if (isSafeBlock(pos.add(1, 0, offZ * -1)) && isSafeBlock(pos.add(-1, 0, offZ * -1))) {
                                        Hole hole = new Hole(false, true, pos, pos.add(0, 0, offZ * -1));
                                        return hole;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

         */
        return null;
    }

    public static boolean checkOffset(BlockPos pos, int offX, int offZ)
    {
        return mc.world.getBlockState(pos).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.add(offX, 0, offZ)).getBlock() == Blocks.AIR && isSafeBlock(pos.add(0, -1, 0)) && isSafeBlock(pos.add(offX, -1, offZ)) && isSafeBlock(pos.add(offX * 2, 0, offZ * 2)) && isSafeBlock(pos.add(-offX, 0, -offZ)) && isSafeBlock(pos.add(offZ, 0, offX)) && isSafeBlock(pos.add(-offZ, 0, -offX)) && isSafeBlock(pos.add(offX, 0, offZ).add(offZ, 0, offX)) && isSafeBlock(pos.add(offX, 0, offZ).add(-offZ, 0, -offX));
    }

    static boolean isSafeBlock(BlockPos pos)
    {
        if (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos).getBlock() == Blocks.ENDER_CHEST)
        {
            return true;

        }
        return false;
    }

    public static BlockPos[] holeOffsets = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(0, -1, 0)};
    public static BlockPos[] surroundOffsets = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};

    public static List<Hole> getHoles(double range, BlockPos playerPos, boolean doubles)
    {
        ArrayList<Hole> holes = new ArrayList<>();
        List<BlockPos> circle = BlockUtils.sphere(range, playerPos, true, false);

        for (BlockPos pos : circle)
        {

            if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR)
            {
                if (isObbyHole(pos))
                {
                    holes.add(new Hole(false, false, pos));
                    continue;
                }

                if (isBedrockHoles(pos))
                {
                    holes.add(new Hole(true, false, pos));
                    continue;
                }

                if (doubles)
                {
                    Hole dh = isDoubleHole(pos);
                    if (dh != null)
                    {
                        if (mc.world.getBlockState(dh.pos1.add(0, 1, 0)).getBlock() == Blocks.AIR || mc.world.getBlockState(dh.pos2.add(0, 1, 0)).getBlock() == Blocks.AIR)
                        {
                            holes.add(dh);
                        }
                    }
                }
            }
        }

        return holes;
    }

    public static List<Hole> getHoles(double range, BlockPos playerPos, boolean doubles, boolean self)
    {
        ArrayList<Hole> holes = new ArrayList<>();

        List<BlockPos> circle = BlockUtils.sphere(range, playerPos, true, false);

        for (BlockPos pos : circle)
        {
            if (pos.equals(playerPos) && self) continue;

            if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR)
            {
                if (isObbyHole(pos))
                {
                    holes.add(new Hole(false, false, pos));
                    continue;
                }

                if (isBedrockHoles(pos))
                {
                    holes.add(new Hole(true, false, pos));
                    continue;
                }

                if (doubles)
                {
                    Hole dh = isDoubleHole(pos);
                    if (dh != null)
                    {
                        if (mc.world.getBlockState(dh.pos1.add(0, 1, 0)).getBlock() == Blocks.AIR || mc.world.getBlockState(dh.pos2.add(0, 1, 0)).getBlock() == Blocks.AIR)
                        {
                            holes.add(dh);
                        }
                    }
                }
            }
        }

        return holes;
    }


    public static List<Hole> getHolesHolesnap(double range, BlockPos playerPos, boolean doubles, boolean self)
    {
        ArrayList<Hole> holes = new ArrayList<>();

        List<BlockPos> circle = BlockUtils.sphere(range, playerPos, true, false);

        for (BlockPos pos : circle)
        {
            if (pos.equals(playerPos) && self) continue;

            if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR)
            {
                if (isObbyHole(pos))
                {
                    if (skyCheck(pos))
                        continue;

                    holes.add(new Hole(false, false, pos));
                    continue;
                }

                if (isBedrockHoles(pos))
                {
                    if (skyCheck(pos))
                        continue;

                    holes.add(new Hole(true, false, pos));
                    continue;
                }

                if (doubles)
                {
                    Hole dh = isDoubleHole(pos);


                    if (dh != null)
                    {
                        if (dh.pos2.equals(playerPos) && self) continue;


                        boolean flag = mc.world.getBlockState(dh.pos1.add(0, 1, 0)).getBlock() == Blocks.AIR;
                        boolean flag2 = mc.world.getBlockState(dh.pos2.add(0, 1, 0)).getBlock() == Blocks.AIR;

                        if (flag || flag2)
                        {

                            //for special under holes
                            if (!flag)
                            {
                                dh.toTarget = dh.pos2;
                            } else if (!flag2)
                            {
                                dh.toTarget = dh.pos1;
                            }
                            if (dh.toTarget != null && skyCheck(dh.toTarget))
                                continue;
                            else if (dh.toTarget == null)
                            {
                                if (skyCheck(dh.pos1) || skyCheck(dh.pos2))
                                    continue;
                            }

                            holes.add(dh);
                        }
                    }
                }
            }
        }

        return holes;
    }

    public static boolean skyCheck(BlockPos pos)
    {
        for (int i = 1; i < 4; i++)
        {
            if (!(mc.world.getBlockState(pos.up(i)).getBlock() == Blocks.AIR))
            {
                return true;
            }
        }
        return false;
    }

    public static double distanceTo(Hole hole)
    {
        Vec3d targetPos;
        if (hole.doubleHole)
        {
            if (hole.toTarget != null)
            {
                targetPos = new Vec3d(hole.toTarget.getX() + 0.5, mc.player.getY(), hole.toTarget.getZ() + 0.5);
            } else
            {
                BlockPos pos1 = hole.pos1;
                BlockPos pos2 = hole.pos2;

                double centerX = ((pos1.getX() + 0.5) + (pos2.getX() + 0.5)) / 2.0;
                double centerZ = ((pos1.getZ() + 0.5) + (pos2.getZ() + 0.5)) / 2.0;
                targetPos = new Vec3d(centerX, mc.player.getY(), centerZ);
            }
        } else
        {
            targetPos = new Vec3d(hole.pos1.getX() + 0.5, mc.player.getY(), hole.pos1.getZ() + 0.5);
        }
        return mc.player.getPos().distanceTo(targetPos);
    }

    public static class Hole {
        public boolean bedrock;
        public boolean doubleHole;
        public BlockPos pos1;
        public BlockPos pos2;
        public BlockPos toTarget;

        public Hole(boolean bedrock, boolean doubleHole, BlockPos pos1, BlockPos pos2)
        {
            this.bedrock = bedrock;
            this.doubleHole = doubleHole;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        public Hole(boolean bedrock, boolean doubleHole, BlockPos pos1, BlockPos pos2, BlockPos toTarget)
        {
            this.bedrock = bedrock;
            this.doubleHole = doubleHole;
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.toTarget = toTarget;
        }

        public Hole(boolean bedrock, boolean doubleHole, BlockPos pos1)
        {
            this.bedrock = bedrock;
            this.doubleHole = doubleHole;
            this.pos1 = pos1;
        }
    }

}
