package me.skitttyy.kami.impl.features.hud;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.gui.GUI;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.RainbowUtil;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Watermark extends HudComponent
{
    public static Watermark INSTANCE;

    public Watermark()
    {
        super("Watermark");
        INSTANCE = this;
    }

    Value<Boolean> wave = new ValueBuilder<Boolean>()
            .withDescriptor("Wave")
            .withValue(false)
            .register(this);
    Value<Boolean> white = new ValueBuilder<Boolean>()
            .withDescriptor("White")
            .withValue(false)
            .register(this);
    Value<Boolean> autoPos = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Pos")
            .withValue(true)
            .withAction(s ->
            {
                xPos.setActive(!s.getValue());
                yPos.setActive(!s.getValue());

            })
            .register(this);

    @SubscribeEvent
    @Override
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);

        if (autoPos.getValue())
        {
            yPos.setValue(1);
            xPos.setValue(1);
        }

        if (NullUtils.nullCheck() || renderCheck(event)) return;


        if (mc.currentScreen instanceof GUI) return;


        this.width = ClickGui.CONTEXT.getRenderer().getTextWidth(getClientName());
        this.height = ClickGui.CONTEXT.getRenderer().getTextHeight("AAA");
        if (wave.getValue())
            RainbowUtil.renderWave(event.getContext(), getClientName(), xPos.getValue().floatValue(), yPos.getValue().floatValue());
        else
            Fonts.doOneText(event.getContext(), getClientName(), xPos.getValue().floatValue(), yPos.getValue().floatValue(), HudColors.getTextColor(yPos.getValue().intValue()), FontModule.INSTANCE.textShadow.getValue());


    }

    public String getClientName()
    {
        return (Objects.equals(KamiMod.NAME, KamiMod.NAME_UNICODE) ? "Sn0w" : KamiMod.NAME) + " " + (white.getValue() ? Formatting.WHITE : "") + "v" + KamiMod.VERSION + "." + StringUtils.truncate(KamiMod.HASH, 7) + "-fabric";
    }

    @Override
    public String getDescription()
    {
        return "Watermark: Display the fact ur sn0wfull so sn0wless know whos boss";
    }

}