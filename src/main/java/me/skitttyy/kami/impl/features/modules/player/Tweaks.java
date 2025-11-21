package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.mixin.accessor.ILivingEntity;
import net.minecraft.entity.EntityPose;

public class Tweaks extends Module {
    Value<Boolean> fastJump = new ValueBuilder<Boolean>()
            .withDescriptor("Fast Jump")
            .withValue(false)
            .register(this);
    public Value<Boolean> noCrawl = new ValueBuilder<Boolean>()
            .withDescriptor("No Crawl")
            .withValue(false)
            .register(this);
    public Value<Boolean> crouch = new ValueBuilder<Boolean>()
            .withDescriptor("Crouch")
            .withValue(false)
            .register(this);

    public Value<Boolean> stealButton = new ValueBuilder<Boolean>()
            .withDescriptor("Steal Button")
            .withValue(false)
            .register(this);
    public Value<Boolean> regearButton = new ValueBuilder<Boolean>()
            .withDescriptor("Regear Button")
            .withValue(false)
            .register(this);
    public static Tweaks INSTANCE;

    public Tweaks()
    {
        super("Tweaks", Category.Player);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (fastJump.getValue() && ((ILivingEntity) mc.player).getLastJumpCooldown() > 0)
            ((ILivingEntity) mc.player).setLastJumpCooldown(0);

        if (noCrawl.getValue() && mc.player.getPose().equals(EntityPose.SWIMMING))
        {
            mc.player.setPose(EntityPose.STANDING);
        }


    }

    @Override
    public String getDescription()
    {
        return "Tweaks: Player tweaks, has stuff like fastjump";
    }
}
