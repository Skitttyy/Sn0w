package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.management.RotationManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class MixinInventoryScreen {


    @Inject(method = "drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"))
    private static void clearHeadRotation(DrawContext context, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        RotationManager.INSTANCE.FROM_INV = true;
    }

    @Inject(method = "drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V", at = @At("TAIL"))
    private static void resetHeadRotation(DrawContext context, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        RotationManager.INSTANCE.FROM_INV = false;

    }

}