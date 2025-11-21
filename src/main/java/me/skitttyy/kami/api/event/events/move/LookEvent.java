package me.skitttyy.kami.api.event.events.move;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.event.Event;
import net.minecraft.util.math.Vec3d;

public class LookEvent {

    @Getter
    @Setter
    public static class LookVelocityEvent extends Event {
        //
        private final Vec3d movementInput;
        private final float speed;
        private final float yaw;
        private Vec3d velocity;

        public LookVelocityEvent(Vec3d movementInput, float speed, float yaw, Vec3d velocity) {
            this.movementInput = movementInput;
            this.speed = speed;
            this.yaw = yaw;
            this.velocity = velocity;
        }
    }

    @Getter
    @Setter
    public static class RotationSetEvent extends Event {

        float yaw;
        float pitch;

        public RotationSetEvent(float yaw, float pitch)
        {
            this.yaw = yaw;
            this.pitch = pitch;
        }


    }


}
