package me.skitttyy.kami.impl.features.modules.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.player.PopEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.WireframeEntityRenderer;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.mixin.accessor.ILimbAnimator;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.util.concurrent.CopyOnWriteArrayList;

public class PopChams extends Module {
    public static PopChams INSTANCE;

    Value<Number> fadeStart = new ValueBuilder<Number>()
            .withDescriptor("Fade Start")
            .withValue(100)
            .withRange(0, 4000)
            .register(this);
    Value<Number> fadeTime = new ValueBuilder<Number>()
            .withDescriptor("Fade Time")
            .withValue(500)
            .withRange(0, 2000)
            .register(this);
    Value<Boolean> self = new ValueBuilder<Boolean>()
            .withDescriptor("Self")
            .withValue(false)
            .register(this);
//    Value<Boolean> renderSkin = new ValueBuilder<Boolean>()
//            .withDescriptor("Skin")
//            .withValue(false)
//            .register(this);
    Value<Sn0wColor> fill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill Color")
            .withValue(new Sn0wColor(0, 255, 218, 100))
            .register(this);
    Value<Sn0wColor> lineColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line Color")
            .withValue(new Sn0wColor(255, 255, 255, 255))
            .register(this);

    private final CopyOnWriteArrayList<PopCham> popList = new CopyOnWriteArrayList<>();


    public PopChams()
    {
        super("PopChams", Category.Render);
        INSTANCE = this;
    }
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if(NullUtils.nullCheck()) return;



        popList.forEach(cham -> renderEntity(event.getMatrices(), cham.player, cham.modelPlayer, cham.getTexture(), cham.startTime, cham));

    }

    private void renderEntity(MatrixStack matrices, LivingEntity entity, PlayerEntityModel<PlayerEntity> modelBase, Identifier texture, long startTime, PopCham cham)
    {
        modelBase.leftPants.visible = false;
        modelBase.rightPants.visible = false;
        modelBase.leftSleeve.visible = false;
        modelBase.rightSleeve.visible = false;
        modelBase.jacket.visible = false;
        modelBase.hat.visible = false;

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        matrices = RenderUtil.matrixFrom(x, y, z);
        matrices.push();

        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180 - entity.bodyYaw)));
        prepareScale(matrices);

        modelBase.animateModel((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), mc.getRenderTickCounter().getTickDelta(false));

        float limbSpeed = Math.min(entity.limbAnimator.getSpeed(), 1f);

        modelBase.setAngles((PlayerEntity) entity, entity.limbAnimator.getPos(), limbSpeed, entity.age, entity.headYaw - entity.bodyYaw, entity.getPitch());

        int lineA = lineColor.getValue().getAlpha();
        int fillA = fill.getValue().getAlpha();
        if (System.currentTimeMillis() - startTime > fadeStart.getValue().longValue())
        {
            long time = System.currentTimeMillis() - startTime - fadeStart.getValue().longValue();
            double normal = MathUtil.normalize(((double) time), 0, fadeTime.getValue().doubleValue());
            normal = MathHelper.clamp(normal, 0, 1);
            normal = (-normal) + 1;
            lineA = (int) (normal * lineA);
            fillA = (int) (normal * fillA);
        }
        WireframeEntityRenderer.renderModel(matrices, (AnimalModel) modelBase, RenderType.BOTH, ColorUtil.newAlpha(fill.getValue().getColor(), fillA), ColorUtil.newAlpha(lineColor.getValue().getColor(), lineA));
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        matrices.pop();

        if (lineA == 0 && fillA == 0)
        {
            popList.remove(cham);
        }

    }


    private static void prepareScale(MatrixStack matrixStack)
    {
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(1.6f, 1.8f, 1.6f);
        matrixStack.translate(0.0F, -1.501F, 0.0F);

    }

    @SubscribeEvent
    public void onTotemPop(PopEvent.TotemPopEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (!self.getValue() && event.getEntity().equals(mc.player)) return;


        if (!(event.getEntity() instanceof PlayerEntity)) return;


        PlayerEntity eventEntity = (PlayerEntity) event.getEntity();

        PlayerEntity entity = new PlayerEntity(mc.world, BlockPos.ORIGIN, eventEntity.getBodyYaw(), new GameProfile(eventEntity.getUuid(), eventEntity.getName().getString())) {
            @Override
            public boolean isSpectator()
            {
                return false;
            }

            @Override
            public boolean isCreative()
            {
                return false;
            }
        };


        entity.copyPositionAndRotation(eventEntity);
        entity.bodyYaw = eventEntity.getBodyYaw();
        entity.headYaw = eventEntity.getHeadYaw();
        entity.handSwingProgress = eventEntity.handSwingProgress;
        entity.handSwingTicks = eventEntity.handSwingTicks;
        entity.setSneaking(eventEntity.isSneaking());
        entity.limbAnimator.setSpeed(eventEntity.limbAnimator.getSpeed());
        ((ILimbAnimator) entity.limbAnimator).setLimbPos(eventEntity.limbAnimator.getPos());
        popList.add(new PopCham(entity, ((AbstractClientPlayerEntity) eventEntity).getSkinTextures()));
    }

    private class PopCham {
        private final PlayerEntity player;
        private final PlayerEntityModel<PlayerEntity> modelPlayer;
        private Identifier texture;
        long startTime;

        public PopCham(PlayerEntity player, SkinTextures texture)
        {
            this.player = player;
            modelPlayer = new PlayerEntityModel<>(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), texture.model().equals(SkinTextures.Model.SLIM));
            modelPlayer.getHead().scale(new Vector3f(-0.3f, -0.3f, -0.3f));
            this.texture = texture.texture();
            this.startTime = System.currentTimeMillis();
        }

//        public void update(CopyOnWriteArrayList<PopCham> arrayList)
//        {
//            if (alpha <= 0)
//            {
//                arrayList.remove(this);
//                player.kill();
//                player.remove(Entity.RemovalReason.KILLED);
//                player.onRemoved();
//                return;
//            }
//        }


        public Identifier getTexture()
        {
            return texture;
        }
    }

    @Override
    public String getDescription()
    {
        return "PopChams: Shows ghosts when someone pops";
    }

}

