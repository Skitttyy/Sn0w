package me.skitttyy.kami.api.event.events.world;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.util.List;

@Getter
@Setter
public class CollisionBoxEvent extends Event {

    private final BlockPos pos;
    private final BlockState state;
    private VoxelShape voxelShape;

    public CollisionBoxEvent(VoxelShape voxelShape, BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
        this.voxelShape = voxelShape;
    }

}
