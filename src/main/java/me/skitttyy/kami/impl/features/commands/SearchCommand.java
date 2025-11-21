package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.management.CommandManager;
import me.skitttyy.kami.api.management.SearchManager;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.util.Formatting;

public class SearchCommand extends Command {
    public SearchCommand()
    {
        super("Search", "add blocks to search esp", new String[]{"search"});
    }

    @Override
    public void run(String[] args)
    {
        try
        {
            boolean[] correction = setIncorrect(args);
            boolean incorrect = correction[0];
            if (incorrect)
            {
                handleIncorrectUsage();
                return;
            }
            switch (args[1].toLowerCase())
            {
                case "add":
                    handleAddBlock(args);
                    break;
                case "del":
                    handleRemoveBlock(args);
                    break;
                default:
                    handleIncorrectUsage();
                    break;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean[] setIncorrect(String[] args)
    {
        boolean incorrect = false;
        if (args.length < 2)
        {
            incorrect = true;
        }
        if (!incorrect)
        {
            if (args[1].equalsIgnoreCase("add"))
            {
                if (args.length < 4)
                {
                    if (args.length != 3)
                    {
                        incorrect = true;
                    }
                }
            } else if (args[1].equalsIgnoreCase("del"))
            {
                if (args.length < 3)
                {
                    incorrect = true;
                }
            }
        }
        return new boolean[]{incorrect};
    }

    private void handleIncorrectUsage()
    {
        ChatUtils.sendMessage(Formatting.RED + "Incorrect usage!");
        ChatUtils.sendMessage(Formatting.RED + "Usage: " + CommandManager.INSTANCE.PREFIX + "Search <add/del> <blockname>");
    }

    private void handleAddBlock(String[] args)
    {
        Block block = SearchManager.INSTANCE.findBlock(args[2].toLowerCase());
        if (block == null)
        {
            ChatUtils.sendMessage("Block does not exist.");
            return;
        }
        String name = block.getName().getString();
        if (!SearchManager.INSTANCE.hasBlock(block))
        {
            SearchManager.INSTANCE.addBlock(args[2]);
            ChatUtils.sendMessage("Block " + name + " added.");
            mc.worldRenderer.reload();
        } else
        {
            ChatUtils.sendMessage("Block " + name + " is already in the Search list.");
        }
    }

    private void handleRemoveBlock(String[] args)
    {
        Block block = SearchManager.INSTANCE.findBlock(args[2]);

        if (block == null)
        {
            ChatUtils.sendMessage("Block does not exist.");
            return;
        }
        String name = block.getName().getString();

        if (SearchManager.INSTANCE.hasBlock(block))
        {

            SearchManager.INSTANCE.removeBlock(block);
            ChatUtils.sendMessage("Block " + name + " removed.");
            mc.worldRenderer.reload();
        } else
        {
            ChatUtils.sendMessage("Block " + name + " is not in the Search list.");
        }
    }

    @Override
    public String[] getFill(String[] args)
    {
        if (args.length == 1)
        {
            return new String[]{"{add,del}", "block"};
        } else
        {
            return new String[]{"{add,del}"};
        }
    }

}
