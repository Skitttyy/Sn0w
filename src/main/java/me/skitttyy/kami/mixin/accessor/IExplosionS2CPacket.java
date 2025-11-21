package me.skitttyy.kami.mixin.accessor;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface IExplosionS2CPacket {
    @Accessor("playerVelocityX")
    @Mutable
    void setPlayerVelocityX(float playerVelocityX);
    @Accessor("playerVelocityY")
    @Mutable
    void setPlayerVelocityY(float playerVelocityY);

    @Accessor("playerVelocityZ")
    @Mutable
    void setPlayerVelocityZ(float playerVelocityZ);
}