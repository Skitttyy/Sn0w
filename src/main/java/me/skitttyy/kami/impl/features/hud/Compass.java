package me.skitttyy.kami.impl.features.hud;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.impl.gui.ClickGui;

import java.awt.*;

public class Compass extends HudComponent {
    Value<Boolean> autoPos = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Pos")
            .withValue(true)
            .withAction(s ->
            {
                xPos.setActive(!s.getValue());
                yPos.setActive(!s.getValue());
            })
            .register(this);

    Value<Number> scale = new ValueBuilder<Number>()
            .withDescriptor("Scale")
            .withValue(16)
            .withRange(1, 60)
            .register(this);

    public Compass()
    {
        super("Compass");
    }

    private enum Direction {
        N,
        W,
        S,
        E
    }

    private static final double half_pi = Math.PI / 2;

    @SubscribeEvent
    @Override
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);

        if (NullUtils.nullCheck() || renderCheck(event)) return;

        if (autoPos.getValue())
        {
            ScaledResolution sr = new ScaledResolution(mc);
            xPos.setValue(sr.getScaledWidth() / 2.D);
            yPos.setValue(sr.getScaledHeight() * 0.8D);
        }
        this.width = 5;
        this.height = 5;

        for (Direction dir : Direction.values())
        {

            double rad = getPosCompass(dir);
            if (dir.name().equals("N"))
            {
                Fonts.doOneText(event.getContext(),
                        dir.name(),
                        (float) (xPos.getValue().floatValue() + getX(rad)),
                        (float) (yPos.getValue().floatValue() + getY(rad)),
                        HudColors.getTextColor(yPos.getValue().intValue()),
                        ClickGui.CONTEXT.getColorScheme().doesTextShadow());
            } else
            {
                Fonts.doOneText(
                        event.getContext(),
                        dir.name(),
                        (float) (xPos.getValue().floatValue() + getX(rad)),
                        (float) (yPos.getValue().floatValue() + getY(rad)),
                        Color.WHITE,
                        ClickGui.CONTEXT.getColorScheme().doesTextShadow());
            }
        }
    }

    private double getPosCompass(Direction dir)
    {

        double yaw = Math.toRadians(MathUtil.wrap(mc.getCameraEntity().getYaw()));
        int index = dir.ordinal();
        return yaw + (index * half_pi);

    }

    private double getX(double rad)
    {
        return Math.sin(rad) * (scale.getValue().intValue());
    }

    private double getY(double rad)
    {

        final double epic_pitch = MathUtil.clamp(mc.getCameraEntity().getPitch() + 30f, -90f, 90f);
        final double pitch_radians = Math.toRadians(epic_pitch);
        return Math.cos(rad) * Math.sin(pitch_radians) * (scale.getValue().intValue());

    }

    @Override
    public String getDescription()
    {
        return "Compass: Forgehax compass that looks really cool and awesome";
    }
}
