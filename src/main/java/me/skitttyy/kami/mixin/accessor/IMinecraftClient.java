package me.skitttyy.kami.mixin.accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.net.Proxy;

@Mixin(MinecraftClient.class)
public interface IMinecraftClient {
    @Accessor("currentFps")
    static int getFps()
    {
        return 0;
    }

    @Mutable
    @Accessor("session")
    void setSession(Session session);

    @Accessor("networkProxy")
    Proxy getProxy();

    @Accessor("itemUseCooldown")
    int getItemUseCooldown();
    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int itemUseCooldown);
    @Invoker
    boolean callDoAttack();
 
    @Invoker
    void callDoItemUse();

    @Accessor("resourceReloadLogger")
    ResourceReloadLogger getResourceReloadLogger();

    @Accessor("renderTickCounter")
    RenderTickCounter.Dynamic getRenderTickCounter();


    @Invoker("doAttack")
    boolean leftClick();
}
