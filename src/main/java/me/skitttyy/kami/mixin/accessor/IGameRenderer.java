package me.skitttyy.kami.mixin.accessor;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.collection.Pool;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface IGameRenderer {
    @Invoker("renderHand")
    void doRenderHand(Camera camera, float tickDelta, Matrix4f matrix4f);
}