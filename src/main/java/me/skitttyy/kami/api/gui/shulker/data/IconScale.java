package me.skitttyy.kami.api.gui.shulker.data;


public class IconScale {
    public int translateX;

    public int translateY;
    public int translateZ;

    public int scale;

    public IconScale(int x, int y, int z, int scale) {
        this.translateX = x;
        this.translateY = y;
        this.translateZ = z;
        this.scale = scale;
    }
}