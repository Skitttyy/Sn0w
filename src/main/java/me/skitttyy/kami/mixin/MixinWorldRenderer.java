package me.skitttyy.kami.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.skitttyy.kami.api.event.events.render.RenderShaderEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.render.Freecam;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer implements IMinecraft {

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void hookRender(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci, @Local MatrixStack matrixStack)
    {



        RenderBuffers.preRender();
        Vec3d pos = camera.getPos().negate();

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180));
        matrixStack.translate(pos.x, pos.y, pos.z);
        new RenderWorldEvent(matrixStack, tickCounter.getTickDelta(false)).post();
        RenderBuffers.postRender();


        RenderBuffers.process();

    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void hookRender$1(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci)
    {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        new RenderShaderEvent(matrixStack, tickCounter.getTickDelta(true)).post();
    }


//    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0))
//    private void replaceShaderHook(PostEffectProcessor instance, float tickDelta) {
//        if (Shaders.INSTANCE.isEnabled() && mc.world != null) {
//            if (ShaderManager.INSTANCE.fullNullCheck()) return;
//
//
//            ShaderManager.Shader shader = Shaders.INSTANCE.getShader();
//            ShaderManager.INSTANCE.setupShader(shader, ShaderManager.INSTANCE.getShaderOutline(shader));
//        } else {
//            instance.render(tickDelta);
//        }
//    }




    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
    private boolean renderSetupTerrainModifyArg(boolean spectator) {
        return Freecam.INSTANCE.isEnabled() || spectator;
    }
}