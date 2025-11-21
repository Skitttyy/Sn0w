package me.skitttyy.kami.api.event.events.render;


import lombok.Getter;
import me.skitttyy.kami.api.event.Event;
import net.minecraft.client.util.math.MatrixStack;

@Getter
public class RenderWorldEvent extends Event {
    private final MatrixStack matrices;
    private final float tickDelta;

    public RenderWorldEvent(MatrixStack matrices, float tickDelta)
    {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

}