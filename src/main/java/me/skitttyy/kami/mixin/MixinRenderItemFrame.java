package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntityRenderer.class)
public class MixinRenderItemFrame<T extends ItemFrameEntity> implements IMinecraft
{
    @Inject(method = "render(Lnet/minecraft/entity/decoration/ItemFrameEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "HEAD"), cancellable = true)
    public void doRender(T itemFrameEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci)
    {


        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.itemFrame.getValue())
        {
            if (mc.player != null)
            {
                if (mc.player.distanceTo(itemFrameEntity) < 6.0) return;
            }

            ci.cancel();
        }
    }
}