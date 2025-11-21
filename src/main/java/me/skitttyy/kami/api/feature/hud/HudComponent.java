package me.skitttyy.kami.api.feature.hud;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.gui.hudeditor.HudEditorGUI;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.gui.ClickGui;

public class HudComponent extends Feature implements IMinecraft
{

    public Value<Number> xPos = new ValueBuilder<Number>()
            .withDescriptor("X Pos")
            .withValue(100)
            .withRange(0, 1000)
            .register(this);

    public Value<Number> yPos = new ValueBuilder<Number>()
            .withDescriptor("Y Pos")
            .withValue(10)
            .withRange(0, 1000)
            .register(this);

    public boolean immovable;
    @Setter
    @Getter
    protected int width = 30;
    @Setter
    @Getter
    protected int height = 9;

    public HudComponent(String name)
    {
        super(name, Category.Hud, FeatureType.Hud);
    }

    public ScaledResolution sr;

    @SubscribeEvent
    public void draw(RenderGameOverlayEvent.Text event)
    {

        sr = new ScaledResolution(mc);
        xPos.setMax(sr.getScaledWidth());
        yPos.setMax(sr.getScaledHeight() - ClickGui.CONTEXT.getRenderer().getTextHeight("AAA"));
    }

    public boolean renderCheck(RenderGameOverlayEvent.Text event)
    {
        return mc.currentScreen instanceof HudEditorGUI && event.getCounter() != null;
    }
}
