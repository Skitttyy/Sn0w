package me.skitttyy.kami.api.management.breaks.data;

import lombok.Getter;
import me.skitttyy.kami.api.utils.world.MineUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;

@Getter
public class BreakData implements IMinecraft
{

    private final BlockPos pos;
    float bestDamage;
    Timer timeout;
    boolean render = true;
    public BreakData(BlockPos pos)
    {
        this.pos = pos;
        bestDamage = 0.0f;
        timeout = new Timer(4000);
        timeout.resetDelay();
        render = true;
    }

    public boolean canRender(){
        return render;
    }

    public void updateDamage(BlockState state)
    {


        ItemStack stack = Items.NETHERITE_PICKAXE.getDefaultStack();
        stack.addEnchantment(mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).entryOf(Enchantments.EFFICIENCY), 5);

        float damage = MineUtils.getDamage(state, stack, false, true);


        bestDamage = MathUtil.clamp(bestDamage + damage, 0.0f, Float.MAX_VALUE);
    }


    public boolean tick()
    {

        if (timeout.isPassed()) return true;


        BlockState state = BlockUtils.getBlockState(pos);


        if (state.isAir()){
            render = false;
            return false;
        }

        if (!BlockUtils.isMineable(state)){
            render = false;
            return false;
        }

        updateDamage(state);

        return false;
    }


}
