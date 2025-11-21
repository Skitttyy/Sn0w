package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.impl.features.modules.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {


    @Shadow
    @Final
    private static Identifier PUMPKIN_BLUR;

    @Shadow
    @Final
    private static Identifier POWDER_SNOW_OUTLINE;

    @Inject(at = @At(value = "RETURN"), method = "renderMainHud")
    public void renderMainHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        new RenderGameOverlayEvent.Text(context, tickCounter).post();
        context.draw();
    }

    @Inject(method = "renderOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci)
    {
        if (NoRender.INSTANCE.isEnabled())
        {
            if (texture.getPath().equals(PUMPKIN_BLUR.getPath()))
            {
                if (NoRender.INSTANCE.pumpkin.getValue())
                {
                    ci.cancel();
                }
            } else if (texture.getPath().equals(POWDER_SNOW_OUTLINE.getPath()))
            {
                if (NoRender.INSTANCE.frost.getValue())
                {
                    ci.cancel();
                }
            }
        }
    }


    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.potionGui.getValue())
        {
            ci.cancel();
        }
    }


}
