package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Formatting;

public class ElytraSwap extends Module {

    public ElytraSwap()
    {
        super("ElytraSwap", Category.Movement);
    }

    Value<Boolean> instaFly = new ValueBuilder<Boolean>()
            .withDescriptor("Auto-Fly")
            .withValue(false)
            .register(this);

    Value<Boolean> firework = new ValueBuilder<Boolean>()
            .withDescriptor("Firework")
            .withValue(false)
            .withParent(instaFly)
            .withParentEnabled(true)
            .register(this);

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;


        doSwap();

    }



    public void doSwap()
    {

        if (!PlayerUtils.isElytraEquipped())
        {
            int slot = InventoryUtils.getInventoryItemSlot(Items.ELYTRA);
            if (slot != -1)
            {
                InventoryUtils.swapArmor(2, slot);
                ChatUtils.sendMessage(new ChatMessage("[Elytra Swap] Equipping " + Formatting.DARK_GRAY + "Elytra" + Formatting.RESET + "!", true, 50016));
                if (instaFly.getValue() && !mc.player.isOnGround())
                {
                    PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    mc.player.startFallFlying();
                    if (firework.getValue())
                        PlayerUtils.doFirework();
                }
            } else
            {
                ChatUtils.sendMessage(new ChatMessage("[Elytra Swap] elytraless dog", true, 50016));
            }
        } else
        {
            int slot = InventoryUtils.findChestplate();
            if (slot != -1)
            {
                InventoryUtils.swapArmor(2, slot);
                ChatUtils.sendMessage(new ChatMessage("[Elytra Swap] Equipping " + Formatting.AQUA + "Chestplate" + Formatting.RESET + "!", true, 50016));
            } else
            {
                ChatUtils.sendMessage(new ChatMessage("[Elytra Swap] chestplateless dog", true, 50016));
            }
        }
        setEnabled(false);
    }

    int offgroundTicks = 0;


    @Override
    public String getDescription()
    {
        return "ElytraSwap: swaps ur chestplate with ur elytra";
    }
}