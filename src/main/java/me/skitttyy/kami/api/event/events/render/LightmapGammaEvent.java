package me.skitttyy.kami.api.event.events.render;

import me.skitttyy.kami.api.event.Event;

public class LightmapGammaEvent extends Event {
    private int gamma;
    public LightmapGammaEvent(int gamma) {
        this.gamma = gamma;
    }

    public int getGamma() {
        return gamma;
    }

    public void setGamma(int gamma) {
        this.gamma = gamma;
    }
}