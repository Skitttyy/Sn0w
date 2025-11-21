package me.skitttyy.kami.mixin.accessor;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author Shoreline
 * pasted by Skitttyy
 */
@Mixin(TextRenderer.class)
public interface ITextRenderer
{
    @Accessor("validateAdvance")
    boolean hookGetValidateAdvance();

    @Invoker("getFontStorage")
    FontStorage hookGetFontStorage(Identifier id);

    @Invoker("drawGlyph")
    void hookDrawGlyph(GlyphRenderer glyphRenderer, boolean bold, boolean italic,
                       float weight, float x, float y, Matrix4f matrix,
                       VertexConsumer vertexConsumer, float red, float green,
                       float blue, float alpha, int light);


    @Invoker("drawLayer")
    float hookDrawLayer(OrderedText text, float x, float y,
                        int color, boolean shadow, Matrix4f matrix,
                        VertexConsumerProvider vertexConsumerProvider,
                        TextRenderer.TextLayerType layerType, int underlineColor,
                        int light);
}