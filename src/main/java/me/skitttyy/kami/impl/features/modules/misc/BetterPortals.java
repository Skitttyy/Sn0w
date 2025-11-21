package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.item.PickaxeItem;

public class BetterPortals extends Module {
    public static BetterPortals INSTANCE;


    public BetterPortals()
    {
        super("BetterPortals", Category.Misc);
        INSTANCE = this;
    }


    @Override
    public String getDescription()
    {
        return "BetterPortals: lets you open guis in portals";
    }
}
