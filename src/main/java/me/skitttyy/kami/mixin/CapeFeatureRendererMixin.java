package me.skitttyy.kami.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.skitttyy.kami.impl.features.modules.render.Capes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeFeatureRenderer.class)
public class CapeFeatureRendererMixin {
    @ModifyExpressionValue(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SkinTextures;capeTexture()Lnet/minecraft/util/Identifier;"))
    private Identifier modifyCapeTexture(Identifier original, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, float h, float j, float k, float l) {
        if (Capes.INSTANCE.isEnabled()) {
            if (abstractClientPlayerEntity.getGameProfile() != null && abstractClientPlayerEntity.getGameProfile().getId() != null) {
                if (MinecraftClient.getInstance().getSession().getUsername().equals(abstractClientPlayerEntity.getGameProfile().getName())) {

                    if (!Capes.INSTANCE.capeMode.getValue().equals("NONE"))
                        return Identifier.of("kami", "capes/" + Capes.INSTANCE.capeMode.getValue().toLowerCase() + ".png");
                } else {
//                    String cape = BotManager.INSTANCE.getCapeForName(abstractClientPlayerEntity.getGameProfile().getName());
//                    if (cape != null) {
//                        return Identifier.of("kami", "capes/" + cape.toLowerCase() + ".png");
//                    }
                }
            }
        }

        return original;
    }
}