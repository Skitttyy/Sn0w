package me.skitttyy.kami.api.utils.players.rotation;

import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;

public class RotationUtils implements IMinecraft {
    public static float[] getRotationsTo(Vec3d src, Vec3d dest)
    {
        float yaw = (float) (Math.toDegrees(Math.atan2(dest.subtract(src).z, dest.subtract(src).x)) - 90);
        float pitch = (float) Math.toDegrees(-Math.atan2(dest.subtract(src).y, Math.hypot(dest.subtract(src).x, dest.subtract(src).z)));
        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch)};
    }



    public static double lookDistanceTo(float yaw, float pitch)
    {
        double yawChange = Math.abs(yaw - mc.player.getYaw());
        double pitchChange = Math.abs(pitch - mc.player.getPitch());

        return Math.sqrt(yawChange * yawChange + pitchChange * pitchChange);
    }


    public static void setRotation(float[] rotation)
    {
        RotationManager.INSTANCE.rotateTo(new Rotation(100, rotation[0], rotation[1]));
    }


    public static void setRotation(Rotation rotation)
    {
        RotationManager.INSTANCE.rotateTo(rotation);
    }
    public static void setRotation(float[] rotation, int priority)
    {
        RotationManager.INSTANCE.rotateTo(new Rotation(priority, rotation[0], rotation[1]));
    }

    public static void setRotation(float yaw, float pitch, int priority)
    {
        RotationManager.INSTANCE.rotateTo(new Rotation(priority, yaw, pitch));
    }

    public static void setRotation(float yaw, float pitch)
    {
        RotationManager.INSTANCE.rotateTo(new Rotation(100, yaw, pitch));
    }




    public static float[] getBlockRotations(BlockPos pos, Direction facing)
    {
        if (facing == null) return null;

        return getBlockRotations(pos, facing, mc.player);
    }


    public static float fixYaw(float yaw)
    {
        float prevYaw = RotationManager.INSTANCE.getServerYaw();
        float diff = yaw - prevYaw;

        if (diff < -180.0f || diff > 180.0f)
        {
            float round = Math.round(Math.abs(diff / 360.0f));
            diff = diff < 0.0f ? diff + 360.0f * round : diff - (360.0f * round);
        }
        return yaw;
    }


    public static void doSilentRotate(Rotation rotation){
        PacketManager.INSTANCE.sendQuietPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), rotation.getYaw(), rotation.getPitch(), mc.player.isOnGround()));
    }
    public static void doSilentRotate(float[] rotation){
        PacketManager.INSTANCE.sendQuietPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), rotation[0], rotation[1], mc.player.isOnGround()));
    }



    public static void silentSync(){
        PacketManager.INSTANCE.sendQuietPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), RotationUtils.getActualYaw(), RotationUtils.getActualPitch(), mc.player.isOnGround()));
    }
    public static void doRotate(BlockPos pos, boolean strictDirection)
    {
        float[] rots = getBlockRotations(pos, BlockUtils.getPlaceableSide(pos, strictDirection));

        if (rots != null)
            setRotation(rots);
    }

    public static void doSilentRotate(BlockPos pos, boolean strictDirection)
    {
        float[] rots = getBlockRotations(pos, BlockUtils.getPlaceableSide(pos, strictDirection));

        if (rots != null)
            doSilentRotate(rots);
    }
    public static void doSilentRotate(BlockPos pos, Direction direction)
    {
        float[] rots = getBlockRotations(pos, direction);

        if (rots != null)
            doSilentRotate(rots);
    }

    public static void doRotate(BlockPos pos, Direction direction)
    {
        float[] rots = getBlockRotations(pos, direction);

        if (rots != null)
            setRotation(rots);
    }
    public static void doRotate(BlockPos pos)
    {
        float[] rots = getRotationsTo(mc.player.getEyePos(), pos.toCenterPos());

        if (rots != null)
            setRotation(rots);
    }



    public static float[] getBlockRotations(BlockPos pos, Direction facing, Entity from)
    {
        Vec3d hitVec = pos.toCenterPos().add(new Vec3d(facing.getUnitVector()).multiply(0.5));
        return getRotationsTo(from.getEyePos(), hitVec);
    }
//    public static float[] getBlockRotations(BlockPos pos, Direction facing, Entity from) {
//        Box bb = BlockUtils.getCombinedBox(pos);
//
//        double x = pos.getX() + (bb.minX + bb.maxX) / 2.0;
//        double y = pos.getY() + (bb.minY + bb.maxY) / 2.0;
//        double z = pos.getZ() + (bb.minZ + bb.maxZ) / 2.0;
//
//        if (facing != null) {
//            x += facing.getVector().getX() * ((bb.minX + bb.maxX) / 2.0);
//            y += facing.getVector().getY() * ((bb.minY + bb.maxY) / 2.0);
//            z += facing.getVector().getZ() * ((bb.minZ + bb.maxZ) / 2.0);
//        }
//
//        return getRotations(x, y, z, from);
//    }

    public static float[] getRotations(double x, double y, double z, Entity f)
    {
        Vec3d eyePos = f.getEyePos();
        return getBlockRotations(x, y, z, (float) eyePos.x, (float) eyePos.y, (float) eyePos.z);
    }

    public static float[] getRotations(BlockPos pos,
                                       Direction facing,
                                       Entity from,
                                       ClientWorld world,
                                       BlockState state)
    {
        VoxelShape shape = state.getCollisionShape(world, pos);
        Box bb = !shape.isEmpty() ? shape.getBoundingBox() : BlockUtils.EMPTY_BOX;

        double x = pos.getX() + (bb.minX + bb.maxX) / 2.0;
        double y = pos.getY() + (bb.minY + bb.maxY) / 2.0;
        double z = pos.getZ() + (bb.minZ + bb.maxZ) / 2.0;

        if (facing != null)
        {
            x += facing.getOffsetX() * ((bb.minX + bb.maxX) / 2.0);
            y += facing.getOffsetY() * ((bb.minY + bb.maxY) / 2.0);
            z += facing.getOffsetZ() * ((bb.minZ + bb.maxZ) / 2.0);
        }

        return getRotations(x, y, z, from);
    }

    public static float[] getBlockRotations(double x, double y, double z, double fromX, double fromY, double fromZ)
    {
        double xDiff = x - fromX;
        double yDiff = y - fromY;
        double zDiff = z - fromZ;
        double dist = MathHelper.sqrt((float) (xDiff * xDiff + zDiff * zDiff));

        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(yDiff, dist) * 180.0 / Math.PI));
        // Is there a better way than to use the previous yaw?


        return new float[]{fixYaw(yaw), pitch};
    }

    public static Vec3d getRotationVector(float yaw, float pitch)
    {
        float vx = -MathHelper.sin(MathUtil.rad(yaw)) * MathHelper.cos(MathUtil.rad(pitch));
        float vz = MathHelper.cos(MathUtil.rad(yaw)) * MathHelper.cos(MathUtil.rad(pitch));
        float vy = -MathHelper.sin(MathUtil.rad(pitch));
        return new Vec3d(vx, vy, vz);
    }


    public static float getActualYaw()
    {
        if (RotationManager.INSTANCE.getRotation() != null)
            return RotationManager.INSTANCE.getRotation().getYaw();

        return mc.player.getYaw();
    }




    public static float getActualPitch()
    {
        if (RotationManager.INSTANCE.getRotation() != null)
            return RotationManager.INSTANCE.getRotation().getPitch();

        return mc.player.getPitch();
    }

    public static void packetRotate(float[] rots)
    {
        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rots[0], rots[1], mc.player.isOnGround()));
    }

    public static void packetRotate(float yaw, float pitch)
    {
        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
    }

}
