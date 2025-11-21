package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.SoundManager;
import me.skitttyy.kami.api.management.notification.NotificationManager;
import me.skitttyy.kami.api.management.notification.types.TopNotification;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import me.skitttyy.kami.impl.features.modules.combat.CatAura;
import me.skitttyy.kami.impl.features.modules.combat.KillAura;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.Random;

public class KillSounds extends Module
{


    public KillSounds()
    {
        super("KillSounds", Category.Misc);
    }

    @SubscribeEvent
    private void onDeath(LivingEvent.Death event)
    {
        if (!(event.getEntity() instanceof PlayerEntity)) return;

        if (!(event.getEntity() == mc.player))
        {
            SoundManager.INSTANCE.play(SoundManager.INSTANCE.KILL_SOUND);
        }
    }

    @Override
    public String getDescription()
    {
        return "KillSounds: plays a sound when you kill someone";
    }
}
