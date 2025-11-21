package me.skitttyy.kami.mixin.accessor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InGameHud.class)
public interface IIngameHud
{
    @Invoker("renderHealthBar")
    void doRenderHealth(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking);


    @Accessor(value = "heartJumpEndTick")
    long getHeartJumpEndTick();


    @Accessor(value = "ticks")
    int getTicks();

    @Accessor(value = "renderHealthValue")
    int getRenderHealthValue();
}