package me.skitttyy.kami.api.utils.ducks;

import net.minecraft.network.packet.Packet;

public interface IClientPlayNetworkHandler {
    void sendQuietPacket(final Packet<?> packet);

}