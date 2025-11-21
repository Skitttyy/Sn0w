package me.skitttyy.kami.mixin.accessor;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerInteractionManager.class)
public interface IClientPlayerInteractionManager {

    @Invoker("syncSelectedSlot")
    void doSyncSelectedSlot();

    @Accessor("currentBreakingProgress")
    float getCurrentBreakProgress();

    @Accessor("currentBreakingProgress")
    void setCurrentBreakingProgress(float currentBreakingProgress);

    @Invoker("interactBlockInternal")
    ActionResult invokeInteractInternal(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult);

}