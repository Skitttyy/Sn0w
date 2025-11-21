package me.skitttyy.kami.api.gui.widget.impl;

import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.value.custom.Bind;
import me.skitttyy.kami.api.gui.widget.IWidget;
import net.minecraft.client.util.InputUtil;

public class BindWidget implements IWidget<Bind>, IComponent {

    Bind value;
    String title;
    Rect dims;
    boolean binding;

    public BindWidget(String title, Bind value){
        this.title = title;
        this.value = value;
        this.dims = new Rect(0, 0, 0, 0);
    }

    @Override
    public Bind getValue() {
        return value;
    }

    @Override
    public void setValue(Bind value) {
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
        return dims;
    }

    @Override
    public void draw(Context context, MouseHelper mouse) {
        getDims().setHeight(context.getMetrics().getButtonHeight());
        context.getRenderer().renderBindWidget(this, context, getDisplayDims(), mouse);

    }

    @Override
    public void click(Context context, MouseHelper mouse, int button) {
        if (getDims().collideWithMouse(mouse)) {
            if (button == 0){
                binding = true;
            }
            if (button == 1){
                getValue().setKey(-1);
                getValue().setIsMouse(false);
            }
        }
    }

    @Override
    public void release(Context context, MouseHelper mouse, int state) {

    }

    @Override
    public void key(Context context, int key, char character) {
        if (binding){
            if (key != InputUtil.GLFW_KEY_ESCAPE) {
                getValue().setKey(key);
                getValue().setIsMouse(false);

            }
            binding = false;
        }
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

    public boolean isBinding() {
        return binding;
    }

    public void setBinding(boolean binding) {
        this.binding = binding;
    }
}
