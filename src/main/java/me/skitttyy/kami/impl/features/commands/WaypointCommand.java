package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.friends.Friend;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.management.WaypointManager;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import net.minecraft.util.Formatting;

public class WaypointCommand extends Command {
    public WaypointCommand()
    {
        super("Waypoint", "Create a waypoint", new String[]{"waypoint"});
    }

    @Override
    public void run(String[] args)
    {
        if (args.length > 2)
        {
            if (args[1].equalsIgnoreCase("create"))
            {
                if (args.length == 3)
                {
                    WaypointManager.WayPoint wp = new WaypointManager.WayPoint((int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ(), args[2], (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address), mc.world.getRegistryKey().getValue().getPath());
                    WaypointManager.INSTANCE.addWayPoint(wp);
                    ChatUtils.sendMessage("Created waypoint " + wp.getName() + " at X: " + ((int) mc.player.getX()) + " Y: " + ((int) mc.player.getY()) + " Z: " + ((int) mc.player.getZ()));
                } else if (args.length == 6)
                {
                    try
                    {
                        int x = Integer.parseInt(args[3]);
                        int y = Integer.parseInt(args[4]);
                        int z = Integer.parseInt(args[5]);


                        WaypointManager.WayPoint wp = new WaypointManager.WayPoint(x, y, z, args[2], (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address), mc.world.getRegistryKey().getValue().getPath());
                        WaypointManager.INSTANCE.addWayPoint(wp);
                        ChatUtils.sendMessage("Created waypoint " + wp.getName() + " at X: " + x + " Y: " + y + " Z: " + z);

                    } catch (Exception e)
                    {
                        ChatUtils.sendMessage(Formatting.RED + "Invalid waypoint!");
                    }
                } else
                {
                    ChatUtils.sendMessage(new ChatMessage(
                            "Invalid format",
                            false,
                            0
                    ));
                }


            } else if (args[1].equalsIgnoreCase("remove"))
            {
                WaypointManager.INSTANCE.wayPoints.removeIf(value -> value.getName().equalsIgnoreCase(args[2]));
                ChatUtils.sendMessage(new ChatMessage(
                        "Removed waypoint with name: " + args[2],
                        false,
                        0
                ));
            } else
            {
                ChatUtils.sendMessage(new ChatMessage(
                        "Invalid format",
                        false,
                        0
                ));
            }
        } else
        {
            ChatUtils.sendMessage(new ChatMessage(
                    "Invalid format",
                    false,
                    0
            ));
        }
    }


    @Override
    public String[] getFill(String[] args)
    {
        if (args.length > 1)
        {
            if (args[1].equalsIgnoreCase("create"))
            {
                return new String[]{"{create,remove}", "name", "x", "y", "z"};
            } else if (args[1].equalsIgnoreCase("remove"))
            {
                return new String[]{"{create,remove}", "{WAYPOINTS}"};
            }
        }
        return new String[]{"{create,remove}", "name", "x", "y", "z"};
    }
}
