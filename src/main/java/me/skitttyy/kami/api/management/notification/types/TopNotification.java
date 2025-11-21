package me.skitttyy.kami.api.management.notification.types;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.utils.render.animation.Animation;
import me.skitttyy.kami.api.utils.render.animation.Easing;
import me.skitttyy.kami.api.utils.render.animation.type.ColorAnimation;
import me.skitttyy.kami.api.utils.render.animation.type.DynamicAnimation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

@Getter
@Setter
public class TopNotification {
    private String text;
    private Timer timer = new Timer();
    private ColorAnimation animation;
    private DynamicAnimation verticalAnimation;
    boolean complete;
    boolean setIndex;
    private Color color;

    public TopNotification(String text, long disableTime, long animationSpeed, Color color)
    {
        this.text = text;
        this.color = color;
        this.timer.setDelay(disableTime);
        this.timer.resetDelay();


        this.animation = new ColorAnimation(color, ColorUtil.newAlpha(color, 0), Easing.CUBIC_IN_OUT, animationSpeed, false);
        animation.setState(true);
        complete = false;
        setIndex = false;
        verticalAnimation = new DynamicAnimation(0, Easing.EXPO_IN_OUT, 200L);
        verticalAnimation.setStateHard(false);
    }


    public void draw(DrawContext context, int index, ScaledResolution resolution)
    {
        animation(index);
        float offset = resolution.getScaledWidth() / 2f + 1f - Fonts.getTextWidth(text) / 2f;
        float textY = 12 + (verticalAnimation.getAnimationValue());

        Fonts.doOneText(context, text, offset, textY, animation.getColor(), true);
    }

    public void animation(int index)
    {
        animation.setState(!timer.isPassed());

        if (timer.isPassed() && animation.getScaledTime() == 0)
        {
            complete = true;
        }

        if (!setIndex)
        {
            verticalAnimation.goal(index * Fonts.getTextHeight("AA"));
            verticalAnimation.setStateHard(true);
            setIndex = true;
            return;
        }

        verticalAnimation.goal(index * Fonts.getTextHeight("AA"));

    }

}
