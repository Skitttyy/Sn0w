package me.skitttyy.kami.mixin.accessor;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientWorld.class)
public interface IClientWorld {
    @Invoker("playSound")
    void hookPlaySound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, boolean useDistance, long seed);

    @Invoker("getPendingUpdateManager")
    PendingUpdateManager accquirePendingUpdateManager();
}