package me.skitttyy.kami.api.utils.render.animation;


import lombok.Setter;

@Setter
public class Animation {
    public long animationTime;
    public Easing easing;

    public long time;
    public boolean state;

    public Animation(Easing easing, long animationTime, boolean state)
    {
        this.easing = easing;
        this.animationTime = animationTime;
        this.state = state;
    }

    public Animation(Easing easing, long animationTime)
    {
        this(easing, animationTime, false);
    }

    public Animation(Easing easing)
    {
        this(easing, 300, false);
    }

    public float getScaledTime()
    {
        double linear = (System.currentTimeMillis() - time) / (float) animationTime;
        if (!state)
        {
            linear = 1.0 - linear;
        }
        return (float) Math.min(Math.max(easing.ease(linear), 0.0), 1.0);
    }

    public boolean getState()
    {
        return state;
    }

    public void setState(boolean state)
    {
        if(this.state == state) return;

        this.state = state;
        time = System.currentTimeMillis();
    }

    public void setStateHard(boolean state)
    {
        this.state = state;
        time = 0;
    }
}