package me.skitttyy.kami.mixin.accessor;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.DisconnectionInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisconnectedScreen.class)
public interface IDisconnectedScreen {
    @Accessor("parent")
    Screen getParent();


    @Accessor("info")
    DisconnectionInfo getDisconnectionInfo();

}
