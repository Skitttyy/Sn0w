package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.sound.SoundEvents;

public class DeathEffects extends Module {

    public static DeathEffects INSTANCE;

    public DeathEffects()
    {
        super("DeathEffects", Category.Render);
        INSTANCE = this;
    }
    @SubscribeEvent
    private void onDeath(PacketEvent.Receive event)
    {
        if (event.getPacket() instanceof EntityStatusS2CPacket pac && pac.getStatus() == 3)
        {
            if (!(pac.getEntity(mc.world) instanceof PlayerEntity)) return;


            doEffect((PlayerEntity) pac.getEntity(mc.world));


        }
    }

    public static void doEffect(PlayerEntity player)
    {
        LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
        lightningEntity.refreshPositionAfterTeleport(player.getX(), player.getY(), player.getZ());
        mc.world.addEntity(lightningEntity);
        mc.player.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 1f);
    }

    @Override
    public String getDescription()
    {
        return "DeathEffects: Spawns lightning when someone dies";
    }
}
