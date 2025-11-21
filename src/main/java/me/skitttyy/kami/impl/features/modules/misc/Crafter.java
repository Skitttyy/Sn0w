package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.StringUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.Item;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;


public class Crafter extends Module
{

    public Value<String> craftingItem = new ValueBuilder<String>()
            .withDescriptor("Item")
            .withValue("stick")
            .register(this);

    public Crafter()
    {
        super("Crafter", Category.Misc);
    }


    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck())
        {
            return;
        }


        Item targetItem = findItem(craftingItem.getValue());

        if (targetItem == null) return;

        List<RecipeResultCollection> recipeResultCollectionList = mc.player.getRecipeBook().getOrderedResults();
        for (RecipeResultCollection recipeResultCollection : recipeResultCollectionList)
        {
            for (RecipeEntry<?> recipe : recipeResultCollection.getRecipes(true))
            {
                final Item item = recipe.value().getResult(mc.world.getRegistryManager()).getItem();

                if (item.equals(targetItem))
                {
                    mc.interactionManager.clickRecipe(mc.player.currentScreenHandler.syncId, recipe, true);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 1, SlotActionType.QUICK_MOVE, mc.player);
                }
            }
        }
    }

    private Item findItem(String name)
    {
        return StringUtils.parseId(Registries.ITEM, name);
    }

    @Override
    public String getDescription()
    {
        return "Crafter: crafts sticks";
    }


}
