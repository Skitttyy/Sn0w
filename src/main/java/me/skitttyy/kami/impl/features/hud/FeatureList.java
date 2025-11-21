package me.skitttyy.kami.impl.features.hud;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.utils.render.animation.Animation;
import me.skitttyy.kami.api.utils.render.animation.Easing;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.management.FeatureManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FeatureList extends HudComponent
{
    //    public Animation animation;
    public static FeatureList INSTANCE;
    Value<String> alignment = new ValueBuilder<String>()
            .withDescriptor("Alignment")
            .withValue("TopLeft")
            .withModes("TopLeft", "BottomLeft", "TopRight", "BottomRight")
            .register(this);
    Value<Boolean> animations = new ValueBuilder<Boolean>()
            .withDescriptor("Animation")
            .withValue(false)
            .register(this);
    Value<String> easing = new ValueBuilder<String>()
            .withDescriptor("Easing")
            .withValue("Linear")
            .withModes("Linear", "Exponential", "Cubic", "Elastic", "Bounce")
            .withParent(animations)
            .withParentEnabled(true)
            .register(this);
    public Value<Number> animationSpeed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(1)
            .withPlaces(2)
            .withParent(animations)
            .withParentEnabled(true)
            .withRange(0.1, 2)
            .register(this);
    public Value<Boolean> animateHudInfo = new ValueBuilder<Boolean>()
            .withDescriptor("Bouncing")
            .withValue(true)
            .withParent(animations)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> noBrackets = new ValueBuilder<Boolean>()
            .withDescriptor("No Brackets")
            .withValue(false)
            .register(this);
    Value<Boolean> spacing = new ValueBuilder<Boolean>()
            .withDescriptor("Spacing")
            .withValue(false)
            .register(this);
    Value<Boolean> autoPos = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Pos")
            .withValue(true)
            .withAction(s ->
            {
                xPos.setActive(!s.getValue());
                yPos.setActive(!s.getValue());

            })
            .register(this);

    public FeatureList()
    {
        super("FeatureList");
        INSTANCE = this;
    }

    public void onEnable()
    {
        super.onEnable();
    }


    @SubscribeEvent
    @Override
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);


        if(autoPos.getValue())
        {
            ScaledResolution resolution = new ScaledResolution(mc);
            int resWidth = resolution.getScaledWidth();
            FeatureList.INSTANCE.xPos.setValue(resWidth - 1);
            FeatureList.INSTANCE.yPos.setValue(1);
        }

        if (NullUtils.nullCheck() || renderCheck(event)) return;

        ArrayList<Feature> sorted = new ArrayList<>();

        for (Feature feature : FeatureManager.INSTANCE.getFeatures())
        {
            feature.animation();
            float progress = (float) MathUtil.distance((float) feature.endpoint, (float) feature.getOffset(),  false);
            if (feature.visible.getValue() && (feature.isEnabled() || (animations.getValue() && progress > 5)))
            {
                sorted.add(feature);
            }
        }
        this.width = (30);
        sorted.sort(Comparator.comparingDouble(mod ->
        {

            float o;
            o = noBrackets.getValue() ? ClickGui.CONTEXT.getRenderer().getTextWidthFloat(mod.getDisplayName() + (!(mod.getHudInfo() == null || mod.getHudInfo().equals("")) ? Formatting.GRAY + " " + mod.getHudInfo() : "")) : ClickGui.CONTEXT.getRenderer().getTextWidth(mod.getDisplayName() + (!(mod.getHudInfo() == null || mod.getHudInfo().equals("")) ? Formatting.GRAY + " [" + Formatting.WHITE + mod.getHudInfo() + Formatting.GRAY + "]" : ""));
            return -o;
        }));

        if (!sorted.isEmpty())
        {
            Feature longest = sorted.get(0);
            String text = longest.getDisplayName() + (!(longest.getHudInfo() == null || longest.getHudInfo().equals("")) ? Formatting.GRAY + " [" + Formatting.WHITE + longest.getHudInfo() + Formatting.GRAY + "]" : "");
            if (noBrackets.getValue())
            {
                text = longest.getDisplayName() + (!(longest.getHudInfo() == null || longest.getHudInfo().equals("")) ? Formatting.GRAY + " " + Formatting.GRAY + longest.getHudInfo() : "");
            }

            int textWidth = ClickGui.CONTEXT.getRenderer().getTextWidth(text);
            width = alignment.getValue().contains("Right") ? -textWidth : textWidth;
            height = alignment.getValue().contains("Bottom") ? -((ClickGui.CONTEXT.getRenderer().getTextHeight("AA") + (spacing.getValue() ? 1 : 0)) * sorted.size()) : ((ClickGui.CONTEXT.getRenderer().getTextHeight("AA") + (spacing.getValue() ? 1 : 0)) * sorted.size());

        } else
        {
            width = alignment.getValue().contains("Right") ? -30 : 30;
            height = alignment.getValue().contains("Bottom") ? -ClickGui.CONTEXT.getRenderer().getTextHeight("AA") : ClickGui.CONTEXT.getRenderer().getTextHeight("AA");
        }
        int textOffset = 0;
        for (Feature module : sorted)
        {
            boolean top = alignment.getValue().contains("Top");
            String text = module.getDisplayName() + (!(module.getHudInfo() == null || module.getHudInfo().equals("")) ? Formatting.GRAY + " [" + Formatting.WHITE + module.getHudInfo() + Formatting.GRAY + "]" : "");
            if (noBrackets.getValue())
            {
                text = module.getDisplayName() + (!(module.getHudInfo() == null || module.getHudInfo().equals("")) ? Formatting.GRAY + " " + Formatting.GRAY + module.getHudInfo() : "");
            }
            if (alignment.getValue().contains("Left"))
            {

                Fonts.doOneText(
                        event.getContext(),
                        text,
                        (float) (xPos.getValue().intValue() - (animations.getValue() ? module.getOffset() : Fonts.getTextWidth(text)) - Fonts.getTextWidth(text)),
                        yPos.getValue().intValue() + (top ? textOffset : -textOffset),
                        HudColors.getTextColor(yPos.getValue().intValue() + (top ? textOffset : -textOffset)),
                        FontModule.INSTANCE.textShadow.getValue()
                );
            }

            if (alignment.getValue().contains("Right"))
            {
                Fonts.doOneText(
                        event.getContext(),
                        text,
                        (float) (xPos.getValue().intValue() + (animations.getValue() ? module.getOffset() : -Fonts.getTextWidth(text))),
                        yPos.getValue().intValue() + (top ? textOffset : -textOffset),
                        HudColors.getTextColor(yPos.getValue().intValue() + (top ? textOffset : -textOffset)),
                        FontModule.INSTANCE.textShadow.getValue()
                );
            }
            textOffset += ClickGui.CONTEXT.getRenderer().getTextHeight(text);

            if (spacing.getValue())
                textOffset += 1;

        }
    }

    public Easing getEasing()
    {
        switch (easing.getValue())
        {
            case "Linear":
                return Easing.LINEAR;
            case "Exponential":
                return Easing.EXPO_IN_OUT;
            case "Elastic":
                return Easing.ELASTIC_IN_OUT;
            case "Bounce":
                return Easing.BOUNCE_IN_OUT;
            case "Cubic":
                return Easing.CUBIC_IN_OUT;

        }
        return Easing.LINEAR;

    }

    @Override
    public String getDescription()
    {
        return "FeatureList: Shows features that are enabled";
    }
}
