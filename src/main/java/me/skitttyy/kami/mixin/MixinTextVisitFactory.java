package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.impl.features.modules.misc.NameHider;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(TextVisitFactory.class)
public abstract class MixinTextVisitFactory implements IMinecraft {

    @Shadow
    private static boolean visitRegularCharacter(Style style, CharacterVisitor visitor, int index, char c) {
        return false;
    }

    @ModifyArg(method = "visitFormatted(Ljava/lang/String;Lnet/minecraft/text/" +
            "Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At(value =
            "INVOKE", target = "Lnet/minecraft/text/TextVisitFactory;" +
            "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;" +
            "Lnet/minecraft/text/CharacterVisitor;)Z", ordinal = 0), index = 0)
    private static String hookVisitFormatted(String text)
    {
        if (text == null)
        {
            return "";
        }
        if (mc.player == null)
        {
            return text;
        }
        if (NameHider.INSTANCE.isEnabled() && mc.player != null)
        {
            final String username = mc.getSession().getUsername();

            if (text.contains(username))
                return text.replaceAll(username, NameHider.INSTANCE.replacement.getValue());
        }
        return text;
    }

    @Inject(method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/" +
            "minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At(value = "HEAD"), cancellable = true)
    private static void hookVisitFormatted$1(String text, int startIndex, Style startingStyle, Style resetStyle,
                                             CharacterVisitor visitor, CallbackInfoReturnable<Boolean> cir) {
        int i = text.length();
        Style style = startingStyle;

        for(int j = startIndex; j < i; ++j) {
            char c = text.charAt(j);
            char d;
            if (c == 167) {
                if (j + 1 >= i) {
                    break;
                }

                d = text.charAt(j + 1);
                if (d == 's') { // Custom client color
                    style = style.withColor(new Color(48, 184, 203, 0).getRGB());
                }
                else {
                Formatting formatting = Formatting.byCode(d);
                if (formatting != null) {
                    style = formatting == Formatting.RESET ? resetStyle : style.withExclusiveFormatting(formatting);
                }
                }

                ++j;
            } else if (Character.isHighSurrogate(c)) {
                if (j + 1 >= i) {
                    if (!visitor.accept(j, style, 65533)) {
                        cir.setReturnValue(false);
                        return;
                    }
                    break;
                }

                d = text.charAt(j + 1);
                if (Character.isLowSurrogate(d)) {
                    if (!visitor.accept(j, style, Character.toCodePoint(c, d))) {
                        cir.setReturnValue(false);
                        return;

                    }

                    ++j;
                } else if (!visitor.accept(j, style, 65533)) {
                    cir.setReturnValue(false);
                    return;

                }
            } else if (!visitRegularCharacter(style, visitor, j, c)) {
                cir.setReturnValue(false);
                return;
            }
        }

        cir.setReturnValue(true);
    }


}
