package me.skitttyy.kami.mixin.chat;

import com.llamalad7.mixinextras.sugar.Local;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.ducks.IChatHud;
import me.skitttyy.kami.api.utils.ducks.IChatHudLine;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.animation.type.TimeAnimation;
import me.skitttyy.kami.api.utils.render.font.OrderedTextPart;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import me.skitttyy.kami.impl.features.modules.render.Chat;
import me.skitttyy.kami.mixin.accessor.ITextRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

@Mixin(ChatHud.class)
public abstract class MixinChatHud implements IChatHud
{
    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;
    @Shadow
    @Final
    private List<ChatHudLine> messages;
    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private int nextId;

    @Unique
    public long prevMillis = -1;

    private ChatHudLine current = null;


    @Unique
    float percent;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I"))
    private void hookTimeAdded(CallbackInfo ci, @Local(ordinal = 13) int chatLineIndex)
    {
        try
        {
            current = messages.get(chatLineIndex);
        } catch (Exception ignored)
        {

        }
    }

    public void updatePercentage(long diff)
    {
        if (Chat.INSTANCE.percentComplete < 1) Chat.INSTANCE.percentComplete += 0.004f * diff;
        Chat.INSTANCE.percentComplete = MathUtil.clamp(Chat.INSTANCE.percentComplete, 0, 1);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/MessageIndicator;indicatorColor()I"))
    private int getIndicatorColor(MessageIndicator instance, @Local(index = 31) int x)
    {
        if (instance.indicatorColor() == new Color(48, 184, 203, 0).getRGB())
        {
            return HudColors.getTextColor(x).getRGB();
        }
        return instance.indicatorColor();
    }


    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 1))
    private void fill(DrawContext instance, int x1, int y1, int x2, int y2, int color)
    {
        if(Chat.INSTANCE.isEnabled() && Chat.INSTANCE.bounce.getValue())
        {
            y1 = (int) (y1 + (9.0f - 9.0f * percent) * getChatScale());
            y2 = (int) (y2 + (9.0f - 9.0f * percent) * getChatScale());
        }
        instance.fill(x1, y1, x2, y2, color);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;" +
                            "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;" +
                            "Lnet/minecraft/text/OrderedText;III)I"))
    private int drawTextWithShadowHook(DrawContext instance,
                                       TextRenderer textRenderer,
                                       OrderedText text,
                                       int x,
                                       int y,
                                       int color)
    {
        TimeAnimation animation = null;
        if (current != null)
        {
            if (Chat.INSTANCE.animationMap.containsKey(current))
            {
                animation = Chat.INSTANCE.animationMap.get(current);
            }
        }

        if (animation != null)
        {
            animation.setState(true);
        }


        List<OrderedTextPart> parts = OrderedTextPart.getParts(text);
        MutableText mutable = Text.empty();
        Iterator var12 = parts.iterator();

        while (var12.hasNext())
        {
            OrderedTextPart part = (OrderedTextPart) var12.next();
            String l = part.getText();
            Style style = part.getStyle();
            if (style.getColor() != null && style.getColor().getRgb() == new Color(48, 184, 203, 0).getRGB())
            {
                mutable.append(Text.literal(l).setStyle(style.withColor(HudColors.getTextColor(y).getRGB())));
            } else
            {
                mutable.append(Text.literal(l).setStyle(style));
            }
        }
        text = Language.getInstance().reorder(mutable);

        Color chatColor = ((animation != null && Chat.INSTANCE.isEnabled() && Chat.INSTANCE.animations.getValue() && Chat.INSTANCE.animationMode.getValue().equals("Fade") ? ColorUtil.interpolate((float) animation.getLinearFactor(), new Color(color), ColorUtil.newAlpha(new Color(color), 0)) : new Color(color)));
        if (FontModule.INSTANCE.shortShadow.getValue())
        {

            ((ITextRenderer) textRenderer).hookDrawLayer(text, (float) ((animation != null && Chat.INSTANCE.isEnabled() && Chat.INSTANCE.animations.getValue() && Chat.INSTANCE.animationMode.getValue().equals("Slide") ? animation.getCurrent() : 0)) - 0.3f, (float) y - 0.3f, chatColor.getRGB(), true, instance.getMatrices().peek().getPositionMatrix(), instance.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 15728880);
            instance.draw();
            return instance.drawText(textRenderer, text, (int) ((animation != null && Chat.INSTANCE.isEnabled() && Chat.INSTANCE.animations.getValue() && Chat.INSTANCE.animationMode.getValue().equals("Slide") ? animation.getCurrent() : 0)), y, chatColor.getRGB(), false);

        } else
        {
            return instance.drawTextWithShadow(textRenderer, text, (int) ((animation != null && Chat.INSTANCE.isEnabled() && Chat.INSTANCE.animations.getValue() && Chat.INSTANCE.animationMode.getValue().equals("Slide") ? animation.getCurrent() : 0)), y, chatColor.getRGB());

        }


    }


    @ModifyVariable(
            method = "render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V",
            at = @At(value = "STORE"), index = 32)
    private int getY(int y)
    {

        if (Chat.INSTANCE.isEnabled() && Chat.INSTANCE.bounce.getValue())
            return (int) (y + (9.0f - 9.0f * percent) * getChatScale());

        return y;
    }


    @Inject(
            method = "render", at = @At("HEAD"))
    private void render(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci)
    {
        long current = System.currentTimeMillis();
        long diff = current - prevMillis;
        prevMillis = current;
        updatePercentage(diff);
        float t = Chat.INSTANCE.percentComplete;
        percent = 1 - (--t) * t * t * t;
        percent = MathUtil.clamp(percent, 0, 1);
    }

    @Shadow
    public abstract void addMessage(Text message);

    @Shadow
    public abstract void addMessage(Text message, MessageSignatureData a, MessageIndicator indicator);

    @Shadow
    public abstract double getChatScale();

    @Override
    public void addChatMessageWithId(Text message, int id)
    {
        nextId = id;
        MessageIndicator indicator = null;

        if (Manager.INSTANCE.indicator.getValue())
        {
            indicator = new MessageIndicator(Manager.INSTANCE.mainChatColor.getValue().equals("Custom") ? new Color(48, 184, 203, 0).getRGB() : Manager.INSTANCE.getFormatColor().getColorValue(), null, null, null);
        }

        addMessage(message, null, indicator);
        nextId = 0;
    }


    @Override
    public void addChatMessageNoId(Text message)
    {
        MessageIndicator indicator = null;

        if (Manager.INSTANCE.indicator.getValue())
        {
            indicator = new MessageIndicator(Manager.INSTANCE.mainChatColor.getValue().equals("Custom") ? new Color(48, 184, 203, 0).getRGB() : Manager.INSTANCE.getFormatColor().getColorValue(), null, null, null);
        }
        addMessage(message, null, indicator);
    }

    @Inject(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onAddMessageAfterNewChatHudLineVisible(ChatHudLine message, CallbackInfo ci)
    {
        try
        {
            ((IChatHudLine) (Object) visibleMessages.get(0)).setOverrideId(nextId);
        } catch (Exception e)
        {
            ChatUtils.sendMessage(Formatting.RED + "Unexpected message state! send your game log to skitty if you see this!");
            e.printStackTrace();
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onAddMessageAfterNewChatHudLine(ChatHudLine message, CallbackInfo ci)
    {
        try
        {
            ((IChatHudLine) (Object) messages.get(0)).setOverrideId(nextId);
        } catch (Exception e)
        {
            ChatUtils.sendMessage(Formatting.RED + "Unexpected message id state! send your game log to skitty if you see this!");
            e.printStackTrace();
        }
    }


    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", cancellable = true)
    private void onAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci)
    {
        visibleMessages.removeIf(msg -> ((IChatHudLine) (Object) msg).getOverrideId() == nextId && nextId != 0);

        for (int i = messages.size() - 1; i > -1; i--)
        {
            if (((IChatHudLine) (Object) messages.get(i)).getOverrideId() == nextId && nextId != 0)
            {
                messages.remove(i);
            }
        }
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void onClear(boolean clearHistory, CallbackInfo ci)
    {

    }

    @Inject(method = "refresh", at = @At("HEAD"))
    private void onRefresh(CallbackInfo ci)
    {

    }


}