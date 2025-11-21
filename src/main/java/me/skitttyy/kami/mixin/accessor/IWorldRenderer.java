package me.skitttyy.kami.mixin.accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.BlockBreakingInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(WorldRenderer.class)
public interface IWorldRenderer {
    /**
     * @return
     */
    @Accessor("frustum")
    Frustum getFrustum();

    /**
     * @return
     */
    @Accessor("blockBreakingInfos")
    Int2ObjectMap<BlockBreakingInfo> getBlockBreakingProgressions();


    @Accessor("bufferBuilders")
    BufferBuilderStorage hookGetBufferBuilders();

    @Accessor("noCullingBlockEntities")
    Set<BlockEntity> hookGetNoCullingBlockEntities();

}