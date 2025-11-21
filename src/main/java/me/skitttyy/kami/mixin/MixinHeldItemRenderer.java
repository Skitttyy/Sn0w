package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.misc.SmallShield;
import me.skitttyy.kami.impl.features.modules.render.ViewModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer implements IMinecraft
{


    @Shadow
    protected abstract void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Redirect(method = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 1))
    private void onRenderItemHook(HeldItemRenderer instance, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
    {
        if (SmallShield.INSTANCE.isEnabled())
        {
            renderFirstPersonItem(player, tickDelta, pitch, hand, swingProgress, item, SmallShield.INSTANCE.offset.getValue().floatValue(), matrices, vertexConsumers, light);
        } else
        {
            renderFirstPersonItem(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
        }
    }


    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;" +
                    "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/" +
                    "item/ItemStack;Lnet/minecraft/client/render/model/json/" +
                    "ModelTransformationMode;ZLnet/minecraft/client/util/math/" +
                    "MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void hookRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta,
                                           float pitch, Hand hand, float swingProgress,
                                           ItemStack item, float equipProgress, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
    {


        if (ViewModel.INSTANCE.isEnabled() && ViewModel.INSTANCE.translate.getValue())
        {
            matrices.scale(ViewModel.INSTANCE.scaleX.getValue().floatValue(), ViewModel.INSTANCE.scaleY.getValue().floatValue(), ViewModel.INSTANCE.scaleZ.getValue().floatValue());
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ViewModel.INSTANCE.rotateX.getValue().floatValue()));
            if (hand == Hand.MAIN_HAND)
            {
                matrices.translate(ViewModel.INSTANCE.posX.getValue().floatValue(), ViewModel.INSTANCE.posY.getValue().floatValue(), ViewModel.INSTANCE.posZ.getValue().floatValue());
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ViewModel.INSTANCE.rotateY.getValue().floatValue()));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(ViewModel.INSTANCE.rotateZ.getValue().floatValue()));
            } else
            {
                matrices.translate(-ViewModel.INSTANCE.posX.getValue().floatValue(), ViewModel.INSTANCE.posY.getValue().floatValue(), ViewModel.INSTANCE.posZ.getValue().floatValue());
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-ViewModel.INSTANCE.rotateY.getValue().floatValue()));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-ViewModel.INSTANCE.rotateZ.getValue().floatValue()));
            }
        }

    }

}