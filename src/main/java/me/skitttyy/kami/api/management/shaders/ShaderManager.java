package me.skitttyy.kami.api.management.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.api.utils.ducks.IPostEffectProcessor;
import me.skitttyy.kami.mixin.accessor.IRenderLayerMultiPhase;
import me.skitttyy.kami.mixin.accessor.IRenderLayerMultiPhaseParameters;
import me.skitttyy.kami.mixin.accessor.IRenderPhaseTextureBase;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import org.lwjgl.opengl.GL30C;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Getter
public class ShaderManager implements IMinecraft
{
    public static ShaderManager INSTANCE;
    private final OutlineVertexConsumerProvider vertexConsumerProvider = new OutlineVertexConsumerProvider(VertexConsumerProvider.immediate(new BufferAllocator(256)));
    private final Function<RenderPhase.TextureBase, RenderLayer> layerCreator;
    private final RenderPhase.Target target;

    private ShaderFramebuffer framebuffer;

    public ManagedShaderEffect defaultShaderEffect;
    public ManagedShaderEffect rainbowShaderEffect;

    public ShaderManager()
    {
        target = new RenderPhase.Target("shader_target", () -> {}, () -> {});
        layerCreator = memoizeTexture(texture -> RenderLayer.of("snow_overlay", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 1536, RenderLayer.MultiPhaseParameters.builder()
                .program(RenderPhase.OUTLINE_PROGRAM).cull(RenderPhase.DISABLE_CULLING).texture(texture).depthTest(RenderPhase.ALWAYS_DEPTH_TEST).target(target).build(RenderLayer.OutlineMode.IS_OUTLINE)));
    }

    public void reloadShaders()
    {
        if (framebuffer == null)
        {
            reloadShadersInternal();
        }
    }

    public void reloadShadersInternal()
    {
        framebuffer = new ShaderFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
        defaultShaderEffect = ShaderEffectManager.getInstance().manage(Identifier.of("kami", "shaders/post/default.json"));
        rainbowShaderEffect = ShaderEffectManager.getInstance().manage(Identifier.of("kami", "shaders/post/rainbow.json"));

    }

    public void applyShader(ManagedShaderEffect shaderEffect, Runnable setup, Runnable runnable)
    {
        Framebuffer mcFramebuffer = mc.getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        if (framebuffer.textureWidth != mcFramebuffer.textureWidth || framebuffer.textureHeight != mcFramebuffer.textureHeight)
        {
            framebuffer.resize(mcFramebuffer.textureWidth, mcFramebuffer.textureHeight, false);
        }

        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer.fbo);
        framebuffer.beginWrite(false);
        runnable.run();
        // Render callbacks here
        framebuffer.endWrite();
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mcFramebuffer.fbo);
        mcFramebuffer.beginWrite(false);
        PostEffectProcessor effect = shaderEffect.getShaderEffect();
        if (effect != null)
        {
            ((IPostEffectProcessor) effect).overwriteBuffer("bufIn", framebuffer);
            Framebuffer bufOut = effect.getSecondaryTarget("bufOut");
            // Setup shader here
            setup.run();
            framebuffer.clear(false);
            mcFramebuffer.beginWrite(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
            RenderSystem.backupProjectionMatrix();
            bufOut.draw(bufOut.textureWidth, bufOut.textureHeight, false);
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }

    }

    public VertexConsumerProvider createVertexConsumers(VertexConsumerProvider parent, Color color)
    {
        return layer ->
        {
            VertexConsumer parentBuffer = parent.getBuffer(layer);
            if (!(layer instanceof RenderLayer.MultiPhase) || ((IRenderLayerMultiPhaseParameters) (Object) ((IRenderLayerMultiPhase) layer).invokeGetPhases()).getOutlineMode() == RenderLayer.OutlineMode.NONE)
            {
                return parentBuffer;
            }

            vertexConsumerProvider.setColor(color.getRed(), color.getGreen(), color.getBlue(), 255);

            VertexConsumer outlineBuffer = vertexConsumerProvider.getBuffer(layerCreator.apply(((IRenderLayerMultiPhaseParameters) (Object) ((IRenderLayerMultiPhase) layer).invokeGetPhases()).getTexture()));
            return outlineBuffer != null ? outlineBuffer : parentBuffer;
        };
    }

    private Function<RenderPhase.TextureBase, RenderLayer> memoizeTexture(Function<RenderPhase.TextureBase, RenderLayer> function)
    {
        return new Function<>()
        {
            private final Map<Identifier, RenderLayer> cache = new HashMap<>();

            public RenderLayer apply(RenderPhase.TextureBase texture)
            {
                return this.cache.computeIfAbsent(((IRenderPhaseTextureBase) texture).invokeGetId().get(), id -> function.apply(texture));
            }
        };
    }

    // Instance without overwritten buffers
    
}