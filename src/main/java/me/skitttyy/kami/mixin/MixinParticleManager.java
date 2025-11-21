package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.event.events.render.ParticleEvent;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager
{

    @Inject(at = @At("HEAD"), method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", cancellable = true)
    public void addParticleHook(Particle particle, CallbackInfo ci)
    {
        ParticleEvent event = new ParticleEvent(particle);
        event.post();
        if (event.isCancelled())
            ci.cancel();
    }
}