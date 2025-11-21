package me.skitttyy.kami.api.utils.render.font;

/**
 * @param value
 * @param textureWidth
 * @param textureHeight
 * @param width
 * @param height
 * @author xgraza
 * @since 1.0
 */
public record Glyph(int textureWidth, int textureHeight, int width, int height, char value, GlyphCache owner)
{

}