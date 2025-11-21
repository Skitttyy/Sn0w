package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;

public class Dolphin extends Module {

    public static Dolphin INSTANCE;

    public Dolphin()
    {
        super("Dolphin", Category.Movement);
        INSTANCE = this;
    }

    Timer timer = new Timer();

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        timer.setDelay(400L);
        if ((mc.player.isSubmergedInWater() || mc.player.isInLava()))
        {
            if (timer.isPassed())
            {
                if (mc.player.isSneaking())
                {
                    PlayerUtils.setMotionY(-0.1);
                } else if (mc.options.jumpKey.isPressed())
                {
                    PlayerUtils.setMotionY(0.09);
                }
            }
        } else
        {
            timer.resetDelay();
        }
    }


    @Override
    public String getHudInfo()
    {
        return "AAC";
    }

    @Override
    public String getDescription()
    {
        return "Dolphin: Swim like a dolphin in water (dive down and up)";
    }
}
