package me.skitttyy.kami.mixin.chat;

import com.mojang.authlib.GameProfile;
import me.skitttyy.kami.api.utils.ducks.IChatHudLine;
import me.skitttyy.kami.api.utils.render.animation.Easing;
import me.skitttyy.kami.api.utils.render.animation.type.TimeAnimation;
import me.skitttyy.kami.impl.features.modules.render.Chat;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.skitttyy.kami.api.wrapper.IMinecraft.mc;

@Mixin(value = ChatHudLine.class)
public abstract class MixinChatHudLine implements IChatHudLine {
    @Shadow @Final private Text content;
    @Unique private int id;
    @Unique private GameProfile sender;

    @Override
    public String getMessageText() {
        return content.getString();
    }

    @Override
    public int getOverrideId() {
        return id;
    }

    @Override
    public void setOverrideId(int id) {
        this.id = id;
    }

    @Inject(
            method = "<init>",
            at = @At(value = "RETURN"))
    private void hookCtr(int creationTick,
                         Text text,
                         MessageSignatureData messageSignatureData,
                         MessageIndicator messageIndicator,
                         CallbackInfo info)
    {


        Chat.INSTANCE.percentComplete = 0.0F;
        Chat.INSTANCE.animationMap.put(
                ChatHudLine.class.cast(this),
                new TimeAnimation(false,
                        -mc.textRenderer.getWidth(text.getString()),
                        0,
                        Chat.INSTANCE.animationSpeed.getValue().longValue(),
                        Easing.LINEAR));
    }
}