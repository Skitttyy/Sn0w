package me.skitttyy.kami.impl.features.modules.misc.autobreak;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.utils.world.MineUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.combat.CatAura;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Getter
@Setter
public class BreakData implements IMinecraft
{
    private BlockPos pos;
    private Direction direction;
    public float[] damages;
    float bestDamage;
    float lastBestDamage;

    int bestSlot;
    Timer timeout;
    Timer actualTimeout;
    boolean queued;
    BlockState lastNonAirState;
    boolean mining;
    boolean lastAir;
    int attempts;
    boolean resetOverride;

    public BreakData(BlockPos pos, Direction direction)
    {
        this.pos = pos;
        this.direction = direction;
        damages = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        bestDamage = 0.0f;
        bestDamage = 0.0f;
        bestSlot = -1;
        timeout = new Timer(AutoBreak.INSTANCE.TIMEOUT);
        actualTimeout = new Timer(6000);
        timeout.resetDelay();
        lastNonAirState = null;

        if (mc.world != null)
        {
            BlockState state = mc.world.getBlockState(pos);
            if (!(state.getBlock() instanceof AirBlock))
            {
                lastNonAirState = mc.world.getBlockState(pos);
            }
        }
        resetOverride = false;
        attempts = 0;
        queued = false;
    }


    public float updateDamage()
    {


        lastBestDamage = bestDamage;
        if (lastNonAirState == null)
            return bestDamage;

        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            float damage = MineUtils.getDamage(lastNonAirState, stack, AutoBreak.INSTANCE.airCheck.getValue(), true);


            damages[i] = MathUtil.clamp(damages[i] + damage, 0.0f, Float.MAX_VALUE);

            if (damages[i] > bestDamage)
            {
                bestDamage = damages[i];
                bestSlot = i;
            }
        }
        return bestDamage;
    }

    public boolean isAir()
    {
        BlockState state = mc.world.getBlockState(getPos());
        return state.getBlock() instanceof AirBlock || state.isLiquid() ;
    }


    public void tick()
    {
        boolean curAir = isAir();

        if (!lastAir && curAir)
        {
            timeout.resetDelay();
            if (!AutoBreak.INSTANCE.onSuccess(this)) return;

        }
        if (!curAir)
            lastNonAirState = BlockUtils.getBlockState(getPos());

        lastAir = curAir;
        updateDamage();


        if (isReady(false) && AutoBreak.INSTANCE.remineMode.getValue().equals("Fast") && curAir)
        {
            AutoBreak.INSTANCE.abort(this);
        }

    }

    public boolean isMining(BlockPos block)
    {
        if (!mining || this.pos == null) return false;

        return block.equals(this.pos);
    }


    public void updateBlock(BlockState state)
    {

        if (!lastAir && (state.isAir() || state.isLiquid()))
        {
            timeout.resetDelay();
            if (!AutoBreak.INSTANCE.onSuccess(this)) return;

        }

        if (!(state.isAir() || state.isLiquid()))
        {
            lastNonAirState = state;
        }
        lastAir = (state.isAir() || state.isLiquid());

        if (isReady(false) && AutoBreak.INSTANCE.remineMode.getValue().equals("Fast") && (state.isAir() || state.isLiquid()))
        {
            AutoBreak.INSTANCE.abort(this);
        }
    }

    public boolean timeout()
    {
        if (attempts < 1)
        {

            timeout.resetDelay();

            return actualTimeout.isPassed();
        }


        if (CatAura.INSTANCE.isEnabled() && CatAura.INSTANCE.renderPos != null && CatAura.INSTANCE.renderPos.up().equals(this.pos))
            timeout.resetDelay();
        return timeout.isPassed();
    }

    public void start(BlockPos pos, Direction direction)
    {
        if (this.pos == pos && mining)
            return;

        this.pos = pos;
        this.direction = direction;
        reset();
        actualTimeout.resetDelay();
        this.mining = true;
        queued = false;
    }

    public void resetOverride()
    {
        this.resetOverride = true;
    }

    public void start()
    {
        this.mining = true;
    }

    void markAttempt()
    {
        if (attempts < 1)
        {
            timeout.resetDelay();
            actualTimeout.resetDelay();
        }

        attempts++;
    }

    public void set(BlockPos pos, Direction direction)
    {
        this.pos = pos;
        this.direction = direction;
    }


    public void reset()
    {
        damages = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        bestDamage = 0.0f;
        lastBestDamage = 0.0f;
        bestSlot = -1;
        timeout = new Timer(AutoBreak.INSTANCE.TIMEOUT);
        timeout.resetDelay();
        attempts = 0;
        mining = false;
        lastAir = false;
        resetOverride = false;
        queued = false;
        actualTimeout = new Timer(6000);
    }


    public void resetSet(BlockPos pos, Direction direction)
    {
        reset();
        set(pos, direction);
    }

    public boolean isReady(boolean packet)
    {
        return bestDamage >= (packet ? 1.0f : AutoBreak.INSTANCE.breakAt.getValue().floatValue());
    }


}