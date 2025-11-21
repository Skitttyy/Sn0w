package me.skitttyy.kami.mixin.sodium;

import me.skitttyy.kami.api.event.events.render.RenderBlockEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.LevelSlice")
public class MixinLevelSlice {

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "getBlockState(III)Lnet/minecraft/block/BlockState;", at = @At("RETURN"), cancellable = true)
    public void renderBlockModel(int x, int y, int z, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        RenderBlockEvent event = new RenderBlockEvent(new BlockPos(x, y, z), state);
        event.post();
        if (event.isCancelled())
            cir.setReturnValue(Blocks.AIR.getDefaultState());
    }

}