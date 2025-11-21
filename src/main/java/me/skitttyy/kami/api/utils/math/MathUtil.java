package me.skitttyy.kami.api.utils.math;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.String.join;
import static java.util.Collections.nCopies;

public class MathUtil implements IMinecraft {
    public static float rad(float angle)
    {
        return (float) (angle * Math.PI / 180);
    }

    public static double normalize(double value, double min, double max)
    {
        return ((value - min) / (max - min));
    }

    public static double roundToPlaces(final double number, final int places)
    {
        BigDecimal decimal = new BigDecimal(number);
        decimal = decimal.setScale(places, RoundingMode.HALF_UP);
        return decimal.doubleValue();
    }

    public String getRomanNumber(int number)
    {
        return join("", nCopies(number, "I"))
                .replace("IIIII", "V")
                .replace("IIII", "IV")
                .replace("VV", "X")
                .replace("VIV", "IX")
                .replace("XXXXX", "L")
                .replace("XXXX", "XL")
                .replace("LL", "C")
                .replace("LXL", "XC")
                .replace("CCCCC", "D")
                .replace("CCCC", "CD")
                .replace("DD", "M")
                .replace("DCD", "CM");
    }

    public static int randomInt(int min, int max)
    {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }


    public static double random(double min, double max)
    {
        return Math.random() * (max - min) + min;
    }

    public static float random(float min, float max)
    {
        return (float) (Math.random() * (max - min) + min);
    }

    public static String getOrdinal(int i)
    {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100)
        {
            case 11:
            case 12:
            case 13:
                return "th";
            default:
                return suffixes[i % 10];

        }
    }

    public static Vec3d roundVector(final Vec3d vec3d, final int places)
    {
        return new Vec3d(roundToPlaces(vec3d.x, places), roundToPlaces(vec3d.y, places), roundToPlaces(vec3d.z, places));
    }

    public static int clamp(int num, int min, int max)
    {
        return num < min ? min : Math.min(num, max);
    }

    public static float clamp(float num, float min, float max)
    {
        return num < min ? min : (Math.min(num, max));
    }

    public static double clamp(double num, double min, double max)
    {
        return num < min ? min : (Math.min(num, max));
    }

    public static double round(double value, int places)
    {
        return places < 0 ? value : (new BigDecimal(value)).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    public static float round(float value, int places)
    {
        return places < 0 ? value : (new BigDecimal(value)).setScale(places, RoundingMode.HALF_UP).floatValue();
    }

    public static float round(float value, int places, float min, float max)
    {
        return MathHelper.clamp(places < 0 ? value : (new BigDecimal(value)).setScale(places, RoundingMode.HALF_UP).floatValue(), min, max);
    }

    public static long clamp(long num, long min, long max)
    {
        return num < min ? min : (Math.min(num, max));
    }

    public static double square(final double input)
    {
        return input * input;
    }

    public static float square(final float input)
    {
        return input * input;
    }

    public static double distance(final float x, final float y, final float x1, final float y1)
    {
        return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
    }

    public static double distance(float x, float y, boolean root)
    {
        double change = Math.abs(x - y);


        if (root)
            return Math.sqrt(square(change));

        return change;
    }

    public static double getRandomDoubleInRange(Random p_82716_0_, double p_82716_1_, double p_82716_3_)
    {
        return p_82716_1_ >= p_82716_3_ ? p_82716_1_ : p_82716_0_.nextDouble() * (p_82716_3_ - p_82716_1_) + p_82716_1_;
    }

    public static float wrap(final float valI)
    {
        float val = valI % 360.0f;
        if (val >= 180.0f)
        {
            val -= 360.0f;
        }
        if (val < -180.0f)
        {
            val += 360.0f;
        }
        return val;
    }

    public static Vec3d rotatePitch(Vec3d vec3d, float pitch)
    {
        float f = MathHelper.cos(pitch);
        float f1 = MathHelper.sin(pitch);
        double d0 = vec3d.x;
        double d1 = vec3d.y * (double) f + vec3d.z * (double) f1;
        double d2 = vec3d.z * (double) f - vec3d.y * (double) f1;
        return new Vec3d(d0, d1, d2);
    }

    public static Vec3d rotateYaw(Vec3d vec3d, float yaw)
    {
        float f = MathHelper.cos(yaw);
        float f1 = MathHelper.sin(yaw);
        double d0 = vec3d.x * (double) f + vec3d.z * (double) f1;
        double d1 = vec3d.y;
        double d2 = vec3d.z * (double) f - vec3d.x * (double) f1;
        return new Vec3d(d0, d1, d2);
    }

    public static boolean isThrowable(Item item)
    {
        return item instanceof EnderPearlItem || item instanceof TridentItem || item instanceof ExperienceBottleItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
    }

    private static float getDistance(Item item)
    {
        return item instanceof BowItem ? 1.0f : 0.4f;
    }

    private static float getThrowVelocity(Item item)
    {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) return 0.5f;
        if (item instanceof ExperienceBottleItem) return 0.59f;
        if (item instanceof TridentItem) return 2f;
        return 1.5f;
    }

    private static int getThrowPitch(Item item)
    {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof ExperienceBottleItem)
            return 20;
        return 0;
    }


    @Getter
    @Setter
    public static class Result {
        private final List<Vec3d> points;
        private HitResult hitResult;

        private Result(List<Vec3d> points, HitResult result)
        {
            this.points = points;
            this.hitResult = result;
        }


    }


    public static Result calcTrajectory(Item item, float yaw)
    {
        List<Vec3d> points = new ArrayList<>();
        double x = RenderUtil.interpolate(mc.player.prevX, mc.player.getX(), mc.getRenderTickCounter().getTickDelta(false));
        double y = RenderUtil.interpolate(mc.player.prevY, mc.player.getY(), mc.getRenderTickCounter().getTickDelta(false));
        double z = RenderUtil.interpolate(mc.player.prevZ, mc.player.getZ(), mc.getRenderTickCounter().getTickDelta(false));

        y = y + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;

        if (item == mc.player.getMainHandStack().getItem())
        {
            x = x - MathHelper.cos(yaw / 180.0f * 3.1415927f) * 0.16f;
            z = z - MathHelper.sin(yaw / 180.0f * 3.1415927f) * 0.16f;
        } else
        {
            x = x + MathHelper.cos(yaw / 180.0f * 3.1415927f) * 0.16f;
            z = z + MathHelper.sin(yaw / 180.0f * 3.1415927f) * 0.16f;
        }

        final float maxDist = getDistance(item);
        double motionX = -MathHelper.sin(yaw / 180.0f * 3.1415927f) * MathHelper.cos(mc.player.getPitch() / 180.0f * 3.1415927f) * maxDist;
        double motionY = -MathHelper.sin((RotationUtils.getActualPitch() - getThrowPitch(item)) / 180.0f * 3.141593f) * maxDist;
        double motionZ = MathHelper.cos(yaw / 180.0f * 3.1415927f) * MathHelper.cos(mc.player.getPitch() / 180.0f * 3.1415927f) * maxDist;

        float power = mc.player.getItemUseTime() / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;

        if (power > 1.0f || power == 0)
        {
            power = 1.0f;
        }

        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;

        final float pow = (item instanceof BowItem ? (power * 2.0f) : item instanceof CrossbowItem ? (2.2f) : 1.0f) * getThrowVelocity(item);

        motionX *= pow;
        motionY *= pow;
        motionZ *= pow;
        if (!mc.player.isOnGround())
            motionY += mc.player.getVelocity().getY();

        Vec3d lastPos;
        points.add(new Vec3d(x, y, z));
        for (int i = 0; i < 300; i++)
        {
            lastPos = new Vec3d(x, y, z);
            x += motionX;
            y += motionY;
            z += motionZ;
            if (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() == Blocks.WATER)
            {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else
            {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }

            if (item instanceof BowItem) motionY -= 0.05000000074505806;
            else if (mc.player.getMainHandStack().getItem() instanceof CrossbowItem) motionY -= 0.05000000074505806;
            else motionY -= 0.03f;


            Vec3d pos = new Vec3d(x, y, z);
            for (Entity ent : mc.world.getEntities())
            {
                if (ent instanceof ArrowEntity || ent.equals(mc.player)) continue;
                if (ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.3)))
                {
                    points.add(pos);
                    return new Result(points, new EntityHitResult(ent, pos));
                }
            }

            BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK)
            {
                points.add(bhr.getPos());
                return new Result(points, bhr);
            }

            if (y <= -65) break;
            if (motionX == 0 && motionY == 0 && motionZ == 0) continue;


            points.add(pos);
        }
        return new Result(points, null);

    }

    public static Result calcTrajectory(EnderPearlEntity entity)
    {
        List<Vec3d> points = new ArrayList<>();
        double x = RenderUtil.interpolate(entity.prevX, entity.getX(), mc.getRenderTickCounter().getTickDelta(false));
        double y = RenderUtil.interpolate(entity.prevY, entity.getY(), mc.getRenderTickCounter().getTickDelta(false));
        double z = RenderUtil.interpolate(entity.prevZ, entity.getZ(), mc.getRenderTickCounter().getTickDelta(false));


        double motionX =  entity.getVelocity().x;
        double motionY = entity.getVelocity().y;
        double motionZ = entity.getVelocity().z;



        Vec3d lastPos;
        points.add(new Vec3d(x, y, z));
        for (int i = 0; i < 300; i++)
        {
            lastPos = new Vec3d(x, y, z);
            x += motionX;
            y += motionY;
            z += motionZ;
            if (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() == Blocks.WATER)
            {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else
            {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }

            motionY -= 0.03f;


            Vec3d pos = new Vec3d(x, y, z);
            for (Entity ent : mc.world.getEntities())
            {
                if (ent instanceof ArrowEntity || ent.equals(entity)) continue;
                if (ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.3)))
                {
                    points.add(pos);
                    return new Result(points, new EntityHitResult(ent, pos));
                }
            }

            BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity));
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK)
            {
                points.add(bhr.getPos());
                return new Result(points, bhr);
            }

            if (y <= -65) break;
            if (motionX == 0 && motionY == 0 && motionZ == 0) continue;


            points.add(pos);
        }
        return new Result(points, null);

    }
    


    public static double magnitudeOfAcceleration(double changeX, double changeY, double time)
    {
        double top = MathHelper.sqrt((float) (Math.pow(changeX, 2) + Math.pow(changeY, 2)));


        return top / time;
    }

    public static double fix(double angle)
    {
        return angle < 0 ? angle + 180 : angle;
    }

}
