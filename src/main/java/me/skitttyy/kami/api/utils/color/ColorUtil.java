package me.skitttyy.kami.api.utils.color;


import me.skitttyy.kami.api.utils.math.MathUtil;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ColorUtil {

    public static Color newAlpha(Color color, int alpha)
    {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    public static int newAlpha(int in, int alpha)
    {
        Color color = new Color(in);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
    }
    public static VertexConsumer color(VertexConsumer consumer, Color color)
    {
        int rgb = color.getRGB();

        float f = (float) ColorHelper.Argb.getAlpha(rgb) / 255.0f;
        float g = (float) ColorHelper.Argb.getRed(rgb) / 255.0f;
        float h = (float) ColorHelper.Argb.getGreen(rgb) / 255.0f;
        float j = (float) ColorHelper.Argb.getBlue(rgb) / 255.0f;
        return consumer.color(g, h, j, f);
    }
    public static Color brighten(Color color, double fraction) {

        int red = (int) Math.round(Math.min(255, color.getRed() + 255 * fraction));
        int green = (int) Math.round(Math.min(255, color.getGreen() + 255 * fraction));
        int blue = (int) Math.round(Math.min(255, color.getBlue() + 255 * fraction));

        int alpha = color.getAlpha();

        return new Color(red, green, blue, alpha);

    }
    public static void reset()
    {
        GL11.glColor4f(1, 1, 1, 1);
    }

    public static Color interpolate(float value, Color start, Color end)
    {
        float sr = start.getRed() / 255f;
        float sg = start.getGreen() / 255f;
        float sb = start.getBlue() / 255f;
        float sa = start.getAlpha() / 255f;

        float er = end.getRed() / 255f;
        float eg = end.getGreen() / 255f;
        float eb = end.getBlue() / 255f;
        float ea = end.getAlpha() / 255f;


        float r = sr * value + er * (1f - value);
        float g = sg * value + eg * (1f - value);
        float b = sb * value + eb * (1f - value);
        float a = sa * value + ea * (1f - value);

        return new Color(r, g, b, a);
    }


    public static Color interpolate(float value, Color start, Color middle, Color end)
    {
        if (value < 0.5f)
        {
            //start to middle
            return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(value, 0, 0.5), 0, 1), middle, start);
        } else
        {
            //middle to end
            return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(value, 0.5, 1.0), 0, 1), end, middle);
        }
    }

    public static Color realInterp(float value, Color start, Color middle, Color end)
    {
        if (value < 0.5f)
        {
            //start to middle
            return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(value, 0, 0.5), 0, 1), middle, start);
        }else if(value < 1.0f){
            return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(value, 0.5, 1.0), 0, 1), end, middle);
        } else
        {
            //middle to end
            return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(value, 1.0, 1.5), 0, 1), start, end);
        }
    }

    public static Color hslToColor(float f, float f2, float f3, float f4)
    {
        if (f2 < 0.0f || f2 > 100.0f)
        {
            throw new IllegalArgumentException("Color parameter outside of expected range - Saturation");
        }
        if (f3 < 0.0f || f3 > 100.0f)
        {
            throw new IllegalArgumentException("Color parameter outside of expected range - Lightness");
        }
        if (f4 < 0.0f || f4 > 1.0f)
        {
            throw new IllegalArgumentException("Color parameter outside of expected range - Alpha");
        }
        f %= 360.0f;
        float f5 = 0.0f;
        f5 = (double) f3 < 0.5 ? f3 * (1.0f + f2) : (f3 /= 100.0f) + (f2 /= 100.0f) - f2 * f3;
        f2 = 2.0f * f3 - f5;
        f3 = Math.max(0.0f, colorCalc(f2, f5, (f /= 360.0f) + 0.33333334f));
        float f6 = Math.max(0.0f, colorCalc(f2, f5, f));
        f2 = Math.max(0.0f, colorCalc(f2, f5, f - 0.33333334f));
        f3 = Math.min(f3, 1.0f);
        f6 = Math.min(f6, 1.0f);
        f2 = Math.min(f2, 1.0f);
        return new Color(f3, f6, f2, f4);
    }

    private static float colorCalc(float f, float f2, float f3)
    {
        if (f3 < 0.0f)
        {
            f3 += 1.0f;
        }
        if (f3 > 1.0f)
        {
            f3 -= 1.0f;
        }
        if (6.0f * f3 < 1.0f)
        {
            float f4 = f;
            return f4 + (f2 - f4) * 6.0f * f3;
        }
        if (2.0f * f3 < 1.0f)
        {
            return f2;
        }
        if (3.0f * f3 < 2.0f)
        {
            float f5 = f;
            return f5 + (f2 - f5) * 6.0f * (0.6666667f - f3);
        }
        return f;
    }

    public static Color getLiquidColor(String name, StatusEffect effect)
    {
        return switch (name)
        {
            case "speed" -> new Color(8171462);
            case "slowness" -> new Color(5926017);
            case "haste" -> new Color(14270531);
            case "mining_fatigue" -> new Color(4866583);
            case "strength" -> new Color(9643043);
            case "instant_health" -> new Color(16262179);
            case "instant_damage" -> new Color(4393481);
            case "jump_boost" -> new Color(2293580);
            case "nausea" -> new Color(5578058);
            case "regeneration" -> new Color(13458603);
            case "resistance" -> new Color(10044730);
            case "fire_resistance" -> new Color(14981690);
            case "water_breathing" -> new Color(3035801);
            case "invisibility" -> new Color(8356754);
            case "blindness" -> new Color(2039587);
            case "night_vision" -> new Color(2039713);
            case "hunger" -> new Color(5797459);
            case "weakness" -> new Color(4738376);
            case "poison" -> new Color(5149489);
            case "wither" -> new Color(3484199);
            case "health_boost" -> new Color(16284963);
            case "absorption" -> new Color(2445989);
            case "saturation" -> new Color(16262179);
            case "glowing" -> new Color(9740385);
            case "levitation" -> new Color(13565951);
            case "luck" -> new Color(3381504);
            case "unluck" -> new Color(12624973);
            default -> new Color(effect.getColor());
        };
    }
}
