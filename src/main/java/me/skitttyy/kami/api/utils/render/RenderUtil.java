package me.skitttyy.kami.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.TextSection;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.render.world.layer.Sn0wLayers;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import me.skitttyy.kami.impl.features.modules.client.Optimizer;
import me.skitttyy.kami.impl.features.modules.render.Nametags;
import me.skitttyy.kami.mixin.accessor.IItemRenderer;
import me.skitttyy.kami.mixin.accessor.ITessellator;
import me.skitttyy.kami.mixin.accessor.IWorldRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers.*;
import static me.skitttyy.kami.api.wrapper.IMinecraft.mc;

public class RenderUtil {
    public static final Tessellator TESSELLATOR = RenderSystem.renderThreadTesselator();

    public static void renderRect(MatrixStack matrices, double x1, double y1, double x2, double y2, int color)
    {
        renderRect(matrices, x1, y1, x2, y2, 0.0, color);
    }

    public static double interpolate(double oldValue, double newValue, double interpolationValue)
    {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static void renderItem(ItemStack stack, ModelTransformationMode renderMode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int seed)
    {
        BakedModel bakedModel = mc.getItemRenderer().getModel(stack, null, null, seed);
        boolean bl;
        if (stack.isEmpty())
        {
            return;
        }
        matrices.push();

        bl = renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED;
        if (bl)
        {
            if (stack.isOf(Items.TRIDENT))
            {
                bakedModel = mc.getItemRenderer().getModels().getModelManager().getModel(ModelIdentifier.ofVanilla("trident", "inventory"));
            } else if (stack.isOf(Items.SPYGLASS))
            {
                bakedModel = mc.getItemRenderer().getModels().getModelManager().getModel(ModelIdentifier.ofVanilla("spyglass", "inventory"));
            }
        }

        bakedModel.getTransformation().getTransformation(renderMode).apply(false, matrices);

        matrices.translate(-0.5f, -0.5f, -0.5f);

        if (bakedModel.isBuiltin() || stack.isOf(Items.TRIDENT) && !bl)
        {
            ((IItemRenderer) mc.getItemRenderer()).getBuiltInModelItemRenderer().render(stack, renderMode, matrices, vertexConsumers, 15728895, OverlayTexture.DEFAULT_UV);
        } else
        {
            ((IItemRenderer) mc.getItemRenderer()).renderBakedItemModelAccess(bakedModel, stack, 15728895, OverlayTexture.DEFAULT_UV, matrices, getItemGlintConsumer(vertexConsumers, RenderLayers.getItemLayer(stack, false), stack.hasGlint()));
        }
        matrices.pop();
    }

    public static VertexConsumer getItemGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean glint)
    {
        if (glint)
        {
            return VertexConsumers.union(vertexConsumers.getBuffer(Sn0wLayers.ENCHANT), vertexConsumers.getBuffer(layer));
        }
        return vertexConsumers.getBuffer(layer);
    }


    public static void renderItemWithCount(DrawContext context, ItemStack item, Point pos, int count, Color textColor, boolean always)
    {
        context.drawItem(item, pos.x, pos.y);

        context.drawItemInSlot(mc.textRenderer, item, pos.x, pos.y);
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 600.0f);

        if ((count > 1) || always)
        {
            if (count >= 1000)
            {
                Fonts.renderText(
                        context.getMatrices(),
                        String.valueOf(count),
                        pos.x + 19 - Fonts.getTextWidth(String.valueOf(count)),
                        pos.y + 9,
                        textColor,
                        true
                );
            } else
            {
                Fonts.renderText(
                        context.getMatrices(),
                        String.valueOf(count),
                        pos.x + 19 - 2 - Fonts.getTextWidth(String.valueOf(count)),
                        pos.y + 9,
                        textColor,
                        true
                );
            }

        }
        context.getMatrices().pop();
    }

    public static void renderOutline(MatrixStack matrices, double x1, double y1, double x2, double y2, int color, boolean rasturize)
    {
        renderOutlineRect(matrices, x1, y1, x2, y2, 0.0, color, rasturize);
    }

    public static void renderGradient(MatrixStack matrices, double startX, double startY, double endX, double endY, int colorStart, int colorEnd, boolean horizontal)
    {

        renderGradient(matrices, startX, startY, endX, endY, colorStart, colorEnd, horizontal, 0);
    }

    public static void renderGradient(MatrixStack matrices, double startX, double startY, double endX, double endY, int colorStart, int colorEnd, boolean horizontal, int z)
    {
        RenderSystem.enableBlend();
        if (RenderSystem.getShader() != GameRenderer.getPositionColorProgram())
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        renderGradient(matrices.peek().getPositionMatrix(), builder, startX, startY, endX, endY, z, colorStart, colorEnd, horizontal);
        BufferRenderer.drawWithGlobalProgram(builder.end());
        RenderSystem.disableBlend();
    }

    public static void renderGradient(Matrix4f matrix, BufferBuilder builder, double startX, double startY, double endX, double endY, double z, int colorStart, int colorEnd, boolean horizontal)
    {
        endX += startX;
        endY += startY;

        float f = (float) ColorHelper.Argb.getAlpha(colorStart) / 255.0f;
        float g = (float) ColorHelper.Argb.getRed(colorStart) / 255.0f;
        float h = (float) ColorHelper.Argb.getGreen(colorStart) / 255.0f;
        float i = (float) ColorHelper.Argb.getBlue(colorStart) / 255.0f;
        float j = (float) ColorHelper.Argb.getAlpha(colorEnd) / 255.0f;
        float k = (float) ColorHelper.Argb.getRed(colorEnd) / 255.0f;
        float l = (float) ColorHelper.Argb.getGreen(colorEnd) / 255.0f;
        float m = (float) ColorHelper.Argb.getBlue(colorEnd) / 255.0f;

        if (horizontal)
        {
            builder.vertex(matrix, (float) startX, (float) startY, (float) z).color(g, h, i, f);
            builder.vertex(matrix, (float) startX, (float) endY, (float) z).color(g, h, i, f);
            builder.vertex(matrix, (float) endX, (float) endY, (float) z).color(k, l, m, j);
            builder.vertex(matrix, (float) endX, (float) startY, (float) z).color(k, l, m, j);
        } else
        {
            builder.vertex(matrix, (float) startX, (float) startY, (float) z).color(g, h, i, f);
            builder.vertex(matrix, (float) startX, (float) endY, (float) z).color(k, l, m, j);
            builder.vertex(matrix, (float) endX, (float) endY, (float) z).color(k, l, m, j);
            builder.vertex(matrix, (float) endX, (float) startY, (float) z).color(g, h, i, f);
        }
    }

    public static void renderRect(MatrixStack matrices, double x1, double y1, double x2, double y2, double z, Color color)
    {
        renderRect(matrices, x1, y1, x2, y2, z, color.getRGB());
    }

    public static void renderRect(MatrixStack matrices, double x1, double y1, double x2, double y2, double z, int color)
    {
        x2 += x1;
        y2 += y1;
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        double i;
        if (x1 < x2)
        {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2)
        {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        float f = (float) ColorHelper.Argb.getAlpha(color) / 255.0f;
        float g = (float) ColorHelper.Argb.getRed(color) / 255.0f;
        float h = (float) ColorHelper.Argb.getGreen(color) / 255.0f;
        float j = (float) ColorHelper.Argb.getBlue(color) / 255.0f;
        RenderSystem.enableBlend();
        if (RenderSystem.getShader() != GameRenderer.getPositionColorProgram())
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);


        BufferBuilder buffer = TESSELLATOR.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix4f, (float) x1, (float) y1, (float) z).color(g, h, j, f);
        buffer.vertex(matrix4f, (float) x1, (float) y2, (float) z).color(g, h, j, f);
        buffer.vertex(matrix4f, (float) x2, (float) y2, (float) z).color(g, h, j, f);
        buffer.vertex(matrix4f, (float) x2, (float) y1, (float) z).color(g, h, j, f);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }


    public static void drawText(String text, Vec3d renderPos, float size)
    {
        Camera camera = mc.gameRenderer.getCamera();
        final Vec3d pos = camera.getPos();


        MatrixStack matrixStack = new MatrixStack();
        matrixStack.push();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrixStack.translate(renderPos.x - pos.getX(), renderPos.y - pos.getY(), renderPos.z - pos.getZ());
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        matrixStack.scale(-0.01f * size, -0.01f * size, -1.0f);

        float distance = (float) renderPos.distanceTo(mc.player.getPos());
        float scaleDistance = (distance / 2.0f) / (2.0f + (2.0f - size));
        if (scaleDistance < 1f)
            scaleDistance = 1;

        matrixStack.scale(scaleDistance, scaleDistance, scaleDistance);

        GL11.glDepthFunc(GL11.GL_ALWAYS);
        float hwidth = Fonts.getTextWidth(text) / 2.0f;
        Fonts.renderText(matrixStack, text, -hwidth, 0.0f, Color.WHITE, true);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
        matrixStack.pop();
    }

    public static void renderOutlineRect(MatrixStack matrices, double x1, double y1, double width, double height, double z, Color color, boolean rasturize)
    {
        renderOutlineRect(matrices, x1, y1, width, height, z, color.getRGB(), rasturize);

    }

    public static void renderOutlineRect(MatrixStack matrices, double x1, double y1, double width, double height, double z, int color, boolean rasturize)
    {

        float x2 = (float) (x1 + width);
        float y2 = (float) (y1 + height);
        if (rasturize) y2 = y2 - 0.1f;
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float f = (float) ColorHelper.Argb.getAlpha(color) / 255.0f;
        float g = (float) ColorHelper.Argb.getRed(color) / 255.0f;
        float h = (float) ColorHelper.Argb.getGreen(color) / 255.0f;
        float j = (float) ColorHelper.Argb.getBlue(color) / 255.0f;
        RenderSystem.enableBlend();
        if (RenderSystem.getShader() != GameRenderer.getPositionColorProgram())
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = TESSELLATOR.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix4f, (float) x1, (float) y1, (float) z).color(g, h, j, f);
        buffer.vertex(matrix4f, (float) x1, (float) y2, (float) z).color(g, h, j, f);


        buffer.vertex(matrix4f, (float) x1, y2, (float) z).color(g, h, j, f);
        buffer.vertex(matrix4f, x2, y2, (float) z).color(g, h, j, f);


        buffer.vertex(matrix4f, x2, y2, (float) z).color(g, h, j, f);
        buffer.vertex(matrix4f, x2, (float) y1, (float) z).color(g, h, j, f);


        buffer.vertex(matrix4f, (float) x2, (float) y1, (float) z).color(g, h, j, f);
        buffer.vertex(matrix4f, (float) x1, (float) y1, (float) z).color(g, h, j, f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }


    /**
     * Render a box
     */
    public static void renderBox(RenderType type, Box box, Color top, Color bottom)
    {
        switch (type)
        {
            case FILL:
                renderFillBox(box, top, bottom);
                break;
            case LINES:
                renderLinesBox(box, top, bottom);
                break;
        }
    }

    public static void renderLinesBox(Box box,
                                      Color top, Color bottom)
    {
        if (Optimizer.INSTANCE.isEnabled() && Optimizer.INSTANCE.frustrum.getValue() && !isFrustumVisible(box))
            return;

        MatrixStack stack = matrixFrom(box.minX, box.minY, box.minZ);
        stack.push();
        drawOutlineBox(stack, box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate()), top, bottom);
        stack.pop();
    }

    /**
     * Checks if box can be seen
     */
    public static boolean isFrustumVisible(Box box)
    {
        return ((IWorldRenderer) mc.worldRenderer).getFrustum().isVisible(box);
    }


    public static void renderFillBox(Box box, Color top, Color bottom)
    {
        if (Optimizer.INSTANCE.isEnabled() && Optimizer.INSTANCE.frustrum.getValue() && !isFrustumVisible(box))
        {
            return;
        }

        MatrixStack stack = matrixFrom(box.minX, box.minY, box.minZ);
        stack.push();

        drawBox(matrixFrom(box.minX, box.minY, box.minZ), box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate()), top, bottom);
        stack.pop();
    }

    /**
     * @param matrices
     * @param box
     */
    public static void drawBox(MatrixStack matrices, Box box, Color top, Color bottom)
    {
        drawBox(matrices, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, top, bottom);
    }

    public static void drawBox(double x1, double y1,
                               double z1, double x2, double y2, double z2, Color color)
    {
        MatrixStack stack = matrixFrom(x1, y1, z2);
        stack.push();

        drawBox(stack, x1, y1, z1, x2, y2, z2, color, color);
        stack.pop();
    }


    public static MatrixStack matrixFrom(double x, double y, double z)
    {
        MatrixStack matrices = new MatrixStack();

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

        return matrices;
    }

    public static void drawLine(MatrixStack matrices, double x1, double y1,
                                double z1, double x2, double y2, double z2, Color top, Color bottom)
    {
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        Vector3f normalVec = getNormal((float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2);
        LINE.begin(matrix4f);
        LINE.color(bottom);
        LINE.vertex(x1, y1, z1).buffer.normal(matrices.peek(), normalVec.x(), normalVec.y(), normalVec.z());
        LINE.color(top);
        LINE.vertex(x2, y2, z2).buffer.normal(matrices.peek(), normalVec.x(), normalVec.y(), normalVec.z());
        LINE.end();

    }

    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2)
    {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

    /**
     * Draws a box spanning from [x1, y1, z1] to [x2, y2, z2].
     * The 3 axes centered at [x1, y1, z1] may be colored differently using
     * xAxisRed, yAxisGreen, and zAxisBlue.
     *
     * <p> Note the coordinates the box spans are relative to current
     * translation of the matrices.
     *
     * @param matrices
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     */
    public static void drawBox(MatrixStack matrices, double x1, double y1,
                               double z1, double x2, double y2, double z2, Color top, Color bottom)
    {
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        QUADS.begin(matrix4f);
        QUADS.color(bottom);
        QUADS.vertex(x1, y1, z1).vertex(x2, y1, z1).vertex(x2, y1, z2).vertex(x1, y1, z2);

        QUADS.color(top);

        QUADS.vertex(x1, y2, z1).vertex(x1, y2, z2).vertex(x2, y2, z2).vertex(x2, y2, z1);
        QUADS.color(bottom);
        QUADS.vertex(x1, y1, z1);
        QUADS.color(top);

        QUADS.vertex(x1, y2, z1).vertex(x2, y2, z1);
        QUADS.color(bottom);

        QUADS.vertex(x2, y1, z1);
        QUADS.vertex(x2, y1, z1);
        QUADS.color(top);

        QUADS.vertex(x2, y2, z1).vertex(x2, y2, z2);
        QUADS.color(bottom);

        QUADS.vertex(x2, y1, z2);


        QUADS.vertex(x1, y1, z2).vertex(x2, y1, z2);
        QUADS.color(top);
        QUADS.vertex(x2, y2, z2).vertex(x1, y2, z2);
        QUADS.color(bottom);
        QUADS.vertex(x1, y1, z1).vertex(x1, y1, z2);
        QUADS.color(top);
        QUADS.vertex(x1, y2, z2).vertex(x1, y2, z1);

        QUADS.end();

    }

    public static void renderTracerLine(Vec3d from, Vec3d to, Color top, Color bottom, float lineWidth)
    {
        final Vec3d eyes = MathUtil.rotateYaw(MathUtil.rotatePitch(new Vec3d(0.0, 0.0, 1.0), -(float) Math.toRadians(mc.player.getPitch())), -(float) Math.toRadians(mc.player.getYaw()));

        renderLine(eyes, from.x, from.y, from.z, to.x, to.y, to.z, top, bottom, lineWidth);
    }

    public static void renderLineFromPosToPos(Vec3d from, Vec3d to, Color top, Color bottom, float lineWidth)
    {

        renderLine(new Vec3d(0, 0, 0), from.x, from.y, from.z, to.x, to.y, to.z, top, bottom, lineWidth);
    }

    public static void renderLine(Vec3d offset, double x1, double y1, double z1, double x2, double y2, double z2, Color top, Color bottom, float width)
    {
        float old = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(width);
        MatrixStack matrices = matrixFrom(x1, y1, z1);
        matrices.push();

        drawLine(matrices, offset.x, offset.y, offset.z, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), top, bottom);
        matrices.pop();
        RenderSystem.lineWidth(old);

    }

    /**
     * @param matrices
     * @param box
     */
    public static void drawOutlineBox(MatrixStack matrices, Box box, Color top, Color bottom)
    {
        renderLinesBox(matrices, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, top, bottom);
    }

    public static void drawCircle(Buffer buffer, float radius, int slices, Vec3d pos, Direction direction, Color color)
    {
//        if (side == 2)
//        {
//            matrices.rotate(90.0f, 1.0f, 0.0f, 0.0f);
//        } else if (side == 3)
//        {
//            GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
//        } else if (side == 4)
//        {
//            GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
//        } else if (side == 5)
//        {
//            GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
//        }
        MatrixStack matrices = matrixFrom(pos.x, pos.y, pos.z);

        matrices.push();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(matrix);
        buffer.color(color);
        for (int i = 0; i <= slices; i++)
        {
            double circleTwo = radius * Math.cos(i * (Math.PI * 4) / slices);


            double circleOne = radius * Math.sin(i * (Math.PI * 4) / slices);
            switch (direction)
            {
                case UP, DOWN:
                    buffer.vertex((float) circleTwo, 0f, (float) circleOne);
                    break;
                case EAST, WEST:
                    buffer.vertex(0f, (float) circleTwo, (float) circleOne);
                    break;
                case SOUTH, NORTH:
                    buffer.vertex((float) circleOne, (float) circleTwo, 0f);
                    break;
            }
        }

        buffer.end();
        matrices.pop();
    }


    /**
     * @param matrices
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     */
    public static void renderLinesBox(MatrixStack matrices, double x1, double y1,
                                      double z1, double x2, double y2, double z2, Color top, Color bottom)
    {
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();


        LINES.begin(matrix4f);

        LINES.color(bottom);

        LINES.vertex(x1, y1, z1).vertex(x2, y1, z1);
        LINES.vertex(x2, y1, z1).vertex(x2, y1, z2);
        LINES.vertex(x2, y1, z2).vertex(x1, y1, z2);
        LINES.vertex(x1, y1, z2).vertex(x1, y1, z1);
        LINES.vertex(x1, y1, z1);

        LINES.color(top);

        LINES.vertex(x1, y2, z1);
        LINES.color(bottom);

        LINES.vertex(x2, y1, z1);
        LINES.color(top);
        LINES.vertex(x2, y2, z1);
        LINES.color(bottom);
        LINES.vertex(x2, y1, z2);
        LINES.color(top);
        LINES.vertex(x2, y2, z2);
        LINES.color(bottom);
        LINES.vertex(x1, y1, z2);
        LINES.color(top);
        LINES.vertex(x1, y2, z2);
        LINES.vertex(x1, y2, z1).vertex(x2, y2, z1);
        LINES.vertex(x2, y2, z1).vertex(x2, y2, z2);
        LINES.vertex(x2, y2, z2).vertex(x1, y2, z2);
        LINES.vertex(x1, y2, z2).vertex(x1, y2, z1);

        LINES.end();
    }

    public static Vector3d set(Vector3d vec, Entity entity, double tickDelta)
    {
        vec.x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
        vec.y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
        vec.z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());

        return vec;
    }

    public static Vector3d set(Vector3d vec, Vec3d v)
    {
        vec.x = v.x;
        vec.y = v.y;
        vec.z = v.z;

        return vec;
    }

    public static void drawWaypoint(TextSection[] sections, double x, double y, double z, Camera camera, Color borderColor)
    {
        float width = 0;
        for (TextSection section : sections)
        {
            width += (int) Fonts.getTextWidth(section.getText());
        }


        width = width / 2;
        final Vec3d pos = camera.getPos();
        MatrixStack matrices = new MatrixStack();
        final double maxRenderDistance = (mc.options.getViewDistance().getValue() << 4);
        Vec3d waypointVec = new Vec3d(x, y, z);

        Vec3d playerPos = Interpolator.getInterpolatedPosition(mc.getCameraEntity(), mc.getRenderTickCounter().getTickDelta(false));
        if (playerPos.distanceTo(waypointVec) > maxRenderDistance)
        {
            final Vec3d delta = waypointVec.subtract(playerPos).normalize();
            waypointVec = new Vec3d(delta.x * maxRenderDistance, delta.y * maxRenderDistance, delta.z * maxRenderDistance);

            waypointVec = playerPos.add(waypointVec);
        }

        x = waypointVec.x;
        y = waypointVec.y;
        z = waypointVec.z;


        Vec3d interpolate = Interpolator.getInterpolatedEyePos(mc.getCameraEntity(), mc.getRenderTickCounter().getTickDelta(false));




        double dx = (pos.getX() - interpolate.getX()) - x;
        double dy = (pos.getY() - interpolate.getY()) - y;
        double dz = (pos.getZ() - interpolate.getZ()) - z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);


        float scaling = 1.0f;

        scaling = (float) (0.0018f + 0.003f * dist);
        if (dist <= 8.0) scaling = 0.0245f;


        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrices.translate(x - pos.getX(), y - pos.getY(), z - pos.getZ());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.scale(-scaling, -scaling, -1.0f);

        RenderUtil.renderRect(matrices, -width - 1.0f, -1.0f, width * 2.0f + 2.0f, mc.textRenderer.fontHeight + 1.5f, 0.0, Nametags.INSTANCE.boxColor.getValue().getColor());
        RenderUtil.renderOutline(matrices, -width - 1.0f, -1.0f, width * 2.0f + 2.0f, mc.textRenderer.fontHeight + 1.5f, borderColor.getRGB(), true);
        drawSections(sections, matrices, -width, 0.0f);

        matrices.pop();


    }

    public static void drawSections(TextSection[] sections, MatrixStack matrices, float x, float y)
    {
        float width = 0;
        for (TextSection section : sections)
        {
            Fonts.renderText(matrices, section.getText(), x + width, y, section.getColor(), FontModule.INSTANCE.textShadow.getValue());
            width += Fonts.getTextWidth(section.getText());
        }
    }

}
