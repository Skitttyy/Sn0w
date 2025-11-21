package me.skitttyy.kami.mixin.accessor;

import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HeldItemRenderer.class)
public interface IHeldItemRenderer {
    @Accessor(value = "equipProgressMainHand")
    void setEquippedProgressMainHand(float var1);

    @Accessor(value = "equipProgressMainHand")
    float getEquippedProgressMainHand();

    @Accessor(value = "equipProgressOffHand")
    void setEquippedProgressOffHand(float var1);

    @Accessor(value = "equipProgressOffHand")
    float getEquippedProgressOffHand();

}