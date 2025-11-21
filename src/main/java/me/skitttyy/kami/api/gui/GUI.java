package me.skitttyy.kami.api.gui;

import lombok.Getter;
import me.skitttyy.kami.api.gui.component.IComponent;
import me.skitttyy.kami.api.gui.context.Context;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.gui.helpers.MouseHelper;
import me.skitttyy.kami.api.gui.helpers.Rect;
import me.skitttyy.kami.api.gui.misc.SearchBar;
import me.skitttyy.kami.api.gui.render.IRenderer;
import me.skitttyy.kami.api.gui.theme.IColorScheme;
import me.skitttyy.kami.api.gui.theme.IMetrics;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


import java.awt.*;

public class GUI extends Screen implements IMinecraft {

    @Getter
    Context context;


    public GUI(Context context)
    {
        super(Text.of("Sn0w"));
        this.context = context;
        addComponents();
        searchBar = new SearchBar(new Rect(1, 1, 100, 12));

    }
    Timer typingTimer = new Timer();
    public static boolean typeCounter = false;
    public static SearchBar searchBar = null;
    public static int bindCounter = 0;



    public void addComponents()
    {
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        if (typingTimer.isPassed(500))
        {
            typeCounter = !typeCounter;
            typingTimer.resetDelay();

            if(bindCounter == 3){
                bindCounter = 0;
            }
            bindCounter++;
        }
        context.setDrawContext(drawContext);
        MouseHelper mouse = new MouseHelper(mouseX, mouseY);
        context.setHelper(mouse);
        context.setPartialTicks(delta);
        getContext().getRenderer().renderBackground(getContext());


        //Watermark

        Fonts.renderText(drawContext, "Sn0w " + KamiMod.VERSION + " by Skitttyy + (" + KamiMod.HASH + ")", 3, context.getScaledResolution().getScaledHeight() - 12, ColorUtil.newAlpha(Color.WHITE, 100), true);


        for (IComponent component : context.getComponents())
        {
            if (component.isActive()) component.draw(context, mouse);


            drawContext.draw();
        }
        getContext().getRenderer().renderLast(getContext());

        drawContext.draw();
        //render the searchbar
        searchBar.onRender(context, mouse);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double horizontal)
    {
        MouseHelper mouse = new MouseHelper((int) mouseX, (int) mouseY);

        if (context.getHovering(mouse) != null)
        {
            context.getHovering(mouse).getDims().setY(context.getHovering(mouse).getDims().getY() + (int) horizontal * 10);
        }

        return super.mouseScrolled(mouseX, mouseY, amount, horizontal);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        MouseHelper mouse = new MouseHelper((int) mouseX, (int) mouseY);
        IComponent hovered = context.getHovering(mouse);
        if (hovered != null)
        {
            if (hovered.isActive()) hovered.click(context, mouse, button);
        }
        searchBar.click(mouse, button);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        MouseHelper mouse = new MouseHelper((int) mouseX, (int) mouseY);
        for (IComponent component : context.getComponents())
        {
            if (component.isActive()) component.release(context, mouse, button);
        }


        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        for (IComponent component : context.getComponents())
        {
            if (component.isActive()) component.key(context, keyCode, (char) keyCode);
        }
        searchBar.key(keyCode);

        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    public void onCharTyped(char character)
    {
        for (IComponent component : context.getComponents())
        {
            if (component.isActive()) component.charTyped(context, character);
        }

        searchBar.charTyped(character);
    }

    public void updateGUI(IColorScheme colorScheme, IMetrics metrics, IRenderer renderer)
    {
        context.setColorScheme(colorScheme);
        context.setMetrics(metrics);
        context.setRenderer(renderer);
    }


    public void enterGui(IColorScheme colorScheme, IMetrics metrics, IRenderer renderer)
    {
        context.setColorScheme(colorScheme);
        context.setMetrics(metrics);
        context.setRenderer(renderer);
        searchBar = new SearchBar(new Rect(1, 1, 100, 12));
        mc.setScreen(this);
    }

    @Override
    public boolean shouldPause()
    {
        return false;
    }
}
