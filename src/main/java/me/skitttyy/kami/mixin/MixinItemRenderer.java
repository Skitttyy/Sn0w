package me.skitttyy.kami.mixin;

import me.skitttyy.kami.impl.features.modules.render.Nametags;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
//    @ModifyArgs(method = "renderBakedItemQuads", at = @At(value = "HEAD", target = "Lnet/minecraft/client/render/BufferBuilder;vertex(FFFIFFIIFFF)V"), cancellable = true)
//    public void vertex(Args args)
//    {
//        args.get()
//        if (Nametags.MCSWAG)
//        {
//            System.out.println("LOL");
//
//        }
//    }
}
