package me.skitttyy.kami.api.value.builder;

import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.impl.gui.components.value.ICustomComponent;
import me.skitttyy.kami.api.gui.component.IComponent;

import java.util.function.Consumer;

public class ValueBuilder<Type> extends Value<Type> {

    Value<String> parent;
    Value<Boolean> booleanParent;
    String page;
    boolean parentEnabled;

    public ValueBuilder() {
    }

    public ValueBuilder<Type> withDescriptor(String name, String tag) {
        setName(name);
        setTag(tag);
        return this;
    }

    public ValueBuilder<Type> withDescriptor(String name) {
        // auto camel case lol
        setName(name);
        String camelCase = name.replace(" ", "");
        char[] chars = camelCase.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        camelCase = new String(chars);
        setTag(camelCase);
        return this;
    }

    public ValueBuilder<Type> withValue(Type value) {
        setValue(value);
        return this;
    }

    public ValueBuilder<Type> withAction(Consumer<Value<Type>> action) {
        setAction(action);
        return this;
    }

    public ValueBuilder<Type> withRange(Type min, Type max) {
        setMin(min);
        setMax(max);
        return this;
    }

    public ValueBuilder<Type> withPlaces(int places) {
        setPlaces(places);
        return this;
    }

    public ValueBuilder<Type> withPageParent(Value<String> parent) {
        this.parent = parent;
        return this;
    }

    public ValueBuilder<Type> withParent(Value<Boolean> parent) {
        this.booleanParent = parent;
        return this;
    }

    public ValueBuilder<Type> withPage(String page) {
        this.page = page;
        return this;
    }


    public ValueBuilder<Type> withParentEnabled(boolean parentEnabled) {
        this.parentEnabled = parentEnabled;
        return this;
    }

    @Override
    public boolean isActive() {
        boolean active = true;

        if (this.parent != null)
            if (!(parent.getValue().equals(page)))
                active = false;

        if (this.booleanParent != null && active) {
            try
            {
                if (!(booleanParent.getValue() == parentEnabled))
                    active = false;
            } catch (Exception e){
                ChatUtils.sendMessage(this.getName());
            }
        }
        return super.isActive() && active;
    }

    public ValueBuilder<Type> withModes(String... modes) {
        setModes(modes);
        return this;
    }

    public ValueBuilder<Type> withComponent(IComponent widget) {
        if (widget instanceof ICustomComponent) {
            ICustomComponent comp = (ICustomComponent) widget;
            comp.setValue(this);
        } else {
            throw new IllegalArgumentException();
        }
        setComponent(widget);
        return this;
    }

    public Value<Type> register(Feature feature) {
        feature.getValues().add(this);
        return this;
    }
}
