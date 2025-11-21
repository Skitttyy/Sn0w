package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.management.SavableManager;
import net.minecraft.util.Util;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FolderCommand extends Command {
    public FolderCommand()
    {
        super("Folder", "opens Sn0w folder", new String[]{"folder"});
    }

    @Override
    public void run(String[] args)
    {
        File dir = new File(SavableManager.MAIN_FOLDER.getAbsolutePath());
        if (!dir.exists()) dir.mkdir();

        Util.getOperatingSystem().open(SavableManager.MAIN_FOLDER.getAbsoluteFile());
    }
}
