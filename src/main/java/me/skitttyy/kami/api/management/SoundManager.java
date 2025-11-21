package me.skitttyy.kami.api.management;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;


public class SoundManager implements IMinecraft {
    public static SoundManager INSTANCE;

    public SoundEvent CLICK_SOUND;
    public SoundEvent KILL_SOUND;

    public SoundManager()
    {
        INSTANCE = this;

        Identifier identifier = Identifier.of("kami:click");
        CLICK_SOUND = SoundEvent.of(identifier);
        Registry.register(Registries.SOUND_EVENT, identifier, CLICK_SOUND);

        identifier = Identifier.of("kami:neverlose");
        KILL_SOUND = SoundEvent.of(identifier);
        Registry.register(Registries.SOUND_EVENT, identifier, KILL_SOUND);

    }

    public void play(SoundEvent sound)
    {
        if (mc.player != null && mc.world != null)
            mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, 1f, 1f);
    }
}
