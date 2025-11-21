package me.skitttyy.kami.api.utils.render.world.layer;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import org.lwjgl.opengl.GL11;

public class Sn0wLayers implements IMinecraft {
    public static final RenderLayer ENCHANT = RenderLayer.of("glint", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, true, false))
            .writeMaskState(RenderPhase.COLOR_MASK).cull(RenderPhase.ENABLE_CULLING).depthTest(new DepthTest()).transparency(RenderPhase.GLINT_TRANSPARENCY).texturing(RenderPhase.GLINT_TEXTURING).build(false));

    protected static class DepthTest extends RenderPhase.DepthTest {
        public DepthTest() {
            super("depth_test", GL11.GL_ALWAYS);
        }

        @Override
        public void startDrawing() {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_EQUAL);
        }

        @Override
        public void endDrawing() {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthFunc(GL11.GL_ALWAYS);
            // GL11.glClearDepth(1.0);
        }
    }
}