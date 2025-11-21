package me.skitttyy.kami.api.utils.world;


import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;

public class MineUtils implements IMinecraft
{
    public static boolean canHarvestBlock(BlockState state, ItemStack stack)
    {
        if (state.isToolRequired())
        {
            return true;
        }

        if (stack.isEmpty())
        {
            return stack.getItem().isCorrectForDrops(stack, state); //TODO: check
        }

        return false;
    }

    public static int findBestTool(BlockPos pos)
    {
        return findBestTool(pos, mc.world.getBlockState(pos));
    }

    public static int findBestTool(BlockPos pos, BlockState state)
    {
        int result = mc.player.getInventory().selectedSlot;
        if (state.getHardness(mc.world, pos) > 0)
        {
            double speed = getSpeed(state, mc.player.getMainHandStack());
            for (int i = 0; i < 9; i++)
            {
                ItemStack stack = mc.player.getInventory().getStack(i);
                double stackSpeed = getSpeed(state, stack);
                if (stackSpeed > speed)
                {
                    speed = stackSpeed;
                    result = i;
                }
            }
        }

        return result;
    }

    public static double getSpeed(BlockState state, ItemStack stack)
    {
        double str = stack.getMiningSpeedMultiplier(state);
        int effect = InventoryUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);
        return Math.max(str + (str > 1.0 ? (effect * effect + 1.0) : 0.0), 0.0);
    }

    public static float getDamage(ItemStack stack, BlockPos pos, boolean auto, boolean onGround)
    {
        BlockState state = mc.world.getBlockState(pos);
        return getDamage(state, stack, auto, onGround);
    }

    public static float getDamage(BlockState state, ItemStack stack, boolean auto, boolean onGround)
    {
        float hardness = state.getBlock().getHardness();
        if (hardness == -1.0f)
        {
            return 0.0f;
        }
        return getDigSpeed(stack, state, auto, onGround)
                / (hardness
                * (canHarvestBlock(state, stack) ? 30 : 100));
    }

    public static float getDamage(BlockState state, ItemStack stack,
                                  BlockPos pos, boolean onGround,
                                  boolean isOnGround)
    {
        float hardness = state.getHardness(mc.world, pos);
        if (hardness == -1.0f)
        {
            return 0.0f;
        }
        return getDigSpeed(stack, state, onGround, isOnGround)
                / (hardness
                * (canHarvestBlock(state, stack) ? 30 : 100));
    }

    private static float getDigSpeed(ItemStack stack, BlockState state,
                                     boolean auto, boolean onGround)
    {
        float digSpeed = 1.0F;

        if (!stack.isEmpty())
        {
            digSpeed *= stack.getMiningSpeedMultiplier(state);
        }

        if (digSpeed > 1.0F)
        {
            int i = InventoryUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);

            if (i > 0 && !stack.isEmpty())
            {
                digSpeed += (float) (i * i + 1);
            }
        }

        if (mc.player.hasStatusEffect(StatusEffects.HASTE))
        {
            //noinspection ConstantConditions
            digSpeed *= 1.0F
                    + (mc.player.getStatusEffect(StatusEffects.HASTE)
                    .getAmplifier() + 1) * 0.2F;
        }

        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE))
        {
            float miningFatigue;
            //noinspection ConstantConditions
            switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE)
                    .getAmplifier())
            {
                case 0:
                    miningFatigue = 0.3F;
                    break;
                case 1:
                    miningFatigue = 0.09F;
                    break;
                case 2:
                    miningFatigue = 0.0027F;
                    break;
                case 3:
                default:
                    miningFatigue = 8.1E-4F;
            }

            digSpeed *= miningFatigue;
        }

        if (mc.player.isInsideWaterOrBubbleColumn()
                && !(InventoryUtils.getEnchantmentLevel(mc.player.getEquippedStack(EquipmentSlot.HEAD), Enchantments.AQUA_AFFINITY) > 0))
        {
            digSpeed /= 5.0F;
        }

        if ((auto && !mc.player.isOnGround()) || (!auto && !onGround))
        {
            digSpeed /= 5.0F;
        }

        return (digSpeed < 0 ? 0 : digSpeed);
    }

    public static boolean canBreak(BlockPos pos)
    {
        return canBreak(mc.world.getBlockState(pos), pos);
    }

    public static boolean canBreak(BlockState state, BlockPos pos)
    {
        return state.getHardness(mc.world, pos) != -1
                && state.getBlock() != Blocks.AIR
                && !state.isLiquid();
    }


}