package me.skitttyy.kami.api.event.events.player;

import me.skitttyy.kami.api.event.Event;
import net.minecraft.entity.Entity;

public class PopEvent {
    public static class TotemPopEvent extends Event {
        Entity entity;
        int totalPops;

        public TotemPopEvent(Entity entity, int totalPops)
        {
            this.entity = entity;
            this.totalPops = totalPops;
        }

        public Entity getEntity()
        {
            return entity;
        }

        public int getTotalPops()
        {
            return totalPops;
        }
    }


    public static class DeathPopEvent extends Event {
        Entity entity;
        int totalPops;

        public DeathPopEvent(Entity entity, int totalPops)
        {
            this.entity = entity;
            this.totalPops = totalPops;
        }

        public Entity getEntity()
        {
            return entity;
        }

        public int getTotalPops()
        {
            return totalPops;
        }
    }



}
