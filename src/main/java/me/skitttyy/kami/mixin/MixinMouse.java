package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.event.events.key.MouseEvent;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.render.Freecam;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse implements IMinecraft {

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    public void mouseEvent(long window, int button, int action, int mods, CallbackInfo ci)
    {
        if (window == mc.getWindow().getHandle())
        {
            MouseEvent event = new MouseEvent(button, MouseEvent.Type.of(action));
            event.post();
            if (event.isCancelled())
            {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private void updateMouseChangeLookDirection(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY) {


        if (Freecam.INSTANCE.isEnabled()) Freecam.INSTANCE.changeLookDirection(cursorDeltaX * 0.15, cursorDeltaY * 0.15);
        else player.changeLookDirection(cursorDeltaX, cursorDeltaY);
    }

}