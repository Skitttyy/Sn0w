package me.skitttyy.kami.impl.gui.components.value;

import me.skitttyy.kami.api.value.Value;

public interface ICustomComponent<Type> {

    void setValue(Value<Type> value);
}