package me.skitttyy.kami.api.gui.flow;

import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;

import java.util.ArrayList;
import java.util.List;

public abstract class Flow implements IComponent {

    public List<IComponent> components;

    public Rect dims;
    int level;

    public Flow(Rect dims, int level){
        this.dims = dims;
        components = new ArrayList<>();
        this.level = level;
    }

    public void positionComponents(Context context){

    }

    @Override
    public void draw(Context context, MouseHelper mouse) {
        for (IComponent component : getComponents()){
            if (component.isActive()){
                component.draw(context, mouse);
            }
        }
    }

    @Override
    public void click(Context context, MouseHelper mouse, int button) {
        for (IComponent component : getComponents()){
            if (component.isActive()) component.click(context, mouse, button);
        }
    }

    @Override
    public void release(Context context, MouseHelper mouse, int state) {
        for (IComponent component : getComponents()){
            if (component.isActive()) component.release(context, mouse, state);
        }
    }

    @Override
    public void key(Context context, int key, char character) {
        for (IComponent component : getComponents()){
            if (component.isActive()) component.key(context, key, character);
        }
    }

    @Override
    public void charTyped(Context context, char character) {
        for (IComponent component : getComponents()){
            if (component.isActive()) component.charTyped(context, character);
        }
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

    public List<IComponent> getComponents() {
        return components;
    }

    @Override
    public int getLevel() {
        return level;
    }
}
