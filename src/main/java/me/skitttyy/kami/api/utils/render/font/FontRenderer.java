package me.skitttyy.kami.api.utils.render.font;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

/**
 * @author xgraza
 * @since 1.0
 */
public interface FontRenderer extends IMinecraft
{
    GlyphVisitor DEFAULT_VISITOR = (renderer, c, x, y, color, shadow) ->
    {
        final int endPoint = renderer.drawGlyph(renderer, c, x, y, color, shadow);
        int endPoint2 = endPoint;
        if (shadow)
        {
            endPoint2 = renderer.drawGlyph(renderer, c, x + 1.0, y + 1.0, color, false);
        }
        return Math.max(endPoint, endPoint2);
    };

    void draw(final Matrix4f matrix4f, final String text, final double x, final double y, final int color, final boolean shadow);

    void draw(final Matrix4f matrix4f, final GlyphVisitor visitor, final String text, final double x, final double y, final int color, final boolean shadow);

    void draw(final Matrix4f matrix4f, final GlyphVisitor visitor, final VertexConsumerProvider vertexConsumers, final String text, final double x, final double y, final int color, final boolean shadow);

    int drawGlyph(final FontRenderer fontRenderer, final char c, final double x, final double y, final int color, final boolean shadow);

    int getStringWidth(final String text);

    Glyph getGlyph(final char c);

    Glyph[] getGlyphMap();
}
