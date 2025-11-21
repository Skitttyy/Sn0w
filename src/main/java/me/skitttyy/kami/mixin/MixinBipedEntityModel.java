package me.skitttyy.kami.mixin;

import me.skitttyy.kami.impl.features.modules.movement.Flight;
import me.skitttyy.kami.impl.features.modules.movement.LongJump;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Type;

@Mixin(BipedEntityModel.class)
public class MixinBipedEntityModel <T extends LivingEntity> extends AnimalModel<T> {

    @Redirect(method = "setAngles", at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.getFallFlyingTicks ()I"))
    int setAngles(T livingEntity)
    {
        if (livingEntity == MinecraftClient.getInstance().player && (LongJump.isGrimJumping() || Flight.isGrimFlying()))
        {
            return 0;
        }
        return livingEntity.getFallFlyingTicks();
    }

    @Shadow
    public Iterable<ModelPart> getHeadParts()
    {
        return null;
    }

    @Shadow
    public Iterable<ModelPart> getBodyParts()
    {
        return null;
    }

    @Shadow
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
    {

    }
}


