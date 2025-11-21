package me.skitttyy.kami.mixin;


import me.skitttyy.kami.api.event.events.render.RenderEntityEvent;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.ducks.ILivingEntity;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.render.Chams;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static org.lwjgl.opengl.GL11C.GL_POLYGON_OFFSET_FILL;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T>
{

    @Unique
    private LivingEntity kami_entity;

    protected MixinLivingEntityRenderer(EntityRendererFactory.Context ctx)
    {
        super(ctx);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void storeEntity(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci)
    {
        kami_entity = livingEntity;
    }

    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F", ordinal = 1))
    public float modifyYaw(float delta, float start, float end)
    {
        if (AntiCheat.INSTANCE.visualize.getValue() && kami_entity == MinecraftClient.getInstance().player && !RotationManager.INSTANCE.FROM_INV)
        {
            ILivingEntity accessor = (ILivingEntity) kami_entity;
            return MathHelper.lerpAngleDegrees(delta, accessor.kami_getPrevHeadYaw(), accessor.kami_getHeadYaw());
        }
        return MathHelper.lerpAngleDegrees(delta, start, end);
    }

    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0))
    public float modifyPitch(float delta, float start, float end)
    {
        if (AntiCheat.INSTANCE.visualize.getValue() && kami_entity == MinecraftClient.getInstance().player && !RotationManager.INSTANCE.FROM_INV)
        {
            ILivingEntity accessor = (ILivingEntity) kami_entity;
            return MathHelper.lerp(delta, accessor.kami_getPrevHeadPitch(), accessor.kami_getHeadPitch());
        }
        return MathHelper.lerp(delta, start, end);
    }


    @Shadow
    protected M model;
    //
    @Shadow
    @Final
    protected List<FeatureRenderer<T, M>> features;

    @Shadow
    protected abstract RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline);


    @Shadow
    protected abstract void setupTransforms(T entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scale);

    @Inject(method = "render*", at = @At(value = "HEAD"), cancellable = true)
    private void hookRender(LivingEntity livingEntity, float f, float g,
                            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci)
    {
        RenderEntityEvent event = new RenderEntityEvent(livingEntity,
                f, g, matrixStack, vertexConsumerProvider, i, model, getRenderLayer((T) livingEntity, true, false, false), features);
        event.post();
        if (event.isCancelled())
        {
            ci.cancel();
        }
    }


//    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
//    private void renderHead(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci)
//    {
//        if (Chams.INSTANCE.checkChams(livingEntity))
//        {
//            GL11.glEnable(GL_POLYGON_OFFSET_FILL);
//            GL11.glPolygonOffset(1.0f, -1100000.0f);
//        }
//    }
//
//    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
//    private void renderTail(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci)
//    {
//        if (Chams.INSTANCE.checkChams(livingEntity))
//        {
//            GL11.glPolygonOffset(1.0f, 1100000.0f);
//            GL11.glDisable(GL_POLYGON_OFFSET_FILL);
//        }
//    }

}