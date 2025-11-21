package me.skitttyy.kami.api.event.events;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class LivingEvent {
    @Getter
    @Setter
    public static class Jump extends Event {

        float yaw;

        public Jump(float yaw)
        {
            this.yaw = yaw;
        }


    }

    public static class SetSprinting extends Event {

        public SetSprinting()
        {

        }
    }

    @Getter
    public static class Death extends Event {

        Entity entity;

        public Death(Entity entity)
        {
            this.entity = entity;
        }
    }


    public static class AttackBlock extends Event {

        private final BlockPos pos;
        private final BlockState state;
        private final Direction direction;

        public AttackBlock(BlockPos pos, BlockState state, Direction direction)
        {
            this.pos = pos;
            this.state = state;
            this.direction = direction;
        }

        public BlockPos getPos()
        {
            return pos;
        }

        public BlockState getState()
        {
            return state;
        }

        public Direction getDirection()
        {
            return direction;
        }
    }

    @Getter
    public static class BreakBlock extends Event {

        private final BlockPos pos;

        public BreakBlock(BlockPos pos)
        {
            this.pos = pos;
        }
    }


    @Getter
    public static class Eat extends Event {

        private final ItemStack stack;

        public Eat(ItemStack stack)
        {
            this.stack = stack;
        }

        public Item getItem()
        {
            return stack.getItem();
        }

    }

    @Getter
    public static class Attack extends Event {
        private Entity entity;

        public Attack(Entity entity)
        {
            this.entity = entity;
        }

    }


    public static class BlockSlowdown extends Event {
        private final BlockState state;

        public BlockSlowdown(BlockState state)
        {
            this.state = state;
        }

        public BlockState getState()
        {
            return state;
        }
    }

}
