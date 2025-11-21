package me.skitttyy.kami.api.utils.color;


import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.regex.Pattern;

public class RainbowUtil {

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§[0123456789abcdefklmnor]");

    private static final int[] colorCodes = new int[]{0, 170, 43520, 43690, 11141120, 11141290, 16755200, 11184810, 5592405, 5592575, 5635925, 5636095, 16733525, 16733695, 16777045, 16777215};




    public static void renderWave(DrawContext stack, String s, Float x, Float y)
    {
        Float updateX = x;
        Color rgb;
        final char[] characters = s.toCharArray();
        int height = ClickGui.CONTEXT.getRenderer().getTextHeight("A");
        int displayColor = 0;
        final String[] parts = COLOR_CODE_PATTERN.split(s);

        for (int i = 0; i < s.length(); i++)
        {
            final char colorCode = characters[i];
            if (colorCode == '\u00A7')
            {
                final char colorChar = characters[i + 1];
                final int codeIndex = "0123456789abcdef".indexOf(colorChar);
                if (codeIndex < 0)
                {
                    if (colorChar == ('r'))
                    {
                        displayColor = 0;
                    }
                } else
                {
                    displayColor = colorCodes[codeIndex];
                }
                i += 2;
            }
            if (displayColor == 0)
            {
                if (HudColors.INSTANCE.mode.getValue().equals("Rainbow"))
                {
                    rgb = effect((long) (((i * 3) + x) * (HudColors.INSTANCE.rainbowLength.getValue().doubleValue() * 10000)), HudColors.INSTANCE.saturation.getValue().floatValue() / 255, HudColors.INSTANCE.brightness.getValue().floatValue() / 255);
                } else
                {
                    rgb = HudColors.getTextColor((int) ((i * height) + x));
                }
            } else
            {
                rgb = new Color(displayColor);
            }
            try
            {
                String str = s.charAt(i) + "";
                Fonts.doOneText(stack, str, updateX, y, rgb, ClickGui.CONTEXT.getColorScheme().doesTextShadow());
                updateX += Fonts.getTextWidth(str);
            } catch (Exception e){

            }
        }
    }


    public static void render32k(DrawContext context, String s, Float x, Float y)
    {
        render32k(context.getMatrices(), s, x, y);
    }

    public static void render32k(MatrixStack stack, String s, Float x, Float y)
    {

        Float updateX = x;
        Color rgb;
        final char[] characters = s.toCharArray();
        int displayColor = 0;
        for (int i = 0; i < s.length(); i++)
        {
            final char colorCode = characters[i];
            if (colorCode == '\u00A7')
            {
                final char colorChar = characters[i + 1];
                final int codeIndex = "0123456789abcdef".indexOf(colorChar);
                if (codeIndex < 0)
                {
                    if (colorChar == ('r'))
                    {
                        displayColor = 0;
                    }
                } else
                {
                    displayColor = colorCodes[codeIndex];
                }
                i += 2;
            }
            if (displayColor == 0)
            {
                rgb = effect((long) (((i * 6L) + x) * (100 * 10000)), 100.0f / 255.0f, 255.0f / 255);
            } else
            {
                rgb = new Color(displayColor);
            }
            String str = s.charAt(i) + "";
            Fonts.renderText(stack, str, updateX, y, rgb, ClickGui.CONTEXT.getColorScheme().doesTextShadow());
            updateX += Fonts.getTextWidth(str);
        }
    }



    public static Color effect(long offset, float saturation, float brightness)
    {
        float hue = (float) ((System.nanoTime() * HudColors.INSTANCE.rainbowSpeed.getValue().floatValue()) + (offset * 100)) / 1.0E10F % 1.0F;
        long color = Long.parseLong(Integer.toHexString(Color.HSBtoRGB(hue, saturation, brightness)), 16);

        Color c = new Color((int) color);
        return new Color(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, c.getAlpha() / 255.0F);
    }
}
