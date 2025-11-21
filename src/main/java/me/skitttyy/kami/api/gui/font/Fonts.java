package me.skitttyy.kami.api.gui.font;

import me.skitttyy.kami.api.utils.render.font.fonts.CustomFontRenderer;
import me.skitttyy.kami.api.utils.render.font.fonts.VanillaTextRenderer;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class Fonts implements IMinecraft
{
    public static final VanillaTextRenderer VANILLA = new VanillaTextRenderer();
    public static CustomFontRenderer CUSTOM;
    public static @NotNull CustomFontRenderer create(float size) throws IOException, FontFormatException
    {



        return new CustomFontRenderer( size / 2f);
    }


    public static void renderText(MatrixStack stack, String text, float x, float y, Color color, boolean shadow)
    {
        if (FontModule.INSTANCE.isEnabled())
        {
            CUSTOM.drawText(stack, text, x, y, color, shadow);
        } else
        {
            VANILLA.drawText(stack, text, x, y, color.getRGB(), shadow);
        }
    }



    public static void renderText(DrawContext context, String text, float x, float y, Color color, boolean shadow)
    {
        if (FontModule.INSTANCE.isEnabled())
        {
            CUSTOM.drawText(context.getMatrices(), text, x, y, color, shadow);
        } else
        {
            VANILLA.drawText(context, context.getMatrices(), text, x, y, color.getRGB(), shadow);
        }
    }

    public static void doOneText(DrawContext context, String text, float x, float y, Color color, boolean shadow)
    {
        if (FontModule.INSTANCE.isEnabled())
        {
            CUSTOM.drawText(context.getMatrices(), text, x, y, color, shadow);
        } else
        {
            if(FontModule.INSTANCE.pop.getValue())
            {
                VANILLA.renderCsgoLayer(context, context.getMatrices(), text, x, y, color.getRGB());
            }else{
                VANILLA.renderTextNoLayer(context, context.getMatrices(), text, x, y, color.getRGB(), shadow);

            }
        }
    }


    public static void doOneHUd(DrawContext context, String text, float x, float y, Color color, boolean shadow)
    {
        if (FontModule.INSTANCE.isEnabled())
        {
            CUSTOM.drawText(context.getMatrices(), text, x, y, color, shadow);
        } else
        {
            VANILLA.renderTextNoLayer(context, context.getMatrices(), text, x, y, color.getRGB(), shadow);
        }
    }

    public static void doOneVanilla(DrawContext context, String text, float x, float y, Color color, boolean shadow)
    {
        VANILLA.renderTextNoLayer(context, context.getMatrices(), text, x, y, color.getRGB(), shadow);
    }


//    public static void renderText(DrawContext context, String text, float x, float y, Color color, boolean shadow, boolean optimize)
//    {
//        if (FontModule.INSTANCE.isEnabled())
//        {
//            CUSTOM.drawText(context.getMatrices(), text, x, y, color.getRGB(), shadow);
//        } else
//        {
//            if (optimize)
//            {
//                context.drawText(mc.textRenderer, text, (int) x, (int) y, color.getRGB(), shadow);
//            } else
//            {
//                VANILLA.drawText(context, context.getMatrices(), text, x, y, color.getRGB(), shadow);
//            }
//        }
//    }






    public static float getTextWidth(String text)
    {
        if (FontModule.INSTANCE.isEnabled())
        {
            return CUSTOM.getStringWidth(text);
        } else
        {
            return mc.textRenderer.getWidth(text);
        }
    }




    public static float getTextHeight(String text)
    {
        if (FontModule.INSTANCE.isEnabled())
        {
            return CUSTOM.getFontHeight();
        } else
        {
            return mc.textRenderer.fontHeight;
        }
    }


    public static void refresh()
    {
        try
        {
            if (CUSTOM != null)
            {
                CUSTOM.close();
                Fonts.CUSTOM = Fonts.create(FontModule.INSTANCE.fontSize.getValue().intValue());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}