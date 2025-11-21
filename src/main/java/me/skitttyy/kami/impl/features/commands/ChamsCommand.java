package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.management.shaders.ShaderManager;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.impl.features.modules.render.Shaders;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.File;
import java.util.Objects;

public class ChamsCommand extends Command {
    String lastFile = "";


    public ChamsCommand()
    {
        super("Chams", "set image", new String[]{"chams"});
    }

    @Override
    public void run(String[] args)
    {
        if (Objects.equals(args[1], "folder"))
        {
            openDirectory();
        } else if (Objects.equals(args[1], "load"))
        {
            if (args.length == 3)
            {
                handleSetFile(args);
            } else
            {
                ChatUtils.sendMessage(Formatting.RED + "Incorrect usage: chams load <filename>");
            }
        }
    }

    private static void openDirectory()
    {
        File spammerFile = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), File.separator + "Sn0w" + File.separator + "chams" + File.separator);

        if (!spammerFile.exists()) spammerFile.mkdir();

        Util.getOperatingSystem().open(spammerFile);
    }

    private void handleSetFile(String[] args)
    {
        File chams = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), File.separator + "Sn0w" + File.separator + "chams" + File.separator + args[2]);
        if (!chams.exists())
        {
            ChatUtils.sendMessage(Formatting.AQUA + "[Chams]" + Formatting.BLUE + " File does not exist!");
        } else
        {
            if (!Shaders.INSTANCE.loadShaderImage(chams))
            {
                ChatUtils.sendMessage(Formatting.AQUA + "[Chams]" + Formatting.RED + " invalid image!");
            } else
            {
                Shaders.INSTANCE.image.setValue(args[2]);
                ChatUtils.sendMessage(Formatting.AQUA + "[Chams]" + Formatting.AQUA + " set chams image!");
            }
        }
    }

    @Override
    public String[] getFill(String[] args)
    {
        return new String[]{"{file,folder,load}", "{CHAMSFILE}"};
    }

}
