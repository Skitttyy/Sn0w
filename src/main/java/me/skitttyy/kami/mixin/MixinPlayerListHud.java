package me.skitttyy.kami.mixin;

import me.skitttyy.kami.impl.features.modules.render.ExtraTab;
import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(PlayerListHud.class)
public abstract class MixinPlayerListHud {
    @Redirect(method = "collectPlayerEntries", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;limit(J)Ljava/util/stream/Stream;"))
    public Stream subList(Stream instance, long l) {
        return instance.limit(ExtraTab.INSTANCE.isEnabled() ? 250 : l);
    }
}