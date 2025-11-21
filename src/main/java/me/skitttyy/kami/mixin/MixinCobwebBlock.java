package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import me.skitttyy.kami.impl.features.modules.movement.NoSlow;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CobwebBlock.class)
public class MixinCobwebBlock
{
    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void onEntityCollisionHook(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci)
    {

        //I know chinese :( u have to do this
        if (MinecraftClient.getInstance().player != null)
        {
            if (state.getBlock().equals(Blocks.COBWEB))
            {
                if (entity.getBoundingBox().intersects(new Box(pos)))
                {
                    if (NoSlow.INSTANCE.isEnabled() && NoSlow.INSTANCE.webs.getValue() && entity.equals(MinecraftClient.getInstance().player))
                    {
                        ci.cancel();
                        if (NoSlow.INSTANCE.mode.getValue().equals("Meow") || NoSlow.INSTANCE.mode.getValue().equals("Grim"))
                        {
                            PacketManager.INSTANCE.sendPacket(new PlayerActionC2SPacket(
                                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
                        }
                    }
                }
            }
        }
    }
}