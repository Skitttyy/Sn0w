package me.skitttyy.kami.api.utils;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.friends.Friend;
import me.skitttyy.kami.api.management.FeatureManager;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.management.WaypointManager;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class CommandUtils implements IMinecraft {


    public static String getAutocomplete(Command command, String argument, int part, boolean endsWith, String[] args)
    {
        String[] fill = command.getFill(args);
        if (fill.length == 0) return "";


        if (fill.length < part + 1)
        {
            return "";
        }

        String fillArgument = fill[part];
        return handleFill(fillArgument, argument);
    }

    public static String handleFill(String fillArgument, String argument)
    {
        if (fillArgument == null) return "";


        switch (fillArgument)
        {
            case "{PLAYERS}":
                PlayerEntity player = getClosestMatchingPlayer(argument, false);
                if (player != null)
                    return player.getName().getString();
                break;
            case "{FEATURE}":
                Feature feature = FeatureManager.INSTANCE.getClosestMatchingFeature(argument);
                if (feature != null)
                    return feature.getName();
                break;
            case "{NONFRIENDS}":
                PlayerEntity nonFriendedPlayer = getClosestMatchingPlayer(argument, true);
                if (nonFriendedPlayer != null)
                    return nonFriendedPlayer.getName().getString();
                break;
            case "{FRIENDS}":
                Friend friend = FriendManager.INSTANCE.getClosestMatchingFriend(argument);
                if (friend != null)
                    return friend.toString();
                break;
            case "{WAYPOINTS}":
                WaypointManager.WayPoint wayPoint = WaypointManager.INSTANCE.getClosestMatchingWaypoint(argument);
                if (wayPoint != null)
                    return wayPoint.getName();
                break;
            case "{SPAMMERFILE}":
                return getClosestMatchingSpammerFile(argument);
            case "{CHAMSFILE}":
                return getClosestMatchingCham(argument);
            case "{CONFIGFILE}":
                return getClosestMatchingConfigFile(argument);
            default:
                if (fillArgument.contains(","))
                {
                    return getMultiFillArgument(fillArgument, argument);
                } else
                {
                    if (fillArgument.startsWith(argument))
                    {
                        return fillArgument;
                    }
                }
        }
        return "";
    }

    public static String getClosestMatchingSpammerFile(String text)
    {
        File spammerFolder = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), File.separator + "Sn0w" + File.separator + "spammer" + File.separator);
        if (!spammerFolder.exists()) return "";


        File[] spammers = spammerFolder.listFiles();
        if (spammers.length == 0) return "";

        String bestSpammer = "";
        double lowestLength = Double.MAX_VALUE;

        for (File spammer : spammers)
        {

            if (Objects.equals(text, "")) return spammer.getName();

            if (text.equals(spammer.getName())) return spammer.getName();

            if (text.length() > spammer.getName().length()) continue;

            if (spammer.getName().startsWith(text) && lowestLength > spammer.getName().length())
            {
                lowestLength = spammer.getName().length();
                bestSpammer = spammer.getName();
            }
        }
        return bestSpammer;
    }


    public static String getClosestMatchingConfigFile(String text)
    {
        File configFolder = new File(mc.runDirectory.getAbsolutePath(), File.separator + "Sn0w" + File.separator + "configs" + File.separator);
        if (!configFolder.exists()) return "";


        File[] configs = configFolder.listFiles();
        if (configs.length == 0) return "";

        String bestConfig = "";
        double lowestLength = Double.MAX_VALUE;

        for (File config : configs)
        {
            String name = config.getName().replace(".yml", "");
            if (Objects.equals(text, "")) return name;

            if (text.equals(name)) return name;

            if (text.length() > name.length()) continue;

            if (name.startsWith(text) && lowestLength > name.length())
            {
                lowestLength = name.length();
                bestConfig = name;
            }
        }
        return bestConfig;
    }

    public static String getClosestMatchingCham(String text)
    {
        File chams = new File(mc.runDirectory.getAbsolutePath(), File.separator + "Sn0w" + File.separator + "chams" + File.separator);
        if (!chams.exists()) return "";


        File[] configs = chams.listFiles();
        if (configs.length == 0) return "";

        String bestConfig = "";
        double lowestLength = Double.MAX_VALUE;

        for (File cham : configs)
        {
            String name = cham.getName();
            if (Objects.equals(text, "")) return name;

            if (text.equals(name)) return name;

            if (text.length() > name.length()) continue;

            if (name.startsWith(text) && lowestLength > name.length())
            {
                lowestLength = name.length();
                bestConfig = name;
            }
        }
        return bestConfig;
    }




    public static PlayerEntity getClosestMatchingPlayer(String text, boolean onlyNonFriends)
    {
        List<AbstractClientPlayerEntity> players = mc.world.getPlayers();

        PlayerEntity bestPlayer = null;
        double lowestLength = Double.MAX_VALUE;

        for (PlayerEntity player : players)
        {
            if (player.equals(mc.player)) continue;

            if (onlyNonFriends && FriendManager.INSTANCE.isFriend(player)) continue;


            if (Objects.equals(text, "")) return player;

            if (text.equals(player.getName().getString())) return player;

            if (text.length() > player.getName().getString().length()) continue;

            if (player.getName().getString().startsWith(text) && lowestLength > player.getName().getString().length())
            {
                lowestLength = player.getName().getString().length();
                bestPlayer = player;
            }
        }
        return bestPlayer;
    }

    public static String getMultiFillArgument(String fix, String argument)
    {

        String formatted = fix.replaceAll("\\{", "");
        formatted = formatted.replaceAll("}", "");
        String[] fill = formatted.split(",");
        String text = "";
        boolean both = argument == "";

        if (both)
        {
            text = "<";

            for (int i = 0; i < fill.length; i++)
            {
                text += fill[i];
                if (i < fill.length - 1) text += "|";
            }
            text += ">";
        } else
        {
            for (String toFill : fill)
            {
                if (toFill.startsWith(argument))
                {
                    text = toFill;
                    break;
                }
            }
        }
        return text;
    }

}
