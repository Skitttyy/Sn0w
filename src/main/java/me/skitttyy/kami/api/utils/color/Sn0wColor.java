package me.skitttyy.kami.api.utils.color;

import lombok.Setter;
import me.skitttyy.kami.impl.features.modules.client.HudColors;

import java.awt.*;

@Setter
public class Sn0wColor {
    Color color;
    boolean sync;
    boolean unsyncable;
    public Sn0wColor(Color color, boolean sync)
    {
        this.sync = sync;
        this.color = color;
    }

    public Sn0wColor(Color color)
    {
        this.sync = false;
        this.color = color;
    }

    public Sn0wColor(int r, int g, int b)
    {
        this.color = new Color(r, g, b);
        this.sync = false;
    }


    public Sn0wColor(int r, int g, int b, int a)
    {
        this.color = new Color(r, g, b, a);
        this.sync = false;
    }
    public Sn0wColor(int r, int g, int b, int a, boolean unsyncable)
    {
        this.color = new Color(r, g, b, a);
        this.sync = false;
        this.unsyncable = unsyncable;
    }
    public Sn0wColor(int r, int g, int b, boolean unsyncable)
    {
        this.color = new Color(r, g, b);
        this.sync = false;
        this.unsyncable = unsyncable;
    }

    public Color getColor()
    {
        if (sync && !unsyncable)
        {
            return ColorUtil.newAlpha(HudColors.getTextColor(0), color.getAlpha());
        }
        return color;
    }

    public Color getColorChatSync()
    {
        if (sync && !unsyncable)
        {
            return new Color(48, 184, 203, 0);
        }
        return color;
    }

    public int getRed() {
        return getColor().getRed();
    }

    public int getGreen() {
        return getColor().getGreen();
    }

    public int getBlue() {
        return getColor().getBlue();
    }

    public float getGlRed() {
        return getRed() / 255f;
    }

    public float getGlBlue() {
        return getBlue() / 255f;
    }

    public float getGlGreen() {
        return getGreen() / 255f;
    }

    public float getGlAlpha() {
        return getAlpha() / 255f;
    }

    public int getAlpha() {
        return getColor().getAlpha();
    }

    public boolean isSyncing(){
        return sync;
    }

    public boolean isUnsyncable(){
        return unsyncable;
    }
}
