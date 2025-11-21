package me.skitttyy.kami.impl.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.ducks.ILivingEntity;
import me.skitttyy.kami.api.utils.render.Interpolator;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.movement.ElytraFly;
import me.skitttyy.kami.impl.features.modules.movement.Flight;
import me.skitttyy.kami.impl.features.modules.movement.LongJump;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import static me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers.LINES;

public class Skeleton extends Module
{

    public Value<Sn0wColor> color = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Color")
            .withValue(new Sn0wColor(255, 255, 255))
            .register(this);

    public static Skeleton INSTANCE;

    public Skeleton()
    {
        super("Skeleton", Category.Render);
        INSTANCE = this;
    }

    @SubscribeEvent(Priority.MODULE_LAST)
    public void onRenderWorld(RenderWorldEvent event)
    {


        if (mc.gameRenderer == null || mc.getCameraEntity() == null)
        {
            return;
        }
        float g = event.getTickDelta();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(mc.isFancyGraphicsOrBetter());
        RenderSystem.enableCull();
        for (Entity entity : mc.world.getEntities())
        {
            if (entity == null || !entity.isAlive())
            {
                continue;
            }
            if (entity instanceof PlayerEntity playerEntity)
            {
                if (mc.options.getPerspective().isFirstPerson() && playerEntity == mc.player)
                {
                    continue;
                }


                Vec3d skeletonPos = Interpolator.getInterpolatedPosition(entity, g);

                PlayerEntityRenderer livingEntityRenderer =
                        (PlayerEntityRenderer) (LivingEntityRenderer<?, ?>) mc.getEntityRenderDispatcher().getRenderer(playerEntity);
                PlayerEntityModel<AbstractClientPlayerEntity> playerModel = livingEntityRenderer.getModel();

                float h = MathHelper.lerpAngleDegrees(g,
                        playerEntity.prevBodyYaw, playerEntity.bodyYaw);
                float j = MathHelper.lerpAngleDegrees(g,
                        playerEntity.prevHeadYaw, playerEntity.headYaw);


                if (AntiCheat.INSTANCE.visualize.getValue() && entity == mc.player && !RotationManager.INSTANCE.FROM_INV)
                {
                    ILivingEntity accessor = (ILivingEntity) entity;
                    j = MathHelper.lerpAngleDegrees(g, accessor.kami_getPrevHeadYaw(), accessor.kami_getHeadYaw());
                }
                BipedEntityModel.ArmPose armPose = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity) playerEntity, Hand.MAIN_HAND);
                BipedEntityModel.ArmPose armPose2 = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity) playerEntity, Hand.OFF_HAND);

                if (armPose.isTwoHanded())
                    armPose2 = playerEntity.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;

                if (playerEntity.getMainArm() == Arm.RIGHT)
                {
                    playerModel.rightArmPose = armPose;
                    playerModel.leftArmPose = armPose2;
                } else
                {
                    playerModel.rightArmPose = armPose2;
                    playerModel.leftArmPose = armPose;
                }


                float n = 0.0f;
                float o = 0.0f;
                if (!playerEntity.hasVehicle() && playerEntity.isAlive())
                {
                    n = playerEntity.limbAnimator.getSpeed(event.getTickDelta());
                    o = playerEntity.limbAnimator.getPos(event.getTickDelta());
                    if (playerEntity.isBaby())
                    {
                        o *= 3.0f;
                    }
                    if (n > 1.0f)
                    {
                        n = 1.0f;
                    }
                }
                float l = playerEntity.age + event.getTickDelta();
                float k = j - h;
                float m = playerEntity.getPitch(g);


                if (AntiCheat.INSTANCE.visualize.getValue() && playerEntity == mc.player && !RotationManager.INSTANCE.FROM_INV)
                {
                    ILivingEntity accessor = (ILivingEntity) playerEntity;
                    m = MathHelper.lerpAngleDegrees(g, accessor.kami_getPrevHeadPitch(), accessor.kami_getHeadPitch());
                }



                playerModel.animateModel((AbstractClientPlayerEntity) playerEntity, o, n, event.getTickDelta());
                playerModel.setAngles((AbstractClientPlayerEntity) playerEntity, o, n, l, k, m);


                boolean swimming = playerEntity.isInSwimmingPose();
                boolean sneaking = playerEntity.isInSneakingPose();
                boolean flying = playerEntity.isFallFlying();

                if (entity == mc.player && (LongJump.isGrimJumping() || Flight.isGrimFlying() || ElytraFly.isPacketFlying()))
                    flying = false;
                ModelPart head = playerModel.head;
                ModelPart leftArm = playerModel.leftArm;
                ModelPart rightArm = playerModel.rightArm;
                ModelPart leftLeg = playerModel.leftLeg;
                ModelPart rightLeg = playerModel.rightLeg;


                playerModel.sneaking = entity.isInSneakingPose();


                MatrixStack matrixStack = RenderUtil.matrixFrom(skeletonPos.x, skeletonPos.y, skeletonPos.z);
                matrixStack.push();
                if (swimming)
                {
                    matrixStack.translate(0, 0.35f, 0);
                }
                matrixStack.multiply(new Quaternionf().setAngleAxis((h + 180.0f) * Math.PI / 180.0f, 0, -1, 0));
                if (swimming || flying)
                {
                    matrixStack.multiply(new Quaternionf().setAngleAxis((90.0f + m) * Math.PI / 180.0f, -1, 0, 0));
                }
                if (swimming)
                {
                    matrixStack.translate(0, -0.95f, 0);
                }

                Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
                LINES.begin(matrix4f);
                LINES.color(color.getValue().getColor());
                LINES.vertex(0, sneaking ? 0.6f : 0.7f,
                        sneaking ? 0.23f : 0);
                LINES.vertex(0, sneaking ? 1.05f : 1.4f,
                        0);
                LINES.vertex(-0.37f, sneaking ? 1.05f :
                        1.35f, 0);
                LINES.vertex(0.37f, sneaking ? 1.05f :
                        1.35f, 0);
                LINES.vertex(-0.15f, sneaking ? 0.6f :
                        0.7f, sneaking ? 0.23f : 0);
                LINES.vertex(0.15f, sneaking ? 0.6f : 0.7f,
                        sneaking ? 0.23f : 0);
                matrixStack.push();
                matrixStack.translate(0, sneaking ? 1.05f : 1.4f, 0);
                rotateSkeleton(matrixStack, head);
                matrix4f = matrixStack.peek().getPositionMatrix();
                LINES.vertex(matrix4f, 0, 0, 0);
                LINES.vertex(matrix4f, 0, 0.25f, 0);
                matrixStack.pop();
                matrixStack.push();
                matrixStack.translate(0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
                rotateSkeleton(matrixStack, rightLeg);
                matrix4f = matrixStack.peek().getPositionMatrix();
                LINES.vertex(matrix4f, 0, 0, 0);
                LINES.vertex(matrix4f, 0, -0.6f, 0);
                matrixStack.pop();
                matrixStack.push();
                matrixStack.translate(-0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
                rotateSkeleton(matrixStack, leftLeg);
                matrix4f = matrixStack.peek().getPositionMatrix();
                LINES.vertex(matrix4f, 0, 0, 0);
                LINES.vertex(matrix4f, 0, -0.6f, 0);
                matrixStack.pop();
                matrixStack.push();
                matrixStack.translate(0.37f, sneaking ? 1.05f : 1.35f, 0);
                rotateSkeleton(matrixStack, rightArm);
                matrix4f = matrixStack.peek().getPositionMatrix();
                LINES.vertex(matrix4f, 0, 0, 0);
                LINES.vertex(matrix4f, 0, -0.55f, 0);
                matrixStack.pop();
                matrixStack.push();
                matrixStack.translate(-0.37f, sneaking ? 1.05f : 1.35f, 0);
                rotateSkeleton(matrixStack, leftArm);
                matrix4f = matrixStack.peek().getPositionMatrix();
                LINES.vertex(matrix4f, 0, 0, 0);
                LINES.vertex(matrix4f, 0, -0.55f, 0);
                matrixStack.pop();
                LINES.draw();
                if (swimming)
                {
                    matrixStack.translate(0, 0.95f, 0);
                }
                if (swimming || flying)
                {
                    matrixStack.multiply(new Quaternionf().setAngleAxis((90.0f + m) * Math.PI / 180.0f, 1, 0, 0));
                }
                if (swimming)
                {
                    matrixStack.translate(0, -0.35f, 0);
                }
                matrixStack.multiply(new Quaternionf().setAngleAxis((h + 180.0f) * Math.PI / 180.0f, 0, 1, 0));
                matrixStack.translate(-skeletonPos.x, -skeletonPos.y, -skeletonPos.z);
                matrixStack.pop();
            }
        }
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    }

    private void rotateSkeleton(MatrixStack matrix, ModelPart modelPart)
    {
        if (modelPart.roll != 0.0f)
        {
            matrix.multiply(RotationAxis.POSITIVE_Z.rotation(modelPart.roll));
        }
        if (modelPart.yaw != 0.0f)
        {
            matrix.multiply(RotationAxis.NEGATIVE_Y.rotation(modelPart.yaw));
        }
        if (modelPart.pitch != 0.0f)
        {
            matrix.multiply(RotationAxis.NEGATIVE_X.rotation(modelPart.pitch));
        }
    }

    @Override
    public String getDescription()
    {
        return "Skeleton: renders players skeletons";
    }
}