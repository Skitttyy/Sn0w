package me.skitttyy.kami.mixin;


import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.move.TravelEvent;
import me.skitttyy.kami.api.event.events.player.ReachEvent;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.ducks.ILivingEntity;
import me.skitttyy.kami.api.utils.players.rotation.Rotation;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.movement.BoatFly;
import me.skitttyy.kami.impl.features.modules.player.Tweaks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements IMinecraft
{
    @Shadow
    public abstract boolean isCreative();

    @Shadow
    public abstract double getEntityInteractionRange();

    @Shadow
    protected abstract boolean canChangeIntoPose(EntityPose pose);

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world)
    {
        super(entityType, world);
    }

    @Inject(method = "tickNewAi", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;headYaw:F"))
    public void updateHeadRotation(CallbackInfo ci)
    {
        Rotation rotation = RotationManager.INSTANCE.getRotation();
        float yaw = getYaw();
        float pitch = getPitch();
        if (rotation != null)
        {
            //noinspection ConstantValue
            if ((Object) this == MinecraftClient.getInstance().player && rotation != null)
            {
                yaw = rotation.getYaw();
                pitch = rotation.getPitch();
            }
        }
        ((ILivingEntity) this).kami_setHeadYaw(yaw);
        ((ILivingEntity) this).kami_setHeadPitch(pitch);
    }

    @Inject(method = "getBlockInteractionRange", at = @At(value = "HEAD"), cancellable = true)
    private void doReachDistance(CallbackInfoReturnable<Double> cir)
    {
        final ReachEvent reachEvent = new ReachEvent((float) this.getAttributeValue(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE));
        reachEvent.post();
        if (reachEvent.isCancelled())
        {
            cir.cancel();
            cir.setReturnValue((double) reachEvent.getReach());
        }
    }

    @Inject(method = "getEntityInteractionRange", at = @At(value = "HEAD"), cancellable = true)
    private void doEntityReachDistance(CallbackInfoReturnable<Double> cir)
    {
        final ReachEvent reachEvent = new ReachEvent((float) this.getAttributeValue(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE));
        reachEvent.post();
        if (reachEvent.isCancelled())
        {
            cir.cancel();
            cir.setReturnValue((double) reachEvent.getReach());
        }
    }

    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    private void hookTravelHead(Vec3d movementInput, CallbackInfo ci)
    {
        TravelEvent.Pre event = new TravelEvent.Pre(movementInput);
        event.post();
        if (event.isCancelled())
        {
//            move(MovementType.SELF, getVelocity());
            ci.cancel();
        }
    }


    @Redirect(
            method = {"updatePose"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;canChangeIntoPose(Lnet/minecraft/entity/EntityPose;)Z",
                    ordinal = 0
            )
    )
    private boolean canEnterPose1(PlayerEntity instance, EntityPose pose)
    {
        return (instance == mc.player && Tweaks.INSTANCE.isEnabled() && Tweaks.INSTANCE.crouch.getValue() && mc.player.isSneaking() && !mc.player.isCrawling()) || this.canChangeIntoPose(pose);
    }

    @Redirect(
            method = {"updatePose"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;canChangeIntoPose(Lnet/minecraft/entity/EntityPose;)Z",
                    ordinal = 1
            )
    )
    private boolean canEnterPose2(PlayerEntity instance, EntityPose pose)
    {
        return (instance == mc.player && Tweaks.INSTANCE.crouch.getValue() && mc.player.isSneaking() && !mc.player.isCrawling() &&Tweaks.INSTANCE.isEnabled()) || this.canChangeIntoPose(pose);
    }

    @Inject(method = "travel", at = @At(value = "RETURN"), cancellable = true)
    private void hookTravelTail(Vec3d movementInput, CallbackInfo ci)
    {
        TravelEvent.Post event = new TravelEvent.Post(movementInput);
        event.post();
    }


    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci)
    {
        LivingEvent.Attack event = new LivingEvent.Attack(target);
        event.post();
        if (event.isCancelled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "shouldDismount", at = @At("HEAD"), cancellable = true)
    protected void shouldDismountHook(CallbackInfoReturnable<Boolean> cir)
    {
        if (BoatFly.INSTANCE.isEnabled())
            cir.setReturnValue(false);
    }

}