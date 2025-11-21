package me.skitttyy.kami.mixin;


import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.event.events.move.MovementPacketsEvent;
import me.skitttyy.kami.api.event.events.move.PushEvent;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.ducks.IClientPlayerEntity;
import me.skitttyy.kami.api.utils.ducks.ILivingEntity;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.misc.BetterPortals;
import me.skitttyy.kami.impl.features.modules.movement.NoSlow;
import me.skitttyy.kami.impl.features.modules.player.Tweaks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements IMinecraft, IClientPlayerEntity {
    //
    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;
    @Shadow
    public double lastX;
    @Shadow
    public double lastBaseY;
    @Shadow
    public double lastZ;
    @Shadow
    public Input input;
    //
    @Shadow
    @Final
    protected MinecraftClient client;
    // Last tick values
    @Shadow
    private boolean lastSneaking;
    @Shadow
    private float lastYaw;
    @Shadow
    private float lastPitch;
    @Shadow
    private boolean lastOnGround;
    //
    @Shadow
    private int ticksSinceLastPositionPacketSent;
    @Shadow
    private boolean autoJumpEnabled;
    //
    @Unique
    private boolean ticking;

    /**
     *
     */
    public MixinClientPlayerEntity()
    {
        // Treating this class as ClientPlayerEntity with mc.player info works
        // Need a better solution
        super(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getGameProfile());
    }

    //
    @Shadow
    protected abstract void sendSprintingPacket();

    //
    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    protected abstract boolean isCamera();

    //
    @Shadow
    protected abstract void autoJump(float dx, float dz);

    //
    @Shadow
    public abstract void tick();


    @Override
    public void doTick()
    {
        super.tick();
    }

    @Override
    public void doSendMovementPackets()
    {
        this.sendMovementPackets();
    }


    @Shadow
    protected abstract void sendMovementPackets();

    @Inject(method = "sendMovementPackets", at = @At(value = "HEAD"), cancellable = true)
    private void hookSendMovementPackets(CallbackInfo ci)
    {
        if (AntiCheat.INSTANCE.acMode.getValue().equals("Soft"))
        {
            TickEvent.PlayerTickEvent.Pre event = new TickEvent.PlayerTickEvent.Pre();
            event.post();

            RotationManager.INSTANCE.onUpdate();
        }
        new TickEvent.MovementTickEvent.Pre().post();
        // Rotation spoof
        MovementPacketsEvent movementPacketsEvent = new MovementPacketsEvent(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround());
        movementPacketsEvent.post();



        double x = movementPacketsEvent.getX();
        double y = movementPacketsEvent.getY();
        double z = movementPacketsEvent.getZ();
        float yaw = movementPacketsEvent.getYaw();
        float pitch = movementPacketsEvent.getPitch();
        boolean ground = movementPacketsEvent.isOnGround();
        if (movementPacketsEvent.isCancelled())
        {
            ci.cancel();
            sendSprintingPacket();
            boolean bl = isSneaking();
            if (bl != lastSneaking)
            {
                ClientCommandC2SPacket.Mode mode = bl ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
                networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode));
                lastSneaking = bl;
            }
            if (isCamera())
            {
                double d = x - lastX;
                double e = y - lastBaseY;
                double f = z - lastZ;
                double g = yaw - lastYaw;
                double h = pitch - lastPitch;
                ++ticksSinceLastPositionPacketSent;
                boolean bl2 = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || ticksSinceLastPositionPacketSent >= 20;
                boolean bl3 = g != 0.0 || h != 0.0;


                if (hasVehicle())
                {
                    Vec3d vec3d = getVelocity();
                    networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(vec3d.x, -999.0, vec3d.z, getYaw(), getPitch(), ground));
                    bl2 = false;
                } else if (bl2 && bl3)
                {
                    networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, ground));
                } else if (bl2)
                {
                    networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, ground));
                } else if (bl3)
                {
                    networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, ground));
                } else if (lastOnGround != isOnGround())
                {
                    networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(ground));
                }
                if (bl2)
                {
                    lastX = x;
                    lastBaseY = y;
                    lastZ = z;
                    ticksSinceLastPositionPacketSent = 0;
                }
                if (bl3)
                {
                    lastYaw = yaw;
                    lastPitch = pitch;
                }
                lastOnGround = ground;
                autoJumpEnabled = client.options.getAutoJump().getValue();
            }



            new TickEvent.MovementTickEvent.Post().post();
            if (AntiCheat.INSTANCE.acMode.getValue().equals("Soft"))
            {
                TickEvent.PlayerTickEvent.Post event = new TickEvent.PlayerTickEvent.Post();
                event.post();
            }
        }

    }


    @Inject(method = "sendMovementPackets", at = @At(value = "RETURN"))
    private void sendMovementPackets(CallbackInfo ci)
    {
        new TickEvent.MovementTickEvent.Post().post();
        if (AntiCheat.INSTANCE.acMode.getValue().equals("Soft"))
        {
            TickEvent.PlayerTickEvent.Post event = new TickEvent.PlayerTickEvent.Post();
            event.post();
        }

    }
    @Inject(method = "move", at = @At(value = "HEAD"), cancellable = true)
    private void hookMove(MovementType movementType, Vec3d movement, CallbackInfo ci)
    {
        final MoveEvent event = new MoveEvent(movementType, movement);
        event.post();

        if (!event.getMovement().equals(movement))
        {
            ci.cancel();
            double d = getX();
            double e = getZ();
            super.move(movementType, event.getMovement());
            autoJump((float) (getX() - d), (float) (getZ() - e));
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = At.Shift.BEFORE, ordinal = 0))
    private void hookTickPre(CallbackInfo ci)
    {
        if (AntiCheat.INSTANCE.acMode.getValue().equals("Strong"))
        {
            TickEvent.PlayerTickEvent.Pre event = new TickEvent.PlayerTickEvent.Pre();
            event.post();

            RotationManager.INSTANCE.onUpdate();
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = At.Shift.AFTER, ordinal = 0))
    private void hookTickPost(CallbackInfo ci)
    {
        if (AntiCheat.INSTANCE.acMode.getValue().equals("Strong"))
        {
            TickEvent.PlayerTickEvent.Post event = new TickEvent.PlayerTickEvent.Post();
            event.post();
        }

    }

    @Inject(method = "pushOutOfBlocks", at = @At(value = "HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double z, CallbackInfo ci)
    {
        PushEvent.Blocks event = new PushEvent.Blocks();
        event.post();
        if (event.isCancelled())
        {
            ci.cancel();
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V", ordinal = 3))
    private void hookSetSprinting(ClientPlayerEntity instance, boolean b)
    {
        final LivingEvent.SetSprinting sprintEvent = new LivingEvent.SetSprinting();
        sprintEvent.post();
        if (sprintEvent.isCancelled())
        {
            instance.setSprinting(true);
        } else
        {
            instance.setSprinting(b);
        }
    }

    @Inject(method = "isInSneakingPose", at = @At("HEAD"), cancellable = true)
    private void isSneaking(CallbackInfoReturnable<Boolean> cir)
    {

        if ((Object) this == mc.player && Tweaks.INSTANCE.isEnabled() && Tweaks.INSTANCE.crouch.getValue() && this.isSneaking())
        {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
    public void shouldSlowDownHook(CallbackInfoReturnable<Boolean> cir)
    {
        if (NoSlow.INSTANCE.isEnabled())
        {
            if (isCrawling())
            {
                if (NoSlow.INSTANCE.crawling.getValue())
                    cir.setReturnValue(false);
            } else
            {
                if (NoSlow.INSTANCE.sneak.getValue())
                    cir.setReturnValue(false);
            }
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean redirectUsingItem(ClientPlayerEntity player)
    {
        if (NoSlow.INSTANCE.canNoSlow()) return false;


        return player.isUsingItem();
    }

    @Inject(method = "tickNausea", at = @At("HEAD"), cancellable = true)
    private void updateNauseaHook(CallbackInfo ci)
    {
        if (BetterPortals.INSTANCE.isEnabled())
            ci.cancel();
    }

}