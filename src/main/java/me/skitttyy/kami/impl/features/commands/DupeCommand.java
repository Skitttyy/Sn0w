package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.management.PacketManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.time.Instant;
import java.util.BitSet;
import java.util.Objects;

public class DupeCommand extends Command
{
    public DupeCommand()
    {
        super("Dupe", "teleports u forward", new String[]{"dupe"});
    }

    @Override
    public void run(String[] args)
    {
        if (Objects.equals(args[1], "desync"))
        {
            mc.player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        if (Objects.equals(args[1], "retrieve"))
        {
            ItemStack stack = Items.GHAST_TEAR.getDefaultStack();
            stack.setCount(64);
            mc.player.setStackInHand(Hand.MAIN_HAND, stack);
        }
    }


}
