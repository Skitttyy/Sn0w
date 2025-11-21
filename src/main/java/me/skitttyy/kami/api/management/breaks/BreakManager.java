package me.skitttyy.kami.api.management.breaks;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.management.breaks.data.BreakData;
import me.skitttyy.kami.api.management.breaks.data.BreakEntry;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.combat.CatAura;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BreakManager implements IMinecraft
{

    public static BreakManager INSTANCE;
    private final ConcurrentHashMap<Integer, BreakEntry> breakPositions = new ConcurrentHashMap<>();


    public BreakManager()
    {
        KamiMod.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck())
        {
            breakPositions.clear();
            return;
        }


        for (Map.Entry<Integer, BreakEntry> entry : breakPositions.entrySet())
        {
            BreakEntry breakEntry = entry.getValue();
            entry.getValue().tick();

            if (breakEntry.getExtraBreak() == null && breakEntry.getNormalBreak() == null)
                breakPositions.remove(entry.getKey());

        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof BlockBreakingProgressS2CPacket packet)
        {
            BreakEntry data = breakPositions.computeIfAbsent(packet.getEntityId(), k -> new BreakEntry());

            data.startMining(packet.getPos());
        }
    }

    public boolean isBreaking(BlockPos pos)
    {
        return getBreakData(pos) != null;
    }

    public boolean isPassed(BlockPos pos, float progress, boolean canRender)
    {

        BreakData data = getBreakData(pos);



        if (data == null) return false;



        if(canRender && !data.canRender())
            return false;


        return data.getBestDamage() >= progress;
    }

    public BreakData getBreakData(BlockPos pos)
    {
        for (Map.Entry<Integer, BreakEntry> entry : breakPositions.entrySet())
        {

            BreakEntry breakEntry = entry.getValue();
            BreakData extra = breakEntry.getExtraBreak();
            BreakData data = breakEntry.getNormalBreak();

            if (extra != null && extra.getPos().equals(pos))
                return extra;


            if (data != null && data.getPos().equals(pos))
                return data;
        }
        return null;
    }

    public List<BreakData> getBreakDatas()
    {
        List<BreakData> datas = new LinkedList<>();


        for (Map.Entry<Integer, BreakEntry> entry : breakPositions.entrySet())
        {

            BreakEntry breakEntry = entry.getValue();
            BreakData extra = breakEntry.getExtraBreak();
            BreakData data = breakEntry.getNormalBreak();

            if (extra != null)
                datas.add(extra);


            if (data != null)
                datas.add(data);

        }
        return datas;
    }
}