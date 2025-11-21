package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.management.shaders.ShaderManager;
import me.skitttyy.kami.impl.features.modules.render.Shaders;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher<T extends Entity>
{
//
//    @Redirect(method = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/render/entity/EntityRenderer.render (Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
//    void onRenderLiving(EntityRenderer instance, T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
//    {
//        if (Shaders.INSTANCE.isEnabled() && Shaders.INSTANCE.shouldRender(entity))
//        {
//
//
//            if (ShaderManager.INSTANCE.fullNullCheck())
//            {
//                instance.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
//                return;
//            }
//            ShaderManager.INSTANCE.startWriting();
//            instance.render(entity, yaw, tickDelta, matrices, ShaderManager.INSTANCE.vertexConsumerProvider, light);
//            ShaderManager.INSTANCE.vertexConsumerProvider.draw();
//            ShaderManager.INSTANCE.stopWriting();
//
//            if (!Shaders.INSTANCE.shouldCancel(entity))
//                instance.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
//
//        } else
//        {
//            instance.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
//        }
//
//    }


}
