package me.skitttyy.kami.mixin.accessor;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientConnection.class)
public interface IClientConnection {

    @Invoker("sendInternal")
    void sendQuietPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush);
}