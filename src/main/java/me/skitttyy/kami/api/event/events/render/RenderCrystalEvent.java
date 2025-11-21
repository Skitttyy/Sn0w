package me.skitttyy.kami.api.event.events.render;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.event.Event;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;


@Getter
@Setter
public class RenderCrystalEvent extends Event
{
    public final EndCrystalEntity endCrystalEntity;
    public final float f;
    public final float g;
    public final MatrixStack matrixStack;
    public final int i;
    public final ModelPart core;
    public final ModelPart frame;
    public RenderCrystalEvent(EndCrystalEntity endCrystalEntity, float f, float g,
                              MatrixStack matrixStack, int i, ModelPart core, ModelPart frame) {
        this.endCrystalEntity = endCrystalEntity;
        this.f = f;
        this.g = g;
        this.matrixStack = matrixStack;
        this.i = i;
        this.core = core;
        this.frame = frame;
    }
}