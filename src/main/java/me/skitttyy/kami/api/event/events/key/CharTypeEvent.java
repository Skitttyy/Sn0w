package me.skitttyy.kami.api.event.events.key;

import lombok.Getter;
import me.skitttyy.kami.api.event.Event;

@Getter
public class CharTypeEvent extends Event {
    private final char character;
    public CharTypeEvent(char character)
    {
        this.character = character;
    }
}
