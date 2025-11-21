package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.event.events.world.EntityEvent;
import me.skitttyy.kami.impl.features.modules.render.CustomSky;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {

    @Shadow
    @Nullable
    public abstract Entity getEntityById(int id);

    @Inject(method = "addEntity", at = @At(value = "RETURN"))
    private void hookAddEntity(Entity entity, CallbackInfo ci)
    {
        new EntityEvent.Add(entity).post();
    }

    @Inject(method = "removeEntity", at = @At(value = "HEAD"))
    private void hookRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci)
    {
        Entity entity = getEntityById(entityId);
        if (entity == null)
        {
            return;
        }
        new EntityEvent.Remove(entity, removalReason).post();
    }


    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void getSkyColorHook(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (CustomSky.INSTANCE.isEnabled()) {
            cir.setReturnValue(new Vec3d(CustomSky.INSTANCE.fogColor.getValue().getGlRed(), CustomSky.INSTANCE.fogColor.getValue().getGlGreen(), CustomSky.INSTANCE.fogColor.getValue().getGlBlue()));
        }
    }

}