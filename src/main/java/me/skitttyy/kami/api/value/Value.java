package me.skitttyy.kami.api.value;

import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.utils.math.MathUtil;

import java.util.function.Consumer;

public class Value <Type> {

    String name;
    String tag;
    Type min;
    Type max;
    String[] modes;
    int places = 9999;
    boolean active = true;
    IComponent component;

    Consumer<Value<Type>> action;
    boolean enabled;
    Type value;

    public Type getValue() {

        return value;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public IComponent getComponent() {
        return component;
    }

    public void setComponent(IComponent component) {
        this.component = component;
    }

    public void setValue(Type value) {
        if(value instanceof Number && places != 9999){
            Number newVal = 0;
            newVal = MathUtil.roundToPlaces(((Number) value).floatValue(), places);
            value = (Type) newVal;
        }
        this.value = value;
        if (action != null){
            action.accept(this);
        }
    }

    public Type getMin() {
        return min;
    }

    public Type getMax() {
        return max;
    }

    public void setMin(Type min) {
        this.min = min;
    }

    public void setMax(Type max) {
        this.max = max;
    }
    public void setPlaces(int places) {
        this.places = places;
    }

    public String[] getModes() {
        return modes;
    }

    public void setModes(String[] modes) {
        this.modes = modes;
    }

    public Consumer<Value<Type>> getAction() {
        return action;
    }

    public void setAction(Consumer<Value<Type>> action) {
        this.action = action;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}