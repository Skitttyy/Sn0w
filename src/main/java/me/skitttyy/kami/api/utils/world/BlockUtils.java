package me.skitttyy.kami.api.utils.world;

import me.skitttyy.kami.api.management.HitboxManager;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.breaks.BreakManager;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.combat.CatAura;
import me.skitttyy.kami.mixin.accessor.IClientPlayerInteractionManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BlockUtils implements IMinecraft
{
    public static Set<Block> SNEAK_BLOCKS = Set.of(Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.FLETCHING_TABLE, Blocks.CARTOGRAPHY_TABLE, Blocks.ENCHANTING_TABLE, Blocks.SMITHING_TABLE, Blocks.STONECUTTER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.JUKEBOX, Blocks.NOTE_BLOCK, Blocks.DISPENSER, Blocks.HOPPER);
    public static Set<Block> SHULKER_BLOCKS = Set.of(Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX);
    public static final Box EMPTY_BOX = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);


    public static List<BlockPos> sphere(double range, BlockPos pos, boolean sphere, boolean hollow)
    {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();

        for (int x = cx - (int) range; x <= cx + range; x++)
        {
            for (int z = cz - (int) range; z <= cz + range; z++)
            {
                for (int y = (sphere ? cy - (int) range : cy); y < (cy + range); y++)
                {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);

                    if (dist < range * range && !(hollow && dist < (range - 1) * (range - 1)))
                    {
                        BlockPos l = new BlockPos(x, y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }


    public static Box getCombinedBox(BlockPos pos)
    {
        VoxelShape shape = mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).offset(pos.getX(), pos.getY(), pos.getZ());
        Box combined = new Box(pos);
        for (Box box : shape.getBoundingBoxes())
        {
            double minX = Math.max(box.minX, combined.minX);
            double minY = Math.max(box.minY, combined.minY);
            double minZ = Math.max(box.minZ, combined.minZ);
            double maxX = Math.min(box.maxX, combined.maxX);
            double maxY = Math.min(box.maxY, combined.maxY);
            double maxZ = Math.min(box.maxZ, combined.maxZ);
            combined = new Box(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return combined;
    }

    public static boolean placeTrace(BlockPos pos)
    {
        for (Direction facing : Direction.values())
        {

            Vec3d vec = new Vec3d(
                    pos.getX() + 0.5 + facing.getOffsetX() / 2f,
                    pos.getY() + 0.5 + facing.getOffsetY() / 2f,
                    pos.getZ() + 0.5 + facing.getOffsetZ() / 2f);

            BlockHitResult result = RaytraceUtils.getResult(vec);
            if (result != null && result.getBlockPos().equals(pos))
            {
                return false;
            }
        }
        return true;
    }


    public static List<Box> subtract(Box bb, Box other)
    {
        List<Box> result = new ArrayList<>();

        if (!bb.intersects(other))
        {
            result.add(bb); // No intersection, return original box
            return result;
        }

        double x1 = Math.max(bb.minX, other.minX);
        double y1 = Math.max(bb.minY, other.minY);
        double z1 = Math.max(bb.minZ, other.minZ);
        double x2 = Math.min(bb.maxX, other.maxX);
        double y2 = Math.min(bb.maxY, other.maxY);
        double z2 = Math.min(bb.maxZ, other.maxZ);

        if (x1 >= x2 || y1 >= y2 || z1 >= z2)
        {
            result.add(bb); // Boxes don't actually overlap
            return result;
        }

        // 1. Left slice
        if (bb.minX < other.minX)
        {
            result.add(new Box(bb.minX, bb.minY, bb.minZ, other.minX, bb.maxY, bb.maxZ));
        }

        // 2. Right slice
        if (bb.maxX > other.maxX)
        {
            result.add(new Box(other.maxX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ));
        }

        // 3. Bottom slice (excluding already covered areas)
        if (bb.minY < other.minY)
        {
            result.add(new Box(Math.max(bb.minX, other.minX), bb.minY, bb.minZ, Math.min(bb.maxX, other.maxX), other.minY, bb.maxZ));
        }

        // 4. Top slice (excluding already covered areas)
        if (bb.maxY > other.maxY)
        {
            result.add(new Box(Math.max(bb.minX, other.minX), other.maxY, bb.minZ, Math.min(bb.maxX, other.maxX), bb.maxY, bb.maxZ));
        }

        // 5. Front slice (excluding already covered areas)
        if (bb.minZ < other.minZ)
        {
            result.add(new Box(Math.max(bb.minX, other.minX), Math.max(bb.minY, other.minY), bb.minZ, Math.min(bb.maxX, other.maxX), Math.min(bb.maxY, other.maxY), other.minZ));
        }

        // 6. Back slice (excluding already covered areas)
        if (bb.maxZ > other.maxZ)
        {
            result.add(new Box(Math.max(bb.minX, other.minX), Math.max(bb.minY, other.minY), other.maxZ, Math.min(bb.maxX, other.maxX), Math.min(bb.maxY, other.maxY), bb.maxZ));
        }

        return result;
    }


    public static boolean canIgnite(BlockPos pos, boolean strictDirection)
    {
        if (mc.world.getBlockState(pos).getBlock() instanceof FireBlock)
            return false;

        if (!mc.world.getBlockState(pos).isReplaceable())
            return false;

        if (BlockUtils.getPlaceableSide(pos, strictDirection) == null)
            return false;

        return true;
    }

    public static boolean canPlaceBlockIgnore(BlockPos pos, boolean strictDirection)
    {
        if (!mc.world.getBlockState(pos).isReplaceable())
            return false;

        if (BlockUtils.getPlaceableSide(pos, strictDirection) == null)
            return false;

        return true;
    }


    public static boolean canPlaceItemFrame(BlockPos pos, Direction direction, boolean strictDirection)
    {
        if (!mc.world.getBlockState(pos.offset(direction)).isReplaceable())
            return false;
        if (mc.world.getBlockState(pos).isReplaceable())
            return false;


        if (strictDirection)
            if (!BlockUtils.canSeeFace(pos, direction)) return false;


        BlockPos frameBlock = pos.offset(direction);
        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(frameBlock)))
        {
            if (entity instanceof ItemFrameEntity frame)
            {
                if (frame.getFacing().equals(direction))
                {
                    return false;
                }
            }
        }

        return true;
    }


    public static ItemFrameEntity getItemFrame(BlockPos pos, Direction direction){

        BlockPos frameBlock = pos.offset(direction);
        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(frameBlock)))
        {
            if (entity instanceof ItemFrameEntity frame)
            {
                if (frame.getFacing().equals(direction))
                {
                    return frame;
                }
            }
        }
        return null;
    }

    public static boolean canPlaceBlock(BlockPos pos)
    {

        if (!mc.world.getBlockState(pos).isReplaceable())
            return false;


        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof PlayerEntity)
            {
                return false;
            }
        }
        return true;
    }

    public static boolean canPlaceBlock(BlockPos pos, boolean strictDirection)
    {

        if (!mc.world.getBlockState(pos).isReplaceable())
            return false;

        if (BlockUtils.getPlaceableSide(pos, strictDirection) == null)
            return false;

        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof PlayerEntity)
            {
                return false;
            }
        }
        return true;
    }


    public static boolean canPlaceBlockPlatformer(BlockPos pos, boolean strictDirection)
    {
        if (BlockUtils.isUnbreakable(mc.world.getBlockState(pos)))
            return false;
        if (BlockUtils.getPlaceableSide(pos, strictDirection) == null)
            return false;

        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof PlayerEntity)
            {
                if (HitboxManager.INSTANCE.isServerCrawling(entity))
                {
                    if (new Box(pos).intersects(HitboxManager.INSTANCE.getCrawlingBoundingBox(entity)))
                        return false;
                } else
                {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean canPlaceBlockIfBeingMined(BlockPos pos, boolean strictDirection)
    {

        if (!BreakManager.INSTANCE.isPassed(pos, 0.5f, false))
            return false;

        if (BlockUtils.getPlaceableSide(pos, strictDirection) == null)
            return false;


        return true;
    }


    public static boolean canPlaceBlock(BlockPos pos, boolean strictDirection, BlockPos pretend)
    {
        if (!mc.world.getBlockState(pos).isReplaceable())
            return false;
        if (BlockUtils.getPlaceableSide(pos, strictDirection, pretend) == null)
            return false;


        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof PlayerEntity)
            {
                return false;
            }
        }
        return true;
    }


    public static boolean canPlaceBlock(BlockPos pos, boolean strictDirection, boolean isAir)
    {

        if (!mc.world.getBlockState(pos).isReplaceable())
            return isAir;

        if (BlockUtils.getPlaceableSide(pos, strictDirection) == null)
            return false;

        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof PlayerEntity)
            {
                return false;
            }
        }
        return true;
    }


    public static Direction getPlaceableSideCrystal(BlockPos pos, boolean strict)
    {

        if (mc.player.getBlockPos().up().equals(pos) && !mc.player.isCrawling())
            return Direction.UP;


        double bestDist = 99999;
        Direction bestSide = null;
        for (Direction side : Direction.values())
        {

            BlockPos neighbour = pos.offset(side);
            BlockState blockState = mc.world.getBlockState(neighbour);


            if (strict)
            {
                if (!canSeeFace(pos, side)) continue;

                if (!blockState.isReplaceable()) continue;
            }


            if (side.equals(Direction.UP))
            {
                return side;
            }
            double dist = distanceTo(pos, side);
            if (bestSide == null || dist < bestDist)
            {
                bestDist = dist;
                bestSide = side;
            }
        }

        return bestSide;
    }

    public static Direction getPlaceableSideCrystal(BlockPos pos, boolean strict, BlockPos ignore)
    {
        if (mc.player.getBlockPos().up().equals(pos) && !mc.player.isCrawling())
            return Direction.UP;


        double bestDist = 99999;
        Direction bestSide = null;
        for (Direction side : Direction.values())
        {

            BlockPos neighbour = pos.offset(side);
            BlockState blockState = mc.world.getBlockState(neighbour);


            if (strict)
            {
                if (!canSeeFace(pos, side)) continue;

                if (!blockState.isReplaceable() && pos != ignore) continue;
            }


            if (side.equals(Direction.UP))
            {
                return side;
            }
            double dist = distanceTo(pos, side);
            if (bestSide == null || dist < bestDist)
            {
                bestDist = dist;
                bestSide = side;
            }
        }

        return bestSide;
    }


    public static Direction getClosestSide(BlockPos pos)
    {


        double bestDist = 99999;
        Direction bestSide = null;
        for (Direction side : Direction.values())
        {
            double dist = distanceTo(pos, side);
            if (bestSide == null || dist < bestDist)
            {
                bestDist = dist;
                bestSide = side;
            }
        }

        return bestSide;
    }

    public static boolean placeBlock(BlockPos pos, Direction direction, Hand hand, boolean packet)
    {
        return placeBlock(pos, direction, hand, packet, true);
    }


    public static boolean placeBlock(BlockPos pos, Direction direction, boolean packet, boolean swing)
    {
        return placeBlock(pos, direction, Hand.MAIN_HAND, packet, swing);
    }

    public static boolean placeBlock(BlockPos pos, Direction direction, boolean packet)
    {
        return placeBlock(pos, direction, Hand.MAIN_HAND, packet, true);
    }


    public static boolean placeBlock(BlockPos pos, Direction direction, Hand hand, boolean packet, boolean swing)
    {
        if (direction == null) return false;

        BlockPos neighbour = pos.offset(direction);
        Direction opposite = direction.getOpposite();
        Vec3d hitVec = neighbour.toCenterPos().add(new Vec3d(opposite.getUnitVector()).multiply(0.5));
        BlockHitResult hitResult = new BlockHitResult(hitVec, opposite, neighbour, false);


        AntiCheat.INSTANCE.handleMultiTask();
        boolean unShift = false;
        if (!mc.player.isSneaking() && SNEAK_BLOCKS.contains(mc.world.getBlockState(neighbour).getBlock()))
        {
            PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            unShift = true;
        }

        if (packet)
        {


            PacketManager.INSTANCE.sendPacket(id -> new PlayerInteractBlockC2SPacket(hand, hitResult, id));
            if (swing)
                mc.player.swingHand(hand);
            else
                PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(hand));


            if (unShift)
                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

            return true;
        } else
        {


            ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hitResult);
            if (swing)
                mc.player.swingHand(hand);
            else
                PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(hand));


            if (unShift)
                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

            return result.isAccepted();
        }
    }



//
//    public static boolean placeBLockAirPlace(BlockPos pos, boolean strict, Hand hand, boolean packet, boolean swing)
//    {
//
//        Direction dir = BlockUtils.getPlaceableSide(pos, strict);
//
//        if(dir != null){
//
//            return  placeBlock(pos, dir, hand, packet, swing);;
//        }
//
//        if (direction == null) return false;
//
//        BlockPos neighbour = pos.offset(direction);
//        Direction opposite = direction.getOpposite();
//        Vec3d hitVec = neighbour.toCenterPos().add(new Vec3d(opposite.getUnitVector()).multiply(0.5));
//        BlockHitResult hitResult = new BlockHitResult(hitVec, opposite, neighbour, false);
//
//
//        AntiCheat.INSTANCE.handleMultiTask();
//        boolean unShift = false;
//        if (!mc.player.isSneaking() && SNEAK_BLOCKS.contains(mc.world.getBlockState(neighbour).getBlock()))
//        {
//            PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
//            unShift = true;
//        }
//
//        if (packet)
//        {
//
//
//            PacketManager.INSTANCE.sendPacket(id -> new PlayerInteractBlockC2SPacket(hand, hitResult, id));
//            if (swing)
//                mc.player.swingHand(hand);
//            else
//                PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(hand));
//
//
//            if (unShift)
//                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
//
//            return true;
//        } else
//        {
//
//
//            ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hitResult);
//            if (swing)
//                mc.player.swingHand(hand);
//            else
//                PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(hand));
//
//
//            if (unShift)
//                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
//
//            return result.isAccepted();
//        }
//    }




    public static boolean placeFrame(BlockPos pos, Direction direction, Hand hand, boolean packet, boolean swing)
    {
        if (direction == null) return false;


        Vec3d hitVec = pos.toCenterPos().add(new Vec3d(direction.getUnitVector()).multiply(0.5));
        BlockHitResult hitResult = new BlockHitResult(hitVec, direction, pos, false);


        AntiCheat.INSTANCE.handleMultiTask();
        boolean unShift = false;
        if (!mc.player.isSneaking() && SNEAK_BLOCKS.contains(mc.world.getBlockState(pos).getBlock()))
        {
            PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            unShift = true;
        }

        if (packet)
        {


            PacketManager.INSTANCE.sendPacket(id -> new PlayerInteractBlockC2SPacket(hand, hitResult, id));
            if (swing)
                mc.player.swingHand(hand);
            else
                PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(hand));


            if (unShift)
                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

            return true;
        } else
        {


            ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hitResult);
            if (swing)
                mc.player.swingHand(hand);
            else
                PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(hand));


            if (unShift)
                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

            return result.isAccepted();
        }
    }

    public static boolean placeBlock(BlockPos pos, Direction direction, Hand hand, boolean packet, boolean swing, Vec3d vec)
    {
        if (direction == null) return false;

        BlockPos neighbour = pos.offset(direction);
        Direction opposite = direction.getOpposite();
        BlockHitResult hitResult = new BlockHitResult(vec, opposite, neighbour, false);


        AntiCheat.INSTANCE.handleMultiTask();


        boolean unShift = false;
        if (!mc.player.isSneaking() && SNEAK_BLOCKS.contains(mc.world.getBlockState(neighbour).getBlock()))
        {
            PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            unShift = true;
        }

        if (packet)
        {


            PacketManager.INSTANCE.sendPacket(id -> new PlayerInteractBlockC2SPacket(hand, hitResult, id));
            if (swing)
                mc.player.swingHand(hand);
            else
                PacketManager.INSTANCE.sendPacket(id -> new HandSwingC2SPacket(hand));


//            if (unShift)
//            {
//                mc.player.setSneaking(false);
//                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
//            }


            if (unShift)
                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));


            return true;
        } else
        {


            ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hitResult);
            if (swing)
                mc.player.swingHand(hand);
            else
                PacketManager.INSTANCE.sendPacket(id -> new HandSwingC2SPacket(hand));


//            if (unShift)
//            {
//                mc.player.setSneaking(false);
//                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
//            }
            if (unShift)
                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));


            return result.isAccepted();
        }
    }

    public static void openBlock(BlockPos pos)
    {
        Direction direction = BlockUtils.getMineableSide(pos, false);
        if (direction != null)
            rightClick(pos, direction, Hand.MAIN_HAND);
    }

    public static void rightClick(BlockPos pos, Direction direction, Hand hand)
    {
        Vec3d hitVec = pos.toCenterPos().add(new Vec3d(direction.getUnitVector()).multiply(0.5));
        BlockHitResult hitResult = new BlockHitResult(hitVec, direction, pos, false);

        mc.interactionManager.interactBlock(mc.player, hand, hitResult);
        mc.player.swingHand(hand);
    }

    public static void interactInternal(BlockPos pos, Direction direction, Hand hand, Vec3d vec)
    {

        BlockPos neighbour = pos.offset(direction);
        Direction opposite = direction.getOpposite();
        BlockHitResult hitResult = new BlockHitResult(vec, opposite, neighbour, false);
        ((IClientPlayerInteractionManager) mc.interactionManager).invokeInteractInternal(mc.player, hand, hitResult);
    }

    public static BlockPos getRoundedBlockPos(final double x, final double y, final double z)
    {
        final int flooredX = MathHelper.floor(x);
        final int flooredY = (int) Math.round(y);
        final int flooredZ = MathHelper.floor(z);
        return new BlockPos(flooredX, flooredY, flooredZ);
    }


    public static Direction getMineableSide(BlockPos pos, boolean strict)
    {
        Direction bestSide = null;

        for (Direction side : Direction.values())
        {

            if (strict && !canSeeFace(pos, side)) continue;

            if (side.equals(Direction.UP))
            {
                return side;
            }


            bestSide = side;
        }

        return bestSide;
    }

    public static double distanceTo(BlockPos pos, Direction direction)
    {
        return mc.player.getEyePos().distanceTo(new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f)
                .add(Vec3d.of(direction.getVector())));
    }

    public static BlockState getBlockState(BlockPos pos)
    {
        return mc.world.getBlockState(pos);
    }

    public static boolean canSeeFace(final BlockPos target, final Direction face)
    {
        final Vec3d eyes = mc.player.getEyePos();
        final Vec3d centered = new Vec3d(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        final Vec3i dir = face.getVector();
        final Vec3d offset = new Vec3d((double) dir.getX(), (double) dir.getY(), (double) dir.getZ());
        final Vec3d scaled = new Vec3d(offset.x * 0.5, offset.y * 0.5, offset.z * 0.5);
        final Vec3d pos = centered.add(scaled);
        switch (face)
        {
            case NORTH:
            {
                return eyes.z < pos.z;
            }
            case EAST:
            {
                return eyes.x > pos.x;
            }
            case SOUTH:
            {
                return eyes.z > pos.z;
            }
            case WEST:
            {
                return eyes.x < pos.x;
            }
            case UP:
            {
                if (AntiCheat.INSTANCE.acMode.getValue().equals("Strong"))
                    return eyes.y > pos.y;

                return (eyes.y + 0.5) > pos.y;
            }
            case DOWN:
            {
                return eyes.y < pos.y;
            }
            default:
            {
                return false;
            }
        }
    }

    public static boolean isUnbreakable(BlockState state)
    {


        return state.getBlock().getHardness() == -1.0f && !state.isAir() && !mc.player.isCreative();
    }

    public static boolean isMineable(BlockState state)
    {


        return state.getBlock().getHardness() != -1.0f && !state.isAir() && !mc.player.isCreative();
    }

    public static boolean canSeeFaceMotion(final BlockPos target, final Direction face)
    {
        Vec3d eyes = mc.player.getEyePos();
        eyes = eyes.add(mc.player.getVelocity().x, mc.player.getVelocity().y, mc.player.getVelocity().z);
        final Vec3d centered = new Vec3d(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        final Vec3i dir = face.getVector();
        final Vec3d offset = new Vec3d((double) dir.getX(), (double) dir.getY(), (double) dir.getZ());
        final Vec3d scaled = new Vec3d(offset.x * 0.5, offset.y * 0.5, offset.z * 0.5);
        final Vec3d pos = centered.add(scaled);
        switch (face)
        {
            case NORTH:
            {
                return eyes.z < pos.z;
            }
            case EAST:
            {
                return eyes.x > pos.x;
            }
            case SOUTH:
            {
                return eyes.z > pos.z;
            }
            case WEST:
            {
                return eyes.x < pos.x;
            }
            case UP:
            {
                if (AntiCheat.INSTANCE.acMode.getValue().equals("Strong"))
                    return eyes.y > pos.y;

                return (eyes.y + 0.5) > pos.y;
            }
            case DOWN:
            {
                return eyes.y < pos.y;
            }
            default:
            {
                return false;
            }
        }
    }

    public static double getDistanceSq(BlockPos pos)
    {
        return pos.getSquaredDistanceFromCenter(mc.player.getX(), mc.player.getEyePos().y, mc.player.getZ());
    }

    public static double getDistanceSq(Entity from, BlockPos to)
    {
        return from.squaredDistanceTo(new Vec3d(to.getX(), to.getY(), to.getZ()));
    }


    public static List<BlockPos> getAllInBox(Box box, BlockPos pos)
    {
        final List<BlockPos> intersections = new ArrayList<>();
        for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++)
        {
            for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++)
            {
                intersections.add(new BlockPos(x, pos.getY(), z));
            }
        }

        return intersections;
    }


    public static List<BlockPos> getAllInBox(Box box)
    {
        final List<BlockPos> intersections = new ArrayList<>();
        for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++)
        {
            for (int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); y++)
            {
                for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++)
                {
                    intersections.add(new BlockPos(x, y, z));
                }
            }
        }
        return intersections;
    }


    public static List<BlockPos> isInBlock(Box box, boolean noSelf)
    {
        final List<BlockPos> blockingBlocks = new ArrayList<>();
        for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++)
        {
            for (int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); y++)
            {
                for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (noSelf && pos.equals(mc.player.getBlockPos().up())) continue;
                    ;

                    if (mc.world.getBlockState(pos).blocksMovement()) blockingBlocks.add(pos);
                }
            }
        }

        return blockingBlocks;
    }


    public static Direction getPlaceableSideMotion(BlockPos pos, boolean strict)
    {

        for (Direction direction : Direction.values())
        {
            BlockPos targetPos = pos.offset(direction);

            BlockState state = mc.world.getBlockState(pos.offset(direction));

            if (state.isReplaceable()) continue;

            if (strict && !canSeeFaceMotion(targetPos, direction.getOpposite()))
                continue;

            return direction;
        }
        return null;
    }

    public static Direction getPlaceableSide(BlockPos pos, boolean strict)
    {

        for (Direction direction : Direction.values())
        {
            BlockPos targetPos = pos.offset(direction);

            BlockState state = mc.world.getBlockState(targetPos);

            if (state.isReplaceable()) continue;


            if (strict && !canSeeFace(targetPos, direction.getOpposite()))
                continue;

            return direction;
        }
        return null;
    }

    public static Direction getPlaceableAirplace(BlockPos pos, boolean strict)
    {

        for (Direction direction : Direction.values())
        {
            BlockPos targetPos = pos.offset(direction);

            if (strict && !canSeeFace(targetPos, direction.getOpposite()))
                continue;

            return direction;
        }
        return null;
    }

    public static boolean canPlaceBlock(BlockPos pos, boolean strictDirection, Set<BlockPos> placed)
    {
        boolean allow = true;
        if (!BlockUtils.isReplaceable(pos))
        {
            return false;
        }
        if (BlockUtils.getPlaceableSide(pos, strictDirection, placed) == null)
        {
            return false;
        }

        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof PlayerEntity)
            {
                allow = false;
                break;
            }
        }
        return allow;
    }

    public static boolean isBlockedOff(BlockPos pos){
        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof PlayerEntity)
            {
                return true;
            }
        }
        return false;
    }


    public static Direction getPlaceableSide(BlockPos pos, boolean strict, List<BlockPos> placed)
    {

        for (Direction direction : Direction.values())
        {
            BlockPos targetPos = pos.offset(direction);

            BlockState state = mc.world.getBlockState(targetPos);

            if (state.isReplaceable() && !placed.contains(targetPos)) continue;

            if (SNEAK_BLOCKS.contains(state.getBlock())) continue;

            if (strict && !canSeeFace(targetPos, direction.getOpposite()))
                continue;

            return direction;
        }
        return null;
    }

    public static Direction getPlaceableSide(BlockPos pos, boolean strict, Set<BlockPos> placed)
    {

        for (Direction direction : Direction.values())
        {
            BlockPos targetPos = pos.offset(direction);

            BlockState state = mc.world.getBlockState(targetPos);

            if (state.isReplaceable() && !placed.contains(targetPos)) continue;


            if (strict && !canSeeFace(targetPos, direction.getOpposite()))
                continue;

            return direction;
        }
        return null;
    }


    public static Direction getPlaceableSide(BlockPos pos, boolean strict, BlockPos pretend)
    {

        for (Direction side : Direction.values())
        {

            BlockPos neighbour = pos.offset(side);
            BlockState blockState = mc.world.getBlockState(neighbour);


            if (SNEAK_BLOCKS.contains(blockState.getBlock())) continue;


            if (!pretend.equals(neighbour))
                if (!blockState.isReplaceable())
                {
                    continue;
                }

            if (strict && !canSeeFace(neighbour, side.getOpposite())) continue;

            if (pretend.equals(neighbour)) return side;

            if (!blockState.isReplaceable())
                return side;
        }

        return null;
    }


    public static Direction getFacing(BlockPos pos)
    {
        for (Direction facing : Direction.values())
        {
            if (!mc.world.getBlockState(pos.offset(facing)).isReplaceable())
            {

                if (SNEAK_BLOCKS.contains(mc.world.getBlockState(pos.offset(facing)).getBlock())) continue;


                return facing;
            }
        }

        return null;
    }

    public static boolean isReplaceable(BlockPos pos)
    {
        return mc.world.getBlockState(pos).isReplaceable();
    }


    public static boolean isInterceptedByCrystal(BlockPos pos)
    {
        for (Entity entity : mc.world.getEntities())
        {
            if (!(CrystalUtil.isEndCrystal(entity)) || !new Box(pos).intersects(entity.getBoundingBox()))
                continue;
            return true;
        }
        return false;
    }

    public static boolean isInterceptedByCrystalThatIsntHit(BlockPos pos)
    {
        for (Entity entity : mc.world.getEntities())
        {
            if (!(CrystalUtil.isEndCrystal(entity)) || !new Box(pos).intersects(entity.getBoundingBox()))
                continue;

            if(CatAura.INSTANCE.hitCrystals.containsKey(entity.getId()))
                continue;
            return true;
        }
        return false;
    }

    public static boolean isInterceptedBy(BlockPos pos, Entity e)
    {
        return new Box(pos).intersects(e.getBoundingBox());
    }

    public static EndCrystalEntity attackInPos(BlockPos pos, int ticksExisted)
    {
        EndCrystalEntity crystal = null;
        double bestDistance = 9999;
        for (Entity entity : mc.world.getEntities())
        {
            if (entity == null || mc.player.distanceTo(entity) > 3 || !CrystalUtil.isEndCrystal(entity) || !entity.isAlive())
                continue;

            if (ticksExisted != 0)
                if (entity.age <= ticksExisted)
                    continue;

            double distance = mc.player.distanceTo(entity);
            if (bestDistance > distance)
            {
                crystal = (EndCrystalEntity) entity;
                bestDistance = distance;
            }
        }


        if (crystal != null)
        {
            PlayerUtils.attackTarget(crystal);
            return crystal;
        }
        return null;
    }

    public static boolean isInterceptedByOtherPlayer(BlockPos pos)
    {
        for (Entity entity : mc.world.getEntities())
        {
            if (entity instanceof PlayerEntity && entity != mc.player && new Box(pos).intersects(entity.getBoundingBox()))
                return true;
        }
        return false;
    }

    public static boolean isBlockedByPlayer(BlockPos pos)
    {
        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof PlayerEntity)
                return true;
        }
        return false;
    }

    public static boolean isBlockedByIgnoreEntity(BlockPos pos)
    {
        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof EndCrystalEntity)
                continue;

            if (entity instanceof ItemEntity)
                continue;

            return true;

        }
        return false;
    }

    public static boolean isInRange(BlockPos pos, double distance)
    {
        return !(getDistanceSq(pos) > MathUtil.square(distance));
    }


    public static boolean canPlaceBlockPretendDistance(BlockPos pos, boolean strictDirection, BlockPos pretend, double distance)
    {

        boolean allow = true;
        Block block = mc.world.getBlockState(pos).getBlock();

        if (!(block instanceof AirBlock) && !(block instanceof FluidBlock))
        {
            return false;
        }


        if (!mc.world.getBlockState(pos).isReplaceable())
        {
            return false;
        }
        if (BlockUtils.getPlaceableSide(pos, strictDirection, pretend) == null)
        {
            return false;
        }

        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (entity instanceof PlayerEntity)
            {
                allow = false;
                break;
            }
        }

        if (getDistanceSq(pos) > MathUtil.square(distance))
            return false;
        return allow;
    }


    public static boolean willPlaceVertical(BlockPos pos)
    {
        if (Math.abs(mc.player.getX() - (double) ((float) pos.getX() + 0.5f))
                < 2.0
                && Math.abs(mc.player.getZ() - (double) ((float) pos.getZ() + 0.5f))
                < 2.0)
        {
            double y = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());

            if (y - (double) pos.getY() > 2.0)
            {
                return true;
            }

            return (double) pos.getY() - y > 0.0;
        }
        return false;
    }

    public static boolean canPlaceWeb(BlockPos pos, boolean strictDirection)
    {
        boolean allow = true;

        Block block = mc.world.getBlockState(pos).getBlock();


        if (!mc.world.getBlockState(pos).isReplaceable())
        {
            allow = false;
            return allow;
        }
        if (BlockUtils.getPlaceableSide(pos, strictDirection) == null)
        {
            allow = false;
            return allow;
        }

        return allow;
    }

    public static List<WorldChunk> getLoadedChunks()
    {
        List<WorldChunk> chunks = new ArrayList<>();
        int viewDist = mc.options.getViewDistance().getValue();
        for (int x = -viewDist; x <= viewDist; x++)
        {
            for (int z = -viewDist; z <= viewDist; z++)
            {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk((int) mc.player.getX() / 16 + x, (int) mc.player.getZ() / 16 + z);

                if (chunk != null) chunks.add(chunk);
            }
        }
        return chunks;
    }

    public static List<BlockEntity> getBlockEntities()
    {
        List<BlockEntity> list = new ArrayList<>();
        for (WorldChunk chunk : getLoadedChunks())
            list.addAll(chunk.getBlockEntities().values());

        return list;
    }

}