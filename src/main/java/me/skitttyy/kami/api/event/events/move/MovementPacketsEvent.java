package me.skitttyy.kami.api.event.events.move;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.event.Event;

@Getter
@Setter
public class MovementPacketsEvent extends Event {
    //
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;

    public MovementPacketsEvent(double x, double y, double z, float yaw, float pitch, boolean onGround)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }


}