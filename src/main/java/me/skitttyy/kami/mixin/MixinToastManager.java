package me.skitttyy.kami.mixin;

import me.skitttyy.kami.impl.features.modules.render.NoRender;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class MixinToastManager {

    @Inject(method = "draw", at = @At(value = "HEAD"), cancellable = true)
    private void hookDraw(DrawContext context, CallbackInfo ci)
    {
        if (NoRender.INSTANCE.advancements.getValue())
        {
            ci.cancel();
        }
    }
}