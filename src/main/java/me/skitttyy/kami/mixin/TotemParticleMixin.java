package me.skitttyy.kami.mixin;

import me.skitttyy.kami.impl.features.modules.render.Particles;
import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(TotemParticle.class)
public class TotemParticleMixin extends AnimatedParticle {
    protected TotemParticleMixin(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider, float upwardsAcceleration)
    {
        super(world, x, y, z, spriteProvider, upwardsAcceleration);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void colorChanger(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider, CallbackInfo ci)
    {
        if (Particles.INSTANCE.isEnabled())
        {
            Color color = Particles.INSTANCE.colorOne.getValue().getColor();
            if (Particles.INSTANCE.doubleColor.getValue())
            {
                Color colorTwo = Particles.INSTANCE.colorTwo.getValue().getColor();

                if (this.random.nextInt(2) == 0)
                {
                    this.setColor((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255);
                } else
                {
                    this.setColor((float) colorTwo.getRed() / 255, (float) colorTwo.getGreen() / 255, (float) colorTwo.getBlue() / 255);
                }
            } else
            {
                this.setColor((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255);
            }
        }
    }
}