package me.skitttyy.kami.api.utils.render.animation.type;

import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.render.animation.Animation;
import me.skitttyy.kami.api.utils.render.animation.Easing;

import java.awt.*;

public class OffsetAnimation extends Animation {

    Color normalColor;
    Color middleColor;
    Color lastColor;

    public OffsetAnimation(Color normalColor, Color middleColor, Color centerColor, Easing easing, long animationTime, boolean state)
    {
        super(easing, animationTime, state);

    }

    public void tick()
    {
        if (getColorTime() == 1.5f && state)
        {
            time = System.currentTimeMillis();
        }
    }


    public float getColorTime()
    {
        double linear = (System.currentTimeMillis() - time) / (float) animationTime;
        if (!state)
        {
            linear = 1.5f - linear;
        }
        return (float) Math.min(Math.max(easing.ease(linear), 0.0), 1.5);
    }

//    public Color getColor(float y)
//    {
//        return ColorUtil.interpolate(getScaledTime(), normalColor, activeColor);
//    }
}
