package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.item.PickaxeItem;

public class NoEntityTrace extends Module {
    private static NoEntityTrace INSTANCE;


    public NoEntityTrace()
    {
        super("NoEntityTrace", Category.Misc);
        INSTANCE = this;
    }

    Value<Boolean> Pickaxe = new ValueBuilder<Boolean>()
            .withDescriptor("Pickaxe")
            .withValue(false)
            .withAction(s ->
            {
            })
            .register(this);

    public static boolean spoofTrace()
    {
        if (!INSTANCE.isEnabled())
        {
            return false;
        }
        if (INSTANCE.Pickaxe.getValue() && !(mc.player.getMainHandStack().getItem() instanceof PickaxeItem))
        {
            return false;
        }
        return true;
    }


    @Override
    public String getDescription()
    {
        return "NoEntityTrace: Makes your crosshair trace past entity's";
    }
}
