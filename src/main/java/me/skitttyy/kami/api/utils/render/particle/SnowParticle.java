package me.skitttyy.kami.api.utils.render.particle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.math.MathUtil;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.Random;

@Getter
@AllArgsConstructor
public class SnowParticle {
    private static final Random RANDOM = new Random();

    private float x, y;
    private final float speed;
    private final int size;

    private float velocityX;
    private float velocityY;

    public void onRender(DrawContext context, double mouseX, double mouseY) {
        double dx = x - mouseX;
        double dy = y - mouseY;
        double distanceSq = MathUtil.square(dx) + MathUtil.square(dy);
        if (distanceSq < MathUtil.square(25) && distanceSq > 0) {
            double distance = Math.sqrt(distanceSq);
            double force = 2.5 / distance;
            velocityX += (float) (dx * force);
            velocityY += (float) (dy * force);
        }

        x += velocityX;
        y += speed + velocityY;

        velocityX *= 0.9f;
        velocityY *= 0.9f;

        if (y > context.getScaledWindowHeight()) {
            y = -size;
            x = RANDOM.nextInt(context.getScaledWindowWidth());
            velocityX = 0;
            velocityY = 0;
        }

        if (x < 0) x = context.getScaledWindowWidth();
        if (x > context.getScaledWindowWidth()) x = 0;
        context.fill((int) x, (int) y, (int) x + size, (int) y + size, ColorUtil.newAlpha(Color.WHITE, 125).getRGB());
    }

    public static SnowParticle random(int screenWidth, int screenHeight) {
        return new SnowParticle(
                (int)(RANDOM.nextFloat() * screenWidth),
                RANDOM.nextInt(screenHeight),
                0.5f + RANDOM.nextFloat() * 1.5f,
                1 + RANDOM.nextInt(3), 0, 0
        );
    }
}