package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

public class PacketCanceller extends Module {
    public static PacketCanceller INSTANCE;

    public PacketCanceller()
    {
        super("PacketCanceller", Category.Player);
        INSTANCE = this;
    }

    public boolean hideText = false;
    public Value<Boolean> cancelPosition = new ValueBuilder<Boolean>()
            .withDescriptor("PlayerMoveC2SPacket.Position")
            .withValue(false)
            .register(this);
    public Value<Boolean> cancelLook = new ValueBuilder<Boolean>()
            .withDescriptor("PlayerMoveC2SPacket.Look")
            .withValue(false)
            .register(this);
    public Value<Boolean> cancelFull = new ValueBuilder<Boolean>()
            .withDescriptor("PlayerMoveC2SPacket.Full")
            .withValue(false)
            .register(this);
    Value<Boolean> lookOnGround = new ValueBuilder<Boolean>()
            .withDescriptor("PlayerMoveC2SPacket.LookAndOnGround")
            .withValue(false)
            .register(this);
    Value<Boolean> onGroundOnly = new ValueBuilder<Boolean>()
            .withDescriptor("PlayerMoveC2SPacket.OnGroundOnly")
            .withValue(false)
            .register(this);
    public Value<Boolean> playerInteractBlock = new ValueBuilder<Boolean>()
            .withDescriptor("PlayerInteractBlockC2SPacket")
            .withValue(false)
            .register(this);
    Value<Boolean> updatePlayerAbilitiesC2SPacket = new ValueBuilder<Boolean>()
            .withDescriptor("UpdatePlayerAbilitiesC2SPacket")
            .withValue(false)
            .register(this);
    Value<Boolean> vehicleMove = new ValueBuilder<Boolean>()
            .withDescriptor("VehicleMoveC2SPacket")
            .withValue(false)
            .register(this);
    public Value<Boolean> playerInteractItemC2SPacket = new ValueBuilder<Boolean>()
            .withDescriptor("PlayerInteractItemC2SPacket")
            .withValue(false)
            .register(this);
    public Value<Boolean> CancelCPacketPlayerTryUseItemOnBlock = new ValueBuilder<Boolean>()
            .withDescriptor("CPacketPlayerTryUseItemOnBlock")
            .withValue(false)
            .register(this);
    Value<Boolean> CancelCPacketEntityAction = new ValueBuilder<Boolean>()
            .withDescriptor("CPacketEntityAction")
            .withValue(false)
            .register(this);
    Value<Boolean> CancelCPacketUseEntity = new ValueBuilder<Boolean>()
            .withDescriptor("CPacketUseEntity")
            .withValue(false)
            .register(this);
    Value<Boolean> CancelCPacketVehicleMove = new ValueBuilder<Boolean>()
            .withDescriptor("CPacketVehicleMove")
            .withValue(false)
            .register(this);

//    @SubscribeEvent
//    public void onPacket(PacketEvent event)
//    {
//        if (NullUtils.nullCheck()) return;
//
//
//
//
//        if ((event.getPacket() instanceof VehicleMoveC2SPacket && CancelCPacketInput.getValue())
//                || (event.getPacket() instanceof CPacketPlayer.Position && CancelPosition.getValue())
//                || (event.getPacket() instanceof CPacketPlayer.PositionRotation && CancelPositionRotation.getValue())
//                || (event.getPacket() instanceof CPacketPlayer.Rotation && CancelRotation.getValue())
//                || (event.getPacket() instanceof CPacketPlayerAbilities && CancelCPacketPlayerAbilities.getValue())
//                || (event.getPacket() instanceof CPacketPlayerDigging && CancelCPacketPlayerDigging.getValue())
//                || (event.getPacket() instanceof CPacketPlayerTryUseItem && CancelCPacketPlayerTryUseItem.getValue())
//                || (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && CancelCPacketPlayerTryUseItemOnBlock.getValue())
//                || (event.getPacket() instanceof CPacketEntityAction && CancelCPacketEntityAction.getValue())
//                || (event.getPacket() instanceof CPacketUseEntity && CancelCPacketUseEntity.getValue())
//                || (event.getPacket() instanceof CPacketVehicleMove && CancelCPacketVehicleMove.getValue()))
//        {
//
//            event.setCanceled(true);
//        }
//    }
}