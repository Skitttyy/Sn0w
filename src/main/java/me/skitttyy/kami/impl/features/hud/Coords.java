package me.skitttyy.kami.impl.features.hud;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.gui.GUI;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.world.World;

import java.text.DecimalFormat;

public class Coords extends HudComponent {
    public static Coords INSTANCE;
    Value<Boolean> direction = new ValueBuilder<Boolean>()
            .withDescriptor("Direction")
            .withValue(true)
            .register(this);
    Value<Boolean> yaw = new ValueBuilder<Boolean>()
            .withDescriptor("Yaw")
            .withValue(false)
            .withParent(direction)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> colorDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Color Direction")
            .withValue(false)
            .withParent(direction)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> autoPos = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Pos")
            .withValue(true)
            .withAction(s ->
            {
                xPos.setActive(!s.getValue());
                yPos.setActive(!s.getValue());

            })
            .register(this);

    public Coords()
    {
        super("Coords");
        this.df = new DecimalFormat("#.#");
        INSTANCE = this;
    }

    DecimalFormat df;


    @SubscribeEvent
    @Override
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);



        if(autoPos.getValue())
        {
            ScaledResolution resolution = new ScaledResolution(mc);
            int resWidth = resolution.getScaledWidth();
            int resHeight = resolution.getScaledHeight();
            float bottom = resHeight - ClickGui.CONTEXT.getRenderer().getTextHeight("pPMCSWAG");
            Coords.INSTANCE.xPos.setValue(1);
            Coords.INSTANCE.yPos.setValue(bottom - 1);
        }

        if (NullUtils.nullCheck() || renderCheck(event)) return;

        if (mc.currentScreen instanceof GUI) return;


        int k = (mc.currentScreen instanceof ChatScreen) ? 14 : 0;

        String coords = getCoords();
        width = ClickGui.CONTEXT.getRenderer().getTextWidth(coords);
        height = -ClickGui.CONTEXT.getRenderer().getTextHeight(coords) * (direction.getValue() ? 2 : 1);
        Fonts.doOneText(event.getContext(),
                getFacing(mc.player.getHorizontalFacing().getName().toUpperCase()),
                xPos.getValue().floatValue() + 1,
                yPos.getValue().floatValue() - ClickGui.CONTEXT.getRenderer().getTextHeight("XYZ") - k,
                HudColors.getTextColor(yPos.getValue().intValue() - ClickGui.CONTEXT.getRenderer().getTextHeight("XYZ")),
                ClickGui.CONTEXT.getColorScheme().doesTextShadow()

        );
        Fonts.doOneText(event.getContext(),
                coords,
                xPos.getValue().floatValue() + 1,
                yPos.getValue().floatValue() - k,
                HudColors.getTextColor(yPos.getValue().intValue()),
                ClickGui.CONTEXT.getColorScheme().doesTextShadow()
        );
    }


    private String getFacing(final String in)
    {
        if (!direction.getValue()) return "";

        String gray = Formatting.DARK_GRAY + "";
        String white = Formatting.WHITE + "";
        String reset = Formatting.RESET + "";

        final String facing = getTitle(in);
        String add;
        if (in.equalsIgnoreCase("North"))
        {
            add = " " + reset + "[" + white + "-Z" + reset;
        } else if (in.equalsIgnoreCase("East"))
        {
            add = " " + reset + "[" + white + "+X" + reset;
        } else if (in.equalsIgnoreCase("South"))
        {
            add = " " + reset + "[" + white + "+Z" + reset;
        } else if (in.equalsIgnoreCase("West"))
        {
            add = " " + reset + "[" + white + "-X" + reset;
        } else
        {
            add = " ERROR";
        }
        if (yaw.getValue())
            add += ", " + white + MathHelper.floor(MathHelper.wrapDegrees(mc.player.getYaw()));

        add += reset + "]";
        return (colorDirection.getValue() ? "" : white) + facing + add;
    }

    public static String getTitle(String in)
    {
        in = Character.toUpperCase(in.toLowerCase().charAt(0)) + in.toLowerCase().substring(1);
        return in;
    }


    public String getCoords()
    {
        String reset = Formatting.RESET + "";
        String white = Formatting.WHITE + "";




        if(mc.world.getRegistryKey() == World.END)
            return "XYZ " + Formatting.WHITE + df.format(mc.player.getX()) + reset + ", " + white + df.format(mc.player.getY()) + reset + ", " + white + df.format(mc.player.getZ());

        if (mc.world.getRegistryKey() == World.NETHER)
            return "XYZ " + Formatting.WHITE + df.format(mc.player.getX()) + reset + ", " + white + df.format(mc.player.getY()) + reset + ", " + white + df.format(mc.player.getZ()) + reset + " [" + white + df.format((mc.player.getX()) * 8) + Formatting.RESET + ", " + Formatting.WHITE + df.format((mc.player.getZ()) * 8) + reset + "]";


        return "XYZ " + Formatting.WHITE + df.format(mc.player.getX()) + reset + ", " + white + df.format(mc.player.getY()) + reset + ", " + white + df.format(mc.player.getZ()) + reset + " [" + white + df.format(((mc.player.getX()) / 8)) + Formatting.RESET + ", " + Formatting.WHITE + df.format(((mc.player.getZ()) / 8)) + reset + "]";
    }

    @Override
    public String getDescription()
    {
        return "Coords: Display your location";
    }
}
