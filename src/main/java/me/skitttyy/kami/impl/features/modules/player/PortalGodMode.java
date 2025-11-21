package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;

import java.util.LinkedList;
import java.util.Queue;

public class PortalGodMode extends Module
{
    public static PortalGodMode INSTANCE;
    ScaledResolution sr;

    public PortalGodMode()
    {
        super("PortalGodMode", Category.Player);
        INSTANCE = this;
    }

    Queue<TeleportConfirmC2SPacket> tpPackets = new LinkedList<>();


    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (event.getPacket() instanceof TeleportConfirmC2SPacket)
        {
            this.tpPackets.add((TeleportConfirmC2SPacket) event.getPacket());
            event.setCancelled(true);
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        while (!this.tpPackets.isEmpty())
        {
            PacketManager.INSTANCE.sendPacket(tpPackets.poll());
        }
    }

    @SubscribeEvent
    public void draw(RenderGameOverlayEvent.Text event)
    {

        sr = new ScaledResolution(mc);


        Fonts.renderText(event.getContext(),
                "Currently In Godmode",
                ((float) sr.getScaledWidth() / 2) - (Fonts.getTextWidth("Currently In Godmode") / 2),
                (sr.getScaledHeight() / 2) + 10,
                HudColors.getTextColor((sr.getScaledHeight() / 2) + 10), FontModule.INSTANCE.textShadow.getValue());
    }

    @Override
    public String getDescription()
    {
        return "PortalGodMode: Makes u invincible when entering portals";
    }
}