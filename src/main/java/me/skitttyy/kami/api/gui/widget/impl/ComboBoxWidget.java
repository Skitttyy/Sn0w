package me.skitttyy.kami.api.gui.widget.impl;

import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.widget.IWidget;
import me.skitttyy.kami.api.utils.chat.ChatUtils;

public class ComboBoxWidget implements IComponent, IWidget<String> {

    String title;
    String[] modes;
    String theValue;
    Rect dims;

    public ComboBoxWidget(String title, String theValue, String[] modes, Rect dims){
        this.title = title;
        this.modes = modes;
        this.theValue = theValue;
        this.dims = dims;
    }

    @Override
    public void draw(Context context, MouseHelper mouse) {
        getDims().setHeight(context.getMetrics().getButtonHeight());
        context.getRenderer().renderComboBox(this, context, getDisplayDims(), mouse);
    }

    @Override
    public void click(Context context, MouseHelper mouse, int button) {
        if (getDisplayDims().collideWithMouse(mouse)){
            int current = 0;
            int index = 0;
            for (String s : modes){
                if (s.equals(getValue())){
                    current = index;
                }
                index++;
            }
            int amount = 1;//button == 0 ? 1 : button == 1 ? -1 : 0;
            if (current + amount > modes.length - 1){
                setValue(modes[0]);
            } else {
                setValue(modes[current + amount]);
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

    @Override
    public String getValue() {
        return theValue;
    }

    @Override
    public void setValue(String value) {
        theValue = value;
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
    public int getLevel() {
        return 3;
    }
}
