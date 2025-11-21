package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.impl.features.commands.AutoRegearCommand;
import me.skitttyy.kami.impl.features.modules.player.ChestStealer;
import me.skitttyy.kami.impl.features.modules.player.Tweaks;
import me.skitttyy.kami.impl.features.modules.render.Tooltips;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.Component;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T>
{
    @Shadow
    protected Slot focusedSlot;

    @Shadow
    protected int x;
    @Shadow
    protected int y;

    @Shadow
    public abstract T getScreenHandler();

    @Shadow
    public abstract void close();

    @Unique
    private static final ItemStack[] ITEMS = new ItemStack[27];

    public MixinHandledScreen(Text title)
    {
        super(title);
    }


    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info)
    {


        if (Tweaks.INSTANCE.regearButton.getValue())
            addDrawableChild(
                    new ButtonWidget.Builder(Text.literal("Save Regear"), button -> AutoRegearCommand.save("kit" + MathUtil.randomInt(0, 32767)))
                            .position(2, 2)
                            .size(70, 17)
                            .build()
            );

        if (Tweaks.INSTANCE.stealButton.getValue() && this.getScreenHandler() instanceof GenericContainerScreenHandler)
            addDrawableChild(
                    new ButtonWidget.Builder(Text.literal("Steal"), button -> ChestStealer.INSTANCE.toggle())
                            .position(x - 32, y)
                            .size(30, 17)
                            .build()
            );

    }


    @Inject(method = "drawMouseoverTooltip", at = @At(value = "HEAD"), cancellable = true)
    private void hookDrawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci)
    {
        if (!Tooltips.INSTANCE.isEnabled()) return;

        if (focusedSlot == null)
        {
            return;
        }
        if (focusedSlot.getStack().contains(DataComponentTypes.CONTAINER))
        {
            ContainerComponent containerComponent = focusedSlot.getStack().get(DataComponentTypes.CONTAINER);
            List<ItemStack> items = containerComponent.stream().toList();
            if (!items.isEmpty())
            {
                Tooltips.INSTANCE.draw(context, x + 10, y, containerComponent.stream().toList(), focusedSlot.getStack().getName().getString());
                ci.cancel();
            }
        }
    }
}