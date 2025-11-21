package me.skitttyy.kami.impl.gui.components.module;

import me.skitttyy.kami.api.gui.GUI;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.render.animation.Easing;
import me.skitttyy.kami.api.utils.render.animation.type.DynamicColorAnimation;
import me.skitttyy.kami.impl.features.modules.client.gui.Sn0wGui;
import me.skitttyy.kami.impl.gui.components.value.*;
import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.flow.Flow;
import me.skitttyy.kami.api.gui.flow.impl.LinearFlow;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.widget.impl.BindWidget;
import me.skitttyy.kami.api.gui.widget.impl.BooleanWidget;
import me.skitttyy.kami.api.value.Value;

import java.awt.*;

public class FeatureButton extends BooleanWidget {

    Feature feature;
    public boolean open;
    Rect header;
    public Flow flow;
    public DynamicColorAnimation animation;

    public FeatureButton(Feature feature, Rect dims)
    {
        super(feature.getName(), dims);
        this.feature = feature;
        this.open = false;
        this.header = new Rect(0, 0, 0, 0);
        this.flow = new LinearFlow(new Rect(0, 0, 0, 0), 2);
        if (feature instanceof Module)
        {
            flow.getComponents().add(new BindWidget("Module Bind", ((Module) feature).getBind()));
        }

        for (Value<?> value : feature.getValues())
        {
            IComponent component = value.getComponent();
            if (component == null)
            {
                if (value.getValue() instanceof Boolean)
                {
                    flow.getComponents().add(new BooleanComponent(((Value<Boolean>) value)));
                }
                if (value.getValue() instanceof String)
                {
                    if (value.getModes() != null)
                    {
                        flow.getComponents().add(new ComboBoxComponent(((Value<String>) value)));
                    } else
                    {
                        flow.getComponents().add(new StringComponent(((Value<String>) value)));
                    }
                }
                if (value.getValue() instanceof Number)
                {
                    flow.getComponents().add(new SliderComponent(((Value<Number>) value)));
                }
                if (value.getValue() instanceof Sn0wColor)
                {
                    flow.getComponents().add(new ColorComponent(((Value<Sn0wColor>) value)));
                }
            } else
            {
                flow.getComponents().add(component);
            }
        }
    }


    @Override
    public void draw(Context context, MouseHelper mouse)
    {
//        if(this.getTitle().contains("Auto")) {


        setValue(feature.isEnabled());

        updateDims(context);


        Color color = (this.getTitle().toLowerCase().contains(GUI.searchBar.value.toLowerCase()) && !GUI.searchBar.value.equals("")) ? Color.YELLOW : (getValue() ? context.getColorScheme().getMainColor(0) : context.getColorScheme().getSecondaryBackgroundColor());

        if (getDisplayDims().collideWithMouse(mouse))
        {
            if(!getValue())
                color = ColorUtil.newAlpha(Color.WHITE, 10);
            color = ColorUtil.brighten(color, 0.1f);
        }

        if (this.animation == null)
        {
            animation = new DynamicColorAnimation(color, Easing.LINEAR, 300);
        }
        animation.gotoColor(color);


        context.getRenderer().renderFeatureButton(this, context, getDisplayDims(), mouse);

        if (open)
        {
            flow.draw(context, mouse);
        }
//        }
    }

    public void updateDims(Context context)
    {
        header.setX(getDims().getX());
        header.setY(getDims().getY());
        header.setWidth(getDims().getWidth());
        header.setHeight(context.getMetrics().getButtonHeight());

        flow.getDims().setX(getDims().getX() + context.getMetrics().getSpacing());
        flow.getDims().setY(getDims().getY() + header.getHeight() + context.getMetrics().getBetweenSpacing());
        flow.getDims().setWidth(getDims().getWidth() - context.getMetrics().getSpacing() * 2);
        flow.positionComponents(context);

        getDims().setHeight(context.getMetrics().getButtonHeight());

        if (open)
        {
            getDims().setHeight(header.getHeight() + flow.getDims().getHeight());
        }
    }

    @Override
    public void click(Context context, MouseHelper mouse, int button)
    {
        super.click(context, mouse, button);
        if (getDisplayDims().collideWithMouse(mouse))
        {
            if (button == 0)
            {
                feature.setEnabled(getValue());
            }
            if (button == 1)
            {
                open = !open;
            }
        }

        if (open) flow.click(context, mouse, button);
    }

    @Override
    public void release(Context context, MouseHelper mouse, int state)
    {
        super.release(context, mouse, state);
        if (open) flow.release(context, mouse, state);

    }

    @Override
    public void key(Context context, int key, char character)
    {
        super.key(context, key, character);
        if (open) flow.key(context, key, character);

    }

    @Override
    public void charTyped(Context context, char character)
    {
        super.charTyped(context, character);
        if (open) flow.charTyped(context, character);

    }

    @Override
    public Rect getDims()
    {
        return super.getDims();
    }

    public Feature getFeature()
    {
        return feature;
    }

    @Override
    public Rect getDisplayDims()
    {
        return header;
    }
}