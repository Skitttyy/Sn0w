package me.skitttyy.kami.api.event.events.move;

import me.skitttyy.kami.api.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalkingForwardEvent extends Event {

    boolean walksForward;

    public WalkingForwardEvent(boolean walksForward)
    {
        this.walksForward = walksForward;
    }
}