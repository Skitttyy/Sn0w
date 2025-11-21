package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.ParticleEvent;
import me.skitttyy.kami.api.event.events.render.RenderFogEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.ExplosionEmitterParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;

import java.util.HashMap;
import java.util.WeakHashMap;

public class NoRender extends Module
{

    public static NoRender INSTANCE;

    public Value<Boolean> blockInside = new ValueBuilder<Boolean>()
            .withDescriptor("Block Inside")
            .withValue(false)
            .register(this);
    public Value<Boolean> fire = new ValueBuilder<Boolean>()
            .withDescriptor("Fire")
            .withValue(false)
            .register(this);
    public Value<Boolean> armor = new ValueBuilder<Boolean>()
            .withDescriptor("Armor")
            .withValue(false)
            .register(this);
    public Value<Boolean> portal = new ValueBuilder<Boolean>()
            .withDescriptor("Swirls")
            .withValue(false)
            .register(this);
    public Value<Boolean> pumpkin = new ValueBuilder<Boolean>()
            .withDescriptor("Pumpkin")
            .withValue(false)
            .register(this);
    public Value<Boolean> potionGui = new ValueBuilder<Boolean>()
            .withDescriptor("Potion Gui")
            .withValue(false)
            .register(this);
    public Value<Boolean> advancements = new ValueBuilder<Boolean>()
            .withDescriptor("Advancements")
            .withValue(false)
            .register(this);
    public Value<Boolean> skyLight = new ValueBuilder<Boolean>()
            .withDescriptor("SkyLight")
            .withValue(false)
            .register(this);
    public Value<Boolean> enchantmentTable = new ValueBuilder<Boolean>()
            .withDescriptor("Enchant Tables")
            .withValue(false)
            .register(this);

    public Value<Boolean> noHurtCam = new ValueBuilder<Boolean>()
            .withDescriptor("Hurt Cam")
            .withValue(false)
            .register(this);
    public Value<Boolean> explosions = new ValueBuilder<Boolean>()
            .withDescriptor("Explosions")
            .withValue(false)
            .register(this);
    public Value<Boolean> liquidVision = new ValueBuilder<Boolean>()
            .withDescriptor("LiquidVision")
            .withValue(false)
            .register(this);
    Value<Boolean> fireworks = new ValueBuilder<Boolean>()
            .withDescriptor("Fireworks")
            .withValue(false)
            .register(this);
    public Value<Boolean> frost = new ValueBuilder<Boolean>()
            .withDescriptor("Frostbite")
            .withValue(false)
            .register(this);
    public Value<Boolean> campFire = new ValueBuilder<Boolean>()
            .withDescriptor("Camp Fires")
            .withValue(false)
            .register(this);
    public Value<Boolean> noFog = new ValueBuilder<Boolean>()
            .withDescriptor("Fog")
            .withValue(false)
            .register(this);
    public Value<String> boss = new ValueBuilder<String>()
            .withDescriptor("BossBar")
            .withValue("None")
            .withModes("None", "Normal", "Stack")
            .register(this);
    public Value<Boolean> itemFrame = new ValueBuilder<Boolean>()
            .withDescriptor("Frames")
            .withValue(false)
            .register(this);
    public Value<Boolean> totem = new ValueBuilder<Boolean>()
            .withDescriptor("Totem Pops")
            .withValue(false)
            .register(this);


    public NoRender()
    {
        super("NoRender", Category.Render);
        INSTANCE = this;
    }

    public static final WeakHashMap<ClientBossBar, Integer> barMap = new WeakHashMap<>();

    @SubscribeEvent
    public void onParticle(ParticleEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (explosions.getValue() && (event.getParticle() instanceof ExplosionLargeParticle || event.getParticle() instanceof ExplosionEmitterParticle)
                || fireworks.getValue() &&(event.getParticle() instanceof FireworksSparkParticle.FireworkParticle || event.getParticle() instanceof FireworksSparkParticle.Flash)
                || campFire.getValue() && event.getParticle() instanceof CampfireSmokeParticle)
        {
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onRenderFog(RenderFogEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (mc.player.isSubmergedIn(FluidTags.LAVA))
        {
            if (liquidVision.getValue())
                event.setCancelled(true);
        } else if (noFog.getValue())
        {
            event.setCancelled(true);
        }

    }


    @SubscribeEvent
    private void onBossText(RenderGameOverlayEvent.BossBar.Text event)
    {
        if (NullUtils.nullCheck()) return;

        if (barMap.isEmpty() || !boss.getValue().equals("Stack")) return;

        ClientBossBar bar = event.bossBar;
        Integer integer = barMap.get(bar);
        barMap.remove(bar);
        if (integer != null) event.name = event.name.copy().append(" x" + integer);
    }

    @SubscribeEvent
    private void onBossIterator(RenderGameOverlayEvent.BossBar.Iterate event)
    {
        if (NullUtils.nullCheck()) return;


        if (boss.getValue().equals("Stack"))
        {
            HashMap<String, ClientBossBar> chosenBarMap = new HashMap<>();
            event.iterator.forEachRemaining(bar ->
            {
                String name = bar.getName().getString();
                if (chosenBarMap.containsKey(name))
                {
                    barMap.compute(chosenBarMap.get(name), (clientBossBar, integer) -> (integer == null) ? 2 : integer + 1);
                } else
                {
                    chosenBarMap.put(name, bar);
                }
            });
            event.iterator = chosenBarMap.values().iterator();
        }
    }





    @Override
    public String getDescription()
    {
        return "NoRender: stops rendering various things";
    }
}
