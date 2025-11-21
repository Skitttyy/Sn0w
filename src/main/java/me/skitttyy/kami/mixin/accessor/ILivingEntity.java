package me.skitttyy.kami.mixin.accessor;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface ILivingEntity {
    @Accessor("lastAttackedTicks")
    int getLastAttackedTicks();

    @Accessor("lastAttackedTicks")
    void setLastAttackedTicks(int ticks);

    @Accessor("jumpingCooldown")
    int getLastJumpCooldown();

    @Accessor("jumpingCooldown")
    void setLastJumpCooldown(int val);

}