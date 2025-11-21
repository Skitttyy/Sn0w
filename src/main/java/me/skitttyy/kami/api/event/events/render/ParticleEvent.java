package me.skitttyy.kami.api.event.events.render;

import me.skitttyy.kami.api.event.Event;
import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class ParticleEvent extends Event {
    private final Particle particle;

    public ParticleEvent(Particle particle) {
        this.particle = particle;
    }

    public Particle getParticle() {
        return particle;
    }

}