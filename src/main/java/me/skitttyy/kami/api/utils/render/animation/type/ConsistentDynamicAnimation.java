package me.skitttyy.kami.api.utils.render.animation.type;

import lombok.Setter;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.Interpolator;
import me.skitttyy.kami.api.utils.render.animation.Animation;
import me.skitttyy.kami.api.utils.render.animation.Easing;


@Setter
public class ConsistentDynamicAnimation extends Animation
{

    float value;
    float lastValue;
    float unitsPer100;
    Easing oldEasing;

    public ConsistentDynamicAnimation(float value, Easing easing, long unitsPer100)
    {
        super(easing, 100, true);
        this.value = value;
        this.lastValue = value;
        this.unitsPer100 = unitsPer100;
        oldEasing = easing;
    }

    public void goal(float goalValue)
    {
        if (getAnimationValue() == value)
        {
            if (oldEasing != null)
            {
                easing = oldEasing;
                oldEasing = null;
            }
        }
        if (value == goalValue) return;

        this.lastValue = getAnimationValue();

        this.animationTime = (long) (((MathUtil.distance(lastValue, goalValue, false)) / unitsPer100) * 100.0);
        this.time = System.currentTimeMillis();

        this.value = goalValue;
    }

    public void updateLastValue(float newLastValue)
    {
        if (lastValue == newLastValue) return;


        this.lastValue = newLastValue;

        this.animationTime = (long) (((MathUtil.distance(lastValue, value, false)) / unitsPer100) * 100.0);
        this.time = System.currentTimeMillis();
        if (easing.equals(Easing.CUBIC_IN_OUT))
        {
            oldEasing = easing;
            easing = Easing.CUBIC_OUT;
        }
    }

    @Override
    public void setEasing(Easing easing)
    {

        if(oldEasing != null){
            oldEasing = easing;
            return;
        }
        super.setEasing(easing);
    }

    public boolean isFinished()
    {

        if (System.currentTimeMillis() - time > animationTime)
        {
            if (oldEasing != null)
            {
                easing = oldEasing;
                oldEasing = null;
            }
            return true;
        }
        return false;
    }


    public float getAnimationValue()
    {
        return Interpolator.interpolateFloat(lastValue, value, getScaledTime());
    }
}

