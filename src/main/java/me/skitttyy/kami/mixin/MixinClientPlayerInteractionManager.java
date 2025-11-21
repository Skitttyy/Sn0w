package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager implements IMinecraft {
    @Shadow
    private GameMode gameMode;

    @Shadow
    protected abstract void syncSelectedSlot();

    @Shadow
    protected abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

    @Inject(method = "attackBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> ci)
    {
        BlockState state = mc.world.getBlockState(pos);
        LivingEvent.AttackBlock event = new LivingEvent.AttackBlock(pos, state, direction);
        event.post();
        if (event.isCancelled())
        {

            ci.cancel();
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "interactItem", at = @At(value = "HEAD"), cancellable = true)
    public void hookInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir)
    {
        cir.cancel();
        if (this.gameMode == GameMode.SPECTATOR)
        {
            cir.setReturnValue(ActionResult.PASS);
        }
        syncSelectedSlot();
        // Strafe fix cuz goofy 1.19 sends move packet when using items
        final float yaw = RotationManager.INSTANCE.isRotating() ? RotationManager.INSTANCE.getRotationYaw() : mc.player.getYaw();
        final float pitch = RotationManager.INSTANCE.isRotating() ? RotationManager.INSTANCE.getRotationPitch() : mc.player.getPitch();
        MutableObject mutableObject = new MutableObject();
        this.sendSequencedPacket(mc.world, (sequence) ->
        {
            PlayerInteractItemC2SPacket playerInteractItemC2SPacket = new PlayerInteractItemC2SPacket(hand, sequence, yaw, pitch);
            ItemStack itemStack = player.getStackInHand(hand);
            if (player.getItemCooldownManager().isCoolingDown(itemStack.getItem()))
            {
                mutableObject.setValue(ActionResult.PASS);
                return playerInteractItemC2SPacket;
            } else
            {
                TypedActionResult<ItemStack> typedActionResult = itemStack.use(mc.world, player, hand);
                ItemStack itemStack2 = (ItemStack) typedActionResult.getValue();
                if (itemStack2 != itemStack)
                {
                    player.setStackInHand(hand, itemStack2);
                }

                mutableObject.setValue(typedActionResult.getResult());
                return playerInteractItemC2SPacket;
            }
        });
        cir.setReturnValue((ActionResult) ((Object) mutableObject.getValue()));
    }


    @Inject(method = "breakBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {

        LivingEvent.BreakBlock event = new LivingEvent.BreakBlock(pos);
        event.post();
        if (event.isCancelled())
        {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }


}