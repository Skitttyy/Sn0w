 package me.skitttyy.kami.impl;

import baritone.api.BaritoneAPI;
import me.skitttyy.kami.api.event.eventbus.EventManager;
import me.skitttyy.kami.api.management.SavableManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.launch.MixinBootstrap;

import javax.naming.CommunicationException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Logger;

public class KamiMod implements ModInitializer
{
    public static Logger LOGGER = Logger.getLogger("sn0w");

    public static long START_TIME = System.currentTimeMillis();

    public static String NAME = "Sn0w";
    public final static String VERSION = "2.1.2+2";

    /**
     * Git hash of the client
     */
    public static final String HASH = "be537a8c5b5a9600";

    /**
     * The snowflake text ❄
     * In chat this is ❅
     */
    public static String NAME_UNICODE = "❅";

    /**
     * Event System
     */
    public static EventManager EVENT_BUS;
    public static boolean BARITONE_AVAILABLE;

    @Override
    public void onInitialize()
    {
        BARITONE_AVAILABLE = exists("baritone.api.BaritoneAPI");

        LOGGER.info(KamiMod.NAME_UNICODE + "Sn0w (" + VERSION + ")");
        Register.INSTANCE = new Register();
        Register.INSTANCE.registerAll();

        SavableManager.INSTANCE.load();
        ShutdownHook.setup();
        System.out.println(getClass().getClassLoader());
        System.out.println(MixinBootstrap.class.getClassLoader());
    }

    public static boolean isBaritonePaused()
    {
        if (!KamiMod.BARITONE_AVAILABLE) return false;

        return BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing();
    }

    static class ShutdownHook extends Thread
    {
        public static void setup()
        {
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        }

        @Override
        public void run()
        {
            super.run();
            try
            {
                SavableManager.INSTANCE.save();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static boolean exists(String name)
    {
        try
        {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e)
        {
            return false;
        }
    }

}
