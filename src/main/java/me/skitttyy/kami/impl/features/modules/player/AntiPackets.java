package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.move.LookEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.mixin.accessor.IPlayerMoveC2SPacket;
import me.skitttyy.kami.mixin.accessor.IPlayerPositionLookS2CPacket;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.Identifier;

public class AntiPackets extends Module {
    public static AntiPackets INSTANCE;

    public AntiPackets()
    {
        super("AntiPackets", Category.Player);
        INSTANCE = this;
    }

    public Value<Boolean> serverRotate = new ValueBuilder<Boolean>()
            .withDescriptor("NoRotate")
            .withValue(false)
            .register(this);
    public Value<Boolean> adjust = new ValueBuilder<Boolean>()
            .withDescriptor("Adjust")
            .withValue(true)
            .withParentEnabled(true)
            .withParent(serverRotate)
            .register(this);
    public Value<Boolean> noHandShake = new ValueBuilder<Boolean>()
            .withDescriptor("Fake Vanilla")
            .withValue(false)
            .register(this);
    public Value<Boolean> slot = new ValueBuilder<Boolean>()
            .withDescriptor("No Slot")
            .withValue(false)
            .register(this);
    private float yaw, pitch;
    private boolean cancelRotate;
    public Timer timer = new Timer();

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (slot.getValue() && event.getPacket() instanceof UpdateSelectedSlotS2CPacket) {
            event.setCancelled(true);
            PacketManager.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }

    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;

        if (noHandShake.getValue())
        {
            if (event.getPacket() instanceof CustomPayloadC2SPacket packet)
            {
                final CustomPayload payload = packet.payload();
                final Identifier identifier = payload.getId().id();

                if (identifier.getPath().equals("brand"))
                {
                    event.setCancelled(true);
                    PacketManager.INSTANCE.sendQuietPacket(new CustomPayloadC2SPacket(new BrandCustomPayload("vanilla")));
                    return;
                }

                if (identifier.getNamespace().equals("fabric") && !mc.isInSingleplayer())
                {
                    event.setCancelled(true);
                }
            }
        }
    }

//    @SubscribeEvent
//    public void onServerRotation(LookEvent.RotationSetEvent event)
//    {
//        event.setCancelled(true);
//        if (adjust.getValue())
//        {
//            float yaw = RotationManager.INSTANCE.getServerYaw();
//            float pitch = RotationManager.INSTANCE.getServerPitch();
//            if (RotationManager.INSTANCE.isRotating())
//            {
//                yaw = RotationManager.INSTANCE.getRotationYaw();
//                pitch = RotationManager.INSTANCE.getRotationPitch();
//            }
//            event.setYaw(yaw);
//            event.setPitch(pitch);
//        }
//    }
    @Override
    public String getDescription()
    {
        return "AntiPackets: Saves u from all sorts of malicious packets except the ones with all your info thats being sent to me!";
    }
}