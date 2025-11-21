package me.skitttyy.kami.api.gui.hudeditor;

import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.management.FeatureManager;

import me.skitttyy.kami.impl.features.modules.client.gui.Sn0wGui;
import me.skitttyy.kami.impl.gui.components.CategoryFrame;
import me.skitttyy.kami.impl.gui.renderer.Renderer;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.hudeditor.components.HudDisplay;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.helpers.Rect;

public class HudEditorGUI extends HudEditor {

    public static Context CONTEXT = new Context(null, Sn0wGui.INSTANCE, Sn0wGui.INSTANCE, new Renderer(), null);

    public static HudEditorGUI INSTANCE;

    public HudEditorGUI() {
        super(CONTEXT);
    }

    @Override
    public void addComponents() {
        super.addComponents();

        int offset = 100;
        for (Module.Category category : Feature.Category.values()) {
            if (category == Feature.Category.Hud) {
                getContext().getComponents().add(new CategoryFrame(category, new Rect(offset, 40, 100, 200)));
                offset += getContext().getMetrics().getFrameWidth() + 10;
            }
        }
        for(Feature feature : FeatureManager.INSTANCE.getFeatures()){
            if(feature.getCategory() == Feature.Category.Hud){
                HudComponent hudModule = (HudComponent) feature;
                getContext().getComponents().add(new HudDisplay(new Rect(hudModule.xPos.getValue().intValue(), hudModule.yPos.getValue().intValue(), hudModule.getWidth(), hudModule.getHeight()), hudModule));
            }
        }
    }


}