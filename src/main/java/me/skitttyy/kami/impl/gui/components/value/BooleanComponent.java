package me.skitttyy.kami.impl.gui.components.value;

import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.widget.impl.BooleanWidget;
import me.skitttyy.kami.api.value.Value;

public class BooleanComponent extends BooleanWidget {

    Value<Boolean> value;

    public BooleanComponent(Value<Boolean> booleanValue) {
        super(booleanValue.getName(), new Rect(0, 0, 0, 0));
        this.value = booleanValue;
    }

    @Override
    public void draw(Context context, MouseHelper mouse) {
        setValue(value.getValue());
        super.draw(context, mouse);
    }

    @Override
    public void setValue(Boolean value) {
        super.setValue(value);
        this.value.setValue(value);
    }

    @Override
    public boolean isActive() {
        return value.isActive();
    }
}
