package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.misc.IllegalLog;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.BitSet;

@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreen implements IMinecraft
{


    @Inject(method = "disconnect", at = @At(value = "HEAD", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    public void onDisconnect(CallbackInfo ci)
    {

        if (IllegalLog.INSTANCE.isEnabled() && !mc.isInSingleplayer())
            PacketManager.INSTANCE.sendQuietPacket(new ChatMessageC2SPacket(
                    "§",
                    Instant.now(),
                    NetworkEncryptionUtils.SecureRandomUtil.nextLong(),
                    null,
                    new LastSeenMessageList.Acknowledgment(1, new BitSet(2))));
    }
}
