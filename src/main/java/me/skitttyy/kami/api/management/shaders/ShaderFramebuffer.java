package me.skitttyy.kami.api.management.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;

// Only use this framebuffer if you are rendering shaders
public class ShaderFramebuffer extends Framebuffer
{
    public ShaderFramebuffer(int width, int height)
    {
        super(false);
        RenderSystem.assertOnRenderThreadOrInit();
        resize(width, height, true);
        setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }
}
