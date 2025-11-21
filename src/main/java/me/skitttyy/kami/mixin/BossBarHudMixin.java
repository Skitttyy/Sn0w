package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.impl.features.modules.render.NoRender;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Iterator;

@Mixin(BossBarHud.class)
public class BossBarHudMixin
{
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo info)
    {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.boss.getValue().equals("None")) info.cancel();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    public Iterator<ClientBossBar> onRender(Collection<ClientBossBar> collection)
    {
        RenderGameOverlayEvent.BossBar.Iterate event = new RenderGameOverlayEvent.BossBar.Iterate(collection.iterator());
        event.post();
        return event.getIterator();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;getName()Lnet/minecraft/text/Text;"))
    public Text onAsFormattedString(ClientBossBar clientBossBar)
    {
        RenderGameOverlayEvent.BossBar.Text event = new RenderGameOverlayEvent.BossBar.Text(clientBossBar.getName(), clientBossBar);
        event.post();
        return event.name;
    }

}