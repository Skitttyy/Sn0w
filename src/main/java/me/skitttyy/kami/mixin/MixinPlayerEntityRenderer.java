package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.movement.Flight;
import me.skitttyy.kami.impl.features.modules.movement.LongJump;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>
{
    public MixinPlayerEntityRenderer(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius)
    {
        super(ctx, model, shadowRadius);
    }

    //cancel nametag rendering
//    @Redirect(method = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At(value = "INVOKE", target = "net/minecraft/client/network/AbstractClientPlayerEntity.isFallFlying ()Z"))
//    boolean setupTransforms(AbstractClientPlayerEntity instance)
//    {
//        {
//            if (instance == MinecraftClient.getInstance().player && LongJump.INSTANCE.isEnabled())
//            {
//                return false;
//            }
//            return instance.isFallFlying();
//        }
//    }

    @Inject(method = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At(value = "HEAD"), cancellable = true)
    void setupTransforms(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h, float i, CallbackInfo ci)
    {
        if (abstractClientPlayerEntity == MinecraftClient.getInstance().player && (LongJump.isGrimJumping() || Flight.isGrimFlying()))
        {
            super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h, i);
            ci.cancel();
        }
    }




    @Shadow
    public Identifier getTexture(AbstractClientPlayerEntity entity)
    {
        return null;
    }
}


