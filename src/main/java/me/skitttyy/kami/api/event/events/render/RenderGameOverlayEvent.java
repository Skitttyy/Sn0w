package me.skitttyy.kami.api.event.events.render;

import lombok.Setter;
import me.skitttyy.kami.api.event.Event;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.util.Iterator;

@Getter
@Setter
public class RenderGameOverlayEvent extends Event
{
    DrawContext context;


    public RenderGameOverlayEvent(DrawContext context)
    {
        this.context = context;
    }

    @Getter
    public static class Text extends RenderGameOverlayEvent
    {
        RenderTickCounter counter;

        public Text(DrawContext context, RenderTickCounter tickCounter)
        {
            super(context);
            this.counter = tickCounter;
        }
    }

    @Getter
    public static class BossBar
    {
        @Getter
        public static class Text extends Event
        {
            public ClientBossBar bossBar;
            public net.minecraft.text.Text name;

            public Text(net.minecraft.text.Text name, ClientBossBar bossBar)
            {
                this.name = name;
                this.bossBar = bossBar;
            }
        }


        @Getter
        public static class Iterate extends Event
        {
            public Iterator<ClientBossBar> iterator;

            public Iterate(Iterator<ClientBossBar> iterator)
            {
                this.iterator = iterator;
            }
        }

    }


}
