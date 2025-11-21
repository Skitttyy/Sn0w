package me.skitttyy.kami.api.utils.ducks;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Vec3d;

public interface ILivingEntity {

    Vec3d kami_prevServerPos();

    boolean kami_forceHasStatusEffect(StatusEffect effect);

    StatusEffectInstance kami_forceGetStatusEffect(StatusEffect effect);

    float kami_getHeadPitch();

    void kami_setHeadPitch(float headPitch);

    float kami_getPrevHeadPitch();

    void kami_setPrevHeadPitch(float prevHeadPitch);

    float kami_getHeadYaw();

    void kami_setHeadYaw(float headYaw);

    float kami_getPrevHeadYaw();

    void kami_setPrevHeadYaw(float prevHeadYaw);

    boolean kami_isInInventory();

    void kami_setInInventory(boolean inInventory);
}