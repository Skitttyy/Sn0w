package me.skitttyy.kami.mixin;


import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.network.ServerEvent;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.impl.KamiMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection
{
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo info)
    {
        PacketEvent event = new PacketEvent.Receive(packet);
        event.post();

        if (event.isCancelled()) info.cancel();
    }


    @Inject(method = "handlePacket", at = @At("TAIL"), cancellable = true)
    private static <T extends PacketListener> void onHaneTail(Packet<T> packet, PacketListener listener, CallbackInfo info)
    {
        new PacketEvent.ReceivePost(packet).post();
    }


    @Inject(at = @At("HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;)V", cancellable = true)
    private void onSendPacketHead(Packet<?> packet, CallbackInfo info)
    {
        PacketEvent event = new PacketEvent.Send(packet);
        event.post();
        if (event.isCancelled()) info.cancel();
    }

    @Inject(at = @At("RETURN"), method = "send(Lnet/minecraft/network/packet/Packet;)V")
    private void onSendTail(Packet<?> packet, CallbackInfo info)
    {
        new PacketEvent.SendPost(packet).post();

        if (PacketManager.INSTANCE.isCached(packet)) PacketManager.INSTANCE.uncache(packet);


    }

    @Inject(method = "disconnect", at = @At(value = "HEAD"))
    private void hookDisconnect(Text disconnectReason, CallbackInfo ci)
    {
        ServerEvent.ServerLeft event = new ServerEvent.ServerLeft();
        event.post();
    }

}
