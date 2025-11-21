package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.ScreenEvent;
import me.skitttyy.kami.api.feature.module.Module;
import net.minecraft.client.gui.screen.DeathScreen;

public class Undead extends Module {
    public static Undead INSTANCE;
    private boolean isDead;

    public Undead() {
        super("Undead", Category.Player);
        INSTANCE = this;
    }


    @SubscribeEvent
    public void onGuiOpened(ScreenEvent.SetScreen event) {
        if (event.getGuiScreen() instanceof DeathScreen) {
            event.setGuiScreen(null);
            isDead = true;
            mc.player.setHealth(20);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (isDead) {
            mc.player.requestRespawn();
            isDead = false;
        }
    }

    @Override
    public String getDescription() {
        return "Undead: lets you walk around while dead, some servers let u do this";
    }
}