package me.skitttyy.kami.mixin;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import me.skitttyy.kami.api.utils.render.RenderTimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.Dynamic.class)
public class MixinRenderTimer {

    @Shadow
    private float lastFrameDuration;
    @Shadow
    private float tickDelta;
    @Shadow
    private long prevTimeMillis;
    @Shadow
    private float tickTime;
    @Shadow
    private FloatUnaryOperator targetMillisPerTick;

    @Inject(method = "beginRenderTick(J)I", at = @At("HEAD"), cancellable = true)
    private void beginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> ci)
    {
        this.lastFrameDuration = (float) ((timeMillis - this.prevTimeMillis) / this.targetMillisPerTick.apply(this.tickTime)) * RenderTimer.getTickLength();
        this.prevTimeMillis = timeMillis;
        this.tickDelta += this.lastFrameDuration;
        int i = (int) this.tickDelta;
        this.tickDelta -= (float) i;

        ci.setReturnValue(i);
    }

}