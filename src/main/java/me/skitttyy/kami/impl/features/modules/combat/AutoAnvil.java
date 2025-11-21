package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.util.math.Direction;


public class AutoAnvil extends Module {
    Timer timer = new Timer();

    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(250)
            .withRange(250, 1500)
            .withAction(set -> timer.setDelay(set.getValue().longValue()))
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(true)
            .register(this);
    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict Direction")
            .withValue(false)
            .register(this);

    public AutoAnvil()
    {
        super("AutoAnvil", Category.Combat);
    }

    Entity target;

    @SubscribeEvent
    public void onUpdatePre(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        target = TargetUtils.getTarget(100F);
        if (target == null) return;


        int AnvilSlot = InventoryUtils.getHotbarItemSlot(Items.ANVIL);
        if (AnvilSlot == -1)
        {
            this.setEnabled(false);
            return;
        }

        if (PlayerUtils.getHighestPlaceableAnvilPos((PlayerEntity) target) != null && timer.isPassed())
        {
            BlockPos pos = PlayerUtils.getHighestPlaceableAnvilPos((PlayerEntity) target);
            Direction direction;
            if ((direction = BlockUtils.getPlaceableSide(pos, strictDirection.getValue())) != null)
            {
                InventoryUtils.switchToSlot(AnvilSlot);
                doRotate(pos);
                BlockUtils.placeBlock(pos, direction, false);
                timer.resetDelay();
            }
        }


    }

    public void doRotate(BlockPos pos)
    {
        if (!rotate.getValue()) return;

        float[] rots = RotationUtils.getBlockRotations(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue()));
        if (rots != null)
            RotationUtils.setRotation(rots);
    }

    @Override
    public String getDescription()
    {
        return "AutoAnvil: Places anvils above players at the highest possible position";
    }
}
