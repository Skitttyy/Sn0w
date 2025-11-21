package me.skitttyy.kami.api.management.network;



import me.skitttyy.kami.api.utils.discord.DiscordEventHandlers;
import me.skitttyy.kami.api.utils.discord.DiscordRPC;
import me.skitttyy.kami.api.utils.discord.DiscordRichPresence;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.RPC;
import org.apache.commons.lang3.time.StopWatch;



public class DiscordPresence implements IMinecraft {
    public static DiscordRichPresence presence;
    private static final DiscordRPC rpc = DiscordRPC.INSTANCE;
    private static Thread thread;

    public static void start()
    {
        if (thread != null)
        {
            thread.interrupt();
        }


        DiscordPresence.presence = new DiscordRichPresence();

        final DiscordEventHandlers handlers = new DiscordEventHandlers();
        System.out.println("Initializing discord RPC");

        rpc.Discord_Initialize("1273333927096094742", handlers, true, "");
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.details = RPC.INSTANCE.text.getValue();
        presence.state = getState();
        presence.largeImageKey = getImageKey();
        presence.largeImageText = "catogod.cc";
        presence.smallImageKey = "cop26logo_cropped_1_";
        presence.smallImageText = "Version: " + KamiMod.VERSION;
        presence.startTimestamp = System.currentTimeMillis() / 1000L;

        rpc.Discord_UpdatePresence(DiscordPresence.presence);
        StopWatch timer = new StopWatch();
        timer.reset();
        thread = new Thread(() ->
        {
            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    //noinspection BusyWait
                    Thread.sleep(2000);
                } catch (InterruptedException ignored)
                {
                    Thread.currentThread().interrupt();
                    return;
                }
                rpc.Discord_RunCallbacks();
                presence.state = getState();
                presence.largeImageKey = getImageKey();
                rpc.Discord_UpdatePresence(DiscordPresence.presence);
            }
        }, "RPC-Callback-Handler");
        thread.setDaemon(true);
        thread.start();
    }

    public static String getState()
    {
        String sn0wUserSuffix = "";
//        if (BotManager.INSTANCE.sn0wUserArrayList != null)
//        {
//            sn0wUserSuffix = " with " + BotManager.INSTANCE.sn0wUserArrayList.size() + " other sn0w users";
//        }
        String playing = mc.player == null
                ? "In the menus"
                : mc.isIntegratedServerRunning()
                ? "Lonely in singleplayer"
                : "Playing multiplayer";

        return playing + sn0wUserSuffix + ".";
    }

    public static synchronized void stop()
    {
        System.out.println("Shutting down Discord RPC");
        if (thread != null && !thread.isInterrupted())
        {
            thread.interrupt();
            thread = null;
        }

        rpc.Discord_Shutdown();
    }

    public static String getImageKey()
    {
        switch (RPC.INSTANCE.image.getValue())
        {
            case "Animals":
                switch (MathUtil.randomInt(1, 7))
                {
                    case 1:
                        return "cato";
                    case 2:
                        return "samoyed2";
                    case 3:
                        return "laptopcat";
                    case 4:
                        return "moneycat";
                    case 5:
                        return "moneycat2";
                    case 6:
                        return "moneycat3";
                    case 7:
                        return "samoyed1";
                }
                break;
            case "Sn0wIcon":
                return "cop26logo_cropped_1_";
            case "Grails":
                switch (MathUtil.randomInt(1, 6))
                {
                    case 1:
                        return "lelcopter";
                    case 2:
                        return "nfttim";
                    case 3:
                        return "phonto";
                    case 4:
                        return "lole1";
                    case 5:
                        return "grail1";
                    case 6:
                        return "grail2";
                }
                break;
        }
        return "cop26logo_cropped_1_";
    }
}