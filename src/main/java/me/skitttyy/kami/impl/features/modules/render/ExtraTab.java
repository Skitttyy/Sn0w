package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.feature.module.Module;

public class ExtraTab extends Module {
    public static ExtraTab INSTANCE;

    public ExtraTab() {
        super("ExtraTab", Category.Render);
        INSTANCE = this;
    }


    @Override
    public String getDescription() {
        return "ExtraTab: Modifies the tab so you can see everyone online";
    }
}
