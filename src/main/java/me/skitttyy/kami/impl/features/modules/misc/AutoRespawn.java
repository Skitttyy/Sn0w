package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.ScreenEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.mixin.accessor.IDeathScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;

import java.awt.*;

public class AutoRespawn extends Module {


    public AutoRespawn() {
        super("AutoRespawn", Category.Misc);
    }

    Value<Number> time = new ValueBuilder<Number>()
            .withDescriptor("Time")
            .withValue(1)
            .withRange(0.1, 5)
            .withPlaces(1)
            .register(this);

    @SubscribeEvent
    public void onGuiOpen(ScreenEvent.SetScreen event) {
        if (event.getGuiScreen() instanceof DeathScreen)
            event.setGuiScreen(new ScreenAutoRespawn((IDeathScreen) event.getGuiScreen()));
    }


    public class ScreenAutoRespawn extends DeathScreen {
        private final long finalTime;

        public ScreenAutoRespawn(IDeathScreen screen) {
            super(screen.getDeathMessage(), screen.isScreenHardcore());
            finalTime = System.currentTimeMillis() + (time.getValue().longValue() * 1000);
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);

            float time = finalTime - System.currentTimeMillis();


            if(mc.player != null)
            {
                if (time <= 0) mc.player.requestRespawn();
            }

            float timeLeft = MathUtil.round((time / 1000.0f), 1);
            if (timeLeft >= 0) {
                String text = "Respawning in " + timeLeft + "s";

                context.drawText(mc.textRenderer, text, width / 2 - mc.textRenderer.getWidth(text) / 2, 8, Color.WHITE.getRGB(), true);
            }
        }
    }

    @Override
    public String getDescription()
    {
        return "AutoRespawn: respawns for you when you die";
    }

}
