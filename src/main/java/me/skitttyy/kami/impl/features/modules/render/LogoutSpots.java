package me.skitttyy.kami.impl.features.modules.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.player.TeamColorEvent;
import me.skitttyy.kami.api.event.events.render.EntityOutlineEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.event.events.world.EntityEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.management.PopManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.color.TextSection;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.Interpolator;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.WireframeEntityRenderer;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.api.utils.world.EntityUtils;
import me.skitttyy.kami.api.utils.world.WorldUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import me.skitttyy.kami.impl.features.modules.client.sense.PingedLocation;
import me.skitttyy.kami.mixin.accessor.ILimbAnimator;
import me.skitttyy.kami.mixin.accessor.IWorldRenderer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogoutSpots extends Module
{

    Value<Sn0wColor> fill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill Color")
            .withValue(new Sn0wColor(47, 0, 255, 150))
            .register(this);
    Value<Sn0wColor> line = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line Color")
            .withValue(new Sn0wColor(255, 255, 255, 255))
            .register(this);
    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Box")
            .withModes("Box", "Model")
            .register(this);
    public Value<Boolean> pops = new ValueBuilder<Boolean>()
            .withDescriptor("Pops")
            .withValue(true)
            .register(this);
    public Value<Sn0wColor> normalColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Normal Color")
            .withValue(new Sn0wColor(0, 255, 255, 255))
            .register(this);
    public Value<Sn0wColor> friendsColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Friends Color")
            .withValue(new Sn0wColor(0, 255, 255, 255))
            .register(this);

    public Value<Sn0wColor> borderColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Border Color")
            .withValue(new Sn0wColor(255, 0, 0, 255))
            .register(this);

    public LogoutSpots()
    {
        super("LogoutESP", Category.Render);
    }

    public static List<LoggedPlayer> players = new CopyOnWriteArrayList<LoggedPlayer>();


    Map<UUID, Pair<PlayerEntity, Long>> lastPlayers = new ConcurrentHashMap<>();


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;


        for (Map.Entry<UUID, Pair<PlayerEntity, Long>> entry : lastPlayers.entrySet())
        {
            long time = System.currentTimeMillis() - entry.getValue().value();

            if (time > 200L)
            {
                lastPlayers.remove(entry.getKey());
            }
        }

    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;

        players.forEach(loggedPlayer -> renderEntity(event.getMatrices(), loggedPlayer.player, loggedPlayer.modelPlayer, loggedPlayer, event));


    }

    @SubscribeEvent
    public void onEntityRemove(EntityEvent.Remove event)
    {

        // crystal being removed from world
        if (event.getEntity() instanceof PlayerEntity entity)
        {
            lastPlayers.put(entity.getUuid(), new Pair<>(entity, System.currentTimeMillis()));
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (NullUtils.nullCheck()) return;


        players.clear();

    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof PlayerListS2CPacket pac)
        {
            if (pac.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER))
            {
                for (PlayerListS2CPacket.Entry ple : pac.getPlayerAdditionEntries())
                {
                    for (LoggedPlayer player : players)
                    {
                        if (!player.id.equals(ple.profile().getId())) continue;

                        ChatUtils.sendMessage(new ChatMessage("[Logout Spots] " + Manager.INSTANCE.getMainColor() + player.playerName + Formatting.RESET + " logged back in!", false, 99922));
                        players.remove(player);
                    }
                }
            }
        }

        if (event.getPacket() instanceof PlayerRemoveS2CPacket pac)
        {
            for (UUID uuid2 : pac.profileIds())
            {
                PlayerEntity playerEntity = mc.world.getPlayerByUuid(uuid2);
                Pair<PlayerEntity, Long> pair = lastPlayers.get(uuid2);

                if (playerEntity == null && pair != null)
                {
                    playerEntity = pair.key();
                }
                if (playerEntity != null)
                {

                    if (playerEntity == mc.player) return;

                    PlayerEntity entity = new PlayerEntity(mc.world, BlockPos.ORIGIN, playerEntity.getBodyYaw(), new GameProfile(playerEntity.getUuid(), playerEntity.getName().getString()))
                    {
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


                    entity.copyPositionAndRotation(playerEntity);
                    entity.bodyYaw = playerEntity.getBodyYaw();
                    entity.headYaw = playerEntity.getHeadYaw();
                    entity.handSwingProgress = playerEntity.handSwingProgress;
                    entity.handSwingTicks = playerEntity.handSwingTicks;
                    entity.setSneaking(playerEntity.isSneaking());
                    entity.limbAnimator.setSpeed(playerEntity.limbAnimator.getSpeed());
                    entity.setHealth(playerEntity.getHealth());
                    entity.setAbsorptionAmount(playerEntity.getAbsorptionAmount());
                    ((ILimbAnimator) entity.limbAnimator).setLimbPos(playerEntity.limbAnimator.getPos());
                    players.add(new LoggedPlayer(entity, ((AbstractClientPlayerEntity) playerEntity).getSkinTextures(), uuid2));
                    lastPlayers.remove(uuid2);
                    break;
                }
            }
        }


    }

    private void renderEntity(MatrixStack matrices, LivingEntity entity, PlayerEntityModel<PlayerEntity> modelBase, LoggedPlayer player, RenderWorldEvent event)
    {
        if (mode.getValue().equals("Model"))
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


            WireframeEntityRenderer.renderModel(matrices, (AnimalModel) modelBase, RenderType.BOTH, fill.getValue().getColor(), line.getValue().getColor());
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            matrices.pop();
        } else
        {
            RenderUtil.renderBox(RenderType.FILL, entity.getBoundingBox(), fill.getValue().getColor(), fill.getValue().getColor());
            RenderUtil.renderBox(RenderType.LINES, entity.getBoundingBox(), line.getValue().getColor(), line.getValue().getColor());

        }
        if (player != null)
        {
            RenderBuffers.scheduleRender(() ->
            {

                renderWaypoint(player, event);
                RenderSystem.enableBlend();

            });
        }

    }


    private void renderWaypoint(LoggedPlayer loc, RenderWorldEvent event)
    {
        Vec3d interpolate = Interpolator.getInterpolatedEyePos(mc.getCameraEntity(), event.getTickDelta());
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d pos = camera.getPos();


        double dx = (pos.getX() - interpolate.getX()) - loc.player.getX();
        double dy = (pos.getY() - interpolate.getY()) - (loc.player.getY() + loc.player.getHeight() + (loc.player.isSneaking() ? 0.4f : 0.43f));
        double dz = (pos.getZ() - interpolate.getZ()) - loc.player.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);


        TextSection[] text = new TextSection[1];
        text[0] = new TextSection(renderEntityName(loc.player, loc), ColorUtil.newAlpha(fill.getValue().getColor(), 255));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GL11.glDepthFunc(GL11.GL_ALWAYS);

        RenderUtil.drawWaypoint(text, loc.player.getX(), loc.player.getY() + loc.player.getHeight() + (loc.player.isSneaking() ? 0.4f : 0.43f), loc.player.getZ(), mc.gameRenderer.getCamera(), borderColor.getValue().getColor());
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();

    }


    private String renderEntityName(final PlayerEntity entityPlayer, LoggedPlayer player)
    {
        String s = Formatting.RED + player.playerName + " logout";

        final double ceil;
        String s2 = Formatting.GREEN.toString();
        if ((ceil = Math.ceil(entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount())) > 0.0)
        {

            if ((entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount()) <= 5)
            {
                s2 = Formatting.RED.toString();
            } else if ((entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount()) > 5 && (entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount()) <= 10)
            {
                s2 = Formatting.GOLD.toString();
            } else if ((entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount()) > 10 && (entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount()) <= 15)
            {
                s2 = Formatting.YELLOW.toString();
            } else if ((entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount()) > 15 && (entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount()) <= 20)
            {
                s2 = Formatting.DARK_GREEN.toString();
            } else if ((entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount()) > 20)
            {
                s2 = Formatting.GREEN.toString();
            }
        } else
        {
            s2 = Formatting.DARK_RED.toString();
        }

        int popsForPlayer = PopManager.INSTANCE.getPops(player.playerName);
        String popstring = Formatting.RED + " (" + Formatting.WHITE + MathHelper.floor(mc.player.distanceTo(entityPlayer)) + "m" + Formatting.RED + ")";
        if (pops.getValue())
        {
            if (popsForPlayer < 1)
            {

            } else
            {
                popstring = Formatting.RED + " (" + Formatting.WHITE + MathHelper.floor(mc.player.distanceTo(entityPlayer)) + "m" + Formatting.AQUA + " -" + popsForPlayer + Formatting.RED + ")";
            }
        }
        return new StringBuilder().insert(0, s).append(s2).append(" ").append((ceil > 0.0) ? Integer.valueOf((int) ceil) : "0").append(popstring).toString();
    }

    private Color renderPing(final PlayerEntity entityPlayer)
    {
        if (FriendManager.INSTANCE.isFriend(entityPlayer))
        {
            return friendsColor.getValue().getColor();
        }
        if (entityPlayer.isInvisible())
        {
            return new Color(128, 128, 128);
        }
        return normalColor.getValue().getColor();
    }

    private static void prepareScale(MatrixStack matrixStack)
    {
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(1.6f, 1.8f, 1.6f);
        matrixStack.translate(0.0F, -1.501F, 0.0F);

    }

    @Override
    public String getDescription()
    {
        return "LogoutESP: highlights logged players";
    }


    class LoggedPlayer
    {
        private final PlayerEntityModel<PlayerEntity> modelPlayer;
        private final PlayerEntity player;
        public String playerName;
        public Box bb;
        public UUID id;

        public LoggedPlayer(PlayerEntity player, SkinTextures texture, UUID id)
        {
            this.player = player;
            this.id = id;
            modelPlayer = new PlayerEntityModel<>(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), texture.model().equals(SkinTextures.Model.SLIM));
            modelPlayer.getHead().scale(new Vector3f(-0.3f, -0.3f, -0.3f));
            this.bb = player.getBoundingBox();
            playerName = player.getName().getString();
        }

    }

}