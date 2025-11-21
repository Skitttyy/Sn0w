package me.skitttyy.kami.api.management.breaks.data;

import lombok.Getter;
import me.skitttyy.kami.api.utils.Pair;
import net.minecraft.util.math.BlockPos;

@Getter
public class BreakEntry
{

    BreakData extraBreak;
    BreakData normalBreak;
    boolean extra = true;

    public BreakEntry()
    {
        extraBreak = null;
        normalBreak = null;
        extra = false;
    }


    public void tick()
    {
        if (normalBreak != null)
        {
            if (normalBreak.tick())
                normalBreak = null;
        }

        if (extraBreak != null)
        {
            if (extraBreak.tick())
                extraBreak = null;
        }

    }

    public void startMining(BlockPos pos)
    {

        if (normalBreak != null && normalBreak.getPos().equals(pos)) return;

        if (extraBreak != null && extraBreak.getPos().equals(pos)) return;

        if (extra)
            extraBreak = new BreakData(pos);
        else
        {
            normalBreak = new BreakData(pos);
        }

        extra = !extra;
    }
}