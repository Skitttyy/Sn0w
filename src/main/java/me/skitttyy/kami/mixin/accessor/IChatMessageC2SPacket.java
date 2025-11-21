package me.skitttyy.kami.mixin.accessor;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatMessageC2SPacket.class)
public interface IChatMessageC2SPacket {
    @Accessor("chatMessage")
    @Mutable
    void setMessage(String text);
}