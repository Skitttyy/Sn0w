package me.skitttyy.kami.api.utils.render;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Interpolator implements IMinecraft {

    public static Vec3d getRenderPosition(Entity entity, float tickDelta) {
        return new Vec3d(entity.getX() - MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
                entity.getY() - MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()),
                entity.getZ() - MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()));
    }
    public static Vec3d getInterpolatedEyePos(Entity entity, float tickDelta) {
        return new Vec3d(entity.getX() - MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
                entity.getY() - MathHelper.lerp(tickDelta, entity.lastRenderY + entity.getStandingEyeHeight(), entity.getY() + entity.getStandingEyeHeight()),
                entity.getZ() - MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()));
    }


    public static Vec3d getInterpolatedPosition(Entity entity, float tickDelta) {
        return new Vec3d(entity.prevX + ((entity.getX() - entity.prevX) * tickDelta),
                entity.prevY + ((entity.getY() - entity.prevY) * tickDelta),
                entity.prevZ + ((entity.getZ() - entity.prevZ) * tickDelta));
    }

    public static float interpolateFloat(float prev, float value, float factor) {
        return prev + ((value - prev) * factor);
    }

    public static double interpolateDouble(double prev, double value, double factor) {
        return prev + ((value - prev) * factor);
    }

    public static Box getInterpolatedBox(Box prevBox, Box box) {

        double delta = mc.isPaused() ? 1f : mc.getRenderTickCounter().getTickDelta(false);

        return new Box(interpolateDouble(prevBox.minX, box.minX, delta),
                interpolateDouble(prevBox.minY, box.minY, delta),
                interpolateDouble(prevBox.minZ, box.minZ, delta),
                interpolateDouble(prevBox.maxX, box.maxX, delta),
                interpolateDouble(prevBox.maxY, box.maxY, delta),
                interpolateDouble(prevBox.maxZ, box.maxZ, delta));
    }

    public static Box getInterpolatedEntityBox(Entity entity){
        Box box = entity.getBoundingBox();
        Box prevBox = entity.getBoundingBox().offset( entity.prevX - entity.getX(), entity.prevY - entity.getY(), entity.prevZ - entity.getZ());

        return getInterpolatedBox(prevBox, box);
    }
}