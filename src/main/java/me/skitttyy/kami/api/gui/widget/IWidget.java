package me.skitttyy.kami.api.gui.widget;

import me.skitttyy.kami.api.gui.helpers.Rect;

public interface IWidget<Type> {

    Type getValue();
    void setValue(Type value);
    String getTitle();
    void setTitle(String title);
    Rect getDisplayDims();
}
