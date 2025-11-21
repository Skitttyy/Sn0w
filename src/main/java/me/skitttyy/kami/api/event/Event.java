package me.skitttyy.kami.api.event;


import me.skitttyy.kami.impl.KamiMod;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CopyOnWriteArrayList;


public abstract class Event {

    private boolean cancelled;
    private static void post(Event event)
    {
        CopyOnWriteArrayList<EventData> dataList = KamiMod.EVENT_BUS.get(event.getClass());
        if (dataList != null)
        {
            for (EventData data : dataList)
            {
                try
                {
                    data.target.invoke(data.source, event);
                } catch (IllegalAccessException | InvocationTargetException ignored)
                {
                    // IGNORED
                }
            }
        }
    }

    public void post()
    {
        this.cancelled = false;
        post(this);
    }

    public boolean isCancelled()
    {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
}