package me.skitttyy.kami.api.gui.context;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.render.IRenderer;
import me.skitttyy.kami.api.gui.theme.IColorScheme;
import me.skitttyy.kami.api.gui.theme.IMetrics;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.impl.features.modules.client.gui.Sn0wGui;
import me.skitttyy.kami.impl.gui.renderer.Renderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Context {

    List<IComponent> components;
    IColorScheme colorScheme;
    IMetrics metrics;
    IRenderer renderer;
    float partialTicks = 0f;
    DrawContext drawContext;
    MouseHelper helper;
    public Context(DrawContext context, IColorScheme colorScheme, IMetrics metrics, IRenderer renderer, MouseHelper helper)
    {
        drawContext = context;
        this.components = new ArrayList<>();
        this.colorScheme = colorScheme;
        this.metrics = metrics;
        this.renderer = renderer;
        this.helper = helper;
    }

    public IComponent getHovering(MouseHelper mouseHelper)
    {
        IComponent hovering = null;
        for (IComponent component : getComponents())
        {
            if (component.getDims().collideWithMouse(mouseHelper)) hovering = component;
        }
        return hovering;
    }
    public ScaledResolution getScaledResolution()
    {
        Window window = MinecraftClient.getInstance().getWindow();
        return new ScaledResolution(window.getScaledWidth(), window.getScaledHeight());
    }



}
