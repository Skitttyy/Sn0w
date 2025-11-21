package me.skitttyy.kami.impl.gui.components.value;

import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.widget.impl.TextEntryWidget;
import me.skitttyy.kami.api.value.Value;

public class StringComponent extends TextEntryWidget {
    Value<String> stringValue;
    public StringComponent(Value<String> stringValue) {
        super(new Rect(0, 0, 0, 0), stringValue.getValue());
        this.stringValue = stringValue;
    }

    @Override
    public String getValue() {
        return stringValue.getValue();
    }

    @Override
    public void setValue(String value) {
        stringValue.setValue(value);
        super.setValue(value);
    }

    @Override
    public boolean isActive() {
        return stringValue.isActive();
    }
}
