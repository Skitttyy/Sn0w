package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.mixin.accessor.IMinecraftClient;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;

public class AutoFish extends Module {

    Value<Boolean> cast = new ValueBuilder<Boolean>()
            .withDescriptor("Cast")
            .withValue(true)
            .register(this);
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(20)
            .withRange(0, 25)
            .withPlaces(0)
            .register(this);
    public AutoFish()
    {
        super("AutoFish", Category.Misc);
    }

    private boolean autoReel;
    private int autoReelTicks;
    //
    private int autoCastTicks;

    Timer timer = new Timer();


    @SubscribeEvent
    public void onPacketInbound(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof PlaySoundS2CPacket packet
                && packet.getSound().value() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH
                && mc.player.getMainHandStack().getItem() == Items.FISHING_ROD)
        {
            FishingBobberEntity fishHook = mc.player.fishHook;
            if (fishHook == null || fishHook.getPlayerOwner() != mc.player)
            {
                return;
            }
            double dist = fishHook.squaredDistanceTo(packet.getX(),
                    packet.getY(), packet.getZ());
            if (dist <= 4)
            {
                autoReel = true;
                autoReelTicks = 4;
            }
        }
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (mc.player.getMainHandStack().getItem() != Items.FISHING_ROD)
        {
            return;
        }
        FishingBobberEntity fishHook = mc.player.fishHook;
        if ((fishHook == null || fishHook.getHookedEntity() != null) && autoCastTicks <= 0)
        {
            ((IMinecraftClient) mc).callDoItemUse();
            autoCastTicks = delay.getValue().intValue();
            return;
        }
        if (autoReel)
        {
            if (autoReelTicks <= 0)
            {
                ((IMinecraftClient) mc).callDoItemUse();
                autoReel = false;
                return;
            }
            autoReelTicks--;
        }
        autoCastTicks--;
    }


    @Override
    public String getHudInfo()
    {
        return "Splash";
    }


    @Override
    public String getDescription()
    {
        return "AutoFish: auto reels in fishing rod when there is a fish";
    }

}
