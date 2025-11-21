package me.skitttyy.kami.impl.features.modules.render;


import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class ViewModel extends Module {

    public static ViewModel INSTANCE;

    public Value<String> fovMode = new ValueBuilder<String>()
            .withDescriptor("FOV Mode")
            .withValue("None")
            .withModes("None", "Custom")
            .withAction(page -> handleFovPage(page.getValue()))
            .register(this);
    public Value<Number> fov = new ValueBuilder<Number>()
            .withDescriptor("FOV")
            .withValue(130)
            .withRange(60, 170)
            .register(this);
    public Value<Boolean> items = new ValueBuilder<Boolean>()
            .withDescriptor("Items")
            .withValue(true)
            .register(this);
    public Value<Number> itemFov = new ValueBuilder<Number>()
            .withDescriptor("Item Fov")
            .withValue(70)
            .withRange(60, 170)
            .register(this);

    public Value<Boolean> translate = new ValueBuilder<Boolean>()
            .withDescriptor("Translate")
            .withValue(false)
            .register(this);


    public Value<Number> posX = new ValueBuilder<Number>()
            .withDescriptor("X")
            .withValue(0)
            .withRange(-3.0, 3.0)
            .withPlaces(2)
            .withParent(translate)
            .withParentEnabled(true)
            .register(this);
    public Value<Number> posY = new ValueBuilder<Number>()
            .withDescriptor("Y")
            .withValue(0)
            .withRange(-3.0, 3.0)
            .withPlaces(2)
            .withParent(translate)
            .withParentEnabled(true)
            .register(this);
    public Value<Number> posZ = new ValueBuilder<Number>()
            .withDescriptor("Z")
            .withValue(0)
            .withRange(-3.0, 3.0)
            .withPlaces(2)
            .withParent(translate)
            .withParentEnabled(true)
            .register(this);

    public Value<Number> scaleX = new ValueBuilder<Number>()
            .withDescriptor("Scale X")
            .withValue(0)
            .withRange(-3.0, 3.0)
            .withPlaces(2)
            .withParent(translate)
            .withParentEnabled(true)
            .register(this);
    public Value<Number> scaleY = new ValueBuilder<Number>()
            .withDescriptor("Scale Y")
            .withValue(1.0)
            .withRange(0.1, 2.0)
            .withPlaces(1)
            .withParent(translate)
            .withParentEnabled(true)
            .register(this);
    public Value<Number> scaleZ = new ValueBuilder<Number>()
            .withDescriptor("Scale Z")
            .withValue(1.0)
            .withRange(0.1, 2.0)
            .withPlaces(1)
            .withParent(translate)
            .withParentEnabled(true)
            .register(this);

    public Value<Number> rotateX = new ValueBuilder<Number>()
            .withDescriptor("Rotate X")
            .withValue(0)
            .withRange(-180, 180)
            .withPlaces(0)
            .withParent(translate)
            .withParentEnabled(true)
            .register(this);
    public Value<Number> rotateY = new ValueBuilder<Number>()
            .withDescriptor("Rotate Y")
            .withValue(0)
            .withRange(-180, 180)
            .withPlaces(0)
            .withParent(translate)
            .withParentEnabled(true)
            .register(this);
    public Value<Number> rotateZ = new ValueBuilder<Number>()
            .withDescriptor("Rotate Z")
            .withValue(0)
            .withRange(-180, 180)
            .withPlaces(0)
            .withParent(translate)
            .withParentEnabled(true)
            .register(this);

    public ViewModel()
    {
        super("ViewModel", Category.Render);
        INSTANCE = this;
    }

    public void handleFovPage(String page)
    {
        fov.setActive(page.equals("Custom"));
        items.setActive(page.equals("Custom"));
        itemFov.setActive(page.equals("Custom") && items.getValue());


    }

    @Override
    public String getDescription()
    {
        return "ViewModel: change where the items render in your hand";
    }

}
