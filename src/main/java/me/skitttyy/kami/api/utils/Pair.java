package me.skitttyy.kami.api.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class Pair<K, V>
{
    private final K key;
    private final V value;

    public Pair(K key,
                V value)
    {
        this.key = key;
        this.value = value;
    }

    public K key()
    {
        return this.key;
    }

    public V value()
    {
        return this.value;
    }

    public static class BlockPair extends Pair<BlockPos, Direction>
    {
        public BlockPair(BlockPos pos,
                         Direction face)
        {
            super(pos, face);
        }
    }

    public static class BreakPair extends Pair<BlockPos, Direction>
    {
        public boolean reset;
        public BreakPair(BlockPos pos,
                         Direction face)
        {
            super(pos, face);
            reset = false;
        }
        public BreakPair(BlockPos pos,
                         Direction face, boolean reset)
        {
            super(pos, face);
            this.reset = reset;
        }
    }
    public static class BlockPairTime extends Pair<BlockPos, Long>
    {
        public BlockPairTime(BlockPos pos)
        {
            super(pos, System.currentTimeMillis());
        }
    }

    public static class BoxPair extends Pair<Box, Long>
    {
        public BoxPair(Box box)
        {
            super(box, System.currentTimeMillis());
        }
    }
}