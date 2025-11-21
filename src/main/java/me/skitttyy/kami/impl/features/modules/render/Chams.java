package me.skitttyy.kami.impl.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderCrystalEvent;
import me.skitttyy.kami.api.event.events.render.RenderEntityEvent;
import me.skitttyy.kami.api.event.events.render.RenderHandEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.ducks.ILivingEntity;
import me.skitttyy.kami.api.utils.render.ChamsModelRenderer;
import me.skitttyy.kami.api.utils.render.Interpolator;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL14.GL_ONE_MINUS_CONSTANT_ALPHA;

public class Chams extends Module
{


    public Value<Boolean> players = new ValueBuilder<Boolean>()
            .withDescriptor("Players")
            .withValue(false)
            .register(this);
    public Value<Boolean> self = new ValueBuilder<Boolean>()
            .withDescriptor("Self")
            .withValue(false)
            .withParent(players)
            .withParentEnabled(true)
            .register(this);
    public Value<Boolean> crystals = new ValueBuilder<Boolean>()
            .withDescriptor("Crystals")
            .withValue(false)
            .register(this);
    public Value<Boolean> hand = new ValueBuilder<Boolean>()
            .withDescriptor("Hand")
            .withValue(false)
            .register(this);
    public Value<Sn0wColor> fill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill")
            .withValue(new Sn0wColor(0, 0, 255, 25))
            .register(this);
    public Value<Sn0wColor> wireColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Frame")
            .withValue(new Sn0wColor(255, 255, 255))
            .register(this);
    public Value<Boolean> shiny = new ValueBuilder<Boolean>()
            .withDescriptor("Shiny")
            .withValue(false)
            .register(this);
    public Value<Boolean> walls = new ValueBuilder<Boolean>()
            .withDescriptor("Walls")
            .withValue(false)
            .register(this);

    public Value<Boolean> cancel = new ValueBuilder<Boolean>()
            .withDescriptor("Cancel")
            .withValue(false)
            .register(this);
    public Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(1)
            .withRange(0, 300)
            .withPlaces(1)
            .register(this);
    public static Chams INSTANCE;

    public Chams()
    {
        super("Chams", Category.Render);
        INSTANCE = this;
    }


    private static float getYaw(Direction direction)
    {
        switch (direction)
        {
            case SOUTH:
            {
                return 90.0f;
            }
            case WEST:
            {
                return 0.0f;
            }
            case NORTH:
            {
                return 270.0f;
            }
            case EAST:
            {
                return 180.0f;
            }
        }
        return 0.0f;
    }

    @SubscribeEvent(Priority.MANAGER_FIRST)
    public void onRenderWorld(RenderWorldEvent event)
    {


        // Entity chams

        if (!walls.getValue())
            RenderSystem.enableDepthTest();


        if (shiny.getValue())
            GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA);

        for (Entity entity : mc.world.getEntities())
        {

            double x = Math.abs(mc.gameRenderer.getCamera().getPos().x - entity.getX());
            double z = Math.abs(mc.gameRenderer.getCamera().getPos().z - entity.getZ());
            double d = (mc.options.getViewDistance().getValue() + 1) * 16;
            if (mc.player.distanceTo(entity) > range.getValue().floatValue() || x > d || z > d)
            {
                continue;
            }
            if (entity instanceof LivingEntity livingEntity && checkChams(livingEntity) || entity instanceof EndCrystalEntity && crystals.getValue())
            {


                Vec3d pos = Interpolator.getInterpolatedPosition(entity, event.getTickDelta());

                Vec3d matrixPos = pos;

                if (entity instanceof LivingEntity)
                {
                    LivingEntityRenderer renderer = (LivingEntityRenderer) mc.getEntityRenderDispatcher().getRenderer(entity);

                    Vec3d vec3d = renderer.getPositionOffset(entity, event.getTickDelta());

                    matrixPos = new Vec3d(matrixPos.x + vec3d.x, matrixPos.y + vec3d.y, matrixPos.z + vec3d.z);
                }

                MatrixStack stack = RenderUtil.matrixFrom(matrixPos.x, matrixPos.y, matrixPos.z);

                stack.push();

                renderEntityChams(stack, entity, event.getTickDelta());

                stack.pop();
            }
        }
        if (shiny.getValue())
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (!walls.getValue())
            RenderSystem.disableDepthTest();
    }


    @SubscribeEvent
    public void onRenderEntity(final RenderEntityEvent event)
    {
        if (RotationManager.INSTANCE.FROM_INV) return;

        if (mc.player.distanceTo(event.getEntity()) > range.getValue().floatValue()) return;

        if (!checkChams(event.entity))
        {
            return;
        }

        if (wireColor.getValue().getColor().getAlpha() == 0 && fill.getValue().getColor().getAlpha() == 0) return;

        if (cancel.getValue())
        {
            event.setCancelled(true);
            float n;
            Direction direction;
            event.matrixStack.push();
            event.model.handSwingProgress = event.entity.getHandSwingProgress(event.g);
            event.model.riding = event.entity.hasVehicle();
            event.model.child = event.entity.isBaby();
            float h = MathHelper.lerpAngleDegrees(event.g, event.entity.prevBodyYaw, event.entity.bodyYaw);
            float j = MathHelper.lerpAngleDegrees(event.g, event.entity.prevHeadYaw, event.entity.headYaw);
            if (AntiCheat.INSTANCE.visualize.getValue() && event.entity == MinecraftClient.getInstance().player && !RotationManager.INSTANCE.FROM_INV)
            {
                ILivingEntity accessor = (ILivingEntity) event.entity;
                j = MathHelper.lerpAngleDegrees(event.g, accessor.kami_getPrevHeadYaw(), accessor.kami_getHeadYaw());
            }
            float k = j - h;
            if (event.entity.hasVehicle() && event.entity.getVehicle() instanceof LivingEntity livingEntity2)
            {
                h = MathHelper.lerpAngleDegrees(event.g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
                k = j - h;
                float l = MathHelper.wrapDegrees(k);
                if (l < -85.0f)
                {
                    l = -85.0f;
                }
                if (l >= 85.0f)
                {
                    l = 85.0f;
                }
                h = j - l;
                if (l * l > 2500.0f)
                {
                    h += l * 0.2f;
                }
                k = j - h;
            }
            float m = MathHelper.lerp(event.g, event.entity.prevPitch, event.entity.getPitch());
            if (AntiCheat.INSTANCE.visualize.getValue() && event.entity == MinecraftClient.getInstance().player && !RotationManager.INSTANCE.FROM_INV)
            {
                ILivingEntity accessor = (ILivingEntity) event.entity;
                m = MathHelper.lerpAngleDegrees(event.g, accessor.kami_getPrevHeadPitch(), accessor.kami_getHeadPitch());
            }

            if (LivingEntityRenderer.shouldFlipUpsideDown(event.entity))
            {
                m *= -1.0f;
                k *= -1.0f;
            }
            if (event.entity.isInPose(EntityPose.SLEEPING) && (direction = event.entity.getSleepingDirection()) != null)
            {
                n = event.entity.getEyeHeight(EntityPose.STANDING) - 0.1f;
                event.matrixStack.translate((float) (-direction.getOffsetX()) * n, 0.0f, (float) (-direction.getOffsetZ()) * n);
            }
            float l = getAnimationProgress(event.entity, event.g);
            if (event.entity instanceof PlayerEntity)
            {
                ChamsModelRenderer.setupPlayerTransforms((AbstractClientPlayerEntity) event.entity, event.matrixStack, l, h, event.g);
            } else
            {
                ChamsModelRenderer.setupTransforms(event.entity, event.matrixStack, l, h, event.g);
            }
            event.matrixStack.scale(-1.0f, -1.0f, 1.0f);
            event.matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
            event.matrixStack.translate(0.0f, -1.501f, 0.0f);
            n = 0.0f;
            float o = 0.0f;
            if (!event.entity.hasVehicle() && event.entity.isAlive())
            {
                n = event.entity.limbAnimator.getSpeed(event.g);
                o = event.entity.limbAnimator.getPos(event.g);
                if (event.entity.isBaby())
                {
                    o *= 3.0f;
                }
                if (n > 1.0f)
                {
                    n = 1.0f;
                }
            }
            event.model.animateModel(event.entity, o, n, event.g);
            event.model.setAngles(event.entity, o, n, l, k, m);
            if (!event.entity.isSpectator())
            {

                for (Object featureRenderer : event.features)
                {
                    ((FeatureRenderer) featureRenderer).render(event.matrixStack, event.vertexConsumerProvider, event.i,
                            event.entity, o, n, event.g, l, k, m);
                }

            }
            event.matrixStack.pop();
        }

    }


    public void renderEntityChams(MatrixStack matrixStack, Entity entity, float tickDelta)
    {
        ChamsModelRenderer.render(matrixStack, entity, tickDelta, fill.getValue().getColor(), wireColor.getValue().getColor(),
                1.0f, wireColor.getValue().getAlpha() != 0, fill.getValue().getColor().getAlpha() != 0, false);
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event)
    {


        if (!hand.getValue()) return;

        

        RenderBuffers.preRender();
        if (shiny.getValue())
            GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA);

        ChamsModelRenderer.renderHand(event.getMatrices(), event.getTickDelta(), wireColor.getValue().getColor(), fill.getValue().getColor(),
                1.0f, wireColor.getValue().getAlpha() != 0, fill.getValue().getColor().getAlpha() != 0, false);

        if (shiny.getValue())
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderBuffers.postRender();


    }

    @SubscribeEvent
    public void onRenderCrystal(RenderCrystalEvent event)
    {
        if (!crystals.getValue())
        {
            return;
        }


        if (mc.player.distanceTo(event.endCrystalEntity) > range.getValue().floatValue()) return;


        if (cancel.getValue())
            event.setCancelled(true);

    }

    public boolean checkChams(Entity entity)
    {
        if (entity instanceof PlayerEntity)
        {
            if (entity == mc.player)
            {
                return self.getValue() && (!mc.options.getPerspective().isFirstPerson() || Freecam.INSTANCE.isEnabled());
            } else
            {
                return players.getValue();
            }
        }
        return false;
    }


    private float getAnimationProgress(LivingEntity entity, float f)
    {
        if (entity instanceof SquidEntity)
        {
            return MathHelper.lerp(f, ((SquidEntity) entity).prevTentacleAngle, ((SquidEntity) entity).tentacleAngle);
        }
        return entity instanceof WolfEntity wolf ? wolf.getTailAngle() : entity.age + f;
    }


    @Override
    public String getDescription()
    {
        return "Chams: allows you to see entity's through walls";
    }
}