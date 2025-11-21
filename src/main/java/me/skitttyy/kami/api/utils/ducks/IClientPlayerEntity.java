package me.skitttyy.kami.api.utils.ducks;

import org.spongepowered.asm.mixin.gen.Invoker;

public interface IClientPlayerEntity {

    float getLastSpoofedYaw();
    float getLastSpoofedPitch();


    void doTick();
    void doSendMovementPackets();
}