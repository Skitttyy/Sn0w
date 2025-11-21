package me.skitttyy.kami.api.utils.players;


import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.mixin.accessor.IClientPlayerInteractionManager;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static me.skitttyy.kami.api.management.RotationManager.swapData;

public class InventoryUtils implements IMinecraft
{
    public static final List<Integer> invalidSlots = Arrays.asList(0, 5, 6, 7, 8);

    public static Item[] CHESTPLATE_ITEMS = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE};


    public static int getHotbarItemSlot(Item item)
    {
        int slot = -1;
        for (int i = 0; i < 9; i++)
        {
            if (mc.player.getInventory().getStack(i).getItem().equals(item))
            {
                slot = i;
                break;
            }
        }
        return slot;
    }


    public static int getNonBlockInHotbar()
    {
        int slot = -1;
        for (int i = 0; i < 9; i++)
        {

            Item item = mc.player.getInventory().getStack(i).getItem();

            if (item == null || (!(item instanceof BlockItem) && !item.getComponents().contains(DataComponentTypes.FOOD)))
            {
                slot = i;
                break;
            }
        }
        return slot;
    }

    public static void pickupSlot(final int slot)
    {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
    }

    public static void swapArmor(int armorSlot, int inSlot)
    {
        int slot = inSlot;
        if (slot < 9) slot += 36;

        ItemStack stack = mc.player.getInventory().getArmorStack(armorSlot);
        armorSlot = 8 - armorSlot;

        pickupSlot(slot);
        boolean rt = !stack.isEmpty();
        pickupSlot(armorSlot);
        if (rt)
        {
            pickupSlot(slot);
        }
    }

    public static void pickArmorSlot(int armorSlot)
    {


        pickupSlot(8 - armorSlot);

    }

    public static int findShulker()
    {
        int shulker = -1;
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != ItemStack.EMPTY)
            {
                if (stack.getItem() instanceof BlockItem)
                {
                    final Block block = ((BlockItem) stack.getItem()).getBlock();
                    if (BlockUtils.SHULKER_BLOCKS.contains(block))
                    {
                        shulker = i;
                        break;
                    }
                }
            }
        }
        return shulker;
    }

    public static int findBlockInHotbar(Block bc)
    {
        int slot = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem)
            {
                Block block = ((BlockItem) stack.getItem()).getBlock();
                if (block == bc)
                {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }

    public static void switchToSlot(boolean silent, int slot)
    {
        if (silent)
        {
            switchToSlotGhost(slot);
            mc.player.getInventory().selectedSlot = slot;
        } else
        {
            if (mc.player.getInventory().selectedSlot != slot)
            {
                final ItemStack[] hotbarCopy = new ItemStack[9];
                for (int i = 0; i < 9; i++)
                {
                    hotbarCopy[i] = mc.player.getInventory().getStack(i);
                }
                swapData.add(new PreSwapData(hotbarCopy, mc.player.getInventory().selectedSlot, slot));
                mc.player.getInventory().selectedSlot = slot;
            }
        }
    }

    public static int findChestplate()
    {
        for (Item item : CHESTPLATE_ITEMS)
        {
            int slot = getInventoryItemSlot(item);
            if (slot != -1)
            {
                return slot;
            }
        }
        return -1;
    }

    public static int getBestToolSlot(Block block)
    {
        float maxSpeed = -1.0F;
        int slot = -1;
        ItemStack stackCur = mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot);
        float normalDestroySpeed = stackCur.getItem().getMiningSpeed(stackCur, block.getDefaultState());
        for (int i = 0; i < 9; i++)
        {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty())
            {
                float speed = itemStack.getItem().getMiningSpeed(itemStack, block.getDefaultState());
                if (speed > maxSpeed)
                {
                    maxSpeed = speed;
                    slot = i;
                }
            }
        }
        if (normalDestroySpeed == maxSpeed) return -1;


        return slot;
    }

    public static int getHotbarItemSlot2(Item item)
    {
        int slot = getHotbarItemSlot(item);
        if (slot == -1) return mc.player.getInventory().selectedSlot;
        else return slot;
    }

    public static int hotbarToInventory(int slot)
    {
        if (slot == -2)
        {
            return 45;
        }

        if (slot > -1 && slot < 9)
        {
            return 36 + slot;
        }

        return slot;
    }

    public static void syncItem()
    {
        ((IClientPlayerInteractionManager) mc.interactionManager).doSyncSelectedSlot();
    }

    public static void switchToBypass(int slot, boolean resync)
    {
        if (slot != -1)
        {
            if (resync)
            {
                resyncClick(slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
            } else
            {
                normalPacketClick(slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
            }
        }
    }

    // mc server sends back a set slot packet with the current item stack amount if its invalid, so we send send diamond block itemstack to make it update
    public static void resyncClick(int slotId, int mouseButton, SlotActionType type)
    {
        packetInvSwap(slotId, mouseButton, type, new ItemStack(Blocks.DIAMOND_BLOCK, 64), mc.player.currentScreenHandler);
    }

    public static void normalPacketClick(int slotId, int mouseButton, SlotActionType type)
    {
        packetInvSwap(slotId, mouseButton, type, mc.player.getInventory().getStack(slotId), mc.player.currentScreenHandler);
    }


    public static void packetInvSwap(int slotId, int mouseButton, SlotActionType type, ItemStack stack, ScreenHandler handler)
    {
        Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>();
        map.put(mouseButton, handler.getSlot(slotId).getStack());
        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(handler.syncId, handler.getRevision(), slotId, mouseButton, type, stack, map));
    }


    public static void throwSlot(int slot)
    {
        if (slot != -1)
        {
            mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.THROW, mc.player);
        }
    }

//    public static void switchToSlotUpdateIf(int slot)
//    {
//
//        if (slot == -1) return;
//
//        mc.player.getInventory().selectedSlot = slot;
//
//        if (RotationManager.INSTANCE.serverSlot != slot)
//            switchToSlotGhost(slot);
//
//    }

    public static void switchToSlot(int slot)
    {
        mc.player.getInventory().selectedSlot = slot;
        syncItem();
    }


    public static double getAttackDamage(ItemStack stack)
    {

        for (AttributeModifiersComponent.Entry entry : stack.getItem().getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers())
        {
            if (entry.modifier().idMatches(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID))
            {
                return entry.modifier().value();
            }
        }
        return 0.0;
    }

    public static float getHitDamage(@NotNull ItemStack weapon, PlayerEntity ent)
    {
        if (mc.player == null) return 0;
        float baseDamage = 1f;

        if (weapon.getItem() instanceof SwordItem swordItem)
            baseDamage = 7;

        if (weapon.getItem() instanceof AxeItem axeItem)
            baseDamage = 9;

        if (mc.player.fallDistance > 0)
            baseDamage += baseDamage / 2f;

        if (mc.player.hasStatusEffect(StatusEffects.STRENGTH))
        {
            int strength = Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.STRENGTH)).getAmplifier() + 1;
            baseDamage += 3 * strength;
        }

        // Reduce by armour
        baseDamage = DamageUtil.getDamageLeft(ent, baseDamage, mc.world.getDamageSources().generic(), ent.getArmor(), (float) ent.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
        return baseDamage;
    }


    public static void getEnchantments(ItemStack itemStack, Object2IntMap<RegistryEntry<Enchantment>> enchantments)
    {
        enchantments.clear();

        if (!itemStack.isEmpty())
        {
            Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> itemEnchantments = itemStack.getItem() == Items.ENCHANTED_BOOK
                    ? itemStack.get(DataComponentTypes.STORED_ENCHANTMENTS).getEnchantmentEntries()
                    : itemStack.getEnchantments().getEnchantmentEntries();

            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantments)
            {
                enchantments.put(entry.getKey(), entry.getIntValue());
            }
        }
    }


    public static int getEnchantmentLevel(ItemStack itemStack, RegistryKey<Enchantment> enchantment)
    {
        if (itemStack.isEmpty()) return 0;
        Object2IntMap<RegistryEntry<Enchantment>> itemEnchantments = new Object2IntArrayMap<>();
        getEnchantments(itemStack, itemEnchantments);
        return getEnchantmentLevel(itemEnchantments, enchantment);
    }

    public static int getEnchantmentLevel(Object2IntMap<RegistryEntry<Enchantment>> itemEnchantments, RegistryKey<Enchantment> enchantment)
    {
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : Object2IntMaps.fastIterable(itemEnchantments))
        {
            if (entry.getKey().matchesKey(enchantment)) return entry.getIntValue();
        }
        return 0;
    }


    public static int getSwordSlot()
    {
        float sharp = 0.0f;
        int slot = -1;

        float bestAxeDmg = 0.0f;
        int bestAxe = -1;
        // Maximize item attack damage

        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof TridentItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof SwordItem)
            {
                float sharpness = getEnchantmentLevel(stack, Enchantments.SHARPNESS) * 0.5f + 0.5f;

                float dmg = (float) (getAttackDamage(stack) + sharpness);

                if (stack.getItem() instanceof SwordItem)
                {
                    if (dmg > sharp)
                    {
                        sharp = dmg;
                        slot = i;
                    }
                } else if (dmg > bestAxeDmg)
                {
                    bestAxeDmg = dmg;
                    bestAxe = i;
                }
            }
        }


        if (slot == -1)
            return bestAxe;

        return slot;
    }


    public static void switchToSlot(Item item)
    {
        mc.player.getInventory().selectedSlot = getHotbarItemSlot2(item);
    }

    public static void switchToSlotGhost(int slot)
    {
        if (slot != -1)
        {
            final ItemStack[] hotbarCopy = new ItemStack[9];
            for (int i = 0; i < 9; i++)
            {
                hotbarCopy[i] = mc.player.getInventory().getStack(i);
            }
            swapData.add(new PreSwapData(hotbarCopy, RotationManager.INSTANCE.serverSlot, slot));
            PacketManager.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(slot));

        }
    }

    public static void switchToSlotGhost(Item item)
    {
        switchToSlotGhost(getHotbarItemSlot2(item));
    }

    public static int getItemCount(Item item)
    {
        int count = 0;
        count = mc.player.getInventory().main.stream().filter(itemStack -> itemStack.getItem().equals(item)).mapToInt(ItemStack::getCount).sum();
        count += mc.player.getInventory().offHand.stream().filter(itemStack -> itemStack.getItem().equals(item)).mapToInt(ItemStack::getCount).sum();

        return count;

    }

    public static int getItemCount(Item item, List<ItemStack> items)
    {
        int count = 0;
        count = items.stream().filter(itemStack -> itemStack.getItem().equals(item)).mapToInt(ItemStack::getCount).sum();
        count += mc.player.getInventory().offHand.stream().filter(itemStack -> itemStack.getItem().equals(item)).mapToInt(ItemStack::getCount).sum();
        return count;
    }


    public static String getArmorPieceName(ItemStack stack)
    {
        if (stack.getItem() == Items.NETHERITE_HELMET
                || stack.getItem() == Items.TURTLE_HELMET
                || stack.getItem() == Items.DIAMOND_HELMET
                || stack.getItem() == Items.GOLDEN_HELMET
                || stack.getItem() == Items.IRON_HELMET
                || stack.getItem() == Items.CHAINMAIL_HELMET
                || stack.getItem() == Items.LEATHER_HELMET)
        {

            return "helmet";
        }

        if (stack.getItem() == Items.NETHERITE_CHESTPLATE
                || stack.getItem() == Items.DIAMOND_CHESTPLATE
                || stack.getItem() == Items.GOLDEN_CHESTPLATE
                || stack.getItem() == Items.IRON_CHESTPLATE
                || stack.getItem() == Items.CHAINMAIL_CHESTPLATE
                || stack.getItem() == Items.LEATHER_CHESTPLATE)
        {

            return "chest";
        }

        if (stack.getItem() == Items.NETHERITE_LEGGINGS
                || stack.getItem() == Items.DIAMOND_LEGGINGS
                || stack.getItem() == Items.GOLDEN_LEGGINGS
                || stack.getItem() == Items.IRON_LEGGINGS
                || stack.getItem() == Items.CHAINMAIL_LEGGINGS
                || stack.getItem() == Items.LEATHER_LEGGINGS)
        {

            return "leggings";
        }
        if (stack.getItem() == Items.NETHERITE_BOOTS
                || stack.getItem() == Items.DIAMOND_BOOTS
                || stack.getItem() == Items.GOLDEN_BOOTS
                || stack.getItem() == Items.IRON_BOOTS
                || stack.getItem() == Items.CHAINMAIL_BOOTS
                || stack.getItem() == Items.LEATHER_BOOTS)
        {
            return "boots";
        }

        return null;
    }


    public static int getItemSlotSmart(Item item)
    {
        int slot = -1;
        for (int i = 36; i >= 0; i--)
        {
            if (mc.player.getInventory().getStack(i).getItem().equals(item))
            {
                slot = i;
                break;
            }
        }

        return slot;
    }

    public static int getInventoryItemSlot(Item item)
    {
        int slot = -1;
        for (int i = 36; i >= 0; i--)
        {
            if (mc.player.getInventory().getStack(i).getItem().equals(item))
            {
                slot = i;
                break;
            }
        }

        return slot;
    }


    public static void moveItemToOffhand(int iSlot, boolean fast)
    {
        int slot = iSlot;
        if (iSlot < 9)
            slot = iSlot + 36;


        if (fast)
        {

            mc.interactionManager.clickSlot(0, slot, 40, SlotActionType.SWAP, mc.player);
            return;
        }
        int returnSlot = 0;


        if (slot == -1) return;

        mc.interactionManager.clickSlot(0, slot < 9 ? slot + 36 : slot, 0, SlotActionType.PICKUP, mc.player);

        mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player);

        for (int i = 0; i < 45; i++)
        {
            if (mc.player.getInventory().getStack(i).isEmpty())
            {
                returnSlot = i;
                break;
            }
        }
        if (returnSlot != -1)
        {
            mc.interactionManager.clickSlot(0, returnSlot < 9 ? returnSlot + 36 : returnSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }


    public static boolean moveItemToOffhand(int slot, int stage)
    {
        int returnSlot = 0;


        if (slot == -1) return false;

        if (stage == 1)
        {
            mc.interactionManager.clickSlot(0, slot < 9 ? slot + 36 : slot, 0, SlotActionType.PICKUP, mc.player);
        }

        if (stage == 2)
        {
            mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player);
        }

        if (stage == 3)
        {
            for (int i = 0; i < 45; i++)
            {
                if (mc.player.getInventory().getStack(i).isEmpty())
                {
                    returnSlot = i;
                    break;
                }
            }
            if (returnSlot != -1)
            {
                mc.interactionManager.clickSlot(0, returnSlot < 9 ? returnSlot + 36 : returnSlot, 0, SlotActionType.PICKUP, mc.player);
            }
            return true;
        }
        return false;
    }



/*    public static void moveItemToOffhand(int slot, int returnSlot) {
        boolean startMoving = true;
        boolean moving = false;
        boolean returning = false;

        if (slot == -1) return;

        if (!moving && startMoving) {
            mc.interactionManager.clickSlot(0, slot < 9 ? slot + 36 : slot, 0, SlotActionType.PICKUP, mc.player);
            moving = true;
            startMoving = false;
        }

        if (moving) {
            mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player);
            moving = false;
            returning = true;
        }

        if (returning) {
            if (returnSlot != -1) {
                mc.interactionManager.clickSlot(0, returnSlot < 9 ? returnSlot + 36 : returnSlot, 0, SlotActionType.PICKUP, mc.player);
            }

            returning = false;
        }

        startMoving = true;
    }*/

    static int lastSlot = -1;

    public static void moveItemToOffhand(Item item, boolean fast)
    {
        int slot = getInventoryItemSlot(item);
        if (lastSlot != -1 && mc.player.getInventory().getStack(lastSlot).getItem().equals(item))
            slot = lastSlot;


        if (slot != -1)
        {
            lastSlot = slot;
            moveItemToOffhand(slot, fast);
        }
    }

    public static void swap(int invSlot, int hotbarSlot)
    {
        mc.interactionManager.clickSlot(0, invSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
    }

    public static boolean moveItemToOffhand(Item item, int stage)
    {
        int slot = getInventoryItemSlot(item);
        if (slot != -1)
        {
            return moveItemToOffhand(slot, stage);
        }
        return false;
    }


    public static void moveItem(int slot, int slotOut)
    {
        mc.interactionManager.clickSlot(0, slot < 9 ? slot + 36 : slot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(0, slotOut < 9 ? slotOut + 36 : slotOut, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(0, slot < 9 ? slot + 36 : slot, 0, SlotActionType.PICKUP, mc.player);
    }

    public static void moveItem(Item item, int slot)
    {
        moveItem(getInventoryItemSlot(item), slot);
    }

    public static class PreSwapData
    {
        private final ItemStack[] preHotbar;

        private final int starting;
        private final int swapTo;

        private Timer clearTime;

        public PreSwapData(ItemStack[] preHotbar, int start, int swapTo)
        {
            this.preHotbar = preHotbar;
            this.starting = start;
            this.swapTo = swapTo;
        }

        public void beginClear()
        {
            clearTime = new Timer();
            clearTime.resetDelay();
        }

        public boolean isPassedClearTime()
        {
            return clearTime != null && clearTime.isPassed(300);
        }

        public ItemStack getPreHolding(int i)
        {
            return preHotbar[i];
        }

        public int getStarting()
        {
            return starting;
        }

        public int getSlot()
        {
            return swapTo;
        }
    }
}
