package me.skitttyy.kami.mixin;


import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import me.skitttyy.kami.api.event.events.move.LookEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.network.ServerEvent;
import me.skitttyy.kami.api.event.events.world.ChunkDataEvent;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.ducks.IClientPlayNetworkHandler;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.player.AntiPackets;
import me.skitttyy.kami.mixin.accessor.IClientConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;


@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler implements ClientPlayPacketListener, TickablePacketListener, IClientPlayNetworkHandler, IMinecraft
{

    @Shadow
    private ClientWorld world;

    @Shadow
    public abstract void clearWorld();

    @Shadow
    public abstract ClientConnection getConnection();

    @Unique
    private boolean worldNotNull;


    public MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection clientConnection, ClientConnectionState clientConnectionState)
    {
        super(client, clientConnection, clientConnectionState);
    }

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void onGameJoinHead(GameJoinS2CPacket packet, CallbackInfo info)
    {
        worldNotNull = world != null;
    }


    @Inject(
            method = {"onChunkData"},
            at = {@At("RETURN")}
    )
    public void onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci, @Share("seenChunk") LocalBooleanRef seenChunkRef)
    {

        new ChunkDataEvent(world.getChunk(packet.getChunkX(), packet.getChunkZ()), seenChunkRef.get()).post();
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info)
    {
        if (worldNotNull)
        {
            new ServerEvent.ServerLeft().post();
        }

        new ServerEvent.ServerJoined().post();
    }


    /**
     * TODO: remove this @Overwrite
     *
     * @author cope
     * @reason cope
     */
    @Overwrite
    public void onBundle(BundleS2CPacket packet)
    {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Iterator var2 = packet.getPackets().iterator();

        while (var2.hasNext())
        {
            Packet<? super ClientPlayPacketListener> packet2 = (Packet) var2.next();

            PacketEvent.Receive event = new PacketEvent.Receive(packet2, packet);
            event.post();
            if (!event.isCancelled())
                packet2.apply(this);
        }
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setVelocityClient(DDD)V", shift = At.Shift.BEFORE), cancellable = true)
    public void onEntityVelocityPacket(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci)
    {
        if (PacketManager.CANCELATHANDLE.contains(packet))
        {
            PacketManager.CANCELATHANDLE.remove(packet);
            ci.cancel();
        }
    }

    @Inject(method = "onExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void onEntityVelocityPacket(ExplosionS2CPacket packet, CallbackInfo ci)
    {
        if (PacketManager.CANCELATHANDLE.contains(packet))
        {
            PacketManager.CANCELATHANDLE.remove(packet);
            ci.cancel();
        }
    }


    /**
     * @author a
     * @reason a
     */
//    @Overwrite
//    public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet)
//    {
//        NetworkThreadUtils.forceMainThread(packet, this, this.client);
//        PlayerEntity playerEntity = this.client.player;
//        Vec3d vec3d = playerEntity.getVelocity();
//        boolean bl = packet.getFlags().contains(PositionFlag.X);
//        boolean bl2 = packet.getFlags().contains(PositionFlag.Y);
//        boolean bl3 = packet.getFlags().contains(PositionFlag.Z);
//        double d;
//        double e;
//        if (bl)
//        {
//            d = vec3d.getX();
//            e = playerEntity.getX() + packet.getX();
//            playerEntity.lastRenderX += packet.getX();
//            playerEntity.prevX += packet.getX();
//        } else
//        {
//            d = 0.0;
//            e = packet.getX();
//            playerEntity.lastRenderX = e;
//            playerEntity.prevX = e;
//        }
//
//        double f;
//        double g;
//        if (bl2)
//        {
//            f = vec3d.getY();
//            g = playerEntity.getY() + packet.getY();
//            playerEntity.lastRenderY += packet.getY();
//            playerEntity.prevY += packet.getY();
//        } else
//        {
//            f = 0.0;
//            g = packet.getY();
//            playerEntity.lastRenderY = g;
//            playerEntity.prevY = g;
//        }
//
//        double h;
//        double i;
//        if (bl3)
//        {
//            h = vec3d.getZ();
//            i = playerEntity.getZ() + packet.getZ();
//            playerEntity.lastRenderZ += packet.getZ();
//            playerEntity.prevZ += packet.getZ();
//        } else
//        {
//            h = 0.0;
//            i = packet.getZ();
//            playerEntity.lastRenderZ = i;
//            playerEntity.prevZ = i;
//        }
//
//        playerEntity.setPosition(e, g, i);
//        playerEntity.setVelocity(d, f, h);
//        float j = packet.getYaw();
//        float k = packet.getPitch();
//
//
//
//        float pitch;
//        float yaw;
//        if (packet.getFlags().contains(PositionFlag.X_ROT))
//        {
//            pitch = RotationUtils.getActualPitch() + k;
//        } else
//        {
//            pitch = k;
//        }
//
//        if (packet.getFlags().contains(PositionFlag.Y_ROT))
//        {
//            yaw = RotationUtils.getActualYaw() + j;
//        } else
//        {
//           yaw = k;
//        }
//
//        if (!AntiPackets.INSTANCE.isEnabled() || !AntiPackets.INSTANCE.serverRotate.getValue())
//        {
//            playerEntity.setPitch(pitch);
//            playerEntity.prevPitch = pitch;
//
//            playerEntity.setYaw(yaw);
//            playerEntity.prevYaw = yaw;
//        }
//
//        this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
//
//
//        if(AntiPackets.INSTANCE.isEnabled() && AntiPackets.INSTANCE.serverRotate.getValue() && AntiPackets.INSTANCE.adjust.getValue()){
//            this.connection.send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), RotationUtils.getActualYaw(), RotationUtils.getActualPitch(), false));
//        }else
//        {
//            this.connection.send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), yaw, pitch, false));
//        }
//    }
    @Overwrite
    public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet)
    {
        double i;
        double h;
        double g;
        double f;
        double e;
        double d;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity playerEntity = this.client.player;
        Vec3d vec3d = playerEntity.getVelocity();
        boolean bl = packet.getFlags().contains((Object) PositionFlag.X);
        boolean bl2 = packet.getFlags().contains((Object) PositionFlag.Y);
        boolean bl3 = packet.getFlags().contains((Object) PositionFlag.Z);
        if (bl)
        {
            d = vec3d.getX();
            e = playerEntity.getX() + packet.getX();
            playerEntity.lastRenderX += packet.getX();
            playerEntity.prevX += packet.getX();
        } else
        {
            d = 0.0;
            playerEntity.lastRenderX = e = packet.getX();
            playerEntity.prevX = e;
        }
        if (bl2)
        {
            f = vec3d.getY();
            g = playerEntity.getY() + packet.getY();
            playerEntity.lastRenderY += packet.getY();
            playerEntity.prevY += packet.getY();
        } else
        {
            f = 0.0;
            playerEntity.lastRenderY = g = packet.getY();
            playerEntity.prevY = g;
        }
        if (bl3)
        {
            h = vec3d.getZ();
            i = playerEntity.getZ() + packet.getZ();
            playerEntity.lastRenderZ += packet.getZ();
            playerEntity.prevZ += packet.getZ();
        } else
        {
            h = 0.0;
            playerEntity.lastRenderZ = i = packet.getZ();
            playerEntity.prevZ = i;
        }
        playerEntity.setPosition(e, g, i);
        playerEntity.setVelocity(d, f, h);
        float j = packet.getYaw();
        float k = packet.getPitch();


        float pitch;
        float yaw;

        if (packet.getFlags().contains((Object) PositionFlag.X_ROT))
        {

            pitch = RotationUtils.getActualPitch() + k;

            if (!AntiPackets.INSTANCE.isEnabled() || !AntiPackets.INSTANCE.serverRotate.getValue())
            {
                playerEntity.setPitch(playerEntity.getPitch() + k);
                playerEntity.prevPitch += k;
            }
        } else
        {
            pitch = k;
            if (!AntiPackets.INSTANCE.isEnabled() || !AntiPackets.INSTANCE.serverRotate.getValue())
            {
                playerEntity.setPitch(k);
                playerEntity.prevPitch = k;
            }
        }


        if (packet.getFlags().contains((Object) PositionFlag.Y_ROT))
        {

            yaw = playerEntity.getYaw() + j;
            if (!AntiPackets.INSTANCE.isEnabled() || !AntiPackets.INSTANCE.serverRotate.getValue())
            {
                playerEntity.setYaw(playerEntity.getYaw() + j);
                playerEntity.prevYaw += j;
            }
        } else
        {
            yaw = j;
            if (!AntiPackets.INSTANCE.isEnabled() || !AntiPackets.INSTANCE.serverRotate.getValue())
            {
                playerEntity.setYaw(j);
                playerEntity.prevYaw = j;
            }
        }
        this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));


        if (!AntiPackets.INSTANCE.isEnabled() || !AntiPackets.INSTANCE.serverRotate.getValue())
        {
            this.connection.send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYaw(), playerEntity.getPitch(), false));
        } else if (AntiPackets.INSTANCE.isEnabled() && AntiPackets.INSTANCE.serverRotate.getValue())
        {
            if (AntiPackets.INSTANCE.adjust.getValue())
            {
                this.connection.send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), RotationUtils.getActualYaw(), RotationUtils.getActualPitch(), false));

            } else
            {
                this.connection.send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), yaw, pitch, false));
            }
        }
    }

    @Override
    public void sendQuietPacket(Packet<?> packet)
    {
        ((IClientConnection) getConnection()).sendQuietPacket(packet, null, true);
    }

}