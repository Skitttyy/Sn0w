package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.event.events.render.RenderBlockEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.render.chunk.RenderedChunk")
public class MixinRenderedChunk {

    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
    public void hookEventRenderBlockModel(BlockPos pos, CallbackInfoReturnable<BlockState> cir)
    {
        BlockState state = cir.getReturnValue();
        RenderBlockEvent event = new RenderBlockEvent(pos, state);
        event.post();
        if (event.isCancelled())
            cir.setReturnValue(Blocks.AIR.getDefaultState());
    }

}