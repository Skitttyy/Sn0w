package me.skitttyy.kami.mixin.accessor;

import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(Tessellator.class)
public interface ITessellator
{
    @Accessor("allocator")
    BufferAllocator getAllocator();
}