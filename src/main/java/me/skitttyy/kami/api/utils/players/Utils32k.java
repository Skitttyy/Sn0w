package me.skitttyy.kami.api.utils.players;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class Utils32k implements IMinecraft {

    public static boolean is32kInHotbar()
    {
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY)
            {
                continue;
            }
            if (checkSharpness(stack))
            {
                return true;
            }
        }
        return false;
    }

    public static int findReverted32ks()
    {
        for (int i = 0; i < 9; i++)
        {

            ItemStack stack = mc.player.getInventory().getStack(i);


            if (stack == ItemStack.EMPTY)
            {
                continue;
            }
            if (!(stack.getItem() instanceof SwordItem))
            {
                continue;
            }
            if (!checkSharpness(stack))
            {
                return i;
            }
        }
        return -1;
    }

    public static boolean isHolding32k(final PlayerEntity player)
    {
        return checkSharpness(player.getMainHandStack());
    }

    public static boolean checkSharpness(ItemStack stack)
    {

        if (stack.isEmpty())
        {
            return false;
        }
        if (!stack.hasEnchantments()) return false;


        float level = InventoryUtils.getEnchantmentLevel(stack, Enchantments.SHARPNESS);

        if (level > 42)
        {
            return true;
        }

        return false;
    }


    public static boolean checkEnchant(ItemStack stack)
    {

        if (stack.isEmpty())
        {
            return false;
        }
        if (!stack.hasEnchantments()) return false;


        float level = InventoryUtils.getEnchantmentLevel(stack, Enchantments.SHARPNESS);

        if (level > 42)
        {
            return true;
        }

        return false;
    }
}
