package me.skitttyy.kami.impl.features.modules.misc;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.world.EntityEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class Tracker extends Module {

    public final Object2IntOpenHashMap<String> registry = new Object2IntOpenHashMap<>();

    public static Tracker INSTANCE;
    private PlayerEntity trackedOpp;
    int usedXP;
    int usedStacks;

    public Tracker()
    {
        super("Tracker", Category.Misc);
        INSTANCE = this;
    }


    @Override
    public void onDisable()
    {
        super.onDisable();

        if (NullUtils.nullCheck()) return;


        usedXP = 0;
        trackedOpp = null;
        usedStacks = 0;
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (trackedOpp == null)
        {
            trackedOpp = (PlayerEntity) TargetUtils.getTarget(1337.0f);
            usedXP = 0;
            usedStacks = 0;
            if(trackedOpp != null)
            {
                ChatUtils.sendMessage(Formatting.BLUE + "Beginning to track " + trackedOpp.getName().getString());
            }

        } else
        {
            if (usedXP >= 64)
            {
                usedXP -= 64;
                usedStacks++;
                ChatUtils.sendMessage(Formatting.AQUA + trackedOpp.getName().getString() + " has wasted " + Formatting.BLUE + usedStacks + Formatting.AQUA + " stacks of xp!");
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingEvent.Death event)
    {
        if (trackedOpp == null) return;

        if (event.getEntity().equals(trackedOpp))
        {
            ChatUtils.sendMessage(Formatting.AQUA + trackedOpp.getName().getString() + "died with " + Formatting.BLUE + usedXP + Formatting.AQUA + " wasted.");
            usedXP = 0;
            usedStacks = 0;
        }
    }

    @Override
    public String getHudInfo()
    {
        if (trackedOpp == null)
        {
            return "";
        } else
        {
            return (usedXP + usedStacks * 64) + "";
        }
    }

    @SubscribeEvent
    public void onEntityAdd(EntityEvent.Add event)
    {
        if (trackedOpp == null) return;

        if (event.getEntity() instanceof ExperienceBottleEntity && Objects.equals(mc.world.getClosestPlayer(event.getEntity(), 3.0), trackedOpp))
        {
            usedXP++;
        }
    }


    @Override
    public String getDescription()
    {
        return "Tracker: Tracks peoples items (useful in duels)";
    }

}
