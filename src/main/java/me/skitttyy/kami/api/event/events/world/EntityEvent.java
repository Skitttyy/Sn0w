package me.skitttyy.kami.api.event.events.world;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.event.Event;
import net.minecraft.entity.Entity;

public class EntityEvent {



    @Getter
    @Setter
    public static class Add extends Event {
        Entity entity;

        public Add(Entity entity)
        {
            this.entity = entity;
        }
    }

    @Getter
    @Setter
    public static class Remove extends Event {
        Entity entity;
        Entity.RemovalReason reason;
        public Remove(Entity entity, Entity.RemovalReason reason)
        {
            this.entity = entity;
            this.reason = reason;
        }
    }

}
