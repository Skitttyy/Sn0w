package me.skitttyy.kami.api.utils.render;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;

@Getter
@Setter
public class ScaledResolution {
    public int scaledWidth;
    public int scaledHeight;
    double scaleFactor;
    public ScaledResolution(int scaledWidth, int scaledHeight)
    {
        this.scaledWidth = scaledWidth;
        this.scaledHeight = scaledHeight;
        this.scaleFactor = 1;
    }

    public ScaledResolution(MinecraftClient client)
    {
        this.scaledWidth = client.getWindow().getScaledWidth();
        this.scaledHeight = client.getWindow().getScaledHeight();
        this.scaleFactor = client.getWindow().getScaleFactor();
    }

}
