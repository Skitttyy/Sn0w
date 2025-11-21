package me.skitttyy.kami.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.event.events.render.RenderHandEvent;
import me.skitttyy.kami.api.management.shaders.ShaderManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.ducks.IVec3d;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.misc.NoEntityTrace;
import me.skitttyy.kami.impl.features.modules.render.*;
import me.skitttyy.kami.mixin.accessor.IGameRenderer;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements IMinecraft
{
    @Shadow
    private float zoom;

    @Shadow
    private float zoomX;

    @Shadow
    private float zoomY;

    @Shadow
    private float viewDistance;


    @Shadow
    private float fovMultiplier;
    @Shadow
    private float lastFovMultiplier;
    @Shadow
    private boolean renderingPanorama;

    @Shadow
    public abstract void updateCrosshairTarget(float tickDelta);

    @Inject(method = "renderNausea", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderNausea(DrawContext context, float distortionStrength, CallbackInfo ci)
    {
        if (NoRender.INSTANCE.portal.getValue() && NoRender.INSTANCE.isEnabled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "tiltViewWhenHurt", at = @At(value = "HEAD"), cancellable = true)
    private void hookTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci)
    {
        if (NoRender.INSTANCE.noHurtCam.getValue() && NoRender.INSTANCE.isEnabled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))    public void hookRenderWorld$2(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 1) Matrix4f matrix4f2, @Local(ordinal = 1) float tickDelta, @Local MatrixStack matrixStack)
    {

        RenderHandEvent event = new RenderHandEvent(matrixStack, mc.getRenderTickCounter().getTickDelta(false));
        event.post();


    }



//
//    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
//    public void postRender3dHook(RenderTickCounter tickCounter, CallbackInfo ci)
//    {
//        if (NullUtils.nullCheck()) return;
//
//
//        if (Shaders.INSTANCE.isEnabled())
//        {
//            ShaderManager.INSTANCE.renderShaders();
//        }
//    }



//
//    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
//    void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci)
//    {
//        if (NullUtils.nullCheck()) return;
//
//
//        if (Shaders.INSTANCE.isEnabled() && Shaders.INSTANCE.hands.getValue())
//        {
//            Camera camera = mc.gameRenderer.getCamera();
//            MatrixStack matrixStack = new MatrixStack();
//            RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());
//            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
//            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
//            RenderSystem.applyModelViewMatrix();
//            ShaderManager.INSTANCE.renderShader(() -> ((IGameRenderer) mc.gameRenderer).doRenderHand(mc.gameRenderer.getCamera(), mc.getRenderTickCounter().getTickDelta(false), matrixStack.peek().getPositionMatrix()), Shaders.INSTANCE.getShader());
//            RenderSystem.getModelViewStack().popMatrix();
//            RenderSystem.applyModelViewMatrix();
//        }
//
//
//
//
//
//    }
//
//
//


    @Inject(method = "findCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void findCrosshairTargetHook(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta, CallbackInfoReturnable<HitResult> cir)
    {
        if (NoEntityTrace.spoofTrace())
        {
            double d = Math.max(blockInteractionRange, entityInteractionRange);
            Vec3d vec3d = camera.getCameraPosVec(tickDelta);
            HitResult hitResult = camera.raycast(d, tickDelta, false);
            cir.setReturnValue(ensureTargetInRangeCustom(hitResult, vec3d, blockInteractionRange));
        }
    }

    @Unique
    private HitResult ensureTargetInRangeCustom(HitResult hitResult, Vec3d cameraPos, double interactionRange)
    {
        Vec3d vec3d = hitResult.getPos();
        if (!vec3d.isInRange(cameraPos, interactionRange))
        {
            Vec3d vec3d2 = hitResult.getPos();
            Direction direction = Direction.getFacing(vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z);
            return BlockHitResult.createMissed(vec3d2, direction, BlockPos.ofFloored(vec3d2));
        } else
        {
            return hitResult;
        }
    }

    private boolean freecamSet = false;

    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info)
    {
        Freecam freecam = Freecam.INSTANCE;


        if ((freecam.isEnabled()) && mc.getCameraEntity() != null && !freecamSet)
        {
            info.cancel();
            Entity cameraE = mc.getCameraEntity();

            double x = cameraE.getX();
            double y = cameraE.getY();
            double z = cameraE.getZ();
            double prevX = cameraE.prevX;
            double prevY = cameraE.prevY;
            double prevZ = cameraE.prevZ;
            float yaw = cameraE.getYaw();
            float pitch = cameraE.getPitch();
            float prevYaw = cameraE.prevYaw;
            float prevPitch = cameraE.prevPitch;
            ((IVec3d) cameraE.getPos()).set(freecam.pos.x, freecam.pos.y - cameraE.getEyeHeight(cameraE.getPose()), freecam.pos.z);
            cameraE.prevX = freecam.prevPos.x;
            cameraE.prevY = freecam.prevPos.y - cameraE.getEyeHeight(cameraE.getPose());
            cameraE.prevZ = freecam.prevPos.z;
            cameraE.setYaw(freecam.yaw);
            cameraE.setPitch(freecam.pitch);
            cameraE.prevYaw = freecam.prevYaw;
            cameraE.prevPitch = freecam.prevPitch;

            freecamSet = true;
            updateCrosshairTarget(tickDelta);
            freecamSet = false;

            ((IVec3d) cameraE.getPos()).set(x, y, z);
            cameraE.prevX = prevX;
            cameraE.prevY = prevY;
            cameraE.prevZ = prevZ;
            cameraE.setYaw(yaw);
            cameraE.setPitch(pitch);
            cameraE.prevYaw = prevYaw;
            cameraE.prevPitch = prevPitch;
        }
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable<Matrix4f> cir)
    {
        if (AspectRatio.INSTANCE.isEnabled())
        {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f)
            {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float) (fov * 0.01745329238474369), AspectRatio.INSTANCE.aspectAmount.getValue().floatValue(), 0.05f, viewDistance * 4.0f));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }

    @Inject(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D", at = @At("HEAD"), cancellable = true)
    public void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> ci)
    {
        if (!this.renderingPanorama && ViewModel.INSTANCE.isEnabled())
        {
            double d = 70.0;
            if (changingFov)
            {
                if (ViewModel.INSTANCE.isEnabled() && !ViewModel.INSTANCE.fovMode.getValue().equals("None"))
                {
                    ci.setReturnValue(ViewModel.INSTANCE.fov.getValue().doubleValue());
                    return;
                }
                d = mc.options.getFov().getValue();
                d *= MathHelper.lerp(tickDelta, this.lastFovMultiplier, this.fovMultiplier);
            } else
            {
                if (ViewModel.INSTANCE.isEnabled() && !ViewModel.INSTANCE.fovMode.getValue().equals("None") && ViewModel.INSTANCE.items.getValue())
                {
                    ci.setReturnValue(ViewModel.INSTANCE.itemFov.getValue().doubleValue());
                    return;
                }
            }

            if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity) camera.getFocusedEntity()).isDead())
            {
                float f = Math.min((float) ((LivingEntity) camera.getFocusedEntity()).deathTime + tickDelta, 20.0F);
                d /= (1.0F - 500.0F / (f + 500.0F)) * 2.0F + 1.0F;
            }

            CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
            if (cameraSubmersionType == CameraSubmersionType.LAVA || cameraSubmersionType == CameraSubmersionType.WATER)
            {
                d *= MathHelper.lerp(this.mc.options.getFovEffectScale().getValue(), 1.0, 0.85714287F);
            }

            ci.setReturnValue(d);
        }
    }


    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void showFloatingItemHook(ItemStack floatingItem, CallbackInfo info)
    {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.totem.getValue())
        {
            info.cancel();
        }
    }

    @Inject(method = "renderFloatingItem", at = @At("HEAD"), cancellable = true)
    private void renderFloatingItemHook(DrawContext context, float tickDelta, CallbackInfo ci)
    {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.totem.getValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir)
    {
        if (BlockHighlight.INSTANCE.isEnabled())
        {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }


}