package me.skitttyy.kami.api.utils.render;

public class RenderTimer {
    public static float TICK_LENGTH = 1.0f;

    public static void setTickLength(float tickLength)
    {
        TICK_LENGTH = tickLength;
    }

    public static float getTickLength()
    {
        return TICK_LENGTH;
    }
}
