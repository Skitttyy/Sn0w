package me.skitttyy.kami.api.gui.widget.impl;

import me.skitttyy.kami.api.gui.GUI;
import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.widget.IWidget;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.render.animation.Easing;
import me.skitttyy.kami.api.utils.render.animation.type.DynamicColorAnimation;

import java.awt.*;

public class BooleanWidget implements IWidget<Boolean>, IComponent {

    String title;
    Rect dims;
    boolean value;

    public DynamicColorAnimation animation;
    public BooleanWidget(String title, Rect dims){
        this.title = title;
        this.dims = dims;
        this.value = true;
    }

    @Override
    public void draw(Context context, MouseHelper mouse) {
        getDims().setHeight(context.getMetrics().getButtonHeight());


        Color color = (getValue() ? context.getColorScheme().getMainColor(0) : context.getColorScheme().getSecondaryBackgroundColor());

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

        context.getRenderer().renderBooleanWidget(this, context, getDisplayDims(), mouse);
    }

    @Override
    public void click(Context context, MouseHelper mouse, int button) {
        if (getDisplayDims().collideWithMouse(mouse)) {
            if (button == 0) {
                setValue(!getValue());
            }
        }
    }

    @Override
    public void release(Context context, MouseHelper mouse, int state) {

    }

    @Override
    public void key(Context context, int key, char character) {

    }

    @Override
    public void charTyped(Context context, char character)
    {

    }

    @Override
    public int getLevel() {
        return 3;
    }

    @Override
    public Rect getDims() {
        return this.dims;
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Rect getDisplayDims() {
        return getDims();
    }
}
