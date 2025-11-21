package me.skitttyy.kami.api.event.events.render;

import me.skitttyy.kami.api.event.Event;
import net.minecraft.entity.Entity;

public class EntityOutlineEvent extends Event {
    private final Entity entity;

    public EntityOutlineEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}