package me.skitttyy.kami.api.gui.widget.impl;

import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;
import me.skitttyy.kami.api.gui.widget.IWidget;

public class SliderWidget implements IWidget<Number>, IComponent {
    int scroll = 1;
    String title;
    public Number value;
    public Number min;
    public Number max;
    boolean dragging;
    boolean locked = true;
    Rect dims;
    Rect slider;
    public SliderWidget(String title, Number number, Number min, Number max){
        this.title = title;
        this.value = number;
        this.min = min;
        this.max = max;
        this.dims = new Rect(0, 0, 0, 0);
        slider = new Rect(0, 0, 0, 0);
    }

    @Override
    public Number getValue() {
        return value;
    }

    @Override
    public void setValue(Number value) {
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

    @Override
    public void draw(Context context, MouseHelper mouse) {
        getDims().setHeight(context.getMetrics().getButtonHeight());

        slider.setHeight(getDims().getHeight());
        slider.setX(getDims().getX());
        slider.setY(getDims().getY());

        double sliderWidth = normalize(getValue().doubleValue(), min.doubleValue(), max.doubleValue()) * getDims().getWidth();
        slider.setWidth(((int) sliderWidth));


//        int dWheel = Mouse.getDWheel();
//        dWheel = Integer.compare(dWheel, 0);
//        dWheel *= scroll;
//        if (getDims().collideWithMouse(mouse)){
//           Number pk = dWheel;
//           this.setValue(pk.doubleValue() + this.value.intValue());
//        }
        if (dragging){
            Number newVal = 0;
            Number difference = max.doubleValue() - min.doubleValue();
            newVal = min.doubleValue() + (MathHelper.clamp(normalize(mouse.getX() - getDims().getX(), 0d, getDims().getWidth()), 0, 1) * difference.doubleValue());
            setValue(newVal);
        }
        context.getRenderer().renderSliderWidget(this, context, getDisplayDims(), slider, mouse);
    }

    @Override
    public void click(Context context, MouseHelper mouse, int button) {
        if (getDims().collideWithMouse(mouse)){
            if (button == 0){
                dragging = true;
            }
        }
    }

    @Override
    public void release(Context context, MouseHelper mouse, int state) {
        dragging = false;
    }

    @Override
    public void key(Context context, int key, char character) {
            if(key == InputUtil.GLFW_KEY_LEFT_CONTROL) {
                locked = true;
            }

    }

    @Override
    public void charTyped(Context context, char character)
    {

    }


    @Override
    public Rect getDims() {
        return dims;
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    double normalize(double value, double min, double max) {
        return ((value - min) / (max - min));
    }

    @Override
    public int getLevel() {
        return 3;
    }
}
