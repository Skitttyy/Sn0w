package me.skitttyy.kami.impl.gui.components.value;

import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.widget.impl.ColorWidget;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.value.Value;

import java.awt.*;

public class ColorComponent extends ColorWidget {
    Value<Sn0wColor> colorValue;
    public ColorComponent(Value<Sn0wColor> colorValue) {
        super(colorValue.getName(), colorValue.getValue(), new Rect(0, 0, 0, 0));
        this.colorValue = colorValue;
    }

    @Override
    public Color getValue() {
        return colorValue.getValue().getColor();
    }


    @Override
    public void setSyncing(boolean syncing) {
        colorValue.getValue().setSync(syncing);
    }

    @Override
    public boolean getSyncing() {
        return colorValue.getValue().isSyncing();
    }

    @Override
    public void setValue(Color value) {
        colorValue.getValue().setColor(value);
    }

    @Override
    public boolean isActive() {
        return colorValue.isActive();
    }
}
