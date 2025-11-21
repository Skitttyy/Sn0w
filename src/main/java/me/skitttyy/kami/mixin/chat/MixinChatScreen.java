package me.skitttyy.kami.mixin.chat;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.event.events.misc.ClientChatEvent;
import me.skitttyy.kami.api.management.CommandManager;
import me.skitttyy.kami.api.utils.CommandUtils;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Objects;

@Mixin(ChatScreen.class)
public class MixinChatScreen extends Screen {

    protected MixinChatScreen(Text title)
    {
        super(title);
    }


    @Shadow
    public void sendMessage(String chatText, boolean addToHistory)
    {
    }


    @Inject(method = "sendMessage", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"), cancellable = true)
    public void hookEventChat(String chatText, boolean addToHistory, CallbackInfo ci)
    {
        ClientChatEvent eventChat = new ClientChatEvent(chatText);
        eventChat.post();
        if (eventChat.isCancelled())
            ci.cancel();
    }

    @Shadow
    protected TextFieldWidget chatField;

    /**
     * @author Skitttyy
     * @reason inject into keypress to remove bs
     */
    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir)
    {
        if (chatField != null && chatField.getText().startsWith(CommandManager.INSTANCE.PREFIX) && keyCode == 258)
        {
            String text = chatField.getText();
            String commandText = getCommandText(text, false);
            if (!Objects.equals(commandText, ""))
            {
                chatField.setText(text + commandText);
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void renderChat(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci)
    {
        if (chatField == null) return;

        if (chatField.getText().startsWith(CommandManager.INSTANCE.PREFIX))
        {
            processAutoComplete(context, chatField.getText());
            RenderUtil.renderOutline(context.getMatrices(), 2, this.height - 14, chatField.getWidth(), chatField.getHeight(), Color.CYAN.getRGB(), true);
        } else if (chatField.getText().startsWith("="))
        {
            RenderUtil.renderOutline(context.getMatrices(), 2, this.height - 14, chatField.getWidth(), chatField.getHeight(), Color.BLUE.getRGB(), true);
        }

    }

    @Unique
    public void processAutoComplete(DrawContext context, String text)
    {

        int x = MinecraftClient.getInstance().textRenderer.getWidth(text + " ");
        int y = chatField.drawsBackground() ? chatField.getY() + (chatField.getHeight() - 8) / 2 : chatField.getY();
        String commandText = getCommandText(text, true);
        if (!commandText.isEmpty())
        {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, commandText, x, y, new Color(134, 242, 252).getRGB());
        }
    }

    @Unique
    public String getCommandText(String text, boolean description)
    {
        try
        {
            String sub = text.substring(1);

            if (sub.isEmpty()) return "";

            String[] args = sub.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            Pair<Command, String> command = null;
            if (args[0] != null)
            {
                command = CommandManager.INSTANCE.findClosestMatchingCommand(args[0]);
            } else
            {
                return "";
            }
            if (command == null)
            {
                return "";
            }
            if (text.endsWith(" ") && command.value().equals(args[0]))
            {
                return CommandUtils.getAutocomplete(command.key(), "", args.length - 1, true, args);
            } else if (text.endsWith(" "))
            {
                return "";
            }

            if (args.length == 1)
            {
                return command.value().substring(args[0].length()) + (description ? Formatting.GRAY + " (" + command.key().getDesc() + ")" : "");
            } else if (Objects.equals(command.value(), args[0]))
            {
                int lengthMinus = args.length - 1;


                String argument = args[lengthMinus];
                String fill = CommandUtils.getAutocomplete(command.key(), argument, lengthMinus - 1, false, args);
                if (!fill.isEmpty())
                    return fill.substring(argument.length());
            }
        } catch (Exception e)
        {

        }
        return "";
    }


}