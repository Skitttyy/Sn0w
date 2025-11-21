package me.skitttyy.kami.api.event.events.player;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.event.Event;

/**
 * @see me.skitttyy.kami.mixin.MixinClientPlayerInteractionManager
 */
@Getter
@Setter
public class ReachEvent extends Event {
    public float reach;
    
    public ReachEvent(float reach)
    {
        this.reach = reach;
    }

}
