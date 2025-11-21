package me.skitttyy.kami.mixin;

import me.skitttyy.kami.impl.features.modules.render.NoRender;
import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ChunkSkyLightProvider.class)
public class MixinChunkSkylightProvider {
    @Inject(method = "method_51531", at = @At(value = "HEAD"), cancellable = true)
    private void hookRecalculateLevel(long blockPos, long l, int lightLevel, CallbackInfo ci)
    {
        if (NoRender.INSTANCE.skyLight.getValue() && NoRender.INSTANCE.isEnabled())
        {
            ci.cancel();
        }
    }
}