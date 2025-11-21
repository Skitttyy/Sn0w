package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.event.events.render.RenderCrystalEvent;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalEntityRenderer.class)
public class MixinEndCrystalEntityRenderer {
    //
    @Shadow
    @Final
    private ModelPart core;
    //
    @Shadow
    @Final
    private ModelPart frame;

    /**
     * @param endCrystalEntity
     * @param f
     * @param g
     * @param matrixStack
     * @param vertexConsumerProvider
     * @param i
     * @param ci
     */
    @Inject(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;" +
            "FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/" +
            "render/VertexConsumerProvider;I)V", at = @At(value = "HEAD"), cancellable = true)
    private void hookRender(EndCrystalEntity endCrystalEntity, float f, float g,
                            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
                            int i, CallbackInfo ci) {
        RenderCrystalEvent event = new RenderCrystalEvent(endCrystalEntity,
                f, g, matrixStack, i, core, frame);
        event.post();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}