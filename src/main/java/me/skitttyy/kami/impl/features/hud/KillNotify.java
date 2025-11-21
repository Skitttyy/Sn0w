package me.skitttyy.kami.impl.features.hud;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.management.SoundManager;
import me.skitttyy.kami.api.management.notification.NotificationManager;
import me.skitttyy.kami.api.management.notification.types.TopNotification;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import me.skitttyy.kami.impl.features.modules.combat.CatAura;
import me.skitttyy.kami.impl.features.modules.combat.KillAura;
import me.skitttyy.kami.impl.features.modules.misc.AutoEZ;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.Random;

public class KillNotify extends HudComponent {
    public KillNotify()
    {
        super("KillNotify");
    }

    int killstreak = 0, kills = 0;

    @SubscribeEvent
    @Override
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);
        this.width = 0;
        this.height = 0;

    }

    public String[] messages = {
            "lel we sent {name} back to the poor farm",
            "awarded the award as imperator we sent {name} to death he will not return home",
            "thanks to this guinness i ordered 3123 attack packets to {name}",
            "skitty used tail whip super effective to poooron {name}",
            "不错的 iq，我们让你很穷 meow {name}",
            "pk arrived he thought he could win {name}",
            "wtf is wrong with this boy {name}",
            "pooron nn hound {name} has crumbled to mcswaghax.fun",
            "{name} has been sent back to the poor novice village}",
            "LEL {name} just got packed up by skittyhack",
            "LOL! LOL! LOL! LOL! LOL! EZZZZZZZZZZZZZZZZ {name}"};

    @Override
    public void onDisable()
    {
        super.onDisable();

        kills = 0;
        killstreak = 0;
    }


    @SubscribeEvent
    private void onDeath(LivingEvent.Death event)
    {
        if (!(event.getEntity() instanceof PlayerEntity)) return;

        if (event.getEntity() == mc.player)
        {
            killstreak = 0;
        } else if (KillAura.INSTANCE.target == event.getEntity() || CatAura.INSTANCE.target == event.getEntity())
        {
            killstreak++;
            kills++;
            Random random = new Random();
            NotificationManager.INSTANCE.addNotification(new TopNotification(messages[random.nextInt(messages.length)].replace("{name}", Manager.INSTANCE.getMainColor() + event.getEntity().getName().getString() + Formatting.RESET), 2000, 200L, Color.WHITE));
            NotificationManager.INSTANCE.addNotification(new TopNotification(killstreak + " kill(s)", 2000, 200L, new Color(255, 85, 85)));
        }
    }

    @Override
    public String getDescription()
    {
        return "KillNotify: lets u know if one of ur opps died";
    }

}