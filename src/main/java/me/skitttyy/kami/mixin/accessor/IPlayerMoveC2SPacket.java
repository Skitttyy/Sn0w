package me.skitttyy.kami.mixin.accessor;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface IPlayerMoveC2SPacket {


    @Accessor("x")
    @Mutable
    void setX(double x);

    @Accessor("x")
    double getX();

    @Accessor("y")
    @Mutable
    void setY(double y);

    @Accessor("y")
    double getY();

    @Accessor("z")
    @Mutable
    void setZ(double z);

    @Accessor("z")
    double getZ();

    @Mutable
    @Accessor("onGround")
    void setOnGround(boolean onGround);

    @Mutable
    @Accessor("yaw")
    void setYaw(float yaw);

    @Mutable
    @Accessor("yaw")
    float getYaw();

    @Mutable
    @Accessor("pitch")
    void setPitch(float pitch);

    @Mutable
    @Accessor("pitch")
    float getPitch();
}
