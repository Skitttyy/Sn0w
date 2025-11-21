package me.skitttyy.kami.api.event.events.render;

import lombok.Getter;
import me.skitttyy.kami.api.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

@Getter
public class RenderBlockEvent extends Event {
    BlockPos pos;
    BlockState state;
    public RenderBlockEvent(BlockPos pos, BlockState state)
    {
        this.pos = pos;
        this.state = state;

    }
}