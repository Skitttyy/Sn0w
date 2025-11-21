package me.skitttyy.kami.api.utils.world;

import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Smart Raytracing.
 * It's recommended to use a resolution of -1.0 whenever possible.
 * TODO: Use Resolution for Offsets to the corners
 */
public class RaytraceUtils implements IMinecraft
{

    public static BlockHitResult getResult(Vec3d vec)
    {
        float[] rotation = RotationUtils.getRotationsTo(mc.player.getEyePos(), vec);
        return RaytraceUtils.getBlockHitResult(rotation[0], rotation[1], 7);
    }

    public static BlockHitResult getBlockHitResult(float yaw, float pitch, RaycastContext.ShapeType type)
    {
        return getBlockHitResult(yaw, pitch, (float) mc.player.getAttributes().getValue(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE), mc.player, type);
    }


    public static BlockHitResult getBlockHitResult(float yaw, float pitch)
    {
        return getBlockHitResult(yaw, pitch, (float) mc.player.getAttributes().getValue(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE));
    }

    public static BlockHitResult getBlockHitResultWithEntity(float yaw, float pitch, Entity from)
    {
        return getBlockHitResult(yaw, pitch, (float) mc.player.getAttributes().getValue(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE), from, RaycastContext.ShapeType.VISUAL);
    }

    public static BlockHitResult getBlockHitResult(float yaw, float pitch, float distance)
    {
        return getBlockHitResult(yaw, pitch, distance, mc.player, RaycastContext.ShapeType.VISUAL);
    }

    public static BlockHitResult getBlockHitResult(float yaw, float pitch, float d, Entity from, RaycastContext.ShapeType shapeType)
    {
        Vec3d vec3d = mc.player.getEyePos();
        Vec3d lookVec = RotationUtils.getRotationVector(yaw, pitch);
        Vec3d rotations = vec3d.add(lookVec.x * d, lookVec.y * d, lookVec.z * d);

        return Optional.ofNullable(
                        mc.world.raycast(new RaycastContext(vec3d, rotations, shapeType, RaycastContext.FluidHandling.NONE, from)))
                .orElseGet(() ->
                        new BlockHitResult(new Vec3d(0.5, 1.0, 0.5), Direction.UP, BlockPos.ORIGIN, false));
    }

    public static HitResult rayCast(final double reach, final float[] angles)
    {
        final double eyeHeight = mc.player.getStandingEyeHeight();
        final Vec3d eyes = new Vec3d(mc.player.getX(), mc.player.getY() + eyeHeight, mc.player.getZ());
        return rayCast(reach, eyes, angles);
    }

    public static HitResult rayCast(final double reach, Vec3d position, final float[] angles)
    {
        // learn to give me real rotations
        if (Float.isNaN(angles[0]) || Float.isNaN(angles[1]))
        {
            return null;
        }

        final Vec3d rotationVector = RotationUtils.getRotationVector(angles[1], angles[0]);
        return mc.world.raycast(new RaycastContext(
                position,
                position.add(rotationVector.x * reach, rotationVector.y * reach, rotationVector.z * reach),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player));
    }


    public static HitResult isLookingResult(Entity camera, Entity target, Vec3d position, final float[] angles, float reach)
    {
        double d = Math.max(reach, reach);
        double e = MathHelper.square(d);

        Vec3d vec3d = position;
        Vec3d vec3d2 = RotationUtils.getRotationVector(angles[0], angles[1]);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
        Box box = camera.getBoundingBox().stretch(vec3d2.multiply(d)).expand(1.0, 1.0, 1.0);
        return ProjectileUtil.raycast(camera, vec3d, vec3d3, box, (entity) ->
        {
            return entity.canHit() && entity.equals(target);
        }, e);
    }


    public static Vec3d getVisibleDirectionPoint(@NotNull Direction dir, @NotNull BlockPos bp, float wallRange, float range)
    {
        Box brutBox = getDirectionBox(dir);

        // EAST, WEST
        if (brutBox.maxX - brutBox.minX == 0)
            for (double y = brutBox.minY; y < brutBox.maxY; y += 0.1f)
                for (double z = brutBox.minZ; z < brutBox.maxZ; z += 0.1f)
                {
                    Vec3d point = new Vec3d(bp.getX() + brutBox.minX, bp.getY() + y, bp.getZ() + z);

                    if (shouldSkipPoint(point, bp, dir, wallRange, range))
                        continue;

                    return point;
                }


        // DOWN, UP
        if (brutBox.maxY - brutBox.minY == 0)
            for (double x = brutBox.minX; x < brutBox.maxX; x += 0.1f)
                for (double z = brutBox.minZ; z < brutBox.maxZ; z += 0.1f)
                {
                    Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + brutBox.minY, bp.getZ() + z);

                    if (shouldSkipPoint(point, bp, dir, wallRange, range))
                        continue;

                    return point;
                }


        // NORTH, SOUTH
        if (brutBox.maxZ - brutBox.minZ == 0)
            for (double x = brutBox.minX; x < brutBox.maxX; x += 0.1f)
                for (double y = brutBox.minY; y < brutBox.maxY; y += 0.1f)
                {
                    Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + y, bp.getZ() + brutBox.minZ);

                    if (shouldSkipPoint(point, bp, dir, wallRange, range))
                        continue;

                    return point;
                }


        return null;
    }

    private static @NotNull Box getDirectionBox(Direction dir)
    {
        return switch (dir)
        {
            case UP -> new Box(.15f, 1f, .15f, .85f, 1f, .85f);
            case DOWN -> new Box(.15f, 0f, .15f, .85f, 0f, .85f);

            case EAST -> new Box(1f, .15f, .15f, 1f, .85f, .85f);
            case WEST -> new Box(0f, .15f, .15f, 0f, .85f, .85f);

            case NORTH -> new Box(.15f, .15f, 0f, .85f, .85f, 0f);
            case SOUTH -> new Box(.15f, .15f, 1f, .85f, .85f, 1f);
        };
    }

    private static boolean shouldSkipPoint(Vec3d point, BlockPos bp, Direction dir, float wallRange, float range)
    {
        RaycastContext context = new RaycastContext(getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
        BlockHitResult result = mc.world.raycast(context);

        float dst = squaredDistanceFromEyes(point);

        if (result != null
                && result.getType() == HitResult.Type.BLOCK
                && !result.getBlockPos().equals(bp)
                && dst > wallRange * wallRange)
            return true;

        return dst > range * range;
    }

    public static float squaredDistanceFromEyes(@NotNull Vec3d vec)
    {
        double d0 = vec.x - mc.player.getX();
        double d1 = vec.z - mc.player.getZ();
        double d2 = vec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        return (float) (d0 * d0 + d1 * d1 + d2 * d2);
    }


    public static Vec3d getEyesPos(@NotNull Entity entity)
    {
        return entity.getPos().add(0, entity.getEyeHeight(entity.getPose()), 0);
    }

}