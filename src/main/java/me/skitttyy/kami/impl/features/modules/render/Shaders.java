package me.skitttyy.kami.impl.features.modules.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.ServerEvent;
import me.skitttyy.kami.api.event.events.render.*;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.management.shaders.ShaderManager;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.EntityUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.mixin.accessor.IGameRenderer;
import me.skitttyy.kami.mixin.accessor.IWorldRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import net.minecraft.block.entity.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;

import java.awt.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;
import java.util.UUID;

public class Shaders extends Module
{
    private float shaderTime;

    private int textureId;
    public boolean ignoreEntityRender;

    public Value<Boolean> players = new ValueBuilder<Boolean>()
            .withDescriptor("Players")
            .withValue(false)
            .register(this);
    public Value<Sn0wColor> playersColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("PlayersColor")
            .withValue(new Sn0wColor(200, 60, 60))
            .withParent(players)
            .withParentEnabled(true)
            .register(this);
    public Value<Boolean> chests = new ValueBuilder<Boolean>()
            .withDescriptor("Chests")
            .withValue(false)
            .register(this);

    public Value<Sn0wColor> chestsColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("ChestsColor")
            .withValue(new Sn0wColor(200, 200, 101))
            .withParent(chests)
            .withParentEnabled(true)
            .register(this);
    public Value<Boolean> eChests = new ValueBuilder<Boolean>()
            .withDescriptor("EChests")
            .withValue(false)
            .register(this);

    public Value<Sn0wColor> echestsColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("EnderChestsColor")
            .withValue(new Sn0wColor(155, 0, 200))
            .withParent(eChests)
            .withParentEnabled(true)
            .register(this);
    public Value<Boolean> crystals = new ValueBuilder<Boolean>()
            .withDescriptor("Crystals")
            .withValue(false)
            .register(this);
    public Value<Sn0wColor> crystalsColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("CrystalsColor")
            .withValue(new Sn0wColor(200, 100, 200))
            .withParent(crystals)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> animals = new ValueBuilder<Boolean>()
            .withDescriptor("Animals")
            .withValue(true)
            .register(this);
    public Value<Sn0wColor> animalsColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("AnimalsColor")
            .withValue(new Sn0wColor(0, 200, 0))
            .withParent(animals)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> monsters = new ValueBuilder<Boolean>()
            .withValue(false)
            .withDescriptor("Monsters")
            .register(this);
    public Value<Sn0wColor> monstersColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("MonstersColor")
            .withValue(new Sn0wColor(200, 60, 60))
            .withParent(monsters)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> others = new ValueBuilder<Boolean>()
            .withDescriptor("Others")
            .withValue(false)
            .register(this);
    public Value<Boolean> self = new ValueBuilder<Boolean>()
            .withDescriptor("Self")
            .withValue(true)
            .register(this);

    public Value<Sn0wColor> selfColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("SelfColor")
            .withValue(new Sn0wColor(200, 60, 60))
            .withParent(self)
            .withParentEnabled(true)
            .register(this);

    public Value<String> image = new ValueBuilder<String>()
            .withDescriptor("imageToUse")
            .withValue("kamidefault")
            .register(this);

    // Shader Range
    public Value<Number> renderDistance = new ValueBuilder<Number>()
            .withDescriptor("RenderDistance")
            .withValue(50.0f)
            .withRange(10.0f, 200.0f)
            .withPlaces(1)
            .register(this);

    // Shader Style
    public Value<Boolean> texture = new ValueBuilder<Boolean>()
            .withDescriptor("Texture")
            .withValue(true)
            .register(this);

    public Value<Number> width = new ValueBuilder<Number>()
            .withDescriptor("Width")
            .withValue(1.5f)
            .withRange(0.1f, 10.0f)
            .withPlaces(2)
            .register(this);

    public Value<Number> outlineQuality = new ValueBuilder<Number>()
            .withDescriptor("Quality")
            .withValue(1)
            .withRange(1, 10)
            .withPlaces(0)
            .withParentEnabled(true)
            .register(this);



    public Value<Number> glowRadius = new ValueBuilder<Number>()
            .withDescriptor("Intensity")
            .withValue(0.3f)
            .withRange(0.01, 2.0f)
            .withPlaces(2)
            .register(this);

    // Shader Mode

    public Value<String> fillMode = new ValueBuilder<String>()
            .withDescriptor("Fill", "fillTag")
            .withValue("Off")
            .withModes("Off", "Default", "Rainbow")
            .register(this);
    public Value<Number> transparency = new ValueBuilder<Number>()
            .withDescriptor("Transparency")
            .withValue(0.35f)
            .withRange(0.0f, 1.0f)
            .withPlaces(1)
            .register(this);
    public Value<Number> factor = new ValueBuilder<Number>()
            .withDescriptor("Factor")
            .withValue(8.0f)
            .withRange(0.1f, 10.0f)
            .withPage("Rainbow")
            .withPageParent(fillMode)
            .withPlaces(1)
            .register(this);


    // Rainbow
    public Value<Number> rainbowSpeed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(0.005f)
            .withRange(0.001f, 0.02f)
            .withPlaces(3)
            .withPage("Rainbow")
            .withPageParent(fillMode)
            .register(this);


    // Entity targets
    public Value<Boolean> hands = new ValueBuilder<Boolean>()
            .withDescriptor("Hands")
            .withValue(true)
            .register(this);



// Add existing ones
// players, animals, monsters, others...



    public Value<Boolean> friends = new ValueBuilder<Boolean>()
            .withDescriptor("FriendsColor")
            .withValue(true)
            .register(this);


    public Value<Boolean> items = new ValueBuilder<Boolean>()
            .withDescriptor("Items")
            .withValue(true)
            .register(this);

    public Value<Sn0wColor> itemsColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("ItemsColor")
            .withValue(new Sn0wColor(200, 100, 0))
            .withParent(items)
            .withParentEnabled(true)
            .register(this);

    // Crystal, Projectiles, Chests, etc.

    public Value<Boolean> projectiles = new ValueBuilder<Boolean>()
            .withDescriptor("Projectiles")
            .withValue(true)
            .register(this);

    public Value<Sn0wColor> projectilesColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("ProjectilesColor")
            .withValue(new Sn0wColor(200, 100, 200))
            .withParent(projectiles)
            .withParentEnabled(true)
            .register(this);

    public Value<Boolean> shulkers = new ValueBuilder<Boolean>()
            .withDescriptor("Shulkers")
            .withValue(false)
            .register(this);

    public Value<Sn0wColor> shulkersColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("ShulkersColor")
            .withValue(new Sn0wColor(200, 0, 106))
            .withParent(shulkers)
            .withParentEnabled(true)
            .register(this);


    public static Shaders INSTANCE;

    public Shaders()
    {
        super("Shaders", Category.Render);
        INSTANCE = this;
        image.setActive(false);
    }


    @Override
    public void onEnable()
    {
        super.onEnable();

        File chams = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), File.separator + "Sn0w" + File.separator + "chams" + File.separator + image.getValue());

        if (chams.exists())
        {
            loadShaderImage(chams);
        }
        ignoreEntityRender = false;
    }


    @SubscribeEvent
    public void onGameJoin(ServerEvent.ServerJoined event)
    {
        if (fillMode.getValue().equals("Image"))
        {
        }
    }

    @SubscribeEvent
    public void onRenderEntityWorld(RenderShaderEvent event)
    {
        ShaderManager.INSTANCE.reloadShaders();
        switch (fillMode.getValue())
        {
            case "Default", "Off" ->
            {
                final ManagedShaderEffect shaderEffect = ShaderManager.INSTANCE.getDefaultShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                ShaderManager.INSTANCE.applyShader(shaderEffect, () ->
                {
                    shaderEffect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("glowThickness", width.getValue().intValue());
                    shaderEffect.setUniformValue("fillColor", 1.0f, 1.0f, 1.0f, !fillMode.getValue().equals("Off") ? transparency.getValue().floatValue() : 0.0f);

                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("glowSampleStep", outlineQuality.getValue().intValue());
                    shaderEffect.setUniformValue("glowIntensityScale", glowRadius.getValue().floatValue());
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                }, () ->
                {
                    renderEntities(event.getTickDelta(), event.getMatrices());
                });
            }
            case "Rainbow" ->
            {
                final ManagedShaderEffect shaderEffect = ShaderManager.INSTANCE.getRainbowShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                ShaderManager.INSTANCE.applyShader(shaderEffect, () ->
                {
                    shaderEffect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("glowThickness", width.getValue().intValue());
                    shaderEffect.setUniformValue("fillColor", 1.0f, 1.0f, 1.0f, !fillMode.getValue().equals("Off") ? transparency.getValue().floatValue() : 0.0f);
                    shaderEffect.setUniformValue("saturation", HudColors.INSTANCE.saturation.getValue().floatValue() / 255);
                    shaderEffect.setUniformValue("lightness", HudColors.INSTANCE.brightness.getValue().floatValue() / 255);
                    shaderEffect.setUniformValue("factor", factor.getValue().floatValue());
                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("glowSampleStep", outlineQuality.getValue().intValue());
                    shaderEffect.setUniformValue("glowIntensityScale", glowRadius.getValue().floatValue());
                    shaderEffect.setUniformValue("time", shaderTime);
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                    shaderTime += rainbowSpeed.getValue().floatValue();
                }, () ->
                {
                    renderEntities(event.getTickDelta(), event.getMatrices());
                });
                break;
            }
        }
    }


    @SubscribeEvent
    public void onRenderCrystal(RenderCrystalEvent event)
    {
        if (mc.player != null && !texture.getValue() && !ignoreEntityRender &&
                mc.player.squaredDistanceTo(event.endCrystalEntity) <= MathUtil.square(renderDistance.getValue().doubleValue()))
        {
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onRenderEntity(RenderEntityEvent event)
    {
        if (mc.player == null || texture.getValue() || !checkShaders(event.entity) || ignoreEntityRender
                || mc.player.squaredDistanceTo(event.entity) > MathUtil.square(renderDistance.getValue().doubleValue()))
        {
            return;
        }
        event.setCancelled(true);
    }

//    @SubscribeEvent
//    public void onRenderItem(RenderItemEvent event)
//    {
//        if (mc.player != null && !texture.getValue() && items.getValue() && !ignoreEntityRender &&
//                mc.player.squaredDistanceTo(event.getItem()) <= MathUtil.square(renderDistance.getValue().doubleValue()))
//        {
//            event.set();
//        }
//    }
//
//    @SubscribeEvent
//    public void onRenderArm(RenderFirstPersonEvent.Head event)
//    {
//        if (mc.player == null || texture.getValue() || !hands.getValue() || ignoreEntityRender)
//        {
//            return;
//        }
//        event.cancel();
//    }

    private void renderEntities(float tickDelta, MatrixStack matrixStack)
    {
        matrixStack.push();
        ignoreEntityRender = true;
        for (Entity entity : mc.world.getEntities())
        {

            if (!RenderUtil.isFrustumVisible(entity.getBoundingBox()))
            {
                continue;
            }

            if (checkShaders(entity))
            {
                Vec3d start = mc.gameRenderer.getCamera().getPos();
                if (start.squaredDistanceTo(entity.getPos()) > MathUtil.square(renderDistance.getValue().doubleValue()))
                {
                    continue;
                }

                Color color = getESPColor(entity);
                Vec3d camera = mc.gameRenderer.getCamera().getPos();
                double d = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
                double e = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
                double f = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
                float g = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
                EntityRenderer<Entity> entityRenderer = (EntityRenderer<Entity>) mc.getEntityRenderDispatcher().getRenderer(entity);
                VertexConsumerProvider vertexConsumerProvider = ShaderManager.INSTANCE.createVertexConsumers(((IWorldRenderer) mc.worldRenderer).hookGetBufferBuilders().getEntityVertexConsumers(), color);
                int light = mc.getEntityRenderDispatcher().getLight(entity, tickDelta);
                try
                {
                    Vec3d vec3d = entityRenderer.getPositionOffset(entity, tickDelta);
                    double x = (d - camera.x) + vec3d.x;
                    double y = (e - camera.y) + vec3d.y;
                    double z = (f - camera.z) + vec3d.z;
                    matrixStack.push();
                    if (Chams.INSTANCE.isEnabled() && (entity instanceof LivingEntity entity1
                            && Chams.INSTANCE.checkChams(entity1) || entity instanceof EndCrystalEntity && Chams.INSTANCE.crystals.getValue()))
                    {
                        Chams.INSTANCE.renderEntityChams(matrixStack, entity, tickDelta);
                    } else
                    {
                        matrixStack.translate(x, y, z);
                        entityRenderer.render(entity, g, tickDelta, matrixStack, vertexConsumerProvider, light);
                        matrixStack.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ());
                    }
                    matrixStack.pop();
                } catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }
            RenderBuffers.postRender();

            // Blockentity shaders
            if (eChests.getValue() || chests.getValue() || shulkers.getValue())
            {
                for (BlockEntity blockEntity : BlockUtils.getBlockEntities())
                {
                    Color color = getStorageESPColor(blockEntity);
                    VertexConsumerProvider vertexConsumerProvider = ShaderManager.INSTANCE.createVertexConsumers(((IWorldRenderer) mc.worldRenderer).hookGetBufferBuilders().getEntityVertexConsumers(), color);
                    if (checkStorageShaders(blockEntity))
                    {
                        Vec3d vec3d = mc.gameRenderer.getCamera().getPos();
                        double d = vec3d.getX();
                        double e = vec3d.getY();
                        double g = vec3d.getZ();
                        BlockPos blockPos3 = blockEntity.getPos();
                        matrixStack.push();
                        matrixStack.translate((double) blockPos3.getX() - d, (double) blockPos3.getY() - e, (double) blockPos3.getZ() - g);
                        mc.getBlockEntityRenderDispatcher().render(blockEntity, tickDelta, matrixStack, vertexConsumerProvider);
                        matrixStack.pop();
                    }
                }
            }
        }

        // ciaohack solutions
        VertexConsumerProvider vertexConsumerProvider = ShaderManager.INSTANCE.createVertexConsumers(((IWorldRenderer) mc.worldRenderer).hookGetBufferBuilders().getEntityVertexConsumers(), Color.BLUE);
        OtherClientPlayerEntity fakePlayerEntity = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("041f2043-a047-482e-a5b2-41c711badc42"), "nigger"));
        fakePlayerEntity.setPosition(0.0, -100000000.0, 0.0);
        fakePlayerEntity.setId(Integer.MAX_VALUE);
        EntityRenderer<Entity> entityRenderer = (EntityRenderer<Entity>) mc.getEntityRenderDispatcher().getRenderer(fakePlayerEntity);
        matrixStack.push();
        matrixStack.translate(0.0, -100000000.0, 0.0);
        entityRenderer.render(fakePlayerEntity, fakePlayerEntity.getYaw(), tickDelta, matrixStack, vertexConsumerProvider, 0);
        matrixStack.pop();

        ignoreEntityRender = false;
        matrixStack.pop();
    }

    @SubscribeEvent
    public void onReloadShader(RenderHandEvent event)
    {
        if (!hands.getValue())
        {
            return;
        }

        switch (fillMode.getValue())
        {
            case "Default", "Off" ->
            {
                final ManagedShaderEffect shaderEffect = ShaderManager.INSTANCE.getDefaultShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                ShaderManager.INSTANCE.applyShader(shaderEffect, () ->
                {
                    shaderEffect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("glowThickness", width.getValue().intValue());
                    shaderEffect.setUniformValue("fillColor", 1.0f, 1.0f, 1.0f, !fillMode.getValue().equals("Off") ? transparency.getValue().floatValue() : 0.0f);

                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("glowSampleStep", outlineQuality.getValue().intValue());
                    shaderEffect.setUniformValue("glowIntensityScale", glowRadius.getValue().floatValue());
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                }, () ->
                {
                    ignoreEntityRender = true;
                    ((IGameRenderer) mc.gameRenderer).doRenderHand(mc.gameRenderer.getCamera(), event.getTickDelta(), event.getMatrices().peek().getPositionMatrix());
                    ignoreEntityRender = false;
                });
            }
            case "Rainbow" ->
            {
                final ManagedShaderEffect shaderEffect = ShaderManager.INSTANCE.getRainbowShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                ShaderManager.INSTANCE.applyShader(shaderEffect, () ->
                {
                    shaderEffect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("glowThickness", width.getValue().intValue());
                    shaderEffect.setUniformValue("fillColor", 1.0f, 1.0f, 1.0f, !fillMode.getValue().equals("Off") ? transparency.getValue().floatValue() : 0.0f);
                    shaderEffect.setUniformValue("saturation", HudColors.INSTANCE.saturation.getValue().floatValue() / 255);
                    shaderEffect.setUniformValue("lightness", HudColors.INSTANCE.brightness.getValue().floatValue() / 255);
                    shaderEffect.setUniformValue("factor", factor.getValue().floatValue());
                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("glowSampleStep", outlineQuality.getValue().intValue());
                    shaderEffect.setUniformValue("glowIntensityScale", glowRadius.getValue().floatValue());
                    shaderEffect.setUniformValue("time", shaderTime);
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                    shaderTime += rainbowSpeed.getValue().floatValue();
                }, () ->
                {
                    ignoreEntityRender = true;
                    ((IGameRenderer) mc.gameRenderer).doRenderHand(mc.gameRenderer.getCamera(), event.getTickDelta(), event.getMatrices().peek().getPositionMatrix());
                    ignoreEntityRender = false;
                });
                break;
            }
        }
    }


    /**
     * Loads the shader image from memory. The image is loaded from the Shoreline folder.
     */
    public boolean loadShaderImage(File file)
    {
        try
        {
            ByteBuffer data = null;
            String[] fileFormats = new String[]{"png", "jpg"};
            for (String fileFormat : fileFormats)
            {
                File shaderFile = file;
                if (shaderFile.exists())
                {
                    FileInputStream fileInputStream = new FileInputStream(shaderFile);
                    data = TextureUtil.readResource(fileInputStream);
                    break;
                } else
                {
                    Optional<Resource> optional = mc.getResourceManager().getResource(Identifier.of("shoreline", "shaders/shader." + fileFormat));
                    if (optional.isEmpty() || optional.get().getInputStream() == null)
                    {
                        continue;
                    }
                    data = TextureUtil.readResource(optional.get().getInputStream());
                    break;
                }
            }
            if (data == null)
            {
                return false;
            }

            data.rewind();
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);

                STBImage.stbi_set_flip_vertically_on_load(true);
                ByteBuffer image = STBImage.stbi_load_from_memory(data, width, height, comp, 3);
                if (image == null)
                {
                    return false;
                }

                textureId = GlStateManager._genTexture();
                GlStateManager._activeTexture(GL32C.GL_TEXTURE0);
                GlStateManager._bindTexture(textureId);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_SWAP_BYTES, GL32C.GL_FALSE);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_LSB_FIRST, GL32C.GL_FALSE);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_ROW_LENGTH, 0);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_IMAGE_HEIGHT, 0);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_SKIP_ROWS, 0);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_SKIP_PIXELS, 0);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_SKIP_IMAGES, 0);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_ALIGNMENT, 4);
                GlStateManager._texParameter(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_WRAP_S, GL32C.GL_REPEAT);
                GlStateManager._texParameter(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_WRAP_T, GL32C.GL_REPEAT);
                GlStateManager._texParameter(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_MIN_FILTER, GL32C.GL_NEAREST);
                GlStateManager._texParameter(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_MAG_FILTER, GL32C.GL_NEAREST);

                image.rewind();
                GL32C.glTexImage2D(GL32C.GL_TEXTURE_2D, 0, GL32C.GL_RGB, width.get(0), height.get(0), 0, GL32C.GL_RGB, GL32C.GL_UNSIGNED_BYTE, image);

                STBImage.stbi_image_free(image);
                STBImage.stbi_set_flip_vertically_on_load(false);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Color getESPColor(Entity entity)
    {
        if (entity instanceof PlayerEntity player)
        {
            if (entity == mc.player)
            {
                return selfColor.getValue().getColor();
            }
            if (friends.getValue() && FriendManager.INSTANCE.isFriend(player))
            {
                return Nametags.INSTANCE.friendsColor.getValue().getColor();
            }
            return playersColor.getValue().getColor();
        }
        if (EntityUtils.isMonster(entity))
        {
            return monstersColor.getValue().getColor();
        }
        if (EntityUtils.isNeutral(entity) || EntityUtils.isPassive(entity))
        {
            return animalsColor.getValue().getColor();
        }
        if (entity instanceof EndCrystalEntity)
        {
            return crystalsColor.getValue().getColor();
        }
        if (entity instanceof ItemEntity)
        {
            return itemsColor.getValue().getColor();
        }
        if (entity instanceof ExperienceBottleEntity
                || entity instanceof EnderPearlEntity)
        {
            return projectilesColor.getValue().getColor();
        }
        return null;
    }

    public Color getStorageESPColor(BlockEntity tileEntity)
    {
        if (tileEntity instanceof ChestBlockEntity chestBlockEntity)
        {
            return chestsColor.getValue().getColor();
        }
        if (tileEntity instanceof EnderChestBlockEntity)
        {
            return echestsColor.getValue().getColor();
        }
        if (tileEntity instanceof ShulkerBoxBlockEntity)
        {
            return shulkersColor.getValue().getColor();
        }
        return null;
    }

    public boolean checkShaders(Entity entity)
    {
        if (entity instanceof PlayerEntity && players.getValue())
        {
            return self.getValue() && (!mc.options.getPerspective().isFirstPerson()) || entity != mc.player;
        }
        return ((EntityUtils.isMonster(entity) && monsters.getValue()
                || EntityUtils.isPassive(entity) && animals.getValue())
                || entity instanceof EndCrystalEntity && crystals.getValue()
                || entity instanceof ItemEntity && items.getValue()
                || entity instanceof ExperienceBottleEntity && projectiles.getValue()
                || entity instanceof EnderPearlEntity && projectiles.getValue()) || others.getValue();
    }

    private boolean checkStorageShaders(BlockEntity blockEntity)
    {
        return blockEntity instanceof ChestBlockEntity && chests.getValue()
                || blockEntity instanceof EnderChestBlockEntity && eChests.getValue()
                || blockEntity instanceof ShulkerBoxBlockEntity && shulkers.getValue();
    }

    @Override
    public String getDescription()
    {
        return "Shaders: render cool fragment shaders over stuff (change image with \"-chams load copenn.png\"";
    }
}