package me.skitttyy.kami.api.event.events.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.skitttyy.kami.api.event.Event;
@Getter
@AllArgsConstructor
public class ClientChatEvent extends Event {
    public String message;
}
