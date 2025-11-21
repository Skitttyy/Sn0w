package me.skitttyy.kami.impl.gui.components;

import me.skitttyy.kami.impl.gui.components.module.FeatureButton;
import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.component.impl.FrameComponent;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.management.FeatureManager;

public class CategoryFrame extends FrameComponent {

    Module.Category category;

    public CategoryFrame(Module.Category category, Rect dims) {
        super(category.toString(), dims);
        this.category = category;
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()){
            if (feature.getCategory() == this.category){
                getFlow().getComponents().add(new FeatureButton(feature, new Rect(0, 0, 0, 0)));
            }
        }
    }

    public Module.Category getCategory() {
        return category;
    }

    public void setCategory(Module.Category category) {
        this.category = category;
    }
}
