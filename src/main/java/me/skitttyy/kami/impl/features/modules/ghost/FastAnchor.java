package me.skitttyy.kami.impl.features.modules.ghost;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;


public class FastAnchor extends Module
{

    public Value<Boolean> explode = new ValueBuilder<Boolean>()
            .withDescriptor("Explode")
            .withValue(false)
            .register(this);
    public Value<Number> slot = new ValueBuilder<Number>()
            .withDescriptor("Slot")
            .withValue(1)
            .withRange(0, 9)
            .withPlaces(0)
            .register(this);
    public FastAnchor()
    {
        super("FastAnchor", Category.Ghost);
    }

    public ANCHOR_STAGE stage = ANCHOR_STAGE.Idle;

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.VanillaTick event)
    {
        if (NullUtils.nullCheck()) return;


        switch (stage)
        {
            case Idle:


                if (mc.player.getInventory().getMainHandStack().getItem() != Items.RESPAWN_ANCHOR)
                    break;



                HitResult hit = mc.crosshairTarget;
                if (hit == null)
                    return;

                if (mc.crosshairTarget.getType() != HitResult.Type.BLOCK)
                    return;


                BlockHitResult hitResult = (BlockHitResult) hit;
                BlockState state = mc.world.getBlockState(hitResult.getBlockPos());

                if (state.getBlock() == Blocks.RESPAWN_ANCHOR)
                {
                    if (state.get(Properties.CHARGES) != 0)
                    {
                        stage = ANCHOR_STAGE.Explode;
                    } else
                    {
                        stage = ANCHOR_STAGE.Charge;

                    }
                } else
                {
                    int slot = InventoryUtils.findBlockInHotbar(Blocks.GLOWSTONE);
                    if(slot == -1) return;


                    stage = ANCHOR_STAGE.Place;
                }

            case Place:
                rightClick();
                stage = ANCHOR_STAGE.Charge;
                break;
            case Charge:

                if(mc.player.getInventory().getMainHandStack().getItem() != Items.GLOWSTONE)
                {
                    int slot = InventoryUtils.findBlockInHotbar(Blocks.GLOWSTONE);

                    if (slot == -1)
                    {
                        stage = ANCHOR_STAGE.Idle;
                        return;
                    }

                    InventoryUtils.switchToSlot(slot);
                }else{
                    rightClick();
                    stage = ANCHOR_STAGE.Explode;
                }
                break;

            case Explode:

                InventoryUtils.switchToSlot(slot.getValue().intValue());

                if(explode.getValue())
                {
                    rightClick();
                }
                stage = ANCHOR_STAGE.Idle;
                break;
        }


    }

    public enum ANCHOR_STAGE
    {
        Idle, Place, Charge, Explode
    }

    private boolean rightClick()
    {
        HitResult hit = mc.crosshairTarget;
        if (hit == null)
            return false;
        if (mc.crosshairTarget.getType() != HitResult.Type.BLOCK)
            return false;
        BlockHitResult blockHit = (BlockHitResult) hit;
        // we don't care about this because it is always block item
        //ActionResult result1 = MC.interactionManager.interactItem(MC.player, MC.world, Hand.MAIN_HAND);
        assert mc.interactionManager != null;
        ActionResult result2 = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
        if (result2 == ActionResult.SUCCESS)
        {
            mc.player.swingHand(Hand.MAIN_HAND);
            return true;
        }
        return false;
    }


    @Override
    public String getDescription()
    {
        return "FastAnchor: anchor macrol";
    }
}