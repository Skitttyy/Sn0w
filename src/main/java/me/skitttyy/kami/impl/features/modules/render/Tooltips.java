package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Tooltips extends Module {
    public static Tooltips INSTANCE;

    public Tooltips()
    {
        super("Tooltips", Category.Render);
        INSTANCE = this;

    }

    public Value<Boolean> icon = new ValueBuilder<Boolean>()
            .withDescriptor("Icon")
            .withValue(true)
            .register(this);
    public Value<Boolean> capacity = new ValueBuilder<Boolean>()
            .withDescriptor("Capacity")
            .withValue(true)
            .register(this);
    public Value<Boolean> aboveItems = new ValueBuilder<Boolean>()
            .withDescriptor("Above Items")
            .withValue(true)
            .register(this);
    public Value<Boolean> Totems = new ValueBuilder<Boolean>()
            .withDescriptor("Totems")
            .withValue(false)
            .withParent(aboveItems)
            .withParentEnabled(true)
            .register(this);
    public Value<Boolean> Crystals = new ValueBuilder<Boolean>()
            .withDescriptor("Crystals")
            .withValue(false)
            .withParent(aboveItems)
            .withParentEnabled(true)
            .register(this);
    public Value<Boolean> Obsidian = new ValueBuilder<Boolean>()
            .withDescriptor("Obsidian")
            .withValue(false)
            .withParent(aboveItems)
            .withParentEnabled(true)
            .register(this);
    public Value<Boolean> XP = new ValueBuilder<Boolean>()
            .withDescriptor("XP")
            .withValue(false)
            .withParent(aboveItems)
            .withParentEnabled(true)
            .register(this);
    public Value<Sn0wColor> borderColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Border Color")
            .withValue(new Sn0wColor(255, 0, 0))
            .register(this);
    public Value<Sn0wColor> insideColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Inside Color")
            .withValue(new Sn0wColor(20, 20, 20))
            .register(this);
    public Value<Sn0wColor> textColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Text Color")
            .withValue(new Sn0wColor(255, 255, 255))
            .register(this);

    public void draw(DrawContext context, int x, int y, List<ItemStack> items, String name)
    {
        if (NullUtils.nullCheck()) return;

        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 999.0f);
        try
        {
            renderBox(context.getMatrices(), x, y);
            renderItems(context, items, x, y + 18);
            List<Item> ItemsList = new ArrayList<>();
            int width = 0;
            if (aboveItems.getValue())
            {
                if (Crystals.getValue())
                    ItemsList.add(Items.END_CRYSTAL);

                if (Totems.getValue())
                    ItemsList.add(Items.TOTEM_OF_UNDYING);

                if (Obsidian.getValue())
                    ItemsList.add(Items.OBSIDIAN);

                if (XP.getValue())
                    ItemsList.add(Items.EXPERIENCE_BOTTLE);

                for (Item item : ItemsList)
                {
                    int count = InventoryUtils.getItemCount(item, items);

                    if (count == 0) continue;

                    RenderUtil.renderItemWithCount(context, item.getDefaultStack(), new Point(x + 1 + (20 * width), y - 18), InventoryUtils.getItemCount(item, items), textColor.getValue().getColor(), true);
                    width++;
                }
            }
            Fonts.renderText(context, name, x + (20 * width) + 2, y - 10, Color.WHITE, FontModule.INSTANCE.textShadow.getValue());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        context.getMatrices().pop();
    }


    private void renderBox(MatrixStack matrices, final int x, final int y)
    {
        RenderUtil.renderRect(matrices, x, y, (17 * 9) + 10, (17 * 3) + 3, 1, insideColor.getValue().getColor());
        RenderUtil.renderOutlineRect(matrices, x, y, (17 * 9) + 10, (17 * 3) + 3, 1, borderColor.getValue().getColor(), true);
    }

    private void renderItems(DrawContext context, List<ItemStack> items, int x, int y)
    {
        for (int size = items.size(), item = 0; item < size; ++item)
        {
            final int slotx = x + 1 + item % 9 * 18;
            final int sloty = y + 1 + (item / 9 - 1) * 18;
            context.drawItem(items.get(item), slotx, sloty);
            context.drawItemInSlot(mc.textRenderer, items.get(item), slotx, sloty);
        }
    }


    @Override
    public String getDescription()
    {
        return "Tooltips: shulker preview and other related stuff";
    }

}