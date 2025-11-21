package me.skitttyy.kami.api.event.events.render;

import lombok.Setter;
import me.skitttyy.kami.api.event.Event;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;

public class ScreenEvent {

    @Setter
    @Getter
    public static class SetScreen extends Event {

        Screen guiScreen;

        public SetScreen(Screen guiScreen)
        {
            this.guiScreen = guiScreen;
        }
    }


}
