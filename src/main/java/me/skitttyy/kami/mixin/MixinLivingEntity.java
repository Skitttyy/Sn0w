package me.skitttyy.kami.mixin;


import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.move.FallFlyingEvent;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.ducks.ILivingEntity;
import me.skitttyy.kami.api.utils.players.rotation.Rotation;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.movement.ElytraFly;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.skitttyy.kami.api.wrapper.IMinecraft.mc;


@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements ILivingEntity
{

    public MixinLivingEntity(EntityType<?> type, World world)
    {
        super(type, world);
    }

    @Shadow
    public int bodyTrackingIncrements;

    @Shadow
    public double serverX;

    @Shadow
    public double serverY;

    @Shadow
    public double serverZ;

    @Shadow
    public double serverYaw;

    @Shadow
    protected double serverPitch;
    @Shadow
    protected double serverHeadYaw;
    @Shadow
    protected int headTrackingIncrements;
    @Unique
    private Vec3d kami_oldServerPos;
    @Unique
    private float kami_headPitch;
    @Unique
    private float kami_prevHeadPitch;
    @Unique
    private float kami_headYaw;
    @Unique
    private float kami_prevHeadYaw;
    @Unique
    private boolean kami_inInventory = false;
    @Shadow
    protected ItemStack activeItemStack;


    @Unique
    private float originalYaw;

    @Shadow
    public abstract float getYaw(float tickDelta);

    @Shadow
    protected abstract void initDataTracker(DataTracker.Builder builder);

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void hookEventJumpPre(CallbackInfo ci)
    {
        //noinspection ConstantValue
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            LivingEvent.Jump jumpEvent = new LivingEvent.Jump(originalYaw = getYaw());
            jumpEvent.post();
            if (jumpEvent.isCancelled())
            {
                ci.cancel();
                return;
            }
            setYaw(jumpEvent.getYaw());
        }
    }

    @Inject(method = "jump", at = @At("TAIL"))
    public void hookEventJumpPost(CallbackInfo ci)
    {
        //noinspection ConstantValue
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            setYaw(originalYaw);
        }
    }

    //    @Inject(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;bodyTrackingIncrements:I"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;updateTrackedPosition(DDD)V"), to = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;serverX:D")))
//    public void leakRotations(CallbackInfo ci)
//    {
//        Rotation rotation = RotationManager.INSTANCE.getRotation();
//        //noinspection ConstantValue
//        if (this.bodyTrackingIncrements > 0 && (Object) this == MinecraftClient.getInstance().player && rotation != null) {
//            RotationManager.INSTANCE.fakeRotation = new Rotation(
//                    (rotation.getYaw() + (float) MathHelper.wrapDegrees(this.serverYaw - (double) rotation.getYaw()) / (float) this.bodyTrackingIncrements) % 360F,
//                    (rotation.getPitch() + (float) (this.serverPitch - (double) rotation.getPitch()) / (float) this.bodyTrackingIncrements) % 360F
//            );
//        }
//    }

    @Inject(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/" +
            "minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;" +
            "Lnet/minecraft/entity/LivingEntity;)" +
            "Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void hookConsumeItem(CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        LivingEvent.Eat event = new LivingEvent.Eat(activeItemStack);
        event.post();
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isFallFlying()Z"))
    public boolean redirectFallFlying(LivingEntity instance)
    {
        if (instance.equals(MinecraftClient.getInstance().player))
        {
            FallFlyingEvent event = new FallFlyingEvent();
            event.post();
            if (event.isCancelled())
                return true;
        }
        return instance.isFallFlying();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;prevStepBobbingAmount:F"), to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;turnHead(FF)F")))
    public float replaceYaw_tick(LivingEntity instance)
    {
        if (AntiCheat.INSTANCE.visualize.getValue())
        {
            Rotation rotation = RotationManager.INSTANCE.getRotation();
            //noinspection ConstantValue
            if ((Object) this == MinecraftClient.getInstance().player && rotation != null)
            {
                return rotation.getYaw();
            }
        }
        return instance.getYaw();
    }


    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPitch()F"))
    public float replacePitch(LivingEntity instance)
    {
        //elytra fix
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            if (AntiCheat.INSTANCE.strafeFix.getValue() && MinecraftClient.getInstance().player != null && RotationManager.INSTANCE.getRotation() != null)
            {
                return RotationManager.INSTANCE.getRotation().getPitch();
            }
        }
        return instance.getPitch();
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d replaceVelocity(LivingEntity instance)
    {
        //elytra fix
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            if (AntiCheat.INSTANCE.strafeFix.getValue() && MinecraftClient.getInstance().player != null && RotationManager.INSTANCE.getRotation() != null)
            {
                return RotationManager.INSTANCE.getRotationVector();
            }
        }
        return instance.getRotationVector();
    }


    @Redirect(method = "turnHead", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
    public float replaceYaw_turnHead(LivingEntity instance)
    {
        if (AntiCheat.INSTANCE.visualize.getValue())
        {
            Rotation rotation = RotationManager.INSTANCE.getRotation();
            //noinspection ConstantValue
            if ((Object) this == MinecraftClient.getInstance().player && rotation != null)
            {
                return rotation.getYaw();
            }
        }
        return instance.getYaw();
    }

    @Inject(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;headTrackingIncrements:I", ordinal = 2))
    public void respectServerHeadYaw(CallbackInfo ci)
    {
        kami_headYaw += (float) MathHelper.wrapDegrees(this.serverHeadYaw - (double) this.kami_headYaw) / (float) this.headTrackingIncrements;
    }

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;headYaw:F"))
    public void setHeadRotation(EntityType<?> entityType, World world, CallbackInfo ci)
    {
        kami_headYaw = getYaw();
        kami_headPitch = getPitch();
    }

    @Unique
    private boolean prevFlying = false;

    @Inject(method = "isFallFlying", at = @At("TAIL"), cancellable = true)
    public void tryReFlyOnLand(CallbackInfoReturnable<Boolean> cir)
    {
        if (NullUtils.nullCheck()) return;


        boolean flying = cir.getReturnValue();
        boolean stoppedFlying = prevFlying && !flying;
        if (ElytraFly.INSTANCE.isEnabled() && ElytraFly.INSTANCE.mode.getValue().equals("Bounce") && stoppedFlying && !KamiMod.isBaritonePaused())
        {
            mc.player.startFallFlying();
            PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            cir.setReturnValue(true);
        }
        prevFlying = flying;
    }


    @Inject(method = "onSpawnPacket", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;headYaw:F", ordinal = 1))
    public void setHeadRotation(EntitySpawnS2CPacket packet, CallbackInfo ci)
    {
        kami_headYaw = packet.getHeadYaw();
        kami_prevHeadYaw = kami_headYaw;

        kami_headPitch = packet.getPitch();
        kami_prevHeadPitch = kami_headPitch;
    }

    @Inject(method = "baseTick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;prevHeadYaw:F"))
    public void updateHeadRotation(CallbackInfo ci)
    {
        kami_prevHeadYaw = kami_headYaw;
        kami_prevHeadPitch = kami_headPitch;
    }

    @Inject(method = "updateTrackedPositionAndAngles", at = @At("HEAD"))
    public void saveOldServerPos(double x, double y, double z, float yaw, float pitch, int interpolationSteps, CallbackInfo ci)
    {
        kami_oldServerPos = new Vec3d(serverX, serverY, serverZ);
    }

    @Override
    public Vec3d kami_prevServerPos()
    {
        return kami_oldServerPos;
    }

    @Override
    public float kami_getHeadPitch()
    {
        return kami_headPitch;
    }

    @Override
    public void kami_setHeadPitch(float headPitch)
    {
        kami_headPitch = headPitch;
    }

    @Override
    public float kami_getPrevHeadPitch()
    {
        return kami_prevHeadPitch;
    }

    @Override
    public void kami_setPrevHeadPitch(float prevHeadPitch)
    {
        kami_prevHeadPitch = prevHeadPitch;
    }

    @Override
    public float kami_getHeadYaw()
    {
        return kami_headYaw;
    }

    @Override
    public void kami_setHeadYaw(float headYaw)
    {
        kami_headYaw = headYaw;
    }

    @Override
    public float kami_getPrevHeadYaw()
    {
        return kami_prevHeadYaw;
    }

    @Override
    public void kami_setPrevHeadYaw(float prevHeadYaw)
    {
        kami_prevHeadYaw = prevHeadYaw;
    }

    @Override
    public boolean kami_isInInventory()
    {
        return kami_inInventory;
    }

    @Override
    public void kami_setInInventory(boolean inInventory)
    {
        kami_inInventory = inInventory;
    }
}