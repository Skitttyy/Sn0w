package me.skitttyy.kami.api.utils.render.animation.type;

import lombok.Setter;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.render.Interpolator;
import me.skitttyy.kami.api.utils.render.animation.Animation;
import me.skitttyy.kami.api.utils.render.animation.Easing;

import java.awt.*;


@Setter
public class DynamicAnimation extends Animation
{

    float value;
    float lastValue;

    public DynamicAnimation(float value, Easing easing, long animationTime)
    {
        super(easing, animationTime, true);
        this.value = value;
        this.lastValue = value;
    }

    public void goal(float goalValue)
    {
        if (value == goalValue) return;

        this.lastValue = getAnimationValue();
        this.time = System.currentTimeMillis();
        this.value = goalValue;
    }

    public boolean isFinished()
    {
        return getScaledTime() == 1.0f;
    }

    public float getAnimationValue()
    {
        return Interpolator.interpolateFloat(lastValue, value, getScaledTime());
    }
}

