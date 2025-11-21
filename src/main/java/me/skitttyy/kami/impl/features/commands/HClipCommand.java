package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import net.minecraft.entity.Entity;

public class HClipCommand extends Command {
    public HClipCommand()
    {
        super("HClip", "teleports u forward", new String[]{"hclip"});
    }

    @Override
    public void run(String[] args)
    {
        if (args.length == 1)
        {
            return;
        }

        if (mc.player == null)
        {
            return;
        }

        double h = Double.parseDouble(args[1]);
        Entity entity = mc.player.getVehicle() != null
                ? mc.player.getVehicle()
                : mc.player;
        double yaw =
                Math.cos(Math.toRadians(mc.player.getYaw() + 90.0f));
        double pit =
                Math.sin(Math.toRadians(mc.player.getYaw() + 90.0f));

        entity.setPosition(
                entity.getX() + h * yaw, entity.getY(), entity.getZ() + h * pit);
    }


    @Override
    public String[] getFill(String[] args)
    {
        if (args.length == 1)
        {
            return new String[]{"Distance"};
        } else
        {
            return new String[]{};
        }
    }

}
