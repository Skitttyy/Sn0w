package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.render.animation.type.TimeAnimation;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import me.skitttyy.kami.mixin.accessor.IGameMessageS2CPacket;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Chat extends Module
{
    public static Chat INSTANCE;
    Value<Boolean> chatTimeStamps = new ValueBuilder<Boolean>()
            .withDescriptor("Chat Time")
            .withValue(false)
            .register(this);
    public Value<Boolean> bounce = new ValueBuilder<Boolean>()
            .withDescriptor("Bounce")
            .withValue(false)
            .register(this);
    public Value<Boolean> animations = new ValueBuilder<Boolean>()
            .withDescriptor("Animations")
            .withValue(false)
            .register(this);
    public Value<String> animationMode = new ValueBuilder<String>()
            .withDescriptor("Animation")
            .withValue("Slide")
            .withModes("Slide", "Fade")
            .withParent(animations)
            .withParentEnabled(true)
            .register(this);
    public Value<Number> animationSpeed = new ValueBuilder<Number>()
            .withDescriptor("Length")
            .withValue(150)
            .withRange(0, 1000)
            .withPlaces(0)
            .withParent(animations)
            .withParentEnabled(true)
            .register(this);

    public float percentComplete = 0.0f;
    public final Map<ChatHudLine, TimeAnimation> animationMap = new HashMap<>();

    public Chat()
    {
        super("Chat", Category.Render);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (event.getPacket() instanceof GameMessageS2CPacket packet)
        {
            if (chatTimeStamps.getValue())
            {
                ((IGameMessageS2CPacket) event.getPacket()).setContent(Text.of(Manager.INSTANCE.getAccent() + "<" + Manager.INSTANCE.getMainColor() + new SimpleDateFormat("k:mm").format(new Date()) + Manager.INSTANCE.getAccent() + ">" + Formatting.RESET + " ").copy().append(packet.content()));
            }
        }

    }

    @Override
    public String getDescription()
    {
        return "Chat: Modifies the chat in various ways to improve or show various things";
    }
}
