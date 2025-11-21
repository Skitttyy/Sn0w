package me.skitttyy.kami.impl.features.hud;

import com.google.common.eventbus.Subscribe;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.gui.GUI;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.gui.hudeditor.HudEditor;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.impl.features.modules.player.Blink;
import me.skitttyy.kami.impl.gui.ClickGui;

public class BlinkDisplay extends HudComponent {
    public BlinkDisplay()
    {
        super("BlinkDisplay");
    }


    @SubscribeEvent
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);
        if (NullUtils.nullCheck() || renderCheck(event)) return;


        this.width = ClickGui.CONTEXT.getRenderer().getTextWidth("Currently Blinked");
        if (Blink.INSTANCE.isEnabled() || mc.currentScreen instanceof HudEditor)
        {
            Fonts.doOneText(
                    event.getContext(),
                    "Currently Blinked",
                    xPos.getValue().floatValue(),
                    yPos.getValue().floatValue(),
                    HudColors.getTextColor(yPos.getValue().intValue()),
                    ClickGui.CONTEXT.getColorScheme().doesTextShadow());
        }
    }

    @Override
    public String getDescription()
    {
        return "BlinkDisplay: Displays if you have blink enabled";
    }
}