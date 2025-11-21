package me.skitttyy.kami.api.utils.render.font.fonts;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import me.skitttyy.kami.mixin.accessor.ITextRenderer;
import net.minecraft.client.font.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.text.TextVisitFactory;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

/**
 * @author Shoreline
 * pasted by Skitttyy
 */
public class VanillaTextRenderer implements IMinecraft {


    public void renderTextNoLayer(DrawContext context, MatrixStack matrices, String text, float x, float y, int color, boolean shadow)
    {

        if (shadow)
            drawNoLayer(context, text, x + (FontModule.INSTANCE.shortShadow.getValue() ? 0.3f : 1.0f), y + (FontModule.INSTANCE.shortShadow.getValue() ? 0.3f : 1.0f), color, matrices.peek().getPositionMatrix(), true);
        drawNoLayer(context, text, x, y, color, matrices.peek().getPositionMatrix(), false);
    }



    public void renderCsgoLayer(DrawContext context, MatrixStack matrices, String text, float x, float y, int color)
    {
        drawNoLayer(context, text, x + 0.4f, y + 0.4f, color, matrices.peek().getPositionMatrix(), true);
        drawNoLayer(context, text, x - 0.4f, y - 0.4f, color, matrices.peek().getPositionMatrix(), true);
        drawNoLayer(context, text, x - 0.4f, y + 0.4f, color, matrices.peek().getPositionMatrix(), true);
        drawNoLayer(context, text, x + 0.4f, y - 0.4f, color, matrices.peek().getPositionMatrix(), true);

        drawNoLayer(context, text, x, y, color, matrices.peek().getPositionMatrix(), false);
    }


    private void drawNoLayer(DrawContext context, String text, float x, float y, int color, Matrix4f matrix, boolean shadow)
    {
        if (text == null)
        {
            return;
        }
        draw(text, x, y, color, shadow, matrix,
                context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
    }

    public void drawText(DrawContext context, MatrixStack matrices, String text, float x, float y, int color, boolean shadow)
    {
        if (shadow)
            draw(context, matrices, text, x + (FontModule.INSTANCE.shortShadow.getValue() ? 0.3f : 1.0f), y + (FontModule.INSTANCE.shortShadow.getValue() ? 0.3f : 1.0f), color, true);
        draw(context, matrices, text, x, y, color, false);
    }

    public void draw(DrawContext context, MatrixStack matrices, String text, float x, float y, int color, boolean shadow)
    {
        this.draw(context, text, x, y, color, matrices.peek().getPositionMatrix(), shadow);
    }

    private void draw(DrawContext context, String text, float x, float y, int color, Matrix4f matrix, boolean shadow)
    {
        if (text == null)
        {
            return;
        }
        draw(text, x, y, color, shadow, matrix,
                context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        context.getVertexConsumers().draw();
    }

    public void drawText(MatrixStack matrices, String text, float x, float y, int color, boolean shadow)
    {
        if (shadow)
            draw(matrices, text, x + (FontModule.INSTANCE.shortShadow.getValue() ? 0.3f : 1.0f), y + (FontModule.INSTANCE.shortShadow.getValue() ? 0.3f : 1.0f), color, true);
        draw(matrices, text, x, y, color, false);
    }

    public void draw(MatrixStack matrices, String text, float x, float y, int color, boolean shadow)
    {
        this.draw(text, x, y, color, matrices.peek().getPositionMatrix(), shadow);
    }

    private void draw(String text, float x, float y, int color, Matrix4f matrix, boolean shadow)
    {
        if (text == null)
        {
            return;
        }
//        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(
//                ((ITessellator) Tessellator.getInstance()).getAllocator());
//        draw(text, x, y, color, shadow, matrix,
//                immediate, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
//        immediate.draw();


        VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
        draw(text, x, y, color, shadow, matrix,
                vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        vertexConsumers.draw();
    }

    public void draw(String text, float x, float y, int color, boolean shadow,
                     Matrix4f matrix, VertexConsumerProvider vertexConsumers,
                     TextRenderer.TextLayerType layerType, int backgroundColor, int light)
    {
        drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    private void drawInternal(String text, float x, float y, int color, boolean shadow,
                              Matrix4f matrix, VertexConsumerProvider vertexConsumers,
                              TextRenderer.TextLayerType layerType, int backgroundColor, int light)
    {
        // color = TextRenderer.tweakTransparency(color);
        Matrix4f matrix4f = new Matrix4f(matrix);
        drawLayer(text, x, y, color, shadow, matrix4f, vertexConsumers, layerType, backgroundColor, light);
    }

    private void drawLayer(String text, float x, float y, int color, boolean shadow,
                           Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider,
                           TextRenderer.TextLayerType layerType, int underlineColor, int light)
    {
        Drawer drawer = new Drawer(vertexConsumerProvider, x, y, color, shadow, matrix, layerType, light);
        TextVisitFactory.visitFormatted(text, Style.EMPTY, drawer);
        drawer.drawLayer();
    }

    public static class Drawer implements CharacterVisitor {
        final VertexConsumerProvider vertexConsumers;
        private final float brightnessMultiplier;
        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;
        private final Matrix4f matrix;
        private final TextRenderer.TextLayerType layerType;
        private final int light;
        float x;
        float y;
        @Nullable
        private List<GlyphRenderer.Rectangle> rectangles;

        public Drawer(VertexConsumerProvider vertexConsumers, float x,
                      float y, int color, boolean shadow, Matrix4f matrix,
                      TextRenderer.TextLayerType layerType, int light)
        {
            this.vertexConsumers = vertexConsumers;
            this.x = x;
            this.y = y;
            this.brightnessMultiplier = shadow ? 0.25f : 1.0f;
            this.red = (float) (color >> 16 & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.green = (float) (color >> 8 & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.blue = (float) (color & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.alpha = (float) (color >> 24 & 0xFF) / 255.0f;
            this.matrix = matrix;
            this.layerType = layerType;
            this.light = light;
        }

        @Override
        public boolean accept(int i, Style style, int j)
        {
            // float n;
            float l;
            float h;
            float g;
            FontStorage fontStorage = ((ITextRenderer) mc.textRenderer).hookGetFontStorage(style.getFont());
            Glyph glyph = fontStorage.getGlyph(j, ((ITextRenderer) mc.textRenderer).hookGetValidateAdvance());
            GlyphRenderer glyphRenderer = style.isObfuscated() && j != 32 ? fontStorage.getObfuscatedGlyphRenderer(glyph) : fontStorage.getGlyphRenderer(j);
            boolean bl = style.isBold();
            TextColor textColor = style.getColor();
            if (textColor != null)
            {
                int k = textColor.getRgb();
                g = (float) (k >> 16 & 0xFF) / 255.0f * this.brightnessMultiplier;
                h = (float) (k >> 8 & 0xFF) / 255.0f * this.brightnessMultiplier;
                l = (float) (k & 0xFF) / 255.0f * this.brightnessMultiplier;
            } else
            {
                g = this.red;
                h = this.green;
                l = this.blue;
            }
            if (!(glyphRenderer instanceof EmptyGlyphRenderer))
            {
                float m = bl ? glyph.getBoldOffset() : 0.0f;
                // n = this.shadow ? glyph.getShadowOffset() : 0.0f;


                ((ITextRenderer) mc.textRenderer).hookDrawGlyph(glyphRenderer, bl, style.isItalic(), m, this.x, this.y, this.matrix,  this.vertexConsumers.getBuffer(glyphRenderer.getLayer(this.layerType)), g, h, l, this.alpha, this.light);
            }
            float m = glyph.getAdvance(bl);
            this.x += m;
            return true;
        }

        public void drawLayer()
        {
            if (this.rectangles != null)
            {
                FontStorage fontStorage = ((ITextRenderer) mc.textRenderer).hookGetFontStorage(Style.DEFAULT_FONT_ID);

                GlyphRenderer glyphRenderer = fontStorage.getRectangleRenderer();
                for (GlyphRenderer.Rectangle rectangle : this.rectangles)
                {
                    glyphRenderer.drawRectangle(rectangle, this.matrix,  this.vertexConsumers.getBuffer(glyphRenderer.getLayer(this.layerType)), this.light);
                }
            }
        }
    }
}