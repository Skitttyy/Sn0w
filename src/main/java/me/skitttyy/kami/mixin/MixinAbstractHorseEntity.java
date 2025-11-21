package me.skitttyy.kami.mixin;

import me.skitttyy.kami.impl.features.modules.player.EntityControl;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public abstract class MixinAbstractHorseEntity extends AnimalEntity {
    protected MixinAbstractHorseEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    public void onIsSaddled(CallbackInfoReturnable<Boolean> cir) {
        if (EntityControl.INSTANCE.isEnabled())
            cir.setReturnValue(true);
    }
}