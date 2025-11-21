package me.skitttyy.kami.api.event.eventbus;

import me.skitttyy.kami.api.event.Event;
import me.skitttyy.kami.api.event.EventData;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;




public class EventManager {
    private final Map<Class<? extends Event>, CopyOnWriteArrayList<EventData>> REGISTRY_MAP = new HashMap();

    public void register(Object o)
    {
        for (Method method : o.getClass().getDeclaredMethods())
        {
            if (!isMethodBad(method))
            {
                register(method, o);
            }
        }
    }

    public void register(Object o, Class<? extends Event> clazz)
    {
        for (Method method : o.getClass().getDeclaredMethods())
        {
            if (!isMethodBad(method, clazz))
            {
                register(method, o);
            }
        }
    }

    private void register(Method method, Object o)
    {
        Class<?> clazz = method.getParameterTypes()[0];
        final EventData methodData = new EventData(o, method, method.getAnnotation(SubscribeEvent.class).value());

        if (!methodData.target.isAccessible())
        {
            methodData.target.setAccessible(true);
        }

        if (REGISTRY_MAP.containsKey(clazz))
        {
            if (!REGISTRY_MAP.get(clazz).contains(methodData))
            {
                REGISTRY_MAP.get(clazz).add(methodData);
                sortListValue((Class<? extends Event>) clazz);
            }
        } else
        {
            REGISTRY_MAP.put((Class<? extends Event>) clazz, new CopyOnWriteArrayList<>() {
                {
                    this.add(methodData);
                }
            });
        }
    }

    public void unregister(final Object o)
    {
        for (CopyOnWriteArrayList<EventData> flexibalArray : REGISTRY_MAP.values())
        {
            flexibalArray.removeIf(methodData -> methodData.source.equals(o));
        }

        cleanMap(true);
    }

    public void unregister(final Object o, final Class<? extends Event> clazz)
    {
        if (REGISTRY_MAP.containsKey(clazz))
        {
            for (final EventData methodData : REGISTRY_MAP.get(clazz))
            {
                if (methodData.source.equals(o))
                {
                    REGISTRY_MAP.get(clazz).remove(methodData);
                }
            }

            cleanMap(true);
        }
    }

    public void cleanMap(boolean b)
    {

        Iterator<Map.Entry<Class<? extends Event>, CopyOnWriteArrayList<EventData>>> iterator = REGISTRY_MAP.entrySet().iterator();

        while (iterator.hasNext())
        {
            if (!b || iterator.next().getValue().isEmpty())
            {
                iterator.remove();
            }
        }
    }

    public void removeEnty(Class<? extends Event> clazz)
    {

        Iterator<Map.Entry<Class<? extends Event>, CopyOnWriteArrayList<EventData>>> iterator = REGISTRY_MAP.entrySet().iterator();

        while (iterator.hasNext())
        {
            if (iterator.next().getKey().equals(clazz))
            {
                iterator.remove();
                break;
            }
        }
    }

    private void sortListValue(Class<? extends Event> clazz)
    {

        CopyOnWriteArrayList<EventData> flexibleArray = new CopyOnWriteArrayList<EventData>();

        for (byte b : Priority.VALUE_ARRAY)
        {
            for (EventData methodData : REGISTRY_MAP.get(clazz))
            {
                if (methodData.priority == b)
                {
                    flexibleArray.add(methodData);
                }
            }
        }

        REGISTRY_MAP.put(clazz, flexibleArray);
    }

    private boolean isMethodBad(final Method method)
    {

        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(SubscribeEvent.class);
    }

    private boolean isMethodBad(Method method, Class<? extends Event> clazz)
    {
        return isMethodBad(method) || method.getParameterTypes()[0].equals(clazz);
    }

    public CopyOnWriteArrayList<EventData> get(final Class<? extends Event> clazz)
    {
        return REGISTRY_MAP.get(clazz);
    }

    public void shutdown()
    {
        REGISTRY_MAP.clear();
    }

}