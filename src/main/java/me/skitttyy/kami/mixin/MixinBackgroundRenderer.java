package me.skitttyy.kami.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.event.events.render.RenderFogEvent;
import me.skitttyy.kami.impl.features.modules.render.CustomSky;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {


    @Inject(method = "applyFog", at = @At(value = "TAIL"))
    private static void hookApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci)
    {
        if (fogType != BackgroundRenderer.FogType.FOG_TERRAIN)
        {
            return;
        }
        RenderFogEvent event = new RenderFogEvent();
        event.post();
        if (event.isCancelled())
        {
            RenderSystem.setShaderFogStart(viewDistance * 4.0f);
            RenderSystem.setShaderFogEnd(viewDistance * 4.25f);
        }

        if (CustomSky.INSTANCE.isEnabled())
        {
            RenderSystem.setShaderFogColor(CustomSky.INSTANCE.fogColor.getValue().getGlRed(), CustomSky.INSTANCE.fogColor.getValue().getGlGreen(), CustomSky.INSTANCE.fogColor.getValue().getGlBlue());
        }

    }


}