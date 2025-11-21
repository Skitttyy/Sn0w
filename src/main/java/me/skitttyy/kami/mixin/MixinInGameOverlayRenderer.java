package me.skitttyy.kami.mixin;

import me.skitttyy.kami.impl.features.modules.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {
    @Inject(method = "renderFireOverlay", at = @At(value = "HEAD"), cancellable = true)
    private static void hookRenderFireOverlay(MinecraftClient client, MatrixStack matrices, CallbackInfo ci)
    {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.fire.getValue())
        {
            ci.cancel();
        }
    }
    @Inject(method = "renderUnderwaterOverlay", at = @At(value = "HEAD"), cancellable = true)
    private static void hookRenderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices, CallbackInfo ci)
    {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.liquidVision.getValue())
        {
            ci.cancel();
        }
    }
    @Inject(method = "renderInWallOverlay", at = @At(value = "HEAD"), cancellable = true)
    private static void hookRenderFireOverlay(Sprite sprite, MatrixStack matrices, CallbackInfo ci)
    {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.blockInside.getValue())
        {
            ci.cancel();
        }
    }
}