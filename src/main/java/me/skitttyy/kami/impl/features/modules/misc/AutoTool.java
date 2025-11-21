package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.utils.world.MineUtils;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;

import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.mixin.accessor.IClientPlayerInteractionManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;


public class AutoTool extends Module {
    public AutoTool()
    {
        super("AutoTool", Category.Misc);
    }

    @SubscribeEvent
    public void onBreakBlock(LivingEvent.AttackBlock event)
    {
        if (NullUtils.nullCheck()) return;

        BlockState state = event.getState();

        if (state == null) return;
        if (state.getBlock() != Blocks.AIR && MineUtils.canBreak(event.getPos()))
        {
            int slot = InventoryUtils.getBestToolSlot(state.getBlock());
            if (slot != -1 && mc.player.getInventory().selectedSlot != slot)
            {
                mc.player.getInventory().selectedSlot = InventoryUtils.getBestToolSlot(state.getBlock());
                ((IClientPlayerInteractionManager) mc.interactionManager).doSyncSelectedSlot();
            }
        }
    }

    @Override
    public String getDescription()
    {
        return "AutoTool: Swaps to the best tool you have to mine a block";
    }
}
