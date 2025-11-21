package me.skitttyy.kami.api.management.notification;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.management.FeatureManager;
import me.skitttyy.kami.api.management.notification.types.CrosshairNotification;
import me.skitttyy.kami.api.management.notification.types.TopNotification;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.hud.ArmorWarner;
import me.skitttyy.kami.impl.features.hud.Info;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager implements IMinecraft {
    public static NotificationManager INSTANCE;
    public List<CrosshairNotification> crosshairNotifications;
    public List<TopNotification> topNotifications;
    ScaledResolution resolution;

    public NotificationManager()
    {
        INSTANCE = this;
        KamiMod.EVENT_BUS.register(this);
        crosshairNotifications = new ArrayList<>();
        topNotifications = new ArrayList<>();

    }

    @SubscribeEvent
    public void draw(RenderGameOverlayEvent.Text event)
    {
        if (NullUtils.nullCheck()) return;


        resolution = new ScaledResolution(mc);
        crosshairNotifications.removeIf(CrosshairNotification::isComplete);
        topNotifications.removeIf(TopNotification::isComplete);

        doCrosshairs(event.getContext());
        doTop(event.getContext());
    }

    public void doCrosshairs(DrawContext context)
    {
        if (!crosshairNotifications.isEmpty())
        {
            int index = 0;
            for (CrosshairNotification notification : crosshairNotifications)
            {
                notification.draw(context, index, resolution);

                if (!notification.getTimer().isPassed())
                    index++;
            }
        }
    }

    public void doTop(DrawContext context)
    {
        int index = 0;


        if (Info.INSTANCE.isEnabled() && !Info.INSTANCE.LAG_COMPONENT.isComplete())
        {
            Info.INSTANCE.LAG_COMPONENT.draw(context, index, resolution);
            index++;
        }
        if (Info.INSTANCE.isEnabled() && !Info.INSTANCE.PEARL_COMPONENT.isComplete())
        {
            Info.INSTANCE.PEARL_COMPONENT.draw(context, index, resolution);
            index++;
        }

        if (ArmorWarner.INSTANCE.isEnabled())
        {
            if (!ArmorWarner.INSTANCE.HELMET_COMPONENT.isComplete())
            {
                ArmorWarner.INSTANCE.HELMET_COMPONENT.draw(context, index, resolution);
                index++;
            }

            if (!ArmorWarner.INSTANCE.CHESTPLATE_COMPONENT.isComplete())
            {
                ArmorWarner.INSTANCE.CHESTPLATE_COMPONENT.draw(context, index, resolution);
                index++;
            }

            if (!ArmorWarner.INSTANCE.LEGGINGS_COMPONENT.isComplete())
            {
                ArmorWarner.INSTANCE.LEGGINGS_COMPONENT.draw(context, index, resolution);
                index++;
            }
            if (!ArmorWarner.INSTANCE.BOOTS_COMPONENT.isComplete())
            {
                ArmorWarner.INSTANCE.BOOTS_COMPONENT.draw(context, index, resolution);
                index++;
            }

        }

        if (!topNotifications.isEmpty())
        {
            for (TopNotification notification : topNotifications)
            {
                notification.draw(context, index, resolution);
                index++;
            }
        }
    }

    public void addNotification(CrosshairNotification notification)
    {
        crosshairNotifications.add(notification);
    }

    public void addNotification(TopNotification notification)
    {
        topNotifications.add(notification);
    }
}
