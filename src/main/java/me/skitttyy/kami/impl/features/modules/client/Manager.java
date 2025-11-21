package me.skitttyy.kami.impl.features.modules.client;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.key.CharTypeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.GUI;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.gui.hudeditor.HudEditorGUI;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.hud.*;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.util.Formatting;

import java.awt.*;

public class Manager extends Module {


    public static Manager INSTANCE;
    public Value<String> clientName = new ValueBuilder<String>()
            .withDescriptor("Client Name")
            .withValue("SN0WUNICODE")
            .register(this);
    public Value<Boolean> indicator = new ValueBuilder<Boolean>()
            .withDescriptor("Indicator")
            .withValue(true)
            .register(this);
    public Value<Boolean> bold = new ValueBuilder<Boolean>()
            .withDescriptor("Bold")
            .withValue(true)
            .register(this);
    public Value<String> chatNotifyMode = new ValueBuilder<String>()
            .withDescriptor("Toggle Style")
            .withValue("Sn0w")
            .withModes("Sn0w", "Sn0w2", "Sn0w3", "DotGod", "NoPrefix", "ForgeHax", "ForgeHaxReal", "Aurora")
            .register(this);


    //i know this is annoying :(
    public Value<String> mainChatColor = new ValueBuilder<String>()
            .withDescriptor("Main Chat")
            .withValue("Aqua")
            .withModes("Aqua", "Black", "Blue", "Dark Aqua", "Dark Blue", "Dark Green", "Dark Gray", "Dark Purple", "Dark Red", "Dark Yellow", "Gold", "Gray", "Green", "Light Purple", "Red", "White", "Yellow", "Custom")
            .register(this);
    public Value<String> accent = new ValueBuilder<String>()
            .withDescriptor("Accent Chat")
            .withValue("Blue")
            .withModes("Aqua", "Black", "Blue", "Dark Aqua", "Dark Blue", "Dark Green", "Dark Gray", "Dark Purple", "Dark Red", "Dark Yellow", "Gold", "Gray", "Green", "Light Purple", "Red", "White", "Yellow", "Custom")
            .register(this);
    public Value<Boolean> hiddenModules = new ValueBuilder<Boolean>()
            .withDescriptor("Hidden")
            .withValue(false)
            .register(this);

    public Color COPIED_COLOR = null;


    public Manager()
    {
        super("Manager", Category.Client);
        INSTANCE = this;
        setEnabled(true);
        KamiMod.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void draw(RenderGameOverlayEvent.Text event)
    {
        if (NullUtils.nullCheck()) return;

        if (clientName.getValue().equals("SN0WUNICODE")) {
            KamiMod.NAME = KamiMod.NAME_UNICODE;
        } else {
            KamiMod.NAME = clientName.getValue();
        }
//
//        if (autoPos.getValue())
//        {
//            ScaledResolution resolution = new ScaledResolution(mc);
//            int resWidth = resolution.getScaledWidth();
//            int resHeight = resolution.getScaledHeight();
//
//            float bottom = resHeight - ClickGui.CONTEXT.getRenderer().getTextHeight("pPMCSWAG");
//            Watermark.INSTANCE.yPos.setValue(1);
//            Watermark.INSTANCE.xPos.setValue(1);
//
//            TextRadar.INSTANCE.yPos.setValue(1 + Fonts.getTextHeight("A"));
//            TextRadar.INSTANCE.xPos.setValue(1);
//
//            FeatureList.INSTANCE.xPos.setValue(resWidth - 1);
//            FeatureList.INSTANCE.yPos.setValue(1);
//            Info.INSTANCE.xPos.setValue(resWidth - 1);
//            Info.INSTANCE.yPos.setValue(bottom - 1);
//            Coords.INSTANCE.xPos.setValue(1);
//            Coords.INSTANCE.yPos.setValue(bottom - 1);
//
//
//        }
    }

    @SubscribeEvent
    public void onCharTyped(CharTypeEvent event)
    {
        if (mc.currentScreen == null) return;

        if (mc.currentScreen instanceof GUI gui)
        {
            gui.onCharTyped(event.getCharacter());
        } else if (mc.currentScreen instanceof HudEditorGUI gui)
        {
            gui.onCharTyped(event.getCharacter());
        }

    }


    public String getMainColor()
    {
        if(mainChatColor.getValue().equals("Custom"))
            return "§s";
        return getFormatColor() + "";
    }
    public Formatting getFormatColor()
    {
        switch (mainChatColor.getValue())
        {
            case "Black":
                return Formatting.BLACK;
            case "Dark Blue":
                return Formatting.DARK_BLUE;
            case "Dark Green":
                return Formatting.DARK_GREEN;
            case "Dark Aqua":
                return Formatting.DARK_AQUA;
            case "Dark Red":
                return Formatting.DARK_RED;
            case "Dark Purple":
                return Formatting.DARK_PURPLE;
            case "Dark Gray":
                return Formatting.DARK_GRAY;
            case "Gold":
                return Formatting.GOLD;
            case "Gray":
                return Formatting.GRAY;
            case "Blue":
                return Formatting.BLUE;
            case "Green":
                return Formatting.GREEN;
            case "Aqua":
                return Formatting.AQUA;
            case "Red":
                return Formatting.RED;
            case "Light Purple":
                return Formatting.LIGHT_PURPLE;
            case "Yellow":
                return Formatting.YELLOW;
            case "White":
                return Formatting.WHITE;
            default:
                return Formatting.AQUA;
        }
    }


    public String getAccent()
    {
        if(accent.getValue().equals("Custom"))
            return "§s";
        return getAccentColor() + "";
    }

    public Formatting getAccentColor()
    {
        switch (accent.getValue())
        {
            case "Black":
                return Formatting.BLACK;
            case "Dark Blue":
                return Formatting.DARK_BLUE;
            case "Dark Green":
                return Formatting.DARK_GREEN;
            case "Dark Aqua":
                return Formatting.DARK_AQUA;
            case "Dark Red":
                return Formatting.DARK_RED;
            case "Dark Purple":
                return Formatting.DARK_PURPLE;
            case "Dark Gray":
                return Formatting.DARK_GRAY;
            case "Gold":
                return Formatting.GOLD;
            case "Aqua":
                return Formatting.AQUA;
            case "Gray":
                return Formatting.GRAY;
            case "Blue":
                return Formatting.BLUE;
            case "Green":
                return Formatting.GREEN;
            case "Red":
                return Formatting.RED;
            case "Light Purple":
                return Formatting.LIGHT_PURPLE;
            case "Yellow":
                return Formatting.YELLOW;
            case "White":
                return Formatting.WHITE;

            default:
                return Formatting.BLUE;
        }
    }


    @Override
    public void onEnable()
    {

    }

    @Override
    public void onDisable()
    {
        this.setEnabled(true);
    }


    @Override
    public String getDescription()
    {
        return "Manager: manages the client. this is important for the client to functions";
    }
}
