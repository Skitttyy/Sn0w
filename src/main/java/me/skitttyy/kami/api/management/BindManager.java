package me.skitttyy.kami.api.management;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.key.InputEvent;
import me.skitttyy.kami.api.event.events.key.KeyboardEvent;
import me.skitttyy.kami.api.event.events.key.MouseEvent;
import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import me.skitttyy.kami.api.binds.IBindable;

import java.util.ArrayList;
import java.util.List;

public class BindManager implements IMinecraft {

    public static BindManager INSTANCE;

    List<IBindable> bindables;

    public BindManager()
    {
        bindables = new ArrayList<>();
        KamiMod.EVENT_BUS.register(this);
    }

    public List<IBindable> getBindables()
    {
        return bindables;
    }

    public void setBindables(List<IBindable> bindables)
    {
        this.bindables = bindables;
    }

    @SubscribeEvent
    public void onKey(KeyboardEvent event)
    {
        if (mc.currentScreen == null)
            for (IBindable bindable : getBindables())
            {
                if (bindable.getKey() == event.getKey() && event.getAction() == GLFW.GLFW_PRESS)
                {
                    bindable.onKey();
                }
            }
    }


    @SubscribeEvent
    public void onKey(MouseEvent event)
    {
        if (!event.getType().equals(MouseEvent.Type.CLICK)) return;

        for (Feature feature : FeatureManager.INSTANCE.getFeatures())
        {
            if (feature instanceof Module)
            {
                Module module = (Module) feature;
                if (module.getBind().getIsMouse())
                {
                    if (module.getBind().getKey() == event.getButton())
                    {
                        module.toggle();
                    }
                }
            }
        }
    }

}
