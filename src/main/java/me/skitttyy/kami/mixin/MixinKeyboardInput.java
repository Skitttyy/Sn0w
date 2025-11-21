package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.event.events.key.InputEvent;
import me.skitttyy.kami.api.event.events.move.SneakEvent;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput extends Input {


    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void hookTick$Pre(boolean slowDown, float slowDownFactor, CallbackInfo info)
    {
        InputEvent event = new InputEvent(this);
        event.post();
        if (event.isCancelled())
        {
            info.cancel();
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 5))
    private boolean sneakHook(KeyBinding instance)
    {
        SneakEvent event = new SneakEvent();
        event.post();
        if (event.isCancelled())
        {
            return false;
        }
        return instance.isPressed();
    }

    /**
     * @param slowDown
     * @param f
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/" +
            "client/input/KeyboardInput;sneaking:Z", shift = At.Shift.BEFORE), cancellable = true)
    private void hookTick$Post(boolean slowDown, float f, CallbackInfo ci)
    {
        InputEvent event = new InputEvent(this);
        event.post();

        if (event.isCancelled())
        {
            ci.cancel();
        }
    }

}