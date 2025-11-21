package me.skitttyy.kami.mixin.accessor;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireworkRocketEntity.class)
public interface IFireworkRocketEntity {
    @Accessor("shooter")
    LivingEntity getShooter();
}