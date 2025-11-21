package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.KamiMod;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Announcer extends Module {


    public static Announcer INSTANCE;

    public Announcer()
    {
        super("Announcer", Category.Misc);
        INSTANCE = this;
    }

    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Self")
            .withModes("Self", "Chat", "Whisper")
            .register(this);
    Value<Boolean> breakBlock = new ValueBuilder<Boolean>()
            .withDescriptor("Break")
            .withValue(true)
            .register(this);
    Value<Boolean> place = new ValueBuilder<Boolean>()
            .withDescriptor("Place")
            .withValue(true)
            .register(this);
    Value<Boolean> eat = new ValueBuilder<Boolean>()
            .withDescriptor("Eat")
            .withValue(true)
            .register(this);
    Value<Boolean> walk = new ValueBuilder<Boolean>()
            .withDescriptor("Walk")
            .withValue(true)
            .register(this);
    Value<Boolean> joins = new ValueBuilder<Boolean>()
            .withDescriptor("Joins")
            .withValue(true)
            .register(this);
    Value<Boolean> timeMessages = new ValueBuilder<Boolean>()
            .withDescriptor("World Time")
            .withValue(true)
            .register(this);

    public static String[] breakMessages = {"I just mined {amount} {name} thanks to Sn0w!", "\u042F \u0442\u043E\u043B\u044C\u043A\u043E \u0447\u0442\u043E \u0434\u043E\u0431\u044B\u043B {amount} {name} \u0431\u043B\u043E\u043A\u0430 \u0431\u043B\u0430\u0433\u043E\u0434\u0430\u0440\u044F Sn0w!"};
    public static String[] placeMessages = {"I just built a castle made out of {amount} {name} thanks to Sn0w!", "\u042F \u0442\u043E\u043B\u044C\u043A\u043E \u0447\u0442\u043E \u043F\u043E\u0441\u0442\u0440\u043E\u0438\u043B \u0437\u0430\u043C\u043E\u043A \u0438\u0437 {amount} {name} \u0431\u043B\u0430\u0433\u043E\u0434\u0430\u0440\u044F Sn0w!"};
    public static String[] eatMessages = {"I just ate {amount} {name} thanks to Sn0w!", "\u042F \u0442\u043E\u043B\u044C\u043A\u043E \u0447\u0442\u043E \u0441\u044A\u0435\u043B {amount} {name} \u0431\u043B\u0430\u0433\u043E\u0434\u0430\u0440\u044F Sn0w!"};
    public static String[] walkMessages = {"I just magically teleported {blocks} blocks thanks to Sn0w!", "\u042F \u043F\u0440\u043E\u0441\u0442\u043E \u0432\u043E\u043B\u0448\u0435\u0431\u043D\u044B\u043C \u043E\u0431\u0440\u0430\u0437\u043E\u043C \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442\u0438\u0440\u043E\u0432\u0430\u043B {blocks} \u0431\u043B\u043E\u043A\u043E\u0432 \u0431\u043B\u0430\u0433\u043E\u0434\u0430\u0440\u044F Sn0w!"};
    public static String[] joinMessages = {"Welcome to %serverip% %name%!", "Welcome to hell, %name%.", "Welcome %name% everyone!", "Hey, %name%", "AbdulaSalaha blesses you, %name%", "Greetings, %name%"};
    public static String[] leaveMessages = {"Goodbye %name%!", "Cya %name%", "Good to see you, %name%", "Catch ya later, %name%", "Farewell, %name%"};

    public static String[] morningMessages = {"I survived another night!", "Good Morning!", "You survived another night!", "The sun is rising in the east, hurrah, hurrah!"};
    public static String[] noonMessages = {"Let's go tanning!", "Let's go to the beach!", "Enjoy the sun outside! It is currently very bright!", "It's the brightest time of the day!"};
    public static String[] afterNoonMessages = {"IT'S HIGH NOON! is what ive would of said if i used future client", "Good afternoon!"};
    public static String[] sunsetMessages = {"You can say \"Sunset has now ended! You may eat your lunch now if you are a muslim.\" if you use futureshit"};
    public static String[] midNightMessages = {"It's so dark outside...", "Its scary out there!"};
    public static String[] nightMessages = {"Let's get comfy!", "Netflix and chill!", "You survived another day!"};
    public static String[] bedtimeMessages = {"BEDTIME NOW!!!", "if ur name is mcswag bedtime is IMMEDIATE.", "You may now sleep!"};
    public static String[] dayLightMessages = {"Good bye, zombies!", "All monsters will be smited down thanks to my cheat."};

    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay", "delay")
            .withValue(5)
            .withRange(1, 15)
            .register(this);
    public Queue<String> messages = new LinkedList<>();

    public static int blockBrokeDelay = 0;
    static int blockPlacedDelay = 0;
    static int jumpDelay = 0;
    static int attackDelay = 0;
    static int eattingDelay = 0;
    static long lastPositionUpdate;
    static double lastPositionX;
    static double lastPositionY;
    static double lastPositionZ;
    private static double speed;
    Timer timer = new Timer();
    Timer timeCooldown = new Timer();

    String heldItem = "";
    int blocksPlaced = 0;
    int blocksBroken = 0;
    int eaten = 0;
    Random random = new Random();

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof PlayerInteractItemC2SPacket && mc.player.getMainHandStack().getItem() instanceof BlockItem)
        {
            if (!place.getValue()) return;

            blocksPlaced++;
            int randomNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);
            if (blockPlacedDelay >= 150 * delay.getValue().intValue())
            {
                if (blocksPlaced > randomNum)
                {
                    String msg = placeMessages[random.nextInt(placeMessages.length)].replace("{amount}", "" + blocksPlaced).replace("{name}", mc.player.getMainHandStack().getName().getString());

                    sendMessage(msg);
                    blocksPlaced = 0;
                    blockPlacedDelay = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof PlayerListS2CPacket packet)
        {
            if (packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER))
            {
                for (PlayerListS2CPacket.Entry entry : packet.getPlayerAdditionEntries())
                {
                    if (entry.profile() != null && !entry.profile().getName().equalsIgnoreCase(""))
                    {
                        if (!entry.profile().getName().equalsIgnoreCase(mc.getSession().getUsername()))
                        {
                            sendMessage(joinMessages[random.nextInt(joinMessages.length)].replace("%name%", entry.profile().getName()));
                        }
                    }
                }
            }
        }

        if (event.getPacket() instanceof PlayerRemoveS2CPacket packet)
        {
            for (UUID uuid : packet.profileIds)
            {
                PlayerListEntry entry = mc.player.networkHandler.getPlayerListEntry(uuid);
                if (entry != null && !entry.getProfile().getName().equalsIgnoreCase("") && !entry.getProfile().getName().equalsIgnoreCase(mc.getSession().getUsername()))
                {
                    sendMessage(leaveMessages[random.nextInt(leaveMessages.length)].replace("%name%", entry.getProfile().getName()));
                }

            }
        }

    }

    @SubscribeEvent
    public void onBlockBreak(LivingEvent.BreakBlock event)
    {
        if (NullUtils.nullCheck()) return;

        blocksBroken++;
        int randomNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);
        if (blockBrokeDelay >= 300 * delay.getValue().intValue())
        {
            if (blocksBroken > randomNum)
            {
                Random random = new Random();
                String msg = breakMessages[random.nextInt(breakMessages.length)]
                        .replace("{amount}", blocksBroken + "")
                        .replace("{name}", mc.world.getBlockState(event.getPos()).getBlock().getName().getString());

                sendMessage(msg);

                blocksBroken = 0;
                blockBrokeDelay = 0;
            }
        }
    }

    public void doTime()
    {
        if (!timeCooldown.isPassed(100)) return;

        if (mc.player.age < 200) return;

        if (mc.world.getTime() == 0)
        {
            sendMessage(morningMessages[random.nextInt(morningMessages.length)]);
            timeCooldown.resetDelay();
        }

        if (mc.world.getTime() == 4000)
        {
            sendMessage(noonMessages[random.nextInt(noonMessages.length)]);
            timeCooldown.resetDelay();

        }
        if (mc.world.getTime() == 6000)
        {
            sendMessage(afterNoonMessages[random.nextInt(afterNoonMessages.length)]);
            timeCooldown.resetDelay();

        }
        if (mc.world.getTime() == 13000)
        {
            sendMessage(bedtimeMessages[random.nextInt(bedtimeMessages.length)]);
            timeCooldown.resetDelay();

        }
        if (mc.world.getTime() == 14000)
        {
            sendMessage(nightMessages[random.nextInt(nightMessages.length)]);
            timeCooldown.resetDelay();

        }
        if (mc.world.getTime() == 16000)
        {
            sendMessage(sunsetMessages[random.nextInt(sunsetMessages.length)]);
            timeCooldown.resetDelay();

        }
        if (mc.world.getTime() == 18000)
        {
            sendMessage(midNightMessages[random.nextInt(midNightMessages.length)]);
            timeCooldown.resetDelay();

        }
        if (mc.world.getTime() == 23000)
        {
            sendMessage(dayLightMessages[random.nextInt(dayLightMessages.length)]);
            timeCooldown.resetDelay();

        }
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        blockBrokeDelay++;
        blockPlacedDelay++;
        jumpDelay++;
        attackDelay++;
        eattingDelay++;
        heldItem = mc.player.getMainHandStack().getName().getString();

//        if (walk.getValue()) {
        if (walk.getValue())
        {
            if (lastPositionUpdate + (5000L * delay.getValue().intValue()) < System.currentTimeMillis())
            {

                double d0 = lastPositionX - mc.player.getX();
                double d2 = lastPositionY - mc.player.getY();
                double d3 = lastPositionZ - mc.player.getZ();


                speed = Math.sqrt(d0 * d0 + d2 * d2 + d3 * d3);

                if (!(speed <= 1) && !(speed > 5000))
                {
                    String walkAmount = new DecimalFormat("0.00").format(speed);

                    Random random = new Random();
                    sendMessage(walkMessages[random.nextInt(walkMessages.length)].replace("{blocks}", "" + walkAmount));

                    lastPositionUpdate = System.currentTimeMillis();
                    lastPositionX = mc.player.getX();
                    lastPositionY = mc.player.getY();
                    lastPositionZ = mc.player.getZ();
                }
            }
        }
        if (!mode.getValue().equals("Self") && timer.isPassed(delay.getValue().intValue() * 1000L) && !messages.isEmpty())
        {
            if (mode.getValue().equals("Whisper"))
            {
                LivingEntity entity = TargetUtils.getTarget(100);
                if (entity != null)
                {
                    ChatUtils.sendChatMessage("/w " + entity.getName().getString() + " " + messages.poll());
                }
            } else
            {
                ChatUtils.sendChatMessage(messages.poll());
            }
            timer.resetDelay();
        }
        if (timeMessages.getValue())
        {
            doTime();
        }
    }


    @SubscribeEvent
    public void onEntityEat(LivingEvent.Eat event)
    {
        if (NullUtils.nullCheck()) return;


        if (!eat.getValue()) return;

        int randomNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);
        if (event.getStack().getComponents().contains(DataComponentTypes.FOOD))
        {
            eaten++;
            if (eattingDelay >= 300 * delay.getValue().intValue())
            {
                if (eaten > randomNum)
                {
                    Random random = new Random();
                    sendMessage(eatMessages[random.nextInt(eatMessages.length)].replace("{amount}", "" + eaten).replace("{name}", "" + event.getItem().getName().getString()));
                    eaten = 0;
                    eattingDelay = 0;
                }
            }
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (NullUtils.nullCheck()) return;


    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;


    }

    public void sendMessage(String message)
    {
        if (KamiMod.NAME != KamiMod.NAME_UNICODE)
        {
            message = message.replace("Sn0w", KamiMod.NAME);
        }
        String ip = (mc.isInSingleplayer() || mc.getNetworkHandler() == null) ? "Singleplayer" : mc.getNetworkHandler().getConnection().getAddress().toString().toLowerCase();

        message = message.replace("%serverip%", ip);


        if (mode.getValue().equals("Self"))
            ChatUtils.sendMessage(Formatting.AQUA + message);
        else
//            mc.player.connection.sendPacket(new CPacketChatMessage(message));
            messages.add(message);
    }

    @Override
    public String getDescription()
    {
        return "Announcer: Flex on sn0wless dogs with this module";
    }
}
