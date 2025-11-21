package me.skitttyy.kami.api.utils;

import lombok.Setter;
import me.skitttyy.kami.api.utils.math.MathUtil;

public class Timer {

    long startTime = System.currentTimeMillis();

    long lastResetTime = 0;


    @Setter
    long delay = 0;
    boolean paused = false;

    public Timer()
    {

    }

    public Timer(long delay)
    {
        this.delay = delay;
    }


    public boolean isPassed()
    {
        if (delay <= 1 && !paused)
        {
            return true;
        }
        return !paused && System.currentTimeMillis() - startTime >= delay;
    }

    public boolean isPassed(long delay)
    {
        if (delay <= 1 && !paused)
        {
            return true;
        }
        return !paused && System.currentTimeMillis() - startTime >= delay;
    }


    public long getTime()
    {
        return System.currentTimeMillis() - startTime;
    }

    public double getTimeUntilDone()
    {
        return MathUtil.round(((startTime + delay) - System.currentTimeMillis()) / 1000d, 1);
    }

    private double timeDifference()
    {
        return MathUtil.round((System.currentTimeMillis() - startTime) / 1000d, 1);
    }

    public void resetDelay()
    {
        long current = System.currentTimeMillis();
        lastResetTime = current - startTime;

        startTime = current;
    }

    public void setDelayCPS(float cps)
    {
        long delay = (long) (1000 / (cps));
        if(cps == 20.0) delay = 0;
        this.setDelay(delay - 10);
    }

    public void setPaused(boolean paused)
    {
        this.paused = paused;
    }

    public boolean isPaused()
    {
        return paused;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public long getResetTime()
    {
        return lastResetTime;
    }
}